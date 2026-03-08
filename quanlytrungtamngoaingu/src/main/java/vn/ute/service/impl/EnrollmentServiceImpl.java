package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Enrollment;
import vn.ute.model.ClassEntity;
import vn.ute.repo.EnrollmentRepository;
import vn.ute.repo.ClassRepository;
import vn.ute.service.EnrollmentService;
import java.util.List;

public class EnrollmentServiceImpl extends AbstractService<Enrollment, Long> implements EnrollmentService {
    private final EnrollmentRepository enrollmentRepo;
    private final ClassRepository classRepo;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepo, ClassRepository classRepo) {
        super(enrollmentRepo);
        this.enrollmentRepo = enrollmentRepo;
        this.classRepo = classRepo;
    }

    @Override
    public Long enrollStudent(Enrollment enrollment) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            // 1. Lấy thông tin lớp học để biết sĩ số tối đa (MaxStudents)
            ClassEntity clazz = classRepo.findById(em, enrollment.getClassEntity().getId())
                    .orElseThrow(() -> new Exception("Lớp học không tồn tại!"));

            // 2. Tích hợp Repository: Lấy danh sách học viên hiện tại trong lớp
            List<Enrollment> currentStudents = enrollmentRepo.findByClassId(em, clazz.getId());

            // 3. Kiểm tra logic nghiệp vụ: Nếu sĩ số hiện tại >= MaxStudents thì không cho đăng ký
            if (currentStudents.size() >= clazz.getMaxStudent()) {
                throw new Exception("Lớp " + clazz.getClassName() + " đã đủ sĩ số (" + clazz.getMaxStudent() + "). Không thể thêm!");
            }

            // 4. Thực hiện đăng ký
            return enrollmentRepo.insert(em, enrollment);
        });
    }

    @Override
    public void cancelEnrollment(Long id) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            Enrollment e = enrollmentRepo.findById(em, id)
                    .orElseThrow(() -> new Exception("Không tìm thấy bản ghi đăng ký ID: " + id));
            
            // Có thể đổi thành Soft Delete bằng cách update status thành 'Dropped'
            // Ở đây tui làm xóa vật lý theo yêu cầu cơ bản
            enrollmentRepo.delete(em, e);
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
        return TransactionManager.execute((EntityManager em) -> enrollmentRepo.findByStatus(em, status));
    }
}