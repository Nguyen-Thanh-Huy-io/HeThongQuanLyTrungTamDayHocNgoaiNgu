package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.ute.model.Schedule;
import vn.ute.repo.ScheduleRepository;
import java.time.LocalDate;
import java.util.List;

public class JpaScheduleRepository extends AbstractJpaRepository<Schedule, Long> implements ScheduleRepository {

    public JpaScheduleRepository() {
        super(Schedule.class);
    }
    
    public JpaScheduleRepository(EntityManager em) {
        super(Schedule.class);
    }

    @Override
    public List<Schedule> findByClassId(EntityManager em,Long classId) {
        return em.createQuery("SELECT s FROM Schedule s WHERE s.classEntity.id = :classId", Schedule.class)
                .setParameter("classId", classId)
                .getResultList();
    }

    @Override
    public List<Schedule> findByStudyDate(EntityManager em,LocalDate studyDate) {
        return em.createQuery("SELECT s FROM Schedule s WHERE s.studyDate = :studyDate", Schedule.class)
                .setParameter("studyDate", studyDate)
                .getResultList();
    }

    @Override
    public List<Schedule> findByRoomId(EntityManager em,Long roomId) {
        return em.createQuery("SELECT s FROM Schedule s WHERE s.room.id = :roomId", Schedule.class)
                .setParameter("roomId", roomId)
                .getResultList();
    }
}