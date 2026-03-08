package vn.ute.service;

import vn.ute.model.Schedule;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleService extends Service<Schedule, Long> {
    Long createSchedule(Schedule sc) throws Exception;
    void updateSchedule(Schedule sc) throws Exception;
    void deleteSchedule(Long id) throws Exception;

    // Các hàm truy vấn từ Repository của bạn
    List<Schedule> getByClass(Long classId) throws Exception;
    List<Schedule> getByRoom(Long roomId) throws Exception;
    List<Schedule> getByDate(LocalDate studyDate) throws Exception;
}