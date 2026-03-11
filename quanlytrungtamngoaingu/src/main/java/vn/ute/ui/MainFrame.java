package vn.ute.ui;

import vn.ute.service.ServiceManager;
import vn.ute.ui.reports.StreamReportsDialog;
import vn.ute.model.*;
import vn.ute.ui.SessionManager;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        mainTabs.addTab("Đào tạo", eduTab);

        // Nhóm 2: Vận hành
        JTabbedPane opsTab = buildOperationsTab();
        mainTabs.addTab("Vận hành", opsTab);

        // Nhóm 3: Tài chính
        JTabbedPane finTab = buildFinanceTab();
        mainTabs.addTab("Tài chính", finTab);

        // Nhóm 4: Hệ thống
        JTabbedPane sysTab = buildSystemTab();
        mainTabs.addTab("Hệ thống", sysTab);

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
            GenericPanel<Course> coursePanel = new GenericPanel<>(Course.class, new CourseTableModel(),
                () -> {
                    try { return sm.getCourseService().findAll(); }
                    catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
                });
            tab.addTab("Khóa học", coursePanel);
            panelRegistry.put("Course", coursePanel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            GenericPanel<ClassEntity> classPanel = new GenericPanel<>(ClassEntity.class, new ClassTableModel(),
                () -> {
                    try {
                        if (sessionManager.isTeacher()) {
                            return loadClassesForCurrentTeacher();
                        }
                        return sm.getClassService().findAll();
                    }
                    catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
                });
            tab.addTab("Lớp học", classPanel);
            panelRegistry.put("ClassEntity", classPanel);
        } catch (Exception e) { 
            e.printStackTrace(); 
        }

        try {
            GenericPanel<Schedule> schedulePanel = new GenericPanel<>(Schedule.class, new ScheduleTableModel(),
                () -> {
                    try {
                        if (sessionManager.isStudent()) {
                            return loadSchedulesForCurrentStudent();
                        }
                        if (sessionManager.isTeacher()) {
                            return loadSchedulesForCurrentTeacher();
                        }
                        return sm.getScheduleService().findAll();
                    }
                    catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
                });
            tab.addTab("Lịch học", schedulePanel);
            panelRegistry.put("Schedule", schedulePanel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            EnrollmentPanel enrollmentPanel = new EnrollmentPanel(sm, sessionManager,
                () -> {
                    try {
                        if (sessionManager.isStudent()) {
                            Long studentId = sessionManager.getCurrentStudentId();
                            if (studentId == null) return Collections.emptyList();
                            return sm.getEnrollmentService().getByStudent(studentId);
                        }
                        return sm.getEnrollmentService().findAll();
                    }
                    catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
                });
            tab.addTab("Đăng ký lớp", enrollmentPanel);
            panelRegistry.put("Enrollment", enrollmentPanel);
        } catch (Exception e) { 
            e.printStackTrace(); 
        }

        try {
            GenericPanel<Attendance> attendancePanel = new GenericPanel<>(Attendance.class, new AttendanceTableModel(),
                () -> {
                    try {
                        if (sessionManager.isStudent()) {
                            Long studentId = sessionManager.getCurrentStudentId();
                            if (studentId == null) return Collections.emptyList();
                            return sm.getAttendanceService().getHistoryByStudent(studentId);
                        }
                        if (sessionManager.isTeacher()) {
                            return loadAttendancesForCurrentTeacher();
                        }
                        return sm.getAttendanceService().findAll();
                    }
                    catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
                });
            tab.addTab("Điểm danh", attendancePanel);
            panelRegistry.put("Attendance", attendancePanel);
        } catch (Exception e) { 
            e.printStackTrace(); 
        }

        try {
            ResultPanel resultPanel = new ResultPanel(sm, sessionManager,
                () -> {
                    try {
                        if (sessionManager.isStudent()) {
                            Long studentId = sessionManager.getCurrentStudentId();
                            if (studentId == null) return Collections.emptyList();
                            return sm.getResultService().getByStudent(studentId);
                        }
                        if (sessionManager.isTeacher()) {
                            return loadResultsForCurrentTeacher();
                        }
                        return sm.getResultService().findAll();
                    }
                    catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
                });
            tab.addTab("Kết quả", resultPanel);
            panelRegistry.put("Result", resultPanel);
        } catch (Exception e) { 
            e.printStackTrace(); 
        }

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
                try {
                    if (sessionManager.isTeacher()) {
                        Long teacherId = sessionManager.getCurrentTeacherId();
                        if (teacherId == null) return Collections.emptyList();
                        return sm.getTeacherService().findById(teacherId)
                                .map(List::of)
                                .orElseGet(Collections::emptyList);
                    }
                    return sm.getTeacherService().findAll();
                }
                catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
            });
        tab.addTab(sessionManager.isTeacher() ? "Hồ sơ giáo viên" : "Giáo viên", teacherPanel);
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
                try {
                    if (sessionManager.isStudent()) {
                        Long studentId = sessionManager.getCurrentStudentId();
                        if (studentId == null) return Collections.emptyList();
                        return sm.getStudentService().findById(studentId)
                                .map(List::of)
                                .orElseGet(Collections::emptyList);
                    }
                    return sm.getStudentService().findAll();
                }
                catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
            });
        tab.addTab(sessionManager.isStudent() ? "Hồ sơ cá nhân" : "Học viên", studentPanel);
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
                try {
                    if (sessionManager.isStudent()) {
                        Long studentId = sessionManager.getCurrentStudentId();
                        if (studentId == null) return Collections.emptyList();
                        return sm.getInvoiceService().getInvoicesByStudent(studentId);
                    }
                    return sm.getInvoiceService().findAll();
                }
                catch (Exception e) { e.printStackTrace(); return Collections.emptyList(); }
            });
        tab.addTab("Hóa đơn", invoicePanel);
        panelRegistry.put("Invoice", invoicePanel);

        // Payments
        GenericPanel<Payment> paymentPanel = new GenericPanel<>(Payment.class, new PaymentTableModel(),
            () -> {
                try {
                    if (sessionManager.isStudent()) {
                        Long studentId = sessionManager.getCurrentStudentId();
                        if (studentId == null) return Collections.emptyList();
                        return sm.getPaymentService().getPaymentsByStudent(studentId);
                    }
                    return sm.getPaymentService().findAll();
                }
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

        switch (role.toLowerCase()) {
            case "admin" -> handleAdminPermissions();
            case "staff" -> handleStaffPermissions();
            case "teacher" -> handleTeacherPermissions();
            case "student" -> handleStudentPermissions();
            default -> handleStudentPermissions();
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
        removeTabByTitle("Hệ thống");

        // Tài chính: chỉ xem
        BasePanel<?> invoicePanel = panelRegistry.get("Invoice");
        if (invoicePanel != null) {
            invoicePanel.hideButtons("add", "edit", "delete");
        }
        BasePanel<?> paymentPanel = panelRegistry.get("Payment");
        if (paymentPanel != null) {
            paymentPanel.hideButtons("add", "edit", "delete");
        }
    }

    /**
     * Teacher: Chỉ xem Classes, Schedules, Attendances, Results. Chỉ sửa điểm/điểm danh.
     */
    private void handleTeacherPermissions() {
        removeTabByTitle("Tài chính");
        removeTabByTitle("Hệ thống");

        // Ẩn tab con trong Đào tạo
        JTabbedPane educationTab = (JTabbedPane) mainTabs.getComponentAt(0);
        if (educationTab != null) {
            removeTabInContainer(educationTab, "Khóa học");
            removeTabInContainer(educationTab, "Đăng ký lớp");
        }

        JTabbedPane operationsTab = findMainTabPane("Vận hành");
        if (operationsTab != null) {
            removeTabInContainer(operationsTab, "Phòng học");
            removeTabInContainer(operationsTab, "Nhân viên");
            removeTabInContainer(operationsTab, "Học viên");
            removeTabInContainer(operationsTab, "Hồ sơ cá nhân");
        }

        // Chỉ sửa điểm & điểm danh
        BasePanel<?> attendancePanel = panelRegistry.get("Attendance");
        if (attendancePanel != null) {
            attendancePanel.hideButtons("add", "delete");
        }
        BasePanel<?> schedulePanel = panelRegistry.get("Schedule");
        if (schedulePanel != null) {
            schedulePanel.hideButtons("add", "edit", "delete");
        }
        BasePanel<?> resultPanel = panelRegistry.get("Result");
        if (resultPanel != null) {
            resultPanel.hideButtons("delete");
        }

        // Classes: chỉ xem
        BasePanel<?> classPanel = panelRegistry.get("ClassEntity");
        if (classPanel != null) {
            classPanel.hideButtons("add", "edit", "delete");
        }

        BasePanel<?> teacherPanel = panelRegistry.get("Teacher");
        if (teacherPanel != null) {
            teacherPanel.hideButtons("add", "delete");
        }
    }

    /**
     * Student: Chỉ xem hồ sơ, đăng ký lớp, lịch học, điểm danh, kết quả, hóa đơn và thanh toán của chính mình.
     */
    private void handleStudentPermissions() {
        removeTabByTitle("Hệ thống");

        // Ẩn tab con trong Đào tạo
        JTabbedPane educationTab = (JTabbedPane) mainTabs.getComponentAt(0);
        if (educationTab != null) {
            removeTabInContainer(educationTab, "Khóa học");
            removeTabInContainer(educationTab, "Lớp học");
        }

        JTabbedPane operationsTab = findMainTabPane("Vận hành");
        if (operationsTab != null) {
            removeTabInContainer(operationsTab, "Phòng học");
            removeTabInContainer(operationsTab, "Giáo viên");
            removeTabInContainer(operationsTab, "Nhân viên");
        }

        // Ẩn tất cả nút bấm ở các tab có quyền
        for (String key : new String[]{"Student", "Schedule", "Attendance", "Result", "Invoice", "Payment"}) {
            BasePanel<?> panel = panelRegistry.get(key);
            if (panel != null) {
                panel.hideButtons("add", "edit", "delete");
            }
        }

        BasePanel<?> enrollmentPanel = panelRegistry.get("Enrollment");
        if (enrollmentPanel != null) {
            enrollmentPanel.hideButtons("edit");
            enrollmentPanel.setButtonVisible("add", true);
            enrollmentPanel.setButtonVisible("delete", true);
        }
    }

    private void removeTabByTitle(String title) {
        for (int i = 0; i < mainTabs.getTabCount(); i++) {
            if (mainTabs.getTitleAt(i).equals(title)) {
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

    private JTabbedPane findMainTabPane(String title) {
        for (int i = 0; i < mainTabs.getTabCount(); i++) {
            if (mainTabs.getTitleAt(i).equals(title) && mainTabs.getComponentAt(i) instanceof JTabbedPane tabbedPane) {
                return tabbedPane;
            }
        }
        return null;
    }

    private List<Schedule> loadSchedulesForCurrentStudent() throws Exception {
        Long studentId = sessionManager.getCurrentStudentId();
        if (studentId == null) {
            return Collections.emptyList();
        }

        List<Enrollment> enrollments = sm.getEnrollmentService().getByStudent(studentId);
        Map<Long, Schedule> schedulesById = new LinkedHashMap<>();

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getClassEntity() == null || enrollment.getClassEntity().getId() <= 0) {
                continue;
            }

            List<Schedule> schedules = sm.getScheduleService().getByClass(enrollment.getClassEntity().getId());
            for (Schedule schedule : schedules) {
                schedulesById.putIfAbsent(schedule.getId(), schedule);
            }
        }

        List<Schedule> result = new ArrayList<>(schedulesById.values());
        result.sort(Comparator
                .comparing(Schedule::getStudyDate)
                .thenComparing(Schedule::getStartTime));
        return result;
    }

    private List<ClassEntity> loadClassesForCurrentTeacher() throws Exception {
        Long teacherId = sessionManager.getCurrentTeacherId();
        if (teacherId == null) {
            return Collections.emptyList();
        }
        return sm.getClassService().findByTeacher(teacherId);
    }

    private List<Schedule> loadSchedulesForCurrentTeacher() throws Exception {
        List<ClassEntity> classes = loadClassesForCurrentTeacher();
        Map<Long, Schedule> schedulesById = new LinkedHashMap<>();

        for (ClassEntity classEntity : classes) {
            List<Schedule> schedules = sm.getScheduleService().getByClass(classEntity.getId());
            for (Schedule schedule : schedules) {
                schedulesById.putIfAbsent(schedule.getId(), schedule);
            }
        }

        List<Schedule> result = new ArrayList<>(schedulesById.values());
        result.sort(Comparator
                .comparing(Schedule::getStudyDate)
                .thenComparing(Schedule::getStartTime));
        return result;
    }

    private List<Attendance> loadAttendancesForCurrentTeacher() throws Exception {
        Set<Long> classIds = loadTeacherClassIds();
        if (classIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Attendance> attendances = sm.getAttendanceService().findAll();
        return attendances.stream()
                .filter(attendance -> attendance.getClassEntity() != null)
                .filter(attendance -> classIds.contains(attendance.getClassEntity().getId()))
                .sorted(Comparator
                        .comparing(Attendance::getAttendDate)
                        .thenComparing(attendance -> attendance.getClassEntity().getClassName(), Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private List<Result> loadResultsForCurrentTeacher() throws Exception {
        Set<Long> classIds = loadTeacherClassIds();
        if (classIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Result> results = sm.getResultService().findAll();
        return results.stream()
                .filter(result -> result.getClassEntity() != null)
                .filter(result -> classIds.contains(result.getClassEntity().getId()))
                .sorted(Comparator.comparing(result -> result.getClassEntity().getClassName(), Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private Set<Long> loadTeacherClassIds() throws Exception {
        List<ClassEntity> classes = loadClassesForCurrentTeacher();
        Set<Long> classIds = new HashSet<>();
        for (ClassEntity classEntity : classes) {
            classIds.add(classEntity.getId());
        }
        return classIds;
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
        if (!sessionManager.isStudent()) {
            bar.add(mReports);
        }
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
        LoginForm loginForm = new LoginForm(sm);
        loginForm.setVisible(true);
        dispose();
    }
}
