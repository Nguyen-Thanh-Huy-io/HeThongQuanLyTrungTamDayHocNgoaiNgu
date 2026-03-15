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

        boolean authenticated = false;
        boolean isBcryptHash = isBcryptFormat(storedHash);

        // Ưu tiên xác thực theo BCrypt nếu dữ liệu đã đúng chuẩn.
        if (isBcryptHash) {
            authenticated = BCrypt.checkpw(password, storedHash);
        } else {
            // Tương thích dữ liệu cũ: cho phép so sánh trực tiếp và tự nâng cấp lên BCrypt khi đúng mật khẩu.
            authenticated = password.equals(storedHash);
            if (authenticated) {
                user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
                accountRepo.update(em, user);
            }
        }

        if (!authenticated) {
            throw new Exception("Mật khẩu không chính xác!");
        }

        return Optional.of(user);
    });
}

    private boolean isBcryptFormat(String value) {
        return value != null && value.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
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
    public void setAccountActive(Long accountId, boolean active) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            UserAccount account = accountRepo.findById(em, accountId)
                    .orElseThrow(() -> new Exception("Không tìm thấy tài khoản!"));
            account.setActive(active);
            accountRepo.update(em, account);
            return null;
        });
    }

    @Override
    public void updateRole(Long accountId, UserAccount.UserRole role) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            if (role == null) {
                throw new Exception("Quyền tài khoản không hợp lệ!");
            }

            UserAccount account = accountRepo.findById(em, accountId)
                    .orElseThrow(() -> new Exception("Không tìm thấy tài khoản!"));

            if (role == UserAccount.UserRole.Teacher && account.getTeacher() == null) {
                throw new Exception("Tài khoản này không liên kết giáo viên, không thể đổi sang quyền Teacher.");
            }
            if (role == UserAccount.UserRole.Student && account.getStudent() == null) {
                throw new Exception("Tài khoản này không liên kết học viên, không thể đổi sang quyền Student.");
            }

            account.setRole(role);
            accountRepo.update(em, account);
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

    @Override
    public UserAccount createAccount(String username, String password, UserAccount.UserRole role) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            // Kiểm tra username đã tồn tại
            if (accountRepo.findByUsername(em, username).isPresent()) {
                throw new Exception("Tên đăng nhập đã tồn tại!");
            }

            UserAccount account = new UserAccount();
            account.setUsername(username);
            account.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
            account.setRole(role);
            account.setActive(true);

            accountRepo.insert(em, account);
            return account;
        });
    }
}