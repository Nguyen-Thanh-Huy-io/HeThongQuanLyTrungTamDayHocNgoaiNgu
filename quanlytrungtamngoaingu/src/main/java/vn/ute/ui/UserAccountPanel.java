package vn.ute.ui;

import vn.ute.model.UserAccount;
import vn.ute.service.ServiceManager;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.function.Supplier;

public class UserAccountPanel extends BasePanel<UserAccount> {
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123456";
    private static final String DEFAULT_STAFF_PASSWORD = "nv123456";
    private static final String DEFAULT_TEACHER_PASSWORD = "gv123456";
    private static final String DEFAULT_STUDENT_PASSWORD = "hs123456";

    private final ServiceManager serviceManager;
    private final Supplier<List<UserAccount>> dataLoader;

    public UserAccountPanel(ServiceManager serviceManager, Supplier<List<UserAccount>> dataLoader) {
        super(new GenericTableModel<>(
                new String[]{"Tên đăng nhập", "Quyền hạn", "Hoạt động"},
                new String[]{"username", "role", "isActive"}
        ));
        this.serviceManager = serviceManager;
        this.dataLoader = dataLoader;

        btnAdd.setText("Reset MK");
        btnEdit.setText("Khóa/Mở");
        btnDelete.setText("Đổi quyền");

        reloadData();
    }

    @Override
    public void reloadData() {
        new Thread(() -> {
            List<UserAccount> accounts = dataLoader.get();
            SwingUtilities.invokeLater(() -> ((GenericTableModel<UserAccount>) tableModel).setData(accounts));
        }).start();
    }

    @Override
    protected void onAdd() {
        UserAccount selected = getSelectedAccount();
        if (selected == null) {
            return;
        }

        String defaultPassword = getDefaultPasswordByRole(selected.getRole());
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Reset mật khẩu tài khoản '" + selected.getUsername() + "' về mặc định?",
                "Xác nhận reset mật khẩu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            serviceManager.getUserAccountService().updatePassword(selected.getId(), defaultPassword);
            JOptionPane.showMessageDialog(this, "Reset mật khẩu thành công. Mật khẩu mặc định: " + defaultPassword);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Reset mật khẩu thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onEdit() {
        UserAccount selected = getSelectedAccount();
        if (selected == null) {
            return;
        }

        boolean nextState = !selected.isActive();
        String actionLabel = nextState ? "mở khóa" : "khóa";
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn " + actionLabel + " tài khoản '" + selected.getUsername() + "'?",
                "Xác nhận thay đổi trạng thái",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            serviceManager.getUserAccountService().setAccountActive(selected.getId(), nextState);
            JOptionPane.showMessageDialog(this, "Cập nhật trạng thái tài khoản thành công.");
            reloadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onDelete() {
        UserAccount selected = getSelectedAccount();
        if (selected == null) {
            return;
        }

        UserAccount.UserRole[] roleOptions = UserAccount.UserRole.values();
        UserAccount.UserRole currentRole = selected.getRole();
        UserAccount.UserRole nextRole = (UserAccount.UserRole) JOptionPane.showInputDialog(
                this,
                "Chọn quyền mới cho tài khoản '" + selected.getUsername() + "':",
                "Đổi quyền tài khoản",
                JOptionPane.PLAIN_MESSAGE,
                null,
                roleOptions,
                currentRole
        );

        if (nextRole == null || nextRole == currentRole) {
            return;
        }

        try {
            serviceManager.getUserAccountService().updateRole(selected.getId(), nextRole);
            JOptionPane.showMessageDialog(this, "Đổi quyền tài khoản thành công.");
            reloadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đổi quyền thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private UserAccount getSelectedAccount() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản.");
            return null;
        }
        return ((GenericTableModel<UserAccount>) tableModel).getRow(row);
    }

    private String getDefaultPasswordByRole(UserAccount.UserRole role) {
        if (role == UserAccount.UserRole.Admin) {
            return DEFAULT_ADMIN_PASSWORD;
        }
        if (role == UserAccount.UserRole.Teacher) {
            return DEFAULT_TEACHER_PASSWORD;
        }
        if (role == UserAccount.UserRole.Student) {
            return DEFAULT_STUDENT_PASSWORD;
        }
        return DEFAULT_STAFF_PASSWORD;
    }
}
