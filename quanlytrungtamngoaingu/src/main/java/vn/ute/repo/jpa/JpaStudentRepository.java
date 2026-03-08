package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.ute.model.Student;
import vn.ute.repo.StudentRepository;
import java.util.Optional;

public class JpaStudentRepository extends AbstractJpaRepository<Student, Long> implements StudentRepository {

    public JpaStudentRepository(EntityManager em) {
        super(Student.class);
    }

    public JpaStudentRepository() {
        super(Student.class);
    }

    @Override
    public Optional<Student> findByEmail(EntityManager em, String email) {
        return em.createQuery("SELECT s FROM Student s WHERE s.email = :email", Student.class)
                 .setParameter("email", email)
                 .getResultStream()
                 .findFirst();
    }

    @Override
    public Optional<Student> findByPhone(EntityManager em, String phone) {
        return em.createQuery("SELECT s FROM Student s WHERE s.phone = :phone", Student.class)
                 .setParameter("phone", phone)
                 .getResultStream()
                 .findFirst();
    }
}