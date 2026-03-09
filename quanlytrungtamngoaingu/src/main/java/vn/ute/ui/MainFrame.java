package vn.ute.ui;

import vn.ute.service.ServiceManager;
import vn.ute.ui.reports.StreamReportsDialog;
import vn.ute.model.*;
import vn.ute.ui.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * MainFrame được tổ chức theo 4 nhóm nghiệp vụ:
 * 1. Đào tạo (Education): Courses, Classes, Enrollments, Attendances, Results
 * 2. Vận hành (Operations): Rooms, Teachers, Staffs, Students
 * 3. Tài chính (Finance): Invoices, Payments
 * 4. Hệ thống (System): UserAccounts
 *
 * Với hỗ trợ Role-based Authorization
 */
public class MainFrame extends JFrame {

    private final ServiceManager sm;
    private final SessionManager sessionManager;
    private JTabbedPane mainTabs;
    private final Map<String, BasePanel<?>> panelRegistry = new HashMap<>();

    public MainFrame(ServiceManager serviceManager) {
        super("Hệ thống Quản lý Trung tâm Ngoại ngữ");
        this.sm = serviceManager;
        this.sessionManager = SessionManager.getInstance();
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        UIUtils.initLookAndFeel();
        buildUI();
        setupPermissions(sessionManager.getCurrentRoleAsString());

        setSize(1200, 750);
        setLocationRelativeTo(null);
    }

    /**
     * Xây dựng giao diện với 4 nhóm nghiệp vụ.
     */
    private void buildUI() {
        mainTabs = new JTabbedPane();
        mainTabs.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.PLAIN, 16));
        mainTabs.setBackground(UIUtils.SECONDARY_COLOR);

        // Nhóm 1: Đào tạo
        JTabbedPane eduTab = buildEducationTab();
        mainTabs.addTab("📚 Đào tạo", eduTab);
        System.out.println(">> Education group created with " + eduTab.getTabCount() + " tabs");

        // Debug: Check if data is loaded
        try {
            var classService = sm.getClassService();
            var classes = classService.findAll();
            System.out.println(">> [DEBUG] Classes loaded: " + classes.size());
            if (!classes.isEmpty()) {
                var firstClass = classes.get(0);
                System.out.println(">> [DEBUG] First class: " + firstClass.getClassName() +
                    ", course: " + (firstClass.getCourse() != null ? firstClass.getCourse().getCourseName() : "NULL") +
                    ", teacher: " + (firstClass.getTeacher() != null ? firstClass.getTeacher().getFullName() : "NULL") +
                    ", room: " + (firstClass.getRoom() != null ? firstClass.getRoom().getRoomName() : "NULL"));
            }
        } catch (Exception e) {
            System.out.println(">> [DEBUG] Error loading classes: " + e.getMessage());
            e.printStackTrace();
        }

        // Nhóm 2: Vận hành
        JTabbedPane opsTab = buildOperationsTab();
        mainTabs.addTab("⚙️ Vận hành", opsTab);
        System.out.println(">> Operations group created with " + opsTab.getTabCount() + " tabs");

        // Nhóm 3: Tài chính
        JTabbedPane finTab = buildFinanceTab();
        mainTabs.addTab("💰 Tài chính", finTab);
        System.out.println(">> Finance group created with " + finTab.getTabCount() + " tabs");

        // Nhóm 4: Hệ thống
        JTabbedPane sysTab = buildSystemTab();
        mainTabs.addTab("⚡ Hệ thống", sysTab);
        System.out.println(">> System group created with " + sysTab.getTabCount() + " tabs");

        System.out.println(">> Main frame created with " + mainTabs.getTabCount() + " main groups");
        System.out.println(">> Main tab names:");
        for (int i = 0; i < mainTabs.getTabCount(); i++) {
            System.out.println("  Main Tab " + i + ": " + mainTabs.getTitleAt(i));
        }
        mainTabs.setSelectedIndex(0); // Force select Education tab
        System.out.println(">> Selected main tab: " + mainTabs.getSelectedIndex() + " (" + mainTabs.getTitleAt(mainTabs.getSelectedIndex()) + ")");

        // Add change listener to track main tab switches
        mainTabs.addChangeListener(e -> {
            int selected = mainTabs.getSelectedIndex();
            System.out.println(">> [MAIN TAB CHANGE] Switched to: " + selected + " (" + mainTabs.getTitleAt(selected) + ")");
        });

        setJMenuBar(createMenuBar());
        getContentPane().add(mainTabs, BorderLayout.CENTER);
        getContentPane().setBackground(UIUtils.SECONDARY_COLOR);
    }

    // =============== NHÓM 1: ĐÀO TẠO (EDUCATION) ===============
    private JTabbedPane buildEducationTab() {
        JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);
        tab.setTabPlacement(JTabbedPane.TOP);
        tab.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT); // Changed from SCROLL to WRAP

        try {
            System.out.println("\n[DEBUG] Starting to build Course tab...");
            GenericPanel<Course> coursePanel = new GenericPanel<>(Course.class, new CourseTableModel(),
                () -> {
                    try { return sm.getCourseService().findAll(); }
                    catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
                });
            System.out.println(">> CoursePanel created: " + coursePanel.getClass().getSimpleName());
            tab.addTab("Khóa học", coursePanel);
            panelRegistry.put("Course", coursePanel);
            System.out.println("✓ Added Courses tab");
        } catch (Exception e) {
            System.out.println("✗ FATAL Error adding Courses: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            System.out.println("\n[DEBUG] Starting to build Classes tab...");
            GenericPanel<ClassEntity> classPanel = new GenericPanel<>(ClassEntity.class, new ClassTableModel(),
                () -> {
                    try { return sm.getClassService().findAll(); }
                    catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
                });
            tab.addTab("Lớp học", classPanel);
            panelRegistry.put("ClassEntity", classPanel);
            System.out.println("✓ Added Classes tab");
        } catch (Exception e) { 
            System.out.println("✗ FATAL Error adding Classes: " + e.getMessage());
            e.printStackTrace(); 
        }

        try {
            System.out.println("\n[DEBUG] Starting to build Enrollments tab...");
            GenericPanel<Enrollment> enrollmentPanel = new GenericPanel<>(Enrollment.class, new EnrollmentTableModel(),
                () -> {
                    try { return sm.getEnrollmentService().findAll(); }
                    catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
                });
            tab.addTab("Đăng ký lớp", enrollmentPanel);
            panelRegistry.put("Enrollment", enrollmentPanel);
            System.out.println("✓ Added Enrollments tab");
        } catch (Exception e) { 
            System.out.println("✗ FATAL Error adding Enrollments: " + e.getMessage());
            e.printStackTrace(); 
        }

        try {
            System.out.println("\n[DEBUG] Starting to build Attendances tab...");
            GenericPanel<Attendance> attendancePanel = new GenericPanel<>(Attendance.class, new AttendanceTableModel(),
                () -> {
                    try { return sm.getAttendanceService().findAll(); }
                    catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
                });
            tab.addTab("Điểm danh", attendancePanel);
            panelRegistry.put("Attendance", attendancePanel);
            System.out.println("✓ Added Attendances tab");
        } catch (Exception e) { 
            System.out.println("✗ FATAL Error adding Attendances: " + e.getMessage());
            e.printStackTrace(); 
        }

        try {
            System.out.println("\n[DEBUG] Starting to build Results tab...");
            GenericPanel<Result> resultPanel = new GenericPanel<>(Result.class, new ResultTableModel(),
                () -> {
                    try { return sm.getResultService().findAll(); }
                    catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
                });
            System.out.println(">> ResultPanel created: " + resultPanel.getClass().getSimpleName());
            tab.addTab("Kết quả", resultPanel);
            panelRegistry.put("Result", resultPanel);
            System.out.println("✓ Added Results tab");
        } catch (Exception e) { 
            System.out.println("✗ FATAL Error adding Results: " + e.getMessage());
            e.printStackTrace(); 
        }

        System.out.println("\n>> Education tabs total: " + tab.getTabCount());
        System.out.println(">> Education tab names:");
        for (int i = 0; i < tab.getTabCount(); i++) {
            String title = tab.getTitleAt(i);
            System.out.println("  Tab " + i + ": '" + title + "' (length: " + title.length() + ")");
            // Check for encoding issues
            for (char c : title.toCharArray()) {
                if (c > 127) System.out.println("    Unicode char: " + c + " (" + (int)c + ")");
            }
        }
        tab.setSelectedIndex(0); // Force select first tab
        System.out.println(">> Selected tab: " + tab.getSelectedIndex() + " (" + tab.getTitleAt(tab.getSelectedIndex()) + ")");

        // Add change listener to track tab switches
        tab.addChangeListener(e -> {
            int selected = tab.getSelectedIndex();
            System.out.println(">> [TAB CHANGE] Education tab switched to: " + selected + " (" + tab.getTitleAt(selected) + ")");
        });

        return tab;
    }

    // =============== NHÓM 2: VẬN HÀNH (OPERATIONS) ===============
    private JTabbedPane buildOperationsTab() {
        JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);
        tab.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

        // Rooms
        GenericPanel<Room> roomPanel = new GenericPanel<>(Room.class, new RoomTableModel(),
            () -> {
                try { return sm.getRoomService().findAll(); }
                catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
            });
        tab.addTab("Phòng học", roomPanel);
        panelRegistry.put("Room", roomPanel);

        // Teachers
        GenericPanel<Teacher> teacherPanel = new GenericPanel<>(Teacher.class, new TeacherTableModel(),
            () -> {
                try { return sm.getTeacherService().findAll(); }
                catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
            });
        tab.addTab("Giáo viên", teacherPanel);
        panelRegistry.put("Teacher", teacherPanel);

        // Staffs
        GenericPanel<Staff> staffPanel = new GenericPanel<>(Staff.class, new StaffTableModel(),
            () -> {
                try { return sm.getStaffService().findAll(); }
                catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
            });
        tab.addTab("Nhân viên", staffPanel);
        panelRegistry.put("Staff", staffPanel);

        // Students
        GenericPanel<Student> studentPanel = new GenericPanel<>(Student.class, new StudentTableModel(),
            () -> {
                try { return sm.getStudentService().findAll(); }
                catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
            });
        tab.addTab("Học viên", studentPanel);
        panelRegistry.put("Student", studentPanel);

        return tab;
    }

    // =============== NHÓM 3: TÀI CHÍNH (FINANCE) ===============
    private JTabbedPane buildFinanceTab() {
        JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);
        tab.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

        // Invoices
        GenericPanel<Invoice> invoicePanel = new GenericPanel<>(Invoice.class, new InvoiceTableModel(),
            () -> {
                try { return sm.getInvoiceService().findAll(); }
                catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
            });
        tab.addTab("Hóa đơn", invoicePanel);
        panelRegistry.put("Invoice", invoicePanel);

        // Payments
        GenericPanel<Payment> paymentPanel = new GenericPanel<>(Payment.class, new PaymentTableModel(),
            () -> {
                try { return sm.getPaymentService().findAll(); }
                catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
            });
        tab.addTab("Thanh toán", paymentPanel);
        panelRegistry.put("Payment", paymentPanel);

        return tab;
    }

    // =============== NHÓM 4: HỆ THỐNG (SYSTEM) ===============
    private JTabbedPane buildSystemTab() {
        JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);
        tab.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

        // User Accounts
        GenericPanel<UserAccount> accountPanel = new GenericPanel<>(UserAccount.class,
            new GenericTableModel<>(
                new String[]{"Tên đăng nhập", "Quyền hạn", "Hoạt động"},
                new String[]{"username", "role", "isActive"}
            ),
            () -> {
                try { return sm.getUserAccountService().findAll(); }
                catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
            });
        tab.addTab("Tài khoản", accountPanel);
        panelRegistry.put("UserAccount", accountPanel);

        return tab;
    }

    // =============== PHÂN QUYỀN (PERMISSIONS) ===============

    /**
     * Thiết lập quyền hạn dựa trên Role.
     */
    private void setupPermissions(String role) {
        if (role == null) {
            role = "Student";
        }

        System.out.println("\n>> [PERMISSIONS] User role: " + role);
        System.out.println(">> [PERMISSIONS] Main tabs before setup: " + mainTabs.getTabCount());
        for (int i = 0; i < mainTabs.getTabCount(); i++) {
            System.out.println("  - " + i + ": " + mainTabs.getTitleAt(i));
        }

        switch (role.toLowerCase()) {
            case "admin" -> handleAdminPermissions();
            case "staff" -> handleStaffPermissions();
            case "teacher" -> handleTeacherPermissions();
            case "student" -> handleStudentPermissions();
            default -> handleStudentPermissions();
        }

        System.out.println(">> [PERMISSIONS] Main tabs after setup: " + mainTabs.getTabCount());
        for (int i = 0; i < mainTabs.getTabCount(); i++) {
            System.out.println("  - " + i + ": " + mainTabs.getTitleAt(i));
        }
    }

    /**
     * Admin: Toàn quyền, hiển thị tất cả 4 nhóm.
     */
    private void handleAdminPermissions() {
        // Hiển thị tất cả tab, tất cả nút bật
        for (BasePanel<?> panel : panelRegistry.values()) {
            panel.setButtonEnabled("add", true);
            panel.setButtonEnabled("edit", true);
            panel.setButtonEnabled("delete", true);
            panel.setButtonVisible("add", true);
            panel.setButtonVisible("edit", true);
            panel.setButtonVisible("delete", true);
        }
    }

    /**
     * Staff: Vận hành + Tài chính, ẩn Xóa ở Tài chính, ẩn toàn nút ở Hệ thống.
     */
    private void handleStaffPermissions() {
        System.out.println(">> [PERMISSIONS] Applying STAFF permissions");
        removeTabByTitle("⚡ Hệ thống");
        System.out.println(">> [PERMISSIONS] Removed System tab");

        // Tài chính: chỉ xem
        BasePanel<?> invoicePanel = panelRegistry.get("Invoice");
        if (invoicePanel != null) {
            invoicePanel.hideButtons("add", "edit", "delete");
        }
        BasePanel<?> paymentPanel = panelRegistry.get("Payment");
        if (paymentPanel != null) {
            paymentPanel.hideButtons("add", "edit", "delete");
        }
        System.out.println(">> [PERMISSIONS] Finance tabs set to read-only");
    }

    /**
     * Teacher: Chỉ xem Classes, Schedules, Attendances, Results. Chỉ sửa điểm/điểm danh.
     */
    private void handleTeacherPermissions() {
        removeTabByTitle("⚙️ Vận hành");
        removeTabByTitle("💰 Tài chính");
        removeTabByTitle("⚡ Hệ thống");

        // Ẩn tab con trong Đào tạo
        JTabbedPane educationTab = (JTabbedPane) mainTabs.getComponentAt(0);
        if (educationTab != null) {
            removeTabInContainer(educationTab, "Khóa học");
            removeTabInContainer(educationTab, "Đăng ký lớp");
        }

        // Chỉ sửa điểm & điểm danh
        BasePanel<?> attendancePanel = panelRegistry.get("Attendance");
        if (attendancePanel != null) {
            attendancePanel.hideButtons("add", "delete");
        }
        BasePanel<?> resultPanel = panelRegistry.get("Result");
        if (resultPanel != null) {
            resultPanel.hideButtons("add", "delete");
        }

        // Classes: chỉ xem
        BasePanel<?> classPanel = panelRegistry.get("ClassEntity");
        if (classPanel != null) {
            classPanel.hideButtons("add", "edit", "delete");
        }
    }

    /**
     * Student: Chỉ xem Kết quả, Hóa đơn, Thanh toán. Ẩn tất cả nút.
     */
    private void handleStudentPermissions() {
        removeTabByTitle("⚙️ Vận hành");
        removeTabByTitle("⚡ Hệ thống");

        // Ẩn tab con trong Đào tạo
        JTabbedPane educationTab = (JTabbedPane) mainTabs.getComponentAt(0);
        if (educationTab != null) {
            removeTabInContainer(educationTab, "Khóa học");
            removeTabInContainer(educationTab, "Lớp học");
            removeTabInContainer(educationTab, "Đăng ký lớp");
            removeTabInContainer(educationTab, "Điểm danh");
        }

        // Ẩn tất cả nút bấm ở các tab có quyền
        for (String key : new String[]{"Result", "Invoice", "Payment"}) {
            BasePanel<?> panel = panelRegistry.get(key);
            if (panel != null) {
                panel.hideButtons("add", "edit", "delete");
            }
        }
    }

    private void removeTabByTitle(String title) {
        for (int i = 0; i < mainTabs.getTabCount(); i++) {
            if (mainTabs.getTitleAt(i).equals(title)) {
                System.out.println(">> [PERMISSIONS] Removing tab: '" + title + "' at index " + i);
                mainTabs.removeTabAt(i);
                break;
            }
        }
    }

    private void removeTabInContainer(JTabbedPane container, String title) {
        for (int i = 0; i < container.getTabCount(); i++) {
            if (container.getTitleAt(i).equals(title)) {
                container.removeTabAt(i);
                break;
            }
        }
    }

    // =============== MENU ===============

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu mFile = new JMenu("Hệ thống");
        JMenuItem miLogout = new JMenuItem("Đăng xuất");
        miLogout.addActionListener(e -> onLogout());
        JMenuItem miExit = new JMenuItem("Thoát");
        miExit.addActionListener(e -> System.exit(0));
        mFile.add(miLogout);
        mFile.addSeparator();
        mFile.add(miExit);

        JMenu mReports = new JMenu("Báo cáo");
        JMenuItem miStream = new JMenuItem("Phân tích Stream API");
        miStream.addActionListener(e -> new StreamReportsDialog(this).setVisible(true));
        mReports.add(miStream);

        JMenu mHelp = new JMenu("Thông tin");
        JMenuItem miAbout = new JMenuItem("Về ứng dụng");
        miAbout.addActionListener(e -> showAbout());
        mHelp.add(miAbout);

        bar.add(mFile);
        bar.add(mReports);
        bar.add(mHelp);
        return bar;
    }

    private void showAbout() {
        String userInfo = sessionManager.getCurrentUser() != null ?
            sessionManager.getCurrentUser().getUsername() + " (" + 
            sessionManager.getCurrentRoleAsString() + ")" : "Anonymous";
        
        JOptionPane.showMessageDialog(this,
            "Hệ thống Quản lý Trung tâm Ngoại ngữ\n" +
            "Phiên bản 1.0\n\n" +
            "User: " + userInfo,
            "Về ứng dụng",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void onLogout() {
        sessionManager.logout();
        JOptionPane.showMessageDialog(this, "Đã đăng xuất thành công.");
        dispose();
    }
}
