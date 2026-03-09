package vn.ute.ui;

public class RoomTableModel extends GenericTableModel<vn.ute.model.Room> {
    private static final String[] COLUMNS = { "Tên phòng", "Sức chứa", "Vị trí", "Trạng thái"};
    private static final String[] FIELDS = {"roomName", "capacity", "location", "status"};

    public RoomTableModel() {
        super(COLUMNS, FIELDS);
    }
}
