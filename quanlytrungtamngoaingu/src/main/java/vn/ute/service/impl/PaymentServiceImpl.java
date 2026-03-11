package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Payment;
import vn.ute.model.Invoice;
import vn.ute.repo.PaymentRepository;
import vn.ute.repo.InvoiceRepository;
import java.math.BigDecimal;
import vn.ute.service.PaymentService;
import java.util.List;

public class PaymentServiceImpl extends AbstractService<Payment, Long> implements PaymentService {
    private final PaymentRepository paymentRepo;
    private final InvoiceRepository invoiceRepo;

    public PaymentServiceImpl(PaymentRepository paymentRepo, InvoiceRepository invoiceRepo) {
        super(paymentRepo);
        this.paymentRepo = paymentRepo;
        this.invoiceRepo = invoiceRepo;
    }

    @Override
    public Long processPayment(Payment payment) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            if (payment == null) {
                throw new Exception("Dữ liệu thanh toán không được để trống!");
            }
            if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new Exception("Số tiền thanh toán phải lớn hơn 0!");
            }
            if (payment.getInvoice() == null || payment.getInvoice().getId() <= 0) {
                throw new Exception("Thanh toán phải gắn với hóa đơn hợp lệ!");
            }

            // 1. Lưu thông tin thanh toán vào DB
            Long paymentId = paymentRepo.insert(em, payment);

            // 2. Tìm hóa đơn liên quan để cập nhật trạng thái
            Invoice inv = invoiceRepo.findById(em, payment.getInvoice().getId())
                    .orElseThrow(() -> new Exception("Không tìm thấy hóa đơn liên quan!"));

            if (inv.getStatus() == Invoice.InvoiceStatus.Cancelled) {
                throw new Exception("Hóa đơn đã hủy, không thể ghi nhận thanh toán!");
            }

            if (inv.getTotalAmount() == null || inv.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new Exception("Tổng tiền hóa đơn không hợp lệ!");
            }

            List<Payment> payments = paymentRepo.findByInvoiceId(em, inv.getId());
            BigDecimal completedTotal = payments.stream()
                    .filter(p -> p.getStatus() == Payment.PayStatus.Completed)
                    .map(Payment::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 3. Cập nhật trạng thái hóa đơn theo tổng thanh toán thành công
            if (completedTotal.compareTo(inv.getTotalAmount()) >= 0) {
                inv.setStatus(Invoice.InvoiceStatus.Paid);
            } else {
                inv.setStatus(Invoice.InvoiceStatus.Issued);
            }
            invoiceRepo.update(em, inv);

            return paymentId;
        });
    }

    @Override
    public List<Payment> getPaymentsByStudent(Long studentId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> {
            return paymentRepo.findByStudentId(em, studentId);
        });
    }

    @Override
    public List<Payment> getPaymentsByInvoice(Long invoiceId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> {
            return paymentRepo.findByInvoiceId(em, invoiceId);
        });
    }
}