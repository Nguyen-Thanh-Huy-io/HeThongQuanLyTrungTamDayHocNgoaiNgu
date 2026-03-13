package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import vn.ute.model.Schedule;
import vn.ute.repo.ScheduleRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class JpaScheduleRepository extends AbstractJpaRepository<Schedule, Long> implements ScheduleRepository {

    public JpaScheduleRepository() {
        super(Schedule.class);
    }
    
    public JpaScheduleRepository(EntityManager em) {
        super(Schedule.class);
    }

    @Override
    public List<Schedule> findAll(EntityManager em) {
        return em.createQuery(
                        "SELECT s FROM Schedule s " +
                        "LEFT JOIN FETCH s.classEntity " +
                        "LEFT JOIN FETCH s.room " +
                        "ORDER BY s.studyDate, s.startTime",
                        Schedule.class
                )
                .getResultList();
    }

    @Override
    public Optional<Schedule> findById(EntityManager em, Long id) {
        try {
            return Optional.of(em.createQuery(
                            "SELECT s FROM Schedule s " +
                            "LEFT JOIN FETCH s.classEntity " +
                            "LEFT JOIN FETCH s.room " +
                            "WHERE s.id = :id",
                            Schedule.class
                    )
                    .setParameter("id", id)
                    .getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<Schedule> findByClassId(EntityManager em,Long classId) {
        return em.createQuery(
                        "SELECT s FROM Schedule s " +
                        "LEFT JOIN FETCH s.classEntity " +
                        "LEFT JOIN FETCH s.room " +
                        "WHERE s.classEntity.id = :classId " +
                        "ORDER BY s.studyDate, s.startTime",
                        Schedule.class
                )
                .setParameter("classId", classId)
                .getResultList();
    }

    @Override
    public List<Schedule> findByStudyDate(EntityManager em,LocalDate studyDate) {
        return em.createQuery(
                        "SELECT s FROM Schedule s " +
                        "LEFT JOIN FETCH s.classEntity " +
                        "LEFT JOIN FETCH s.room " +
                        "WHERE s.studyDate = :studyDate " +
                        "ORDER BY s.startTime",
                        Schedule.class
                )
                .setParameter("studyDate", studyDate)
                .getResultList();
    }

    @Override
    public List<Schedule> findByRoomId(EntityManager em,Long roomId) {
        return em.createQuery(
                        "SELECT s FROM Schedule s " +
                        "LEFT JOIN FETCH s.classEntity " +
                        "LEFT JOIN FETCH s.room " +
                        "WHERE s.room.id = :roomId " +
                        "ORDER BY s.studyDate, s.startTime",
                        Schedule.class
                )
                .setParameter("roomId", roomId)
                .getResultList();
    }
}