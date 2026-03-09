package vn.ute.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

import vn.ute.ui.UIUtils;

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
        setSize(450, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Hiển thị giữa màn hình
        setResizable(false);
        
        initUI();
    }

    private void initUI() {
        // Sử dụng JPanel làm container chính với padding và màu nền sáng
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(UIUtils.SECONDARY_COLOR);

        // Header (có thể thêm logo nếu muốn)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        lblTitle.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 24));
        lblTitle.setForeground(UIUtils.PRIMARY_COLOR);
        header.add(lblTitle, BorderLayout.CENTER);
        // placeholder cho logo ở trái
        // JLabel lblLogo = new JLabel(new ImageIcon(getClass().getResource("/icons/logo16.png")));
        // header.add(lblLogo, BorderLayout.WEST);
        mainPanel.add(header, BorderLayout.NORTH);

        // Form nhập liệu sử dụng GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Tên đăng nhập:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtUsername = new JTextField(15);
        formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Mật khẩu:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        txtPassword = new JPasswordField(15);
        formPanel.add(txtPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        // dùng UIUtils để tạo nút có style nhất quán
        btnLogin = UIUtils.createPrimaryButton("Đăng nhập");

        // Xử lý sự kiện đăng nhập
        btnLogin.addActionListener(this::handleLogin);
        
        // Cho phép nhấn Enter để đăng nhập
        getRootPane().setDefaultButton(btnLogin);

        btnPanel.add(btnLogin);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void handleLogin(ActionEvent e) {
    String username = txtUsername.getText().trim();
    String password = new String(txtPassword.getPassword());

    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
        return;
    }

    try {
        // GỌI THẲNG HÀM AUTHENTICATE TỪ SERVICE
        // Hàm này đã bao gồm: Tìm user, check active, và check BCrypt
        Optional<UserAccount> userOpt = serviceManager.getUserAccountService().authenticate(username, password);

        if (userOpt.isPresent()) {
            UserAccount user = userOpt.get();
            
            // SET USER VÀO SESSION MANAGER ĐỂ MAINFRAME BIẾT ROLE
            sessionManager = SessionManager.getInstance();
            sessionManager.setCurrentUser(user);

            JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
            new MainFrame(serviceManager).setVisible(true);
            this.dispose();
        } 
    } catch (Exception ex) {
        // Hiển thị đúng thông báo lỗi từ Service (ví dụ: "Mật khẩu không chính xác")
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}
}