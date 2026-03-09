package vn.ute.ui;

public class ClassTableModel extends MappedTableModel<vn.ute.model.ClassEntity> {
    public ClassTableModel() {
        super(
            new String[]{"Tên lớp", "Khóa học", "Giảng viên", "Ngày bắt đầu", "Sĩ số tối đa", "Phòng", "Trạng thái"},
            new String[]{"className", "course.courseName", "teacher.fullName", "startDate", "maxStudent", "room.roomName", "status"},
            null, null, null, null, null, null, null
        );
    }
}
