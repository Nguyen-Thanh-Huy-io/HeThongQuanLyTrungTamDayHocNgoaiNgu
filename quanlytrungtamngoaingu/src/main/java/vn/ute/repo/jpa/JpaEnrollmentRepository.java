package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.ute.model.Enrollment;
import vn.ute.repo.EnrollmentRepository;
import java.util.List;
import java.util.Optional;

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
        return em.createQuery("SELECT e FROM Enrollment e WHERE e.classEntity.id = :classId", Enrollment.class)
                .setParameter("classId", classId)
                .getResultList();
    }

    @Override
    public Optional<Enrollment> findByStudentAndClassId(EntityManager em, Long studentId, Long classId) {
        return em.createQuery(
                        "SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.classEntity.id = :classId",
                        Enrollment.class
                )
                .setParameter("studentId", studentId)
                .setParameter("classId", classId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Enrollment> findByStatus(EntityManager em, Enrollment.EnrollStatus status) {
        return em.createQuery("SELECT e FROM Enrollment e WHERE e.status = :status", Enrollment.class)
                .setParameter("status", status)
                .getResultList();
    }
}