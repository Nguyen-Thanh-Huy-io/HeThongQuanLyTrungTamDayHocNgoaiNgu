package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Enrollment;
import vn.ute.model.ClassEntity;
import vn.ute.model.Student;
import vn.ute.model.Course;
import vn.ute.model.Teacher;
import vn.ute.model.Room;
import vn.ute.model.Schedule;
import vn.ute.model.Invoice;
import vn.ute.repo.EnrollmentRepository;
import vn.ute.repo.ClassRepository;
import vn.ute.repo.StudentRepository;
import vn.ute.repo.CourseRepository;
import vn.ute.repo.TeacherRepository;
import vn.ute.repo.RoomRepository;
import vn.ute.repo.ScheduleRepository;
import vn.ute.repo.InvoiceRepository;
import vn.ute.service.EnrollmentService;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

public class EnrollmentServiceImpl extends AbstractService<Enrollment, Long> implements EnrollmentService {
    private final EnrollmentRepository enrollmentRepo;
    private final ClassRepository classRepo;
    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final TeacherRepository teacherRepo;
    private final RoomRepository roomRepo;
    private final ScheduleRepository scheduleRepo;
    private final InvoiceRepository invoiceRepo;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepo, ClassRepository classRepo, StudentRepository studentRepo,
                                 CourseRepository courseRepo, TeacherRepository teacherRepo, RoomRepository roomRepo,
                                 ScheduleRepository scheduleRepo, InvoiceRepository invoiceRepo) {
        super(enrollmentRepo);
        this.enrollmentRepo = enrollmentRepo;
        this.classRepo = classRepo;
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
        this.teacherRepo = teacherRepo;
        this.roomRepo = roomRepo;
        this.scheduleRepo = scheduleRepo;
        this.invoiceRepo = invoiceRepo;
    }

    @Override
    public Long enrollStudent(Enrollment enrollment) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            if (enrollment == null || enrollment.getStudent() == null || enrollment.getClassEntity() == null) {
                throw new Exception("Thông tin đăng ký không hợp lệ!");
            }

            Long studentId = enrollment.getStudent().getId();
            Student student = studentRepo.findById(em, studentId)
                    .orElseThrow(() -> new Exception("Học viên không tồn tại!"));
            if (student.getStatus() != Student.Status.Active) {
                throw new Exception("Học viên đang không hoạt động, không thể ghi danh!");
            }

            ClassEntity clazz = classRepo.findById(em, enrollment.getClassEntity().getId())
                    .orElseThrow(() -> new Exception("Lớp học không tồn tại!"));

            if (clazz.getMaxStudent() <= 0) {
                throw new Exception("Lớp học chưa thiết lập sĩ số tối đa hợp lệ!");
            }

            Enrollment existing = enrollmentRepo
                    .findByStudentAndClassId(em, studentId, clazz.getId())
                    .orElse(null);

            if (existing != null && existing.getStatus() != Enrollment.EnrollStatus.Dropped) {
                throw new Exception("Học viên đã có đăng ký trong lớp này!");
            }

            List<Enrollment> currentStudents = enrollmentRepo.findByClassId(em, clazz.getId());
            long activeEnrollments = currentStudents.stream()
                    .filter(e -> e.getStatus() != Enrollment.EnrollStatus.Dropped)
                    .count();

            if (activeEnrollments >= clazz.getMaxStudent()) {
                throw new Exception("Lớp " + clazz.getClassName() + " đã đủ sĩ số (" + clazz.getMaxStudent() + "). Không thể thêm!");
            }

            if (existing != null) {
                existing.setStatus(Enrollment.EnrollStatus.Enrolled);
                existing.setResult(Enrollment.ResultType.NA);
                existing.setEnrollmentDate(LocalDate.now());
                enrollmentRepo.update(em, existing);
                return existing.getId();
            }

            enrollment.setStudent(student);
            enrollment.setClassEntity(clazz);
            if (enrollment.getEnrollmentDate() == null) {
                enrollment.setEnrollmentDate(LocalDate.now());
            }
            if (enrollment.getStatus() == null) {
                enrollment.setStatus(Enrollment.EnrollStatus.Enrolled);
            }
            if (enrollment.getResult() == null) {
                enrollment.setResult(Enrollment.ResultType.NA);
            }

            return enrollmentRepo.insert(em, enrollment);
        });
    }

    @Override
    public void cancelEnrollment(Long id) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            Enrollment e = enrollmentRepo.findById(em, id)
                    .orElseThrow(() -> new Exception("Không tìm thấy bản ghi đăng ký ID: " + id));

            e.setStatus(Enrollment.EnrollStatus.Dropped);
            e.setResult(Enrollment.ResultType.NA);
            enrollmentRepo.update(em, e);
            return null;
        });
    }

    @Override
    public List<Enrollment> getByStudent(Long studentId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> enrollmentRepo.findByStudentId(em, studentId));
    }

    @Override
    public List<Enrollment> getByClass(Long classId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> enrollmentRepo.findByClassId(em, classId));
    }

    @Override
    public List<Enrollment> getByStatus(String status) throws Exception {
        return TransactionManager.execute((EntityManager em) -> {
            if (status == null || status.isBlank()) {
                throw new Exception("Trạng thái ghi danh không được để trống!");
            }

            Enrollment.EnrollStatus parsedStatus = null;
            for (Enrollment.EnrollStatus value : Enrollment.EnrollStatus.values()) {
                if (value.name().equalsIgnoreCase(status.trim())) {
                    parsedStatus = value;
                    break;
                }
            }

            if (parsedStatus == null) {
                throw new Exception("Trạng thái ghi danh không hợp lệ: " + status);
            }
            return enrollmentRepo.findByStatus(em, parsedStatus);
        });
    }

    @Override
    public Long enrollStudentInCourse(Long studentId, Long courseId) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            // Kiểm tra student
            Student student = studentRepo.findById(em, studentId)
                    .orElseThrow(() -> new Exception("Học viên không tồn tại!"));
            if (student.getStatus() != Student.Status.Active) {
                throw new Exception("Học viên đang không hoạt động!");
            }

            // Kiểm tra course
            Course course = courseRepo.findById(em, courseId)
                    .orElseThrow(() -> new Exception("Khóa học không tồn tại!"));
            if (course.getStatus() != Course.Status.Active) {
                throw new Exception("Khóa học không hoạt động!");
            }

            // Kiểm tra đã đăng ký chưa
            List<Enrollment> existingEnrollments = enrollmentRepo.findByStudentId(em, studentId);
            for (Enrollment e : existingEnrollments) {
                if (e.getClassEntity().getCourse().getId() == courseId && e.getStatus() == Enrollment.EnrollStatus.Enrolled) {
                    throw new Exception("Học viên đã đăng ký khóa học này!");
                }
            }

            // Chọn teacher ngẫu nhiên
            List<Teacher> teachers = teacherRepo.findAll(em);
            if (teachers.isEmpty()) {
                throw new Exception("Không có giáo viên nào!");
            }
            Teacher teacher = teachers.get(new Random().nextInt(teachers.size()));

            // Chọn room ngẫu nhiên
            List<Room> rooms = roomRepo.findAll(em);
            if (rooms.isEmpty()) {
                throw new Exception("Không có phòng học nào!");
            }
            Room room = rooms.get(new Random().nextInt(rooms.size()));

            // Tạo ClassEntity mới
            ClassEntity classEntity = new ClassEntity();
            classEntity.setClassName(course.getCourseName() + " - Lớp " + (classRepo.findAll(em).size() + 1));
            classEntity.setCourse(course);
            classEntity.setTeacher(teacher);
            classEntity.setStartDate(LocalDate.now());
            classEntity.setEndDate(LocalDate.now().plusWeeks(course.getDuration() != null ? course.getDuration() : 4));
            classEntity.setMaxStudent(20);
            classEntity.setRoom(room);
            classEntity.setStatus(ClassEntity.ClassStatus.Open);
            classRepo.insert(em, classEntity);

            // Tạo Schedule ngẫu nhiên (ví dụ 3 buổi/tuần)
            Random rand = new Random();
            LocalDate currentDate = LocalDate.now();
            for (int i = 0; i < 12; i++) { // 12 buổi
                Schedule schedule = new Schedule();
                schedule.setClassEntity(classEntity);
                schedule.setStudyDate(currentDate.plusDays(i * 7 + rand.nextInt(7))); // Ngẫu nhiên trong tuần
                schedule.setStartTime(LocalTime.of(9 + rand.nextInt(8), 0)); // 9-16h
                schedule.setEndTime(schedule.getStartTime().plusHours(2));
                schedule.setRoom(room);
                scheduleRepo.insert(em, schedule);
            }

            // Tạo Enrollment
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setClassEntity(classEntity);
            enrollment.setEnrollmentDate(LocalDate.now());
            enrollment.setStatus(Enrollment.EnrollStatus.Enrolled);
            enrollmentRepo.insert(em, enrollment);

            // Tạo Invoice
            Invoice invoice = new Invoice();
            invoice.setStudent(student);
            invoice.setTotalAmount(course.getFee());
            invoice.setIssueDate(LocalDate.now());
            invoice.setStatus(Invoice.InvoiceStatus.Issued);
            invoice.setNote("Hóa đơn cho khóa học: " + course.getCourseName());
            invoiceRepo.insert(em, invoice);

            return enrollment.getId();
        });
    }
}