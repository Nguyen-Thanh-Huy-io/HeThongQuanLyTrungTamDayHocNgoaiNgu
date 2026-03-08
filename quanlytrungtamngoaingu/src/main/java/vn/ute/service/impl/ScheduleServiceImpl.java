package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Schedule;
import vn.ute.repo.ScheduleRepository;
import vn.ute.service.ScheduleService;
import java.time.LocalDate;
import java.util.List;

public class ScheduleServiceImpl extends AbstractService<Schedule, Long> implements ScheduleService {
    private final ScheduleRepository scheduleRepo;

    public ScheduleServiceImpl(ScheduleRepository scheduleRepo) {
        super(scheduleRepo);
        this.scheduleRepo = scheduleRepo;
    }

    @Override
    public Long createSchedule(Schedule sc) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            // LOGIC KIỂM TRA TRÙNG LỊCH (Conflict Detection)
            // Lấy tất cả lịch hiện có của phòng này
            List<Schedule> existingSchedules = scheduleRepo.findByRoomId(em, sc.getRoom().getId());
            
            for (Schedule existing : existingSchedules) {
                // Kiểm tra nếu cùng ngày học
                if (existing.getStudyDate().equals(sc.getStudyDate())) {
                    // Kiểm tra giao thoa thời gian (Overlap)
                    // (Giả sử Model Schedule có getStartTime() và getEndTime())
                    boolean isOverlap = sc.getStartTime().isBefore(existing.getEndTime()) 
                                     && existing.getStartTime().isBefore(sc.getEndTime());
                    
                    if (isOverlap) {
                        throw new Exception("Phòng " + sc.getRoom().getRoomName() + 
                            " đã có lịch từ " + existing.getStartTime() + " đến " + existing.getEndTime());
                    }
                }
            }
            
            return scheduleRepo.insert(em, sc);
        });
    }

    @Override
    public void updateSchedule(Schedule sc) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            scheduleRepo.update(em, sc);
            return null;
        });
    }

    @Override
    public void deleteSchedule(Long id) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            Schedule sc = scheduleRepo.findById(em, id)
                    .orElseThrow(() -> new Exception("Không tìm thấy lịch học ID: " + id));
            scheduleRepo.delete(em, sc);
            return null;
        });
    }

    @Override
    public List<Schedule> getByClass(Long classId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> scheduleRepo.findByClassId(em, classId));
    }

    @Override
    public List<Schedule> getByRoom(Long roomId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> scheduleRepo.findByRoomId(em, roomId));
    }

    @Override
    public List<Schedule> getByDate(LocalDate studyDate) throws Exception {
        return TransactionManager.execute((EntityManager em) -> scheduleRepo.findByStudyDate(em, studyDate));
    }
}