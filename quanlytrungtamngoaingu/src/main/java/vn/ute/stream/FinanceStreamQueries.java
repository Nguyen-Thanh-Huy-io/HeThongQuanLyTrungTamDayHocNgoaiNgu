package vn.ute.stream;

import vn.ute.model.Invoice;
import vn.ute.model.Payment;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

public class FinanceStreamQueries {

    /** 4. Tính tổng doanh thu thực tế (Tổng các Payment đã hoàn tất) */
    public static BigDecimal totalActualRevenue(List<Payment> payments) {
        return payments.stream()
                .filter(p -> "Completed".equals(p.getStatus()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** 5. Tìm các Invoice còn nợ (Status là 'Issued' và chưa được trả đủ) */
    public static List<Invoice> pendingInvoices(List<Invoice> invoices) {
        return invoices.stream()
                .filter(inv -> !"Paid".equals(inv.getStatus()))
                .sorted(Comparator.comparing(Invoice::getIssueDate))
                .collect(Collectors.toList());
    }

    public static Map<String, BigDecimal> revenueByMethod(List<Payment> payments) {
    return payments.stream()
            .filter(p -> "Completed".equals(p.getStatus()))
            .collect(Collectors.groupingBy(
                p -> p.getPaymentMethod().toString(),
                Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
            ));
        }
}