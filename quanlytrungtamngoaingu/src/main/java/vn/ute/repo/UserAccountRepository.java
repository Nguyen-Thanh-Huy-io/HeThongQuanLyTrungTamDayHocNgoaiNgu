package vn.ute.repo;

import jakarta.persistence.EntityManager;
import vn.ute.model.UserAccount;
import java.util.Optional;

public interface UserAccountRepository extends Repository<UserAccount, Long> {
    // Tìm tài khoản theo tên đăng nhập (Dùng cho Security/Login)
    Optional<UserAccount> findByUsername(EntityManager em,String username);
    
    // Tìm tài khoản liên kết với một sinh viên cụ thể
    Optional<UserAccount> findByStudentId(EntityManager em,Long studentId);
    
    // Tìm tài khoản liên kết với một giáo viên cụ thể
    Optional<UserAccount> findByTeacherId(EntityManager em,Long teacherId);
    
    // Kiểm tra trạng thái hoạt động của tài khoản
    boolean isAccountActive(EntityManager em,String username);
}