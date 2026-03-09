package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.UserAccount;
import vn.ute.repo.UserAccountRepository;
import vn.ute.service.UserAccountService;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

public class UserAccountServiceImpl extends AbstractService<UserAccount, Long> implements UserAccountService {
    private final UserAccountRepository accountRepo;

    public UserAccountServiceImpl(UserAccountRepository accountRepo) {
        super(accountRepo);
        this.accountRepo = accountRepo;
    }

    @Override
public Optional<UserAccount> authenticate(String username, String password) throws Exception {
    return TransactionManager.execute((EntityManager em) -> {
        // 1. Kiểm tra tài khoản có tồn tại không
        Optional<UserAccount> userOpt = accountRepo.findByUsername(em, username);
        
        if (userOpt.isEmpty()) {
            throw new Exception("Tên đăng nhập không tồn tại!");
        }

        UserAccount user = userOpt.get();

        // 2. Kiểm tra trạng thái Active (Tránh cho phép user bị khóa đăng nhập)
        if (!user.isActive()) {
            throw new Exception("Tài khoản này hiện đang bị khóa!");
        }

        // 3. Kiểm tra mật khẩu bằng BCrypt
        String storedHash = user.getPasswordHash();
        
        // Kiểm tra an toàn: Nếu hash trong DB bị rỗng hoặc null sẽ báo lỗi thay vì văng Exception hệ thống
        if (storedHash == null || storedHash.isEmpty()) {
            throw new Exception("Lỗi dữ liệu: Tài khoản chưa được thiết lập mật khẩu!");
        }

        // So sánh mật khẩu người dùng nhập (thô) với mã băm trong Database
        // Cần import org.mindrot.jbcrypt.BCrypt;
        try {
            if (!BCrypt.checkpw(password, storedHash)) {
                throw new Exception("Mật khẩu không chính xác!");
            }
        } catch (IllegalArgumentException e) {
            // Trường hợp chuỗi trong DB không đúng định dạng BCrypt (ví dụ bạn lỡ lưu "123456" trực tiếp)
            throw new Exception("Lỗi hệ thống: Định dạng mật khẩu trong cơ sở dữ liệu không hợp lệ!");
        }

        return Optional.of(user);
    });
}

    @Override
    public void updatePassword(Long accountId, String newPassword) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            UserAccount acc = accountRepo.findById(em, accountId)
                    .orElseThrow(() -> new Exception("Không tìm thấy tài khoản!"));
            // Hash password mới
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            acc.setPasswordHash(hashedPassword);
            accountRepo.update(em, acc);
            return null;
        });
    }

    @Override
    public Optional<UserAccount> getByStudent(Long studentId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> accountRepo.findByStudentId(em, studentId));
    }

    @Override
    public Optional<UserAccount> getByTeacher(Long teacherId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> accountRepo.findByTeacherId(em, teacherId));
    }
}