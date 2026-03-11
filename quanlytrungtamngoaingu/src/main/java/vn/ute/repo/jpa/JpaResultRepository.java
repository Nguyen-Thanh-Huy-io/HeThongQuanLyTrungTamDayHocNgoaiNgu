package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.ute.model.Result;
import vn.ute.repo.ResultRepository;
import java.util.List;
import java.util.Optional;

public class JpaResultRepository extends AbstractJpaRepository<Result, Long> implements ResultRepository {

    public JpaResultRepository() {
        super(Result.class);
    }

    public JpaResultRepository(EntityManager em) {
        super(Result.class);
    }

    @Override
    public List<Result> findByClassId(EntityManager em,Long classId) {
        return em.createQuery("SELECT r FROM Result r WHERE r.classEntity.id = :classId", Result.class)
                .setParameter("classId", classId)
                .getResultList();
    }

    @Override
    public List<Result> findByStudentId(EntityManager em,Long studentId) {
        return em.createQuery("SELECT r FROM Result r WHERE r.student.id = :studentId", Result.class)
                .setParameter("studentId", studentId)
                .getResultList();
    }

    @Override
    public Optional<Result> findByStudentAndClass(EntityManager em,Long studentId, Long classId) {
        return em.createQuery("SELECT r FROM Result r WHERE r.student.id = :studentId AND r.classEntity.id = :classId", Result.class)
                .setParameter("studentId", studentId)
                .setParameter("classId", classId)
                .getResultStream().findFirst();
    }
}