package vn.ute.ui;

import vn.ute.model.Staff;
import vn.ute.model.Student;
import vn.ute.model.Teacher;
import vn.ute.model.UserAccount;
import vn.ute.service.ServiceManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

        btnCreateAccount = UIUtils.createPrimaryButton("Tạo tài khoản");
        btnCreateAccount.addActionListener(e -> onCreateAccount());
        toolbar.add(btnCreateAccount, 0);
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

    // ─── Tạo tài khoản ──────────────────────────────────────────────────────

    private void onCreateAccount() {
        // ── Build form ──────────────────────────────────────────────────────
        JTextField usernameField    = new JTextField(22);
        JPasswordField pwField      = new JPasswordField(22);
        JPasswordField pwConfirm    = new JPasswordField(22);
        JComboBox<UserAccount.UserRole> roleCombo = new JComboBox<>(UserAccount.UserRole.values());
        roleCombo.setSelectedItem(UserAccount.UserRole.Teacher);

        JLabel entityLabel  = new JLabel("Giáo viên liên kết (*):");
        JComboBox<EntityOption> entityCombo = new JComboBox<>();
        entityCombo.setPreferredSize(new Dimension(280, 32));

        // Render entity combo nicely
        entityCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value != null ? value.toString() : "— Không chọn —");
                return this;
            }
        });

        // Initial fill
        refreshEntityCombo(entityCombo, entityLabel, (UserAccount.UserRole) roleCombo.getSelectedItem());

        roleCombo.addActionListener(e ->
            refreshEntityCombo(entityCombo, entityLabel, (UserAccount.UserRole) roleCombo.getSelectedItem()));

        // ── Layout using GridBagLayout ──────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR),
                "Thông tin tài khoản",
                TitledBorder.LEFT, TitledBorder.TOP,
                UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 13f),
                UIUtils.PRIMARY_COLOR));
        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(6, 10, 6, 10);
        g.anchor  = GridBagConstraints.WEST;
        g.fill    = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormRow(form, g, row++, "Tên đăng nhập (*):", usernameField);
        addFormRow(form, g, row++, "Mật khẩu (*):",       pwField);
        addFormRow(form, g, row++, "Xác nhận mật khẩu (*):", pwConfirm);
        addFormRow(form, g, row++, "Quyền hạn (*):",      roleCombo);
        addFormRow(form, g, row,   entityLabel,            entityCombo);

        int option = JOptionPane.showConfirmDialog(
                this, form, "Tạo tài khoản mới",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option != JOptionPane.OK_OPTION) return;

        // ── Validate ────────────────────────────────────────────────────────
        String username = usernameField.getText().trim();
        String password = new String(pwField.getPassword());
        String confirm  = new String(pwConfirm.getPassword());
        UserAccount.UserRole role = (UserAccount.UserRole) roleCombo.getSelectedItem();
        EntityOption selectedEntity = (EntityOption) entityCombo.getSelectedItem();

        if (username.isEmpty()) {
            showError("Tên đăng nhập không được để trống.");
            return;
        }
        if (password.isEmpty()) {
            showError("Mật khẩu không được để trống.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Mật khẩu xác nhận không khớp.");
            return;
        }
        if (role == null) {
            showError("Vui lòng chọn quyền hạn.");
            return;
        }

        // Teacher/Student/Staff BẮT BUỘC phải liên kết
        if (role != UserAccount.UserRole.Admin) {
            if (selectedEntity == null || selectedEntity.id == null) {
                showError("Bạn phải chọn " + roleLabel(role) + " để liên kết với tài khoản này.\n"
                        + "Nếu chưa có, hãy tạo " + roleLabel(role) + " trước rồi quay lại.");
                return;
            }
        }

        // ── Confirm ─────────────────────────────────────────────────────────
        String entityInfo = (selectedEntity != null && selectedEntity.id != null)
                ? "\nLiên kết: " + selectedEntity
                : "\n(Không liên kết thực thể)";
        int confirm2 = JOptionPane.showConfirmDialog(
                this,
                "Tạo tài khoản '" + username + "' quyền " + role + "?" + entityInfo,
                "Xác nhận tạo tài khoản",
                JOptionPane.YES_NO_OPTION);
        if (confirm2 != JOptionPane.YES_OPTION) return;

        // ── Persist ─────────────────────────────────────────────────────────
        try {
            Long linkedId = (selectedEntity != null) ? selectedEntity.id : null;
            serviceManager.getUserAccountService().createAccount(username, password, role, linkedId);
            JOptionPane.showMessageDialog(this, "Tạo tài khoản thành công!");
            reloadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Tạo tài khoản thất bại: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Nạp danh sách thực thể chưa có tài khoản vào combo, cập nhật label. */
    private void refreshEntityCombo(JComboBox<EntityOption> combo, JLabel label,
                                    UserAccount.UserRole role) {
        combo.removeAllItems();
        if (role == null || role == UserAccount.UserRole.Admin) {
            label.setText("Liên kết với:");
            combo.addItem(new EntityOption(null, "— Admin không cần liên kết —"));
            combo.setEnabled(false);
            return;
        }
        combo.setEnabled(true);

        try {
            // Lấy danh sách ID đã có tài khoản để lọc ra
            List<UserAccount> existingAccounts = serviceManager.getUserAccountService().findAll();

            switch (role) {
                case Teacher -> {
                    label.setText("Giáo viên liên kết (*):");
                    Set<Long> linkedIds = existingAccounts.stream()
                            .filter(a -> a.getRole() == UserAccount.UserRole.Teacher
                                      && a.getTeacher() != null)
                            .map(a -> a.getTeacher().getId())
                            .collect(Collectors.toSet());

                    List<Teacher> teachers = serviceManager.getTeacherService().findAll().stream()
                            .filter(t -> !linkedIds.contains(t.getId()))
                            .sorted((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getFullName(), b.getFullName()))
                            .toList();

                    if (teachers.isEmpty()) {
                        combo.addItem(new EntityOption(null, "Tất cả giáo viên đã có tài khoản"));
                    } else {
                        teachers.forEach(t -> combo.addItem(
                                new EntityOption(t.getId(), "[GV#" + t.getId() + "] " + t.getFullName()
                                        + (t.getSpecialty() != null ? " — " + t.getSpecialty() : ""))));
                    }
                }
                case Student -> {
                    label.setText("Học viên liên kết (*):");
                    Set<Long> linkedIds = existingAccounts.stream()
                            .filter(a -> a.getRole() == UserAccount.UserRole.Student
                                      && a.getStudent() != null)
                            .map(a -> a.getStudent().getId())
                            .collect(Collectors.toSet());

                    List<Student> students = serviceManager.getStudentService().findAll().stream()
                            .filter(s -> !linkedIds.contains(s.getId()))
                            .sorted((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getFullName(), b.getFullName()))
                            .toList();

                    if (students.isEmpty()) {
                        combo.addItem(new EntityOption(null, "Tất cả học viên đã có tài khoản"));
                    } else {
                        students.forEach(s -> combo.addItem(
                                new EntityOption(s.getId(), "[HV#" + s.getId() + "] " + s.getFullName()
                                        + (s.getEmail() != null ? " — " + s.getEmail() : ""))));
                    }
                }
                case Staff -> {
                    label.setText("Nhân viên liên kết (*):");
                    Set<Long> linkedIds = existingAccounts.stream()
                            .filter(a -> a.getRole() == UserAccount.UserRole.Staff
                                      && a.getStaff() != null)
                            .map(a -> a.getStaff().getId())
                            .collect(Collectors.toSet());

                    List<Staff> staffList = serviceManager.getStaffService().findAll().stream()
                            .filter(s -> !linkedIds.contains(s.getId()))
                            .sorted((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getFullName(), b.getFullName()))
                            .toList();

                    if (staffList.isEmpty()) {
                        combo.addItem(new EntityOption(null, "Tất cả nhân viên đã có tài khoản"));
                    } else {
                        staffList.forEach(s -> combo.addItem(
                                new EntityOption(s.getId(), "[NV#" + s.getId() + "] " + s.getFullName())));
                    }
                }
                default -> combo.addItem(new EntityOption(null, "— Không cần liên kết —"));
            }
        } catch (Exception ex) {
            combo.addItem(new EntityOption(null, "Lỗi tải dữ liệu: " + ex.getMessage()));
        }
    }

    private void addFormRow(JPanel form, GridBagConstraints g, int row, String labelText, JComponent field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        form.add(new JLabel(labelText), g);
        g.gridx = 1; g.weightx = 1;
        form.add(field, g);
    }

    private void addFormRow(JPanel form, GridBagConstraints g, int row, JLabel label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        form.add(label, g);
        g.gridx = 1; g.weightx = 1;
        form.add(field, g);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private String roleLabel(UserAccount.UserRole role) {
        return switch (role) {
            case Teacher -> "giáo viên";
            case Student -> "học viên";
            case Staff   -> "nhân viên";
            default      -> "thực thể";
        };
    }

    // ─── Reset password ─────────────────────────────────────────────────────

    @Override
    protected void onAdd() {
        UserAccount selected = getSelectedAccount();
        if (selected == null) return;

        String newPassword = JOptionPane.showInputDialog(
                this, "Nhập mật khẩu mới cho tài khoản '" + selected.getUsername() + "':");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mật khẩu không được để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Đổi mật khẩu tài khoản '" + selected.getUsername() + "'?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            serviceManager.getUserAccountService().updatePassword(selected.getId(), newPassword.trim());
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thất bại: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Khóa / Mở tài khoản ────────────────────────────────────────────────

    @Override
    protected void onEdit() {
        UserAccount selected = getSelectedAccount();
        if (selected == null) return;

        boolean nextState = !selected.isActive();
        String actionLabel = nextState ? "mở khóa" : "khóa";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn " + actionLabel + " tài khoản '" + selected.getUsername() + "'?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            serviceManager.getUserAccountService().setAccountActive(selected.getId(), nextState);
            JOptionPane.showMessageDialog(this, "Cập nhật trạng thái tài khoản thành công.");
            reloadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thất bại: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Đổi quyền ──────────────────────────────────────────────────────────

    @Override
    protected void onDelete() {
        UserAccount selected = getSelectedAccount();
        if (selected == null) return;

        UserAccount.UserRole nextRole = (UserAccount.UserRole) JOptionPane.showInputDialog(
                this, "Chọn quyền mới cho tài khoản '" + selected.getUsername() + "':",
                "Đổi quyền", JOptionPane.PLAIN_MESSAGE, null,
                UserAccount.UserRole.values(), selected.getRole());

        if (nextRole == null || nextRole == selected.getRole()) return;

        try {
            serviceManager.getUserAccountService().updateRole(selected.getId(), nextRole);
            JOptionPane.showMessageDialog(this, "Đổi quyền tài khoản thành công.");
            reloadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đổi quyền thất bại: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

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

    // ─── Inner helper class ──────────────────────────────────────────────────

    private static class EntityOption {
        final Long   id;
        final String label;

        EntityOption(Long id, String label) {
            this.id    = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}


