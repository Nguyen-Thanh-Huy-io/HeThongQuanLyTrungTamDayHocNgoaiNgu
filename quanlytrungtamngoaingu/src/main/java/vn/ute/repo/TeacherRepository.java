package vn.ute.repo;

import jakarta.persistence.EntityManager;
import vn.ute.model.Teacher;
import java.util.Optional;

public interface TeacherRepository extends Repository<Teacher, Long> {
    // Tìm giáo viên theo email
    Optional<Teacher> findByEmail(EntityManager em,String email);
}