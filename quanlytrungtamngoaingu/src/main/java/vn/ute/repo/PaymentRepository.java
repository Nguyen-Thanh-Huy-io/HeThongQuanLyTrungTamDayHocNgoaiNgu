package vn.ute.repo;

import jakarta.persistence.EntityManager;
import vn.ute.model.Payment;
import java.util.List;

public interface PaymentRepository extends Repository<Payment, Long> {
    // Tìm lịch sử thanh toán của sinh viên
    List<Payment> findByStudentId(EntityManager em,Long studentId);
    
    // Tìm các thanh toán thuộc một hóa đơn cụ thể
    List<Payment> findByInvoiceId(EntityManager em,Long invoiceId);
}