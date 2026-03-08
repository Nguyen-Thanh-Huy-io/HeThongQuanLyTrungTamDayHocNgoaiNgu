package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.ute.model.ClassEntity;
import vn.ute.repo.ClassRepository;
import java.util.List;

public class JpaClassRepository extends AbstractJpaRepository<ClassEntity, Long> implements ClassRepository {

    public JpaClassRepository() {
        super(ClassEntity.class);
    }

    public JpaClassRepository(EntityManager em) {
        super(ClassEntity.class);
    }

    @Override
    public List<ClassEntity> findByCourseId(EntityManager em,Long courseId) {
        return em.createQuery("SELECT c FROM Class c WHERE c.course.id = :courseId", ClassEntity.class)
                .setParameter("courseId", courseId)
                .getResultList();
    }

    @Override
    public List<ClassEntity> findByTeacherId(EntityManager em,Long teacherId) {
        return em.createQuery("SELECT c FROM Class c WHERE c.teacher.id = :teacherId", ClassEntity.class)
                .setParameter("teacherId", teacherId)
                .getResultList();
    }

    @Override
    public List<ClassEntity> findByRoomId(EntityManager em,Long roomId) {
        return em.createQuery("SELECT c FROM Class c WHERE c.room.id = :roomId", ClassEntity.class)
                .setParameter("roomId", roomId)
                .getResultList();
    }
}