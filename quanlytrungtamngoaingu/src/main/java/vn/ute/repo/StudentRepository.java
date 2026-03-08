package vn.ute.repo;

import jakarta.persistence.EntityManager;
import vn.ute.model.Student;
import java.util.Optional;

public interface StudentRepository extends Repository<Student, Long> {
    // Thêm EntityManager em để thực hiện truy vấn trong transaction
    Optional<Student> findByEmail(EntityManager em, String email);
    
    Optional<Student> findByPhone(EntityManager em, String phone);
}