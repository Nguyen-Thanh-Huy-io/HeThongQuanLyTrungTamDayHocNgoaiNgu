package vn.ute.ui;

import vn.ute.service.ServiceManager;
import vn.ute.ui.reports.StreamReportsDialog;

import javax.swing.*;
import java.awt.*;
import vn.ute.model.*;

public class MainFrame extends JFrame {

    private final ServiceManager sm ;

    public MainFrame(ServiceManager serviceManager) {

        super("Hệ thống Quản lý Trung tâm Ngoại ngữ - Toàn diện (Generic UI)");
        this.sm = serviceManager;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        buildUI();

        setSize(1100, 700);
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        JTabbedPane mainTabs = new JTabbedPane();

        // --- NHÓM 1: ĐÀO TẠO ---
        JTabbedPane academicTabs = new JTabbedPane(JTabbedPane.LEFT);
        
        // 1. Student
        // Trong MainFrame.java - Nhóm Đào tạo
        academicTabs.addTab("Học viên", new GenericPanel<Student>(
            // Tiêu đề cột hiển thị trên bảng
            new String[]{"ID", "Họ tên", "Ngày sinh", "Giới tính", "SĐT", "Email", "Ngày ĐK", "Trạng thái"},
            // Tên biến (Field) TRONG class Student (Phải khớp 100%)
            new String[]{"id", "fullName", "dateOfBirth", "gender", "phone", "email", "registrationDate", "status"}, 
            () -> {
                try {
                    return sm.getStudentService().findAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    return java.util.Collections.emptyList();
                }
            }
        ));

        // 2. Course
        academicTabs.addTab("Khóa học", new GenericPanel<Course>(
            // Tiêu đề cột (Hiển thị cho người dùng)
            new String[]{"ID", "Tên khóa học", "Trình độ", "Thời lượng", "Đơn vị", "Học phí", "Trạng thái"},
            // Tên biến (Fields) trong Entity Course (Phải khớp 100%)
            new String[]{"id", "courseName", "level", "duration", "durationUnit", "fee", "status"}, 
            () -> {
                try {
                    return sm.getCourseService().findAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    return java.util.Collections.emptyList();
                }
            }
        ));

        // 3. Class
        // Trong MainFrame.java - Nhóm Đào tạo
        academicTabs.addTab("Lớp học", new GenericPanel<ClassEntity>(
            // Tiêu đề cột
            new String[]{"ID", "Tên lớp", "Khóa học", "Giảng viên", "Ngày bắt đầu", "Sĩ số tối đa", "Phòng", "Trạng thái"},
            // Tên biến (Fields) trong ClassEntity
            new String[]{"id", "className", "course", "teacher", "startDate", "maxStudent", "room", "status"}, 
            () -> {
                try {
                    return sm.getClassService().findAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    return java.util.Collections.emptyList();
                }
            }
        ));

        // 4. Result
        academicTabs.addTab("Kết quả", new GenericPanel<Result>(
            // Tiêu đề cột
            new String[]{"Mã KQ", "Học viên", "Lớp học", "Điểm số", "Xếp loại", "Nhận xét"},
            // Tên biến trong Entity Result (phải khớp 100% với file Result.java của bạn)
            new String[]{"id", "student", "classEntity", "score", "grade", "comment"}, 
            () -> {
                try {
                    // Service sẽ gọi JPA để lấy 4 dòng dữ liệu bạn vừa INSERT
                    return sm.getResultService().findAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    return java.util.Collections.emptyList();
                }
            }
        ));

        // 5. Attendance
        // Trong MainFrame.java - Nhóm Quản lý Đào tạo
        academicTabs.addTab("Điểm danh", new GenericPanel<Attendance>(
            // 1. Tiêu đề cột (Hiển thị trên giao diện người dùng)
            new String[]{"Mã điểm danh", "Học viên", "Lớp học", "Ngày học", "Trạng thái", "Ghi chú"},
            
            // 2. Tên biến (Fields) trong class Attendance (Phải khớp 100% với Entity)
            new String[]{"id", "student", "classEntity", "attendDate", "status", "note"}, 
            
            // 3. Hàm gọi Service để lấy dữ liệu từ Database
            () -> {
                try {
                    return sm.getAttendanceService().findAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    return java.util.Collections.emptyList();
                }
            }
        ));

        // --- NHÓM 2: NHÂN SỰ ---
        JTabbedPane hrTabs = new JTabbedPane(JTabbedPane.LEFT);
        
        // 6. Teacher
        hrTabs.addTab("Giáo viên", new GenericPanel<Teacher>(
            new String[]{"Họ tên", "Chuyên môn", "Số điện thoại", "Email", "Ngày vào làm", "Trạng thái"},
            new String[]{"fullName", "specialty", "phone", "email", "hireDate", "status"}, 
            () -> {
                try {
                    return sm.getTeacherService().findAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    return java.util.Collections.emptyList();
                }
            }
        ));

        // 7. Staff
        // Trong MainFrame.java - Nhóm Nhân sự & Cơ sở
        hrTabs.addTab("Nhân viên", new GenericPanel<Staff>(
            // 1. Tiêu đề cột (Bỏ "ID", chỉ hiện thông tin nghiệp vụ)
            new String[]{"Họ tên", "Chức vụ", "Số điện thoại", "Email", "Trạng thái"},
            
            // 2. Tên biến (Fields) - Bỏ "id", bắt đầu từ fullName
            new String[]{"fullName", "role", "phone", "email", "status"}, 
            
            // 3. Lấy dữ liệu từ Service
            () -> {
                try {
                    return sm.getStaffService().findAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    return java.util.Collections.emptyList();
                }
            }
        ));

        // 8. Room
        hrTabs.addTab("Phòng học", new GenericPanel<Room>(
            new String[]{"ID", "Tên phòng", "Sức chứa", "Vị trí", "Trạng thái"},
            new String[]{"id", "roomName", "capacity", "location", "status"}, 
            () -> {
                try {
                    // Đảm bảo getRoomService().findAll() trả về List<Room>
                    return sm.getRoomService().findAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    return java.util.Collections.emptyList();
                }
            }
        ));

        // --- NHÓM 3: TÀI CHÍNH ---
        JTabbedPane financeTabs = new JTabbedPane(JTabbedPane.LEFT);
        
        // 9. Invoice
        // Trong MainFrame.java - Nhóm Tài chính & Vận hành
        financeTabs.addTab("Hóa đơn", new GenericPanel<Invoice>(
            // 1. Tiêu đề cột (Bỏ "ID")
            new String[]{"Học viên", "Tổng tiền", "Ngày xuất", "Trạng thái", "Ghi chú"},
            
            // 2. Tên biến (Fields) - Bỏ "id"
            new String[]{"student", "totalAmount", "issueDate", "status", "note"}, 
            
            // 3. Lấy dữ liệu từ Service
            () -> {
                try {
                    return sm.getInvoiceService().findAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    return java.util.Collections.emptyList();
                }
            }
        ));

        // 10. Payment
        // Trong MainFrame.java - Nhóm Tài chính & Vận hành
        // Trong MainFrame.java - Nhóm Tài chính & Hệ thống
        financeTabs.addTab("Thanh toán", new GenericPanel<Payment>(
            // 1. Tiêu đề cột (Bỏ ID, thêm Mã tham chiếu/Số lệnh)
            new String[]{"Học viên", "Hóa đơn", "Số tiền", "Phương thức", "Ngày thanh toán", "Mã giao dịch", "Trạng thái"},
            
            // 2. Tên biến (Fields) - Bỏ "id", khớp hoàn toàn với Entity Payment
            new String[]{"student", "invoice", "amount", "paymentMethod", "paymentDate", "referenceCode", "status"}, 
            
            // 3. Gọi Service lấy dữ liệu mẫu bạn vừa INSERT
            () -> {
                try {
                    return sm.getPaymentService().findAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    return java.util.Collections.emptyList();
                }
            }
        ));
        // --- NHÓM 4: VẬN HÀNH ---
        JTabbedPane opTabs = new JTabbedPane(JTabbedPane.LEFT);

        // 11. Enrollment
        // Trong MainFrame.java - Nhóm Tài chính & Vận hành (hoặc Quản lý Đào tạo tùy bạn sắp xếp)
        opTabs.addTab("Đăng ký lớp", new GenericPanel<Enrollment>(
            // 1. Tiêu đề cột (Đã bỏ "ID")
            new String[]{"Học viên", "Lớp học", "Ngày đăng ký", "Trạng thái", "Kết quả"},
            
            // 2. Tên biến (Fields) - Bỏ "id", khớp với class Enrollment
            new String[]{"student", "classEntity", "enrollmentDate", "status", "result"}, 
            
            // 3. Gọi Service lấy dữ liệu
            () -> {
                try {
                    return sm.getEnrollmentService().findAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    return java.util.Collections.emptyList();
                }
            }
        ));

        // 12. Schedule
        // Trong MainFrame.java - Nhóm Quản lý Đào tạo (hoặc Vận hành)
        academicTabs.addTab("Lịch học", new GenericPanel<Schedule>(
            // 1. Tiêu đề cột hiển thị (Thân thiện với người dùng)
            new String[]{"Lớp học", "Ngày học", "Giờ bắt đầu", "Giờ kết thúc", "Phòng học"},
            
            // 2. Tên biến (Fields) - Khớp 100% với Entity Schedule
            // "classEntity" và "room" là các Object, sẽ tự gọi toString() để hiện tên
            new String[]{"classEntity", "studyDate", "startTime", "endTime", "room"}, 
            
            // 3. Gọi Service lấy dữ liệu
            () -> {
                try {
                    return sm.getScheduleService().findAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    return java.util.Collections.emptyList();
                }
            }
        ));

        // 13. Account
        // Trong MainFrame.java - Nhóm Hệ thống
        opTabs.addTab("Tài khoản", new GenericPanel<UserAccount>(
            // 1. Tiêu đề cột (Dễ hiểu, chuyên nghiệp)
            new String[]{"Tên đăng nhập", "Quyền hạn", "Giáo viên", "Học viên", "Nhân viên", "Hoạt động"},
            
            // 2. Tên biến (Fields) - Lưu ý: KHÔNG đưa "passwordHash" vào đây
            new String[]{"username", "role", "teacher", "student", "staff", "isActive"}, 
            
            // 3. Gọi Service lấy dữ liệu mẫu bạn vừa INSERT
            () -> {
                try {
                    return sm.getUserAccountService().findAll();
                } catch (Exception e) {
                    e.printStackTrace();
                    return java.util.Collections.emptyList();
                }
            }
        ));

        mainTabs.addTab("Hệ thống", opTabs);

        // Thanh Menu
        setJMenuBar(createMenuBar());
        getContentPane().add(mainTabs, BorderLayout.CENTER);
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu mFile = new JMenu("Hệ thống");
        JMenuItem miExit = new JMenuItem("Thoát");
        miExit.addActionListener(e -> System.exit(0));
        mFile.add(miExit);

        JMenu mReports = new JMenu("Báo cáo");
        JMenuItem miStream = new JMenuItem("Phân tích Stream API");
        miStream.addActionListener(e -> onOpenStreamReports());
        mReports.add(miStream);

        bar.add(mFile);
        bar.add(mReports);
        return bar;
    }

    private void onOpenStreamReports() {
        new StreamReportsDialog(this).setVisible(true);
    }
}