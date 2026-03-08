package vn.ute.repo;

import jakarta.persistence.EntityManager;
import vn.ute.model.Invoice;
import java.util.List;

public interface InvoiceRepository extends Repository<Invoice, Long> {
    // Tìm hóa đơn theo sinh viên
    List<Invoice> findByStudentId(EntityManager em,Long studentId);
    
    // Tìm hóa đơn theo trạng thái (Issued, Paid, Cancelled)
    List<Invoice> findByStatus(EntityManager em,String status);
}