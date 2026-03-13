package vn.ute.ui;

public class ScheduleTableModel extends MappedTableModel<vn.ute.model.Schedule> {
    public ScheduleTableModel() {
        super(
            new String[]{"Lớp học", "Ngày học", "Giờ bắt đầu", "Giờ kết thúc", "Phòng học"},
            new String[]{"classEntity.className", "studyDate", "startTime", "endTime", "room.roomName"},
            null, null, null, null, null
        );
    }
}
