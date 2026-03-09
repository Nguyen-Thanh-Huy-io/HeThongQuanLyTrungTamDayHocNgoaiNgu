package vn.ute.ui;

public class CourseTableModel extends GenericTableModel<vn.ute.model.Course> {
    private static final String[] COLUMNS = {"ID", "Tên khóa học", "Trình độ", "Thời lượng", "Đơn vị", "Học phí", "Trạng thái"};
    private static final String[] FIELDS = {"id", "courseName", "level", "duration", "durationUnit", "fee", "status"};

    public CourseTableModel() {
        super(COLUMNS, FIELDS);
    }
}
