package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.ute.model.Enrollment;
import vn.ute.repo.EnrollmentRepository;
import java.util.List;

public class JpaEnrollmentRepository extends AbstractJpaRepository<Enrollment, Long> implements EnrollmentRepository {

    public JpaEnrollmentRepository() {
        super(Enrollment.class);
    }
    public JpaEnrollmentRepository(EntityManager em) {
        super(Enrollment.class);
    }

    @Override
    public List<Enrollment> findByStudentId(EntityManager em,Long studentId) {
        return em.createQuery("SELECT e FROM Enrollment e WHERE e.student.id = :studentId", Enrollment.class)
                .setParameter("studentId", studentId)
                .getResultList();
    }

    @Override
    public List<Enrollment> findByClassId(EntityManager em,Long classId) {
        return em.createQuery("SELECT e FROM Enrollment e WHERE e.clazz.id = :classId", Enrollment.class)
                .setParameter("classId", classId)
                .getResultList();
    }

    @Override
    public List<Enrollment> findByStatus(EntityManager em,String status) {
        return em.createQuery("SELECT e FROM Enrollment e WHERE e.status = :status", Enrollment.class)
                .setParameter("status", status)
                .getResultList();
    }
}