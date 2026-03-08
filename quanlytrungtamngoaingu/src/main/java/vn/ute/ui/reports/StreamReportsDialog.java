package vn.ute.ui.reports;

import vn.ute.model.*;
import vn.ute.service.ServiceManager;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StreamReportsDialog extends JDialog {
    private final ServiceManager sm = ServiceManager.getInstance();
    private final JTextArea reportArea = new JTextArea();

    public StreamReportsDialog(Frame owner) {
        super(owner, "Phân tích dữ liệu bằng Stream API", true);
        setLayout(new BorderLayout());

        // Khu vực hiển thị kết quả
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        add(new JScrollPane(reportArea), BorderLayout.CENTER);

        // Bảng điều khiển nút bấm
        JPanel btnPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        
        JButton btnRevenue = new JButton("Thống kê doanh thu (Payment Stream)");
        btnRevenue.addActionListener(e -> runRevenueReport());

        JButton btnStatus = new JButton("Tỉ lệ Điểm danh (Attendance Stream)");
        btnStatus.addActionListener(e -> runAttendanceReport());

        JButton btnRoles = new JButton("Phân bổ Tài khoản (UserAccount Stream)");
        btnRoles.addActionListener(e -> runAccountReport());

        btnPanel.add(btnRevenue);
        btnPanel.add(btnStatus);
        btnPanel.add(btnRoles);

        add(btnPanel, BorderLayout.WEST);
        setSize(800, 500);
        setLocationRelativeTo(owner);
    }

    private void runRevenueReport() {
    try {
        // Phải để trong try-catch vì findAll() ném Exception
        List<Payment> payments = sm.getPaymentService().findAll();
        
        java.math.BigDecimal total = payments.stream()
                .filter(p -> p.getStatus() == Payment.PayStatus.Completed)
                .map(Payment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        reportArea.setText("--- BÁO CÁO TÀI CHÍNH ---\n");
        reportArea.append("Tổng doanh thu thực thu: " + String.format("%,.2f", total) + " VNĐ\n");
    } catch (Exception e) {
        reportArea.setText("Lỗi nạp dữ liệu tài chính: " + e.getMessage());
        e.printStackTrace();
    }
}

private void runAttendanceReport() {
    try {
        List<Attendance> list = sm.getAttendanceService().findAll();
        
        Map<Attendance.AttendStatus, Long> stats = list.stream()
                .collect(Collectors.groupingBy(Attendance::getStatus, Collectors.counting()));

        reportArea.setText("--- THỐNG KÊ CHUYÊN CẦN ---\n");
        if (list.isEmpty()) reportArea.append("(Chưa có dữ liệu điểm danh)");
        stats.forEach((status, count) -> reportArea.append(status + ": " + count + " buổi\n"));
    } catch (Exception e) {
        reportArea.setText("Lỗi nạp dữ liệu điểm danh: " + e.getMessage());
    }
}

private void runAccountReport() {
    try {
        List<UserAccount> accounts = sm.getUserAccountService().findAll();
        
        Map<UserAccount.UserRole, Long> roles = accounts.stream()
                .collect(Collectors.groupingBy(UserAccount::getRole, Collectors.counting()));

        reportArea.setText("--- PHÂN BỔ TÀI KHOẢN HỆ THỐNG ---\n");
        roles.forEach((role, count) -> reportArea.append(role + ": " + count + " user\n"));
    } catch (Exception e) {
        reportArea.setText("Lỗi nạp dữ liệu tài khoản: " + e.getMessage());
    }
}
}