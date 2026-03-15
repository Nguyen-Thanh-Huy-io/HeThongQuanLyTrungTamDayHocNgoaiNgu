package vn.ute.ui;

import vn.ute.model.UserAccount;
import vn.ute.service.ServiceManager;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.Box;

public class UserAccountPanel extends BasePanel<UserAccount> {

    private final ServiceManager serviceManager;
    private final Supplier<List<UserAccount>> dataLoader;
    private JButton btnCreateAccount;

    public UserAccountPanel(ServiceManager serviceManager, Supplier<List<UserAccount>> dataLoader) {
        super(new GenericTableModel<>(
                new String[]{"Tên đăng nhập", "Quyền hạn", "Hoạt động"},
                new String[]{"username", "role", "isActive"}
        ));
        this.serviceManager = serviceManager;
        this.dataLoader = dataLoader;

        // Thêm nút Tạo tài khoản
        btnCreateAccount = UIUtils.createPrimaryButton("Tạo tài khoản");
        btnCreateAccount.addActionListener(e -> onCreateAccount());
        toolbar.add(btnCreateAccount, 0); // Thêm vào đầu toolbar
        toolbar.add(Box.createHorizontalStrut(6), 1);

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

        String newPassword = JOptionPane.showInputDialog(this, "Nhập mật khẩu mới cho tài khoản '" + selected.getUsername() + "':");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mật khẩu không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Đổi mật khẩu tài khoản '" + selected.getUsername() + "'?",
                "Xác nhận đổi mật khẩu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            serviceManager.getUserAccountService().updatePassword(selected.getId(), newPassword.trim());
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCreateAccount() {
        // Dialog nhập thông tin tài khoản mới
        String username = JOptionPane.showInputDialog(this, "Nhập tên đăng nhập:");
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        UserAccount.UserRole[] roles = UserAccount.UserRole.values();
        UserAccount.UserRole selectedRole = (UserAccount.UserRole) JOptionPane.showInputDialog(
                this,
                "Chọn quyền cho tài khoản:",
                "Chọn quyền",
                JOptionPane.PLAIN_MESSAGE,
                null,
                roles,
                UserAccount.UserRole.Student
        );

        if (selectedRole == null) {
            return;
        }

        String password = JOptionPane.showInputDialog(this, "Nhập mật khẩu cho tài khoản:");
        if (password == null || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mật khẩu không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Tạo tài khoản '" + username + "' với quyền " + selectedRole + " và mật khẩu đã nhập?",
                "Xác nhận tạo tài khoản",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            serviceManager.getUserAccountService().createAccount(username.trim(), password.trim(), selectedRole);
            JOptionPane.showMessageDialog(this, "Tạo tài khoản thành công!");
            reloadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Tạo tài khoản thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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

    @Override
    public void setButtonEnabled(String buttonName, boolean enabled) {
        super.setButtonEnabled(buttonName, enabled);
        if ("create".equalsIgnoreCase(buttonName)) {
            btnCreateAccount.setEnabled(enabled);
        }
    }

    @Override
    public void setButtonVisible(String buttonName, boolean visible) {
        super.setButtonVisible(buttonName, visible);
        if ("create".equalsIgnoreCase(buttonName)) {
            btnCreateAccount.setVisible(visible);
        }
    }
}
