package vn.ute.ui;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

import vn.ute.ui.UIUtils;

import vn.ute.model.Student;
import vn.ute.model.UserAccount;
import vn.ute.service.ServiceManager;
import vn.ute.ui.SessionManager;

public class LoginForm extends JFrame {
    private ServiceManager serviceManager;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private SessionManager sessionManager;

    public LoginForm(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        
        setTitle("HỆ THỐNG QUẢN LÝ TRUNG TÂM - ĐĂNG NHẬP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        initUI();
        pack();
        setMinimumSize(new Dimension(520, 360));
        setLocationRelativeTo(null); // Hiển thị giữa màn hình
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new MigLayout("fill,insets 18", "[grow]", "[grow]"));
        mainPanel.setBackground(new Color(244, 247, 251));

        JPanel card = new JPanel(new MigLayout("fillx,insets 22,gap 10", "[grow]", "[]12[]12[]18[]"));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 231, 239)),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblTitle = new JLabel("Dang Nhap", SwingConstants.CENTER);
        lblTitle.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 24));
        lblTitle.setForeground(new Color(32, 83, 117));
        card.add(lblTitle, "growx, wrap");

        JPanel formPanel = new JPanel(new MigLayout("fillx,insets 0,gapx 10,gapy 10", "[120!][grow]", "[][]"));
        formPanel.setOpaque(false);
        formPanel.add(new JLabel("Ten dang nhap:"));
        txtUsername = new JTextField(22);
        formPanel.add(txtUsername, "growx, wrap");
        formPanel.add(new JLabel("Mat khau:"));
        txtPassword = new JPasswordField(22);
        formPanel.add(txtPassword, "growx");
        card.add(formPanel, "growx, wrap");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnPanel.setOpaque(false);
        btnLogin = UIUtils.createPrimaryButton("Đăng nhập");

        // Xử lý sự kiện đăng nhập
        btnLogin.addActionListener(this::handleLogin);
        
        // Cho phép nhấn Enter để đăng nhập
        getRootPane().setDefaultButton(btnLogin);

        btnPanel.add(btnLogin);
        card.add(btnPanel, "growx, wrap");

        JLabel hint = new JLabel("Tai khoan duoc cap boi quan tri vien", SwingConstants.CENTER);
        hint.setForeground(new Color(115, 125, 138));
        card.add(hint, "growx");

        mainPanel.add(card, "wmin 470, hmin 300, center");
        add(mainPanel, BorderLayout.CENTER);
    }

    private void handleLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            Optional<UserAccount> userOpt = serviceManager.getUserAccountService().authenticate(username, password);

            if (userOpt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Đăng nhập thất bại.", "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
                return;
            }

            UserAccount user = userOpt.get();

            // Nếu là Student nhưng chưa được liên kết với Student entity, thử tìm student bằng email/phone
            if (user.getRole() == UserAccount.UserRole.Student && user.getStudent() == null) {
                try {
                    Optional<Student> studentOpt = Optional.empty();
                    studentOpt = serviceManager.getStudentService().findByEmail(user.getUsername());
                    if (studentOpt.isEmpty()) {
                        studentOpt = serviceManager.getStudentService().findByPhone(user.getUsername());
                    }
                    studentOpt.ifPresent(user::setStudent);
                } catch (Exception ex) {
                    // Nếu không tìm được, vẫn tiến hành đăng nhập nhưng sẽ thông báo khi cần
                }
            }

            sessionManager = SessionManager.getInstance();
            sessionManager.setCurrentUser(user);

            btnLogin.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            setVisible(false);

            SwingUtilities.invokeLater(() -> openMainFrame());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void openMainFrame() {
        try {
            MainFrame mainFrame = new MainFrame(serviceManager);
            mainFrame.setVisible(true);
            dispose();
        } catch (Exception ex) {
            setCursor(Cursor.getDefaultCursor());
            btnLogin.setEnabled(true);
            setVisible(true);
            SessionManager.getInstance().logout();
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể mở giao diện sau đăng nhập: " + ex.getMessage(),
                    "Lỗi khởi tạo giao diện",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }
}