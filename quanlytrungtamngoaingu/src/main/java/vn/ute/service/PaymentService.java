package vn.ute.service;

import vn.ute.model.Payment;
import java.util.List;

public interface PaymentService extends Service<Payment, Long> {
    // Nghiệp vụ xử lý thanh toán và cập nhật hóa đơn
    Long processPayment(Payment payment) throws Exception;
    
    // Các hàm truy vấn theo Repository của bạn
    List<Payment> getPaymentsByStudent(Long studentId) throws Exception;
    List<Payment> getPaymentsByInvoice(Long invoiceId) throws Exception;
}