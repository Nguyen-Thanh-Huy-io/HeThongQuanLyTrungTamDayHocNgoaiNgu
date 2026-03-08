package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Attendance;
import vn.ute.repo.AttendanceRepository;
import vn.ute.service.AttendanceService;
import java.time.LocalDate;
import java.util.List;

public class AttendanceServiceImpl extends AbstractService<Attendance, Long> implements AttendanceService {
    private final AttendanceRepository attendanceRepo;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepo) {
        super(attendanceRepo);
        this.attendanceRepo = attendanceRepo;
    }

    @Override
    public void saveAttendanceList(List<Attendance> attendanceList) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            for (Attendance a : attendanceList) {
                if (a.getId() == 0) {
                    // Nếu chưa có ID thì là điểm danh mới
                    attendanceRepo.insert(em, a);
                } else {
                    // Nếu đã có ID (đã điểm danh rồi) thì cập nhật lại trạng thái
                    attendanceRepo.update(em, a);
                }
            }
            return null;
        });
    }

    @Override
    public List<Attendance> getByClassAndDate(Long classId, LocalDate date) throws Exception {
        return TransactionManager.execute((EntityManager em) -> 
            attendanceRepo.findByClassIdAndDate(em, classId, date)
        );
    }

    @Override
    public List<Attendance> getHistoryByStudent(Long studentId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> 
            attendanceRepo.findByStudentId(em, studentId)
        );
    }
}