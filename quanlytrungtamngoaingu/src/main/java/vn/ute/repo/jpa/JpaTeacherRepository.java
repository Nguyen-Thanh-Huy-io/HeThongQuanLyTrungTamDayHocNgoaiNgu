package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import vn.ute.model.Teacher;
import vn.ute.repo.TeacherRepository;
import java.util.Optional;

public class JpaTeacherRepository extends AbstractJpaRepository<Teacher, Long> implements TeacherRepository {

    public JpaTeacherRepository() {
        super(Teacher.class);
    }
    
    public JpaTeacherRepository(EntityManager em) {
        super(Teacher.class);
    }

    @Override
    public Optional<Teacher> findByEmail(EntityManager em,String email) {
        try {
            Teacher teacher = em.createQuery("SELECT t FROM Teacher t WHERE t.email = :email", Teacher.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(teacher);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}