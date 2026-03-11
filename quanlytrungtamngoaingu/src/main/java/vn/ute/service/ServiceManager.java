package vn.ute.service;

import vn.ute.repo.jpa.*;
import vn.ute.service.impl.*;

/**
 * ServiceManager quản lý tập trung các Service.
 * Các Service sẽ sử dụng TransactionManager để tự tạo và quản lý EntityManager.
 */
public class ServiceManager {
    private static ServiceManager instance;

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final CourseService courseService;
    private final ClassService classService;
    private final EnrollmentService enrollmentService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final ScheduleService scheduleService;
    private final AttendanceService attendanceService;
    private final StaffService staffService;
    private final UserAccountService userAccountService;
    private final ResultService resultService;
    private final RoomService roomService;

    private ServiceManager() {
        // Khởi tạo Repositories mà không cần truyền EntityManager cố định ở đây
        // Các Service Implementation của bạn (ví dụ StudentServiceImpl) 
        // sẽ nhận các Repo này và dùng TransactionManager.execute((em) -> repo.findAll(em))
        
        var studentRepo = new JpaStudentRepository();
        var teacherRepo = new JpaTeacherRepository();
        var courseRepo = new JpaCourseRepository();
        var classRepo = new JpaClassRepository();
        var enrollRepo = new JpaEnrollmentRepository();
        var invoiceRepo = new JpaInvoiceRepository();
        var paymentRepo = new JpaPaymentRepository();
        var scheduleRepo = new JpaScheduleRepository();
        var attendanceRepo = new JpaAttendanceRepository();
        var staffRepo = new JpaStaffRepository();
        var accountRepo = new JpaUserAccountRepository();
        var resultRepo = new JpaResultRepository();
        var roomRepo = new JpaRoomRepository();

        // Khởi tạo các Service
        this.studentService = new StudentServiceImpl(studentRepo);
        this.teacherService = new TeacherServiceImpl(teacherRepo);
        this.courseService = new CourseServiceImpl(courseRepo);
        this.classService = new ClassServiceImpl(classRepo, roomRepo);
        this.enrollmentService = new EnrollmentServiceImpl(enrollRepo, classRepo, studentRepo);
        this.invoiceService = new InvoiceServiceImpl(invoiceRepo);
        this.paymentService = new PaymentServiceImpl(paymentRepo, invoiceRepo);
        this.scheduleService = new ScheduleServiceImpl(scheduleRepo);
        this.attendanceService = new AttendanceServiceImpl(attendanceRepo);
        this.staffService = new StaffServiceImpl(staffRepo);
        this.userAccountService = new UserAccountServiceImpl(accountRepo);
        this.resultService = new ResultServiceImpl(resultRepo, enrollRepo);
        this.roomService = new RoomServiceImpl(roomRepo);
    }

    public static synchronized ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }

    // --- Getters ---
    public StudentService getStudentService() { return studentService; }
    public TeacherService getTeacherService() { return teacherService; }
    public CourseService getCourseService() { return courseService; }
    public ClassService getClassService() { return classService; }
    public EnrollmentService getEnrollmentService() { return enrollmentService; }
    public InvoiceService getInvoiceService() { return invoiceService; }
    public PaymentService getPaymentService() { return paymentService; }
    public ScheduleService getScheduleService() { return scheduleService; }
    public AttendanceService getAttendanceService() { return attendanceService; }
    public StaffService getStaffService() { return staffService; }
    public UserAccountService getUserAccountService() { return userAccountService; }
    public ResultService getResultService() { return resultService; }
    public RoomService getRoomService() { return roomService; }
}