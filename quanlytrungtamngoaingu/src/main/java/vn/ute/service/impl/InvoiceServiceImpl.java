package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Invoice;
import vn.ute.repo.InvoiceRepository;
import vn.ute.service.InvoiceService;
import java.util.List;

public class InvoiceServiceImpl extends AbstractService<Invoice, Long> implements InvoiceService {
    private final InvoiceRepository invoiceRepo;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepo) {
        super(invoiceRepo);
        this.invoiceRepo = invoiceRepo;
    }

    @Override
    public Long createInvoice(Invoice invoice) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            // Có thể thêm logic kiểm tra xem sinh viên đã có hóa đơn chưa thanh toán cho lớp này chưa
            return invoiceRepo.insert(em, invoice);
        });
    }

    @Override
    public void updateStatus(Long invoiceId, String status) throws Exception {
    TransactionManager.executeInTransaction((EntityManager em) -> {
        Invoice inv = invoiceRepo.findById(em, invoiceId)
                .orElseThrow(() -> new Exception("Không tìm thấy hóa đơn ID: " + invoiceId));
        
        try {
            // Chuyển đổi String sang Enum trước khi set
            inv.setStatus(Invoice.InvoiceStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new Exception("Trạng thái hóa đơn không hợp lệ: " + status);
        }

        invoiceRepo.update(em, inv);
        return null;
    });
}

    @Override
    public List<Invoice> getInvoicesByStudent(Long studentId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> {
            return invoiceRepo.findByStudentId(em, studentId);
        });
    }

    @Override
    public List<Invoice> getInvoicesByStatus(String status) throws Exception {
        return TransactionManager.execute((EntityManager em) -> {
            return invoiceRepo.findByStatus(em, status);
        });
    }
}