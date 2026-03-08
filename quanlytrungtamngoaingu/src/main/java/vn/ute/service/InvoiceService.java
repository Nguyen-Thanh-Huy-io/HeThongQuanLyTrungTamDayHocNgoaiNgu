package vn.ute.service;

import vn.ute.model.Invoice;
import java.util.List;

public interface InvoiceService extends Service<Invoice, Long> {
    Long createInvoice(Invoice invoice) throws Exception;
    void updateStatus(Long invoiceId, String status) throws Exception;
    List<Invoice> getInvoicesByStudent(Long studentId) throws Exception;
    List<Invoice> getInvoicesByStatus(String status) throws Exception;
}