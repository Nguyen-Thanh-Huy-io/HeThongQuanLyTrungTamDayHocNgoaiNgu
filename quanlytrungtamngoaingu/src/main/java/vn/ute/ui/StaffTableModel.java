package vn.ute.ui;

public class StaffTableModel extends GenericTableModel<vn.ute.model.Staff> {
    private static final String[] COLUMNS = {"Họ tên", "Chức vụ", "Số điện thoại", "Email", "Trạng thái"};
    private static final String[] FIELDS = {"fullName", "role", "phone", "email", "status"};

    public StaffTableModel() {
        super(COLUMNS, FIELDS);
    }
}
