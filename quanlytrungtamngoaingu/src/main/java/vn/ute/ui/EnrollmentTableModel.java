package vn.ute.ui;

public class EnrollmentTableModel extends MappedTableModel<vn.ute.model.Enrollment> {
    public EnrollmentTableModel() {
        super(
            new String[]{"Học viên", "Lớp học", "Ngày đăng ký", "Trạng thái", "Kết quả"},
            new String[]{"student.fullName", "classEntity.className", "enrollmentDate", "status", "result"},
            null, null, null, null, null
        );
    }
}
