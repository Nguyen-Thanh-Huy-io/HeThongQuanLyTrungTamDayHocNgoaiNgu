package vn.ute.repo;

import jakarta.persistence.EntityManager;
import vn.ute.model.Schedule;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends Repository<Schedule, Long> {
    // Tìm lịch học của một lớp cụ thể
    List<Schedule> findByClassId(EntityManager em,Long classId);
    
    // Tìm lịch học trong một ngày cụ thể (để xem hôm nay có những lớp nào)
    List<Schedule> findByStudyDate(EntityManager em,LocalDate studyDate);
    
    // Tìm lịch sử dụng của một phòng (để tránh trùng lịch)
    List<Schedule> findByRoomId(EntityManager em,Long roomId);
}