package vn.ute.ui;

public class AttendanceTableModel extends MappedTableModel<vn.ute.model.Attendance> {
    public AttendanceTableModel() {
        super(
            new String[]{"Học viên", "Lớp học", "Ngày học", "Trạng thái", "Ghi chú"},
            new String[]{"student.fullName", "classEntity.className", "attendDate", "status", "note"},
            null, null, null, null, null
        );
    }
}
