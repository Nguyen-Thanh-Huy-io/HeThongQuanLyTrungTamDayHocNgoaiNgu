package vn.ute.repo;

import jakarta.persistence.EntityManager;
import vn.ute.model.Enrollment;
import java.util.List;

public interface EnrollmentRepository extends Repository<Enrollment, Long> {
    // Tìm danh sách đăng ký của một sinh viên
    List<Enrollment> findByStudentId(EntityManager em,Long studentId);
    
    // Tìm danh sách sinh viên trong một lớp
    List<Enrollment> findByClassId(EntityManager em,Long classId);
    
    // Tìm theo trạng thái (Enrolled, Dropped, Completed)
    List<Enrollment> findByStatus(EntityManager em,String status);
}