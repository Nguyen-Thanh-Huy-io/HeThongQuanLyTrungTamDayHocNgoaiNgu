package vn.ute.ui;

public class StudentEnrollmentTableModel extends MappedTableModel<vn.ute.model.Enrollment> {
    public StudentEnrollmentTableModel() {
        super(
            new String[]{"Giảng viên", "Lớp học", "Khóa học", "Ngày đăng ký", "Trạng thái", "Kết quả"},
            new String[]{"classEntity.teacher.fullName", "classEntity.className", "classEntity.course.courseName", "enrollmentDate", "status", "result"},
            null, null, null, null, null, null
        );
    }
}