package vn.ute.service;

import vn.ute.model.UserAccount;
import java.util.Optional;

public interface UserAccountService extends Service<UserAccount, Long> {
    // Nghiệp vụ đăng nhập tổng hợp
    Optional<UserAccount> authenticate(String username, String password) throws Exception;
    
    // Tìm kiếm tài khoản liên kết
    Optional<UserAccount> getByStudent(Long studentId) throws Exception;
    Optional<UserAccount> getByTeacher(Long teacherId) throws Exception;
    
    // Đổi mật khẩu
    void updatePassword(Long accountId, String newPassword) throws Exception;
}