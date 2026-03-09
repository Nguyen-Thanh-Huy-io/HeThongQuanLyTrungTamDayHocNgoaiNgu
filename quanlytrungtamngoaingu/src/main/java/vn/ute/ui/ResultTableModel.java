package vn.ute.ui;

public class ResultTableModel extends MappedTableModel<vn.ute.model.Result> {
    public ResultTableModel() {
        super(
            new String[]{"Học viên", "Lớp học", "Điểm số", "Xếp loại", "Nhận xét"},
            new String[]{"student.fullName", "classEntity.className", "score", "grade", "comment"},
            null, null, null, null, null
        );
    }
}
