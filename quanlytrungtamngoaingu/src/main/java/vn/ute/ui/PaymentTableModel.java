package vn.ute.ui;

public class PaymentTableModel extends MappedTableModel<vn.ute.model.Payment> {
    public PaymentTableModel() {
        super(
            new String[]{"Học viên", "Hóa đơn", "Số tiền", "Phương thức", "Ngày thanh toán", "Mã giao dịch", "Trạng thái"},
            new String[]{"student.fullName", "invoice.id", "amount", "paymentMethod", "paymentDate", "referenceCode", "status"},
            null, null, null, null, null, null, null
        );
    }
}
