package vn.ute.ui.reports;

import vn.ute.model.*;
import vn.ute.service.ServiceManager;
import vn.ute.ui.UIUtils;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Trang Báo cáo & Thống kê toàn diện dành cho Admin.
 * Gồm 4 tab: Tổng quan, Tài chính, Học vụ, Nhân sự & Vận hành.
 */
public class AdminReportPanel extends JPanel {

    private final ServiceManager sm;

    // ── KPI labels ─────────────────────────────────────────────────────────────
    private JLabel lblRevenue, lblStudents, lblActiveClasses, lblPendingInv;
    private JLabel lblLastUpdated;

    // ── Table models (Dashboard) ───────────────────────────────────────────────
    private final DefaultTableModel enrollStatusModel = readonlyModel("Trạng thái", "Số lượng", "Tỉ lệ %");
    private final DefaultTableModel accountRoleModel  = readonlyModel("Vai trò", "Số tài khoản");

    // ── Table models (Finance) ────────────────────────────────────────────────
    private final DefaultTableModel payMethodModel   = readonlyModel("Phương thức TT", "Số GD", "Doanh thu (VNĐ)");
    private final DefaultTableModel invoiceStatModel = readonlyModel("Trạng thái hóa đơn", "Số lượng", "Tổng tiền (VNĐ)");

    // ── Table models (Academic) ───────────────────────────────────────────────
    private final DefaultTableModel enrollLevelModel = readonlyModel("Trình độ", "Số đăng ký");
    private final DefaultTableModel attendanceModel  = readonlyModel("Trạng thái điểm danh", "Số buổi", "Tỉ lệ %");
    private final DefaultTableModel resultModel      = readonlyModel("Kết quả học tập", "Số học viên", "Tỉ lệ %");
    private final DefaultTableModel gradeModel       = readonlyModel("Xếp loại (Grade)", "Số học viên");

    // ── Table models (Operations) ─────────────────────────────────────────────
    private final DefaultTableModel teacherModel     = readonlyModel("Giáo viên", "Chuyên môn", "Số lớp");
    private final DefaultTableModel classStatusModel = readonlyModel("Trạng thái lớp", "Số lượng");
    private final DefaultTableModel courseModel      = readonlyModel("Khóa học", "Cấp độ", "Học phí (VNĐ)");

    // ─────────────────────────────────────────────────────────────────────────
    public AdminReportPanel(ServiceManager sm) {
        this.sm = sm;
        setLayout(new BorderLayout(0, 0));
        setBackground(UIUtils.APP_BG);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(),   BorderLayout.CENTER);
        loadData();
    }

    // ══════════════════════════════════ HEADER ════════════════════════════════

    private JPanel buildHeader() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIUtils.SURFACE);
        bar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, UIUtils.BORDER_COLOR),
            javax.swing.BorderFactory.createEmptyBorder(14, 22, 14, 22)));

        // Left: title + timestamp
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Báo cáo & Thống kê Hệ thống");
        title.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 20f));
        title.setForeground(UIUtils.PRIMARY_COLOR);
        title.setAlignmentX(LEFT_ALIGNMENT);

        lblLastUpdated = new JLabel("Nhấn 'Làm mới' để tải dữ liệu");
        lblLastUpdated.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.PLAIN, 12f));
        lblLastUpdated.setForeground(UIUtils.TEXT_SECONDARY);
        lblLastUpdated.setAlignmentX(LEFT_ALIGNMENT);

        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(lblLastUpdated);

        // Right: refresh button
        JButton btnRefresh = UIUtils.createPrimaryButton("⟳  Làm mới");
        btnRefresh.setPreferredSize(new Dimension(130, 38));
        btnRefresh.addActionListener(e -> loadData());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnRefresh);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ══════════════════════════════════ TABS ══════════════════════════════════

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 14f));
        tabs.setBackground(UIUtils.APP_BG);
        tabs.addTab("  Tổng quan  ",              buildDashboardTab());
        tabs.addTab("  Tài chính  ",              buildFinanceTab());
        tabs.addTab("  Học vụ  ",                 buildAcademicTab());
        tabs.addTab("  Nhân sự & Vận hành  ",     buildOperationsTab());
        return tabs;
    }

    // ══════════════════════════════ TAB 1 – TỔNG QUAN ════════════════════════

    private JPanel buildDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBackground(UIUtils.APP_BG);
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 22, 20, 22));

        // KPI row
        lblRevenue      = new JLabel("—");
        lblStudents     = new JLabel("—");
        lblActiveClasses = new JLabel("—");
        lblPendingInv   = new JLabel("—");

        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.add(kpiCard("Tổng Doanh Thu",        "VNĐ",         lblRevenue,       new Color(21, 101, 192)));
        kpiRow.add(kpiCard("Tổng Học Viên",         "người",        lblStudents,      new Color(39, 174, 96)));
        kpiRow.add(kpiCard("Lớp đang mở / học",     "lớp",          lblActiveClasses, new Color(230, 81, 0)));
        kpiRow.add(kpiCard("Hóa đơn chưa thanh toán","hóa đơn",   lblPendingInv,    new Color(192, 57, 43)));

        // Bottom: two summary tables
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setOpaque(false);
        bottomRow.add(section("Phân bổ trạng thái đăng ký lớp",  styledScrollPane(enrollStatusModel)));
        bottomRow.add(section("Phân bổ tài khoản theo vai trò",   styledScrollPane(accountRoleModel)));

        panel.add(kpiRow,     BorderLayout.NORTH);
        panel.add(bottomRow,  BorderLayout.CENTER);
        return panel;
    }

    /** Builds a single KPI card with a colored vertical accent stripe. */
    private JPanel kpiCard(String label, String unit, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new RoundedBorder(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160), 1, 12));

        // Accent stripe
        JPanel stripe = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
        };
        stripe.setPreferredSize(new Dimension(5, 0));
        stripe.setOpaque(false);
        card.add(stripe, BorderLayout.WEST);

        // Content
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 10));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.PLAIN, 12f));
        lbl.setForeground(UIUtils.TEXT_SECONDARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accent);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel unitLbl = new JLabel(unit);
        unitLbl.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.PLAIN, 11f));
        unitLbl.setForeground(UIUtils.TEXT_SECONDARY);
        unitLbl.setAlignmentX(LEFT_ALIGNMENT);

        content.add(lbl);
        content.add(Box.createVerticalStrut(6));
        content.add(valueLabel);
        content.add(Box.createVerticalStrut(2));
        content.add(unitLbl);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // ══════════════════════════════ TAB 2 – TÀI CHÍNH ════════════════════════

    private JPanel buildFinanceTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 18, 0));
        panel.setBackground(UIUtils.APP_BG);
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 22, 20, 22));
        panel.add(section("Doanh thu theo phương thức thanh toán", styledScrollPane(payMethodModel)));
        panel.add(section("Tình trạng hóa đơn",                    styledScrollPane(invoiceStatModel)));
        return panel;
    }

    // ══════════════════════════════ TAB 3 – HỌC VỤ ═══════════════════════════

    private JPanel buildAcademicTab() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 18, 18));
        panel.setBackground(UIUtils.APP_BG);
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 22, 20, 22));
        panel.add(section("Số đăng ký theo trình độ khóa học", styledScrollPane(enrollLevelModel)));
        panel.add(section("Thống kê điểm danh",                 styledScrollPane(attendanceModel)));
        panel.add(section("Kết quả đầu ra học viên",            styledScrollPane(resultModel)));
        panel.add(section("Xếp loại (Grade) học viên",          styledScrollPane(gradeModel)));
        return panel;
    }

    // ══════════════════════════════ TAB 4 – NHÂN SỰ & VẬN HÀNH ══════════════

    private JPanel buildOperationsTab() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 18, 18));
        panel.setBackground(UIUtils.APP_BG);
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 22, 20, 22));
        panel.add(section("Giáo viên phụ trách nhiều lớp nhất",  styledScrollPane(teacherModel)));
        panel.add(section("Phân loại trạng thái lớp học",         styledScrollPane(classStatusModel)));
        panel.add(section("Danh sách khóa học & học phí",         styledScrollPane(courseModel)));
        return panel;
    }

    // ══════════════════════════════ SHARED UI HELPERS ════════════════════════

    /** Wraps a DefaultTableModel in a styled JScrollPane. */
    private JScrollPane styledScrollPane(DefaultTableModel model) {
        JTable table = new JTable(model);
        UIUtils.styleTable(table);
        table.setFillsViewportHeight(true);

        // Center-align non-first columns
        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerR);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(UIUtils.SURFACE);
        return sp;
    }

    /** Creates a white card panel with a title label and content. */
    private JPanel section(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(UIUtils.SURFACE);
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1),
            javax.swing.BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JLabel lbl = new JLabel(title);
        lbl.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 14f));
        lbl.setForeground(UIUtils.PRIMARY_COLOR);
        lbl.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_COLOR),
            javax.swing.BorderFactory.createEmptyBorder(0, 0, 6, 0)));

        card.add(lbl,     BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private static DefaultTableModel readonlyModel(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    // ══════════════════════════════ DATA LOADING ══════════════════════════════

    /** Loads all data on a background thread, then updates UI on EDT. */
    public void loadData() {
        lblLastUpdated.setText("Đang tải dữ liệu...");

        new SwingWorker<ReportData, Void>() {
            @Override
            protected ReportData doInBackground() throws Exception {
                ReportData d = new ReportData();
                d.payments  = sm.getPaymentService().findAll();
                d.students  = sm.getStudentService().findAll();
                d.classes   = sm.getClassService().findAll();
                d.invoices  = sm.getInvoiceService().findAll();
                d.enrolls   = sm.getEnrollmentService().findAll();
                d.attends   = sm.getAttendanceService().findAll();
                d.results   = sm.getResultService().findAll();
                d.accounts  = sm.getUserAccountService().findAll();
                d.teachers  = sm.getTeacherService().findAll();
                d.courses   = sm.getCourseService().findAll();
                return d;
            }

            @Override
            protected void done() {
                try {
                    ReportData d = get();
                    applyDashboard(d);
                    applyFinance(d);
                    applyAcademic(d);
                    applyOperations(d);
                    lblLastUpdated.setText("Cập nhật lúc: " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss  dd/MM/yyyy")));
                } catch (InterruptedException | ExecutionException ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblLastUpdated.setText("Lỗi tải dữ liệu: " + cause.getMessage());
                    cause.printStackTrace();
                }
            }
        }.execute();
    }

    // ── Container for all fetched data ─────────────────────────────────────
    private static class ReportData {
        List<Payment>    payments;
        List<Student>    students;
        List<ClassEntity> classes;
        List<Invoice>    invoices;
        List<Enrollment> enrolls;
        List<Attendance> attends;
        List<Result>     results;
        List<UserAccount> accounts;
        List<Teacher>    teachers;
        List<Course>     courses;
    }

    // ── Dashboard ──────────────────────────────────────────────────────────
    private void applyDashboard(ReportData d) {
        // KPI: Total revenue (Completed payments)
        BigDecimal totalRev = d.payments.stream()
            .filter(p -> p.getStatus() == Payment.PayStatus.Completed)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblRevenue.setText(String.format("%,.0f", totalRev));

        // KPI: Students
        lblStudents.setText(String.valueOf(d.students.size()));

        // KPI: Active classes (Open + Ongoing)
        long activeCount = d.classes.stream()
            .filter(c -> c.getStatus() == ClassEntity.ClassStatus.Open
                      || c.getStatus() == ClassEntity.ClassStatus.Ongoing)
            .count();
        lblActiveClasses.setText(String.valueOf(activeCount));

        // KPI: Pending invoices (Draft + Issued)
        long pendingCount = d.invoices.stream()
            .filter(i -> i.getStatus() == Invoice.InvoiceStatus.Draft
                      || i.getStatus() == Invoice.InvoiceStatus.Issued)
            .count();
        lblPendingInv.setText(String.valueOf(pendingCount));

        // Enrollment status breakdown
        int totalEnrolls = d.enrolls.size();
        Map<Enrollment.EnrollStatus, Long> eMap = d.enrolls.stream()
            .collect(Collectors.groupingBy(Enrollment::getStatus, Collectors.counting()));
        enrollStatusModel.setRowCount(0);
        eMap.entrySet().stream()
            .sorted(Map.Entry.<Enrollment.EnrollStatus, Long>comparingByValue().reversed())
            .forEach(e -> {
                double pct = totalEnrolls > 0 ? e.getValue() * 100.0 / totalEnrolls : 0;
                enrollStatusModel.addRow(new Object[]{
                    fmtEnroll(e.getKey()), e.getValue(), String.format("%.1f%%", pct)
                });
            });

        // Account role breakdown
        Map<UserAccount.UserRole, Long> roleMap = d.accounts.stream()
            .collect(Collectors.groupingBy(UserAccount::getRole, Collectors.counting()));
        accountRoleModel.setRowCount(0);
        roleMap.entrySet().stream()
            .sorted(Map.Entry.<UserAccount.UserRole, Long>comparingByValue().reversed())
            .forEach(e -> accountRoleModel.addRow(new Object[]{
                fmtUserRole(e.getKey()), e.getValue()
            }));
    }

    // ── Finance ────────────────────────────────────────────────────────────
    private void applyFinance(ReportData d) {
        List<Payment> completed = d.payments.stream()
            .filter(p -> p.getStatus() == Payment.PayStatus.Completed)
            .toList();

        // Revenue by payment method
        Map<Payment.Method, List<Payment>> byMethod = completed.stream()
            .collect(Collectors.groupingBy(Payment::getPaymentMethod));
        payMethodModel.setRowCount(0);
        byMethod.entrySet().stream()
            .sorted((a, b) -> {
                BigDecimal sa = a.getValue().stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal sb = b.getValue().stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                return sb.compareTo(sa);
            })
            .forEach(e -> {
                BigDecimal sum = e.getValue().stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                payMethodModel.addRow(new Object[]{
                    fmtMethod(e.getKey()), e.getValue().size(), String.format("%,.0f", sum)
                });
            });
        // Totals row
        if (!completed.isEmpty()) {
            BigDecimal total = completed.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            payMethodModel.addRow(new Object[]{"─── TỔNG ───", completed.size(), String.format("%,.0f", total)});
        }

        // Invoice status breakdown
        Map<Invoice.InvoiceStatus, List<Invoice>> invMap = d.invoices.stream()
            .collect(Collectors.groupingBy(Invoice::getStatus));
        invoiceStatModel.setRowCount(0);
        // Ensure natural order: Draft, Issued, Paid, Cancelled
        for (Invoice.InvoiceStatus status : Invoice.InvoiceStatus.values()) {
            List<Invoice> list = invMap.getOrDefault(status, Collections.emptyList());
            BigDecimal sum = list.stream().map(Invoice::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            invoiceStatModel.addRow(new Object[]{
                fmtInvoiceStatus(status), list.size(), String.format("%,.0f", sum)
            });
        }
    }

    // ── Academic ───────────────────────────────────────────────────────────
    private void applyAcademic(ReportData d) {
        // Enrollment by course level
        Map<String, Long> levelMap = d.enrolls.stream()
            .filter(e -> e.getClassEntity() != null
                      && e.getClassEntity().getCourse() != null
                      && e.getClassEntity().getCourse().getLevel() != null)
            .collect(Collectors.groupingBy(
                e -> e.getClassEntity().getCourse().getLevel().toString(),
                Collectors.counting()));
        enrollLevelModel.setRowCount(0);
        levelMap.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .forEach(e -> enrollLevelModel.addRow(new Object[]{fmtLevel(e.getKey()), e.getValue()}));

        // Attendance stats
        int totalA = d.attends.size();
        Map<Attendance.AttendStatus, Long> attMap = d.attends.stream()
            .collect(Collectors.groupingBy(Attendance::getStatus, Collectors.counting()));
        attendanceModel.setRowCount(0);
        for (Attendance.AttendStatus s : Attendance.AttendStatus.values()) {
            long cnt = attMap.getOrDefault(s, 0L);
            double pct = totalA > 0 ? cnt * 100.0 / totalA : 0;
            attendanceModel.addRow(new Object[]{fmtAttend(s), cnt, String.format("%.1f%%", pct)});
        }

        // Enrollment result (Pass / Fail / NA)
        int totalE = d.enrolls.size();
        Map<Enrollment.ResultType, Long> rfMap = d.enrolls.stream()
            .collect(Collectors.groupingBy(Enrollment::getResult, Collectors.counting()));
        resultModel.setRowCount(0);
        for (Enrollment.ResultType t : Enrollment.ResultType.values()) {
            long cnt = rfMap.getOrDefault(t, 0L);
            double pct = totalE > 0 ? cnt * 100.0 / totalE : 0;
            resultModel.addRow(new Object[]{fmtResultType(t), cnt, String.format("%.1f%%", pct)});
        }

        // Grade distribution from Result
        Map<String, Long> gradeMap = d.results.stream()
            .filter(r -> r.getGrade() != null && !r.getGrade().isBlank())
            .collect(Collectors.groupingBy(Result::getGrade, Collectors.counting()));
        gradeModel.setRowCount(0);
        gradeMap.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .forEach(e -> gradeModel.addRow(new Object[]{e.getKey(), e.getValue()}));
        if (gradeMap.isEmpty()) {
            gradeModel.addRow(new Object[]{"(chưa có dữ liệu)", "—"});
        }
    }

    // ── Operations ─────────────────────────────────────────────────────────
    private void applyOperations(ReportData d) {
        // Top teachers by class count
        Map<Teacher, Long> tMap = d.classes.stream()
            .filter(c -> c.getTeacher() != null)
            .collect(Collectors.groupingBy(ClassEntity::getTeacher, Collectors.counting()));
        teacherModel.setRowCount(0);
        tMap.entrySet().stream()
            .sorted(Map.Entry.<Teacher, Long>comparingByValue().reversed())
            .limit(10)
            .forEach(e -> {
                Teacher t = e.getKey();
                teacherModel.addRow(new Object[]{
                    t.getFullName(),
                    t.getSpecialty() != null ? t.getSpecialty() : "—",
                    e.getValue()
                });
            });
        if (tMap.isEmpty()) {
            teacherModel.addRow(new Object[]{"(chưa có dữ liệu)", "—", "—"});
        }

        // Class status breakdown
        classStatusModel.setRowCount(0);
        Map<ClassEntity.ClassStatus, Long> csMap = d.classes.stream()
            .collect(Collectors.groupingBy(ClassEntity::getStatus, Collectors.counting()));
        for (ClassEntity.ClassStatus s : ClassEntity.ClassStatus.values()) {
            long cnt = csMap.getOrDefault(s, 0L);
            classStatusModel.addRow(new Object[]{fmtClassStatus(s), cnt});
        }

        // Course listing sorted by fee desc
        courseModel.setRowCount(0);
        d.courses.stream()
            .sorted(Comparator.comparing(Course::getFee).reversed())
            .forEach(c -> courseModel.addRow(new Object[]{
                c.getCourseName(),
                fmtLevel(c.getLevel() != null ? c.getLevel().toString() : "—"),
                String.format("%,.0f", c.getFee())
            }));
        if (d.courses.isEmpty()) {
            courseModel.addRow(new Object[]{"(chưa có dữ liệu)", "—", "—"});
        }
    }

    // ══════════════════════════════ TRANSLATION HELPERS ══════════════════════

    private static String fmtEnroll(Enrollment.EnrollStatus s) {
        return switch (s) {
            case Enrolled  -> "Đang học";
            case Completed -> "Hoàn thành";
            case Dropped   -> "Bỏ học";
        };
    }

    private static String fmtUserRole(UserAccount.UserRole r) {
        return switch (r) {
            case Admin   -> "Quản trị viên";
            case Teacher -> "Giáo viên";
            case Student -> "Học viên";
            case Staff   -> "Nhân viên";
        };
    }

    private static String fmtMethod(Payment.Method m) {
        return switch (m) {
            case Cash    -> "Tiền mặt";
            case Bank    -> "Chuyển khoản";
            case Momo    -> "MoMo";
            case ZaloPay -> "ZaloPay";
            case Card    -> "Thẻ ngân hàng";
            case Other   -> "Khác";
        };
    }

    private static String fmtInvoiceStatus(Invoice.InvoiceStatus s) {
        return switch (s) {
            case Draft     -> "Nháp";
            case Issued    -> "Đã phát hành";
            case Paid      -> "Đã thanh toán";
            case Cancelled -> "Đã hủy";
        };
    }

    private static String fmtLevel(String s) {
        return switch (s) {
            case "Beginner"     -> "Cơ bản (Beginner)";
            case "Intermediate" -> "Trung cấp (Intermediate)";
            case "Advanced"     -> "Nâng cao (Advanced)";
            default             -> s;
        };
    }

    private static String fmtAttend(Attendance.AttendStatus s) {
        return switch (s) {
            case Present -> "Có mặt";
            case Absent  -> "Vắng mặt";
            case Late    -> "Đi trễ";
        };
    }

    private static String fmtResultType(Enrollment.ResultType t) {
        return switch (t) {
            case Pass -> "Đạt";
            case Fail -> "Không đạt";
            case NA   -> "Chưa đánh giá";
        };
    }

    private static String fmtClassStatus(ClassEntity.ClassStatus s) {
        return switch (s) {
            case Planned   -> "Lên kế hoạch";
            case Open      -> "Đang mở đăng ký";
            case Ongoing   -> "Đang học";
            case Completed -> "Đã kết thúc";
            case Cancelled -> "Đã hủy";
        };
    }

    // ══════════════════════════════ ROUNDED BORDER ═══════════════════════════

    private static class RoundedBorder extends AbstractBorder {
        private final Color  color;
        private final int    thickness;
        private final int    arc;

        RoundedBorder(Color color, int thickness, int arc) {
            this.color     = color;
            this.thickness = thickness;
            this.arc       = arc;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Double(x + 0.5, y + 0.5, w - 1, h - 1, arc, arc));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int pad = arc / 4;
            return new Insets(pad, pad, pad, pad);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            int pad = arc / 4;
            insets.set(pad, pad, pad, pad);
            return insets;
        }
    }
}
