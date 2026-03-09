package vn.ute.ui;

public class StudentTableModel extends GenericTableModel<vn.ute.model.Student> {
    private static final String[] COLUMNS = {"Họ tên", "Ngày sinh", "Giới tính", "SĐT", "Email", "Ngày ĐK", "Trạng thái"};
    private static final String[] FIELDS = {"fullName", "dateOfBirth", "gender", "phone", "email", "registrationDate", "status"};

    public StudentTableModel() {
        super(COLUMNS, FIELDS);
    }
}
