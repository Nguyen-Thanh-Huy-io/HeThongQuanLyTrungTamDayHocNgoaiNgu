package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Enrollment;
import vn.ute.model.ClassEntity;
import vn.ute.model.Student;
import vn.ute.repo.EnrollmentRepository;
import vn.ute.repo.ClassRepository;
import vn.ute.repo.StudentRepository;
import vn.ute.service.EnrollmentService;
import java.util.List;
import java.time.LocalDate;

public class EnrollmentServiceImpl extends AbstractService<Enrollment, Long> implements EnrollmentService {
    private final EnrollmentRepository enrollmentRepo;
    private final ClassRepository classRepo;
    private final StudentRepository studentRepo;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepo, ClassRepository classRepo, StudentRepository studentRepo) {
        super(enrollmentRepo);
        this.enrollmentRepo = enrollmentRepo;
        this.classRepo = classRepo;
        this.studentRepo = studentRepo;
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
}