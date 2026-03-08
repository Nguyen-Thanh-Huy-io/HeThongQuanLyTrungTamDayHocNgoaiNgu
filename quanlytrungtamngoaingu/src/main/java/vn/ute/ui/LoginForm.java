package vn.ute.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

import vn.ute.model.UserAccount;
import vn.ute.service.ServiceManager;

public class LoginForm extends JFrame {
    private ServiceManager serviceManager;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginForm(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        
        setTitle("HỆ THỐNG QUẢN LÝ TRUNG TÂM - ĐĂNG NHẬP");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Hiển thị giữa màn hình
        setResizable(false);
        
        initUI();
    }

    private void initUI() {
        // Sử dụng JPanel làm container chính với padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JLabel lblTitle = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(41, 128, 185)); // Màu xanh chuyên nghiệp
        mainPanel.add(lblTitle, BorderLayout.NORTH);

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
        btnLogin = new JButton("Đăng nhập");
        btnLogin.setPreferredSize(new Dimension(120, 35));
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));

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