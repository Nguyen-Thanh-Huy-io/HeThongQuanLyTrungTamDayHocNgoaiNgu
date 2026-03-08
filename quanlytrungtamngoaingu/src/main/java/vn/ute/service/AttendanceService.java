package vn.ute.service;

import vn.ute.model.Attendance;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceService extends Service<Attendance, Long> {
    // Lưu hoặc cập nhật danh sách điểm danh cho cả lớp
    void saveAttendanceList(List<Attendance> attendanceList) throws Exception;
    
    // Lấy danh sách điểm danh của một lớp trong một ngày cụ thể
    List<Attendance> getByClassAndDate(Long classId, LocalDate date) throws Exception;
    
    // Xem lịch sử điểm danh của một sinh viên
    List<Attendance> getHistoryByStudent(Long studentId) throws Exception;
}