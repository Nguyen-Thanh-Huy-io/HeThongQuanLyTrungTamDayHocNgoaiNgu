package vn.ute.ui;

public class TeacherScheduleTableModel extends MappedTableModel<vn.ute.model.Schedule> {
    public TeacherScheduleTableModel() {
        super(
            new String[]{"Ngày học", "Giờ bắt đầu", "Giờ kết thúc", "Lớp học", "Khóa học", "Phòng học", "Trạng thái lớp"},
            new String[]{"studyDate", "startTime", "endTime", "classEntity.className", "classEntity.course.courseName", "room.roomName", "classEntity.status"},
            null, null, null, null, null, null, null
        );
    }
}