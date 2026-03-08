package vn.ute.repo;

import jakarta.persistence.EntityManager;
import vn.ute.model.Attendance;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends Repository<Attendance, Long> {
    // Lấy danh sách điểm danh của một lớp trong một ngày
    List<Attendance> findByClassIdAndDate(EntityManager em,Long classId, LocalDate date);
    
    // Lấy lịch sử vắng/trễ của một sinh viên
    List<Attendance> findByStudentId(EntityManager em,Long studentId);
}