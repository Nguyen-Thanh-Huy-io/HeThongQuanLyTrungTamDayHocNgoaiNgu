package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Payment;
import vn.ute.model.Invoice;
import vn.ute.repo.PaymentRepository;
import vn.ute.repo.InvoiceRepository;
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
            // 1. Lưu thông tin thanh toán vào DB
            Long paymentId = paymentRepo.insert(em, payment);

            // 2. Tìm hóa đơn liên quan để cập nhật trạng thái
            Invoice inv = invoiceRepo.findById(em, payment.getInvoice().getId())
                    .orElseThrow(() -> new Exception("Không tìm thấy hóa đơn liên quan!"));

            // 3. Cập nhật trạng thái sang PAID (Dùng Enum để tránh lỗi String)
            inv.setStatus(Invoice.InvoiceStatus.Paid);
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