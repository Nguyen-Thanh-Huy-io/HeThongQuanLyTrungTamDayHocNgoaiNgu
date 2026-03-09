package vn.ute.ui;

public class ScheduleTableModel extends GenericTableModel<vn.ute.model.Schedule> {
    private static final String[] COLUMNS = {"Lớp học", "Ngày học", "Giờ bắt đầu", "Giờ kết thúc", "Phòng học"};
    private static final String[] FIELDS = {"classEntity", "studyDate", "startTime", "endTime", "room"};

    public ScheduleTableModel() {
        super(COLUMNS, FIELDS);
    }
}
