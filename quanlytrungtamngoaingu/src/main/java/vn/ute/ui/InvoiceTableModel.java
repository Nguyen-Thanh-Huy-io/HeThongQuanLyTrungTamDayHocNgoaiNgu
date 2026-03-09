package vn.ute.ui;

public class InvoiceTableModel extends MappedTableModel<vn.ute.model.Invoice> {
    public InvoiceTableModel() {
        super(
            new String[]{"Học viên", "Tổng tiền", "Ngày xuất", "Trạng thái", "Ghi chú"},
            new String[]{"student.fullName", "totalAmount", "issueDate", "status", "note"},
            null, null, null, null, null
        );
    }
}
