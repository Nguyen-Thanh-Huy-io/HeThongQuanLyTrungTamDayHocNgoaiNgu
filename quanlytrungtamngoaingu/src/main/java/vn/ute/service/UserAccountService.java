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

    // Khóa/mở khóa tài khoản
    void setAccountActive(Long accountId, boolean active) throws Exception;

    // Đổi quyền tài khoản
    void updateRole(Long accountId, UserAccount.UserRole role) throws Exception;

    // Tạo tài khoản mới (không liên kết entity — chỉ dùng cho Admin)
    UserAccount createAccount(String username, String password, UserAccount.UserRole role) throws Exception;

    /**
     * Tạo tài khoản và liên kết ngay với Teacher / Student / Staff.
     * linkedEntityId là teacher_id / student_id / staff_id tương ứng với role.
     */
    UserAccount createAccount(String username, String password, UserAccount.UserRole role, Long linkedEntityId) throws Exception;
}