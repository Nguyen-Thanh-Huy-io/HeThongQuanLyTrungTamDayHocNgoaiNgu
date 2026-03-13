package vn.ute.ui;

public class StudentScheduleTableModel extends MappedTableModel<vn.ute.model.Schedule> {
    public StudentScheduleTableModel() {
        super(
            new String[]{"Ngày học", "Giờ bắt đầu", "Giờ kết thúc", "Khóa học", "Lớp học", "Giảng viên", "Phòng học"},
            new String[]{"studyDate", "startTime", "endTime", "classEntity.course.courseName", "classEntity.className", "classEntity.teacher.fullName", "room.roomName"},
            null, null, null, null, null, null, null
        );
    }
}