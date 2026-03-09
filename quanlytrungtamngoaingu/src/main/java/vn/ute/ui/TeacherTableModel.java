package vn.ute.ui;

public class TeacherTableModel extends GenericTableModel<vn.ute.model.Teacher> {
    private static final String[] COLUMNS = {"Họ tên", "Chuyên môn", "Số điện thoại", "Email", "Ngày vào làm", "Trạng thái"};
    private static final String[] FIELDS = {"fullName", "specialty", "phone", "email", "hireDate", "status"};

    public TeacherTableModel() {
        super(COLUMNS, FIELDS);
    }
}
