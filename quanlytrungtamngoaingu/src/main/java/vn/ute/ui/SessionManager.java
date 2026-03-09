package vn.ute.ui;

import vn.ute.model.UserAccount;

/**
 * SessionManager lưu thông tin người dùng hiện tại sau khi đăng nhập.
 * Được sử dụng xuyên suốt ứng dụng để kiểm soát phân quyền và lọc dữ liệu.
 */
public class SessionManager {
    private static SessionManager instance;
    private UserAccount currentUser;

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Thiết lập user đã đăng nhập sau khi login thành công.
     */
    public void setCurrentUser(UserAccount user) {
        this.currentUser = user;
    }

    /**
     * Lấy thông tin user hiện tại.
     */
    public UserAccount getCurrentUser() {
        return currentUser;
    }

    /**
     * Lấy role của user hiện tại.
     */
    public UserAccount.UserRole getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    /**
     * Lấy role dưới dạng string.
     */
    public String getCurrentRoleAsString() {
        UserAccount.UserRole role = getCurrentRole();
        String roleStr = role != null ? role.name() : "Student";
        return roleStr;
    }

    /**
     * Lấy ID của teacher nếu user đó là teacher.
     */
    public Long getCurrentTeacherId() {
        if (currentUser != null && currentUser.getTeacher() != null) {
            return currentUser.getTeacher().getId();
        }
        return null;
    }

    /**
     * Lấy ID của student nếu user đó là student.
     */
    public Long getCurrentStudentId() {
        if (currentUser != null && currentUser.getStudent() != null) {
            return currentUser.getStudent().getId();
        }
        return null;
    }

    /**
     * Kiểm tra xem user có quyền admin không.
     */
    public boolean isAdmin() {
        return getCurrentRole() == UserAccount.UserRole.Admin;
    }

    /**
     * Kiểm tra xem user có quyền staff không.
     */
    public boolean isStaff() {
        return getCurrentRole() == UserAccount.UserRole.Staff;
    }

    /**
     * Kiểm tra xem user có quyền teacher không.
     */
    public boolean isTeacher() {
        return getCurrentRole() == UserAccount.UserRole.Teacher;
    }

    /**
     * Kiểm tra xem user có quyền student không.
     */
    public boolean isStudent() {
        return getCurrentRole() == UserAccount.UserRole.Student;
    }

    /**
     * Xóa session (logout).
     */
    public void logout() {
        currentUser = null;
    }
}
