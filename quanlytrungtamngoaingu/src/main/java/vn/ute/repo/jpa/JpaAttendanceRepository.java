package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.ute.model.Attendance;
import vn.ute.repo.AttendanceRepository;
import java.time.LocalDate;
import java.util.List;

public class JpaAttendanceRepository extends AbstractJpaRepository<Attendance, Long> implements AttendanceRepository {

    public JpaAttendanceRepository() {
        super(Attendance.class);
    }
    public JpaAttendanceRepository(EntityManager em) {
        super(Attendance.class);
    }

    @Override
    public List<Attendance> findByClassIdAndDate(EntityManager em,Long classId, LocalDate date) {
        return em.createQuery("SELECT a FROM Attendance a WHERE a.classEntity.id = :classId AND a.attendDate = :date", Attendance.class)
                .setParameter("classId", classId)
                .setParameter("date", date)
                .getResultList();
    }

    @Override
    public List<Attendance> findByStudentId(EntityManager em,Long studentId) {
        return em.createQuery("SELECT a FROM Attendance a WHERE a.student.id = :studentId", Attendance.class)
                .setParameter("studentId", studentId)
                .getResultList();
    }
}