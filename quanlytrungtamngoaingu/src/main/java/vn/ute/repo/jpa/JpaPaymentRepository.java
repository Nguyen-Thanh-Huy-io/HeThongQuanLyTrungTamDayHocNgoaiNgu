package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.ute.model.Payment;
import vn.ute.repo.PaymentRepository;
import java.util.List;

public class JpaPaymentRepository extends AbstractJpaRepository<Payment, Long> implements PaymentRepository {

    public JpaPaymentRepository() {
        super(Payment.class);
    }

    public JpaPaymentRepository(EntityManager em) {
        super(Payment.class);
    }

    @Override
    public List<Payment> findByStudentId(EntityManager em,Long studentId) {
        return em.createQuery("SELECT p FROM Payment p WHERE p.student.id = :studentId", Payment.class)
                .setParameter("studentId", studentId)
                .getResultList();
    }

    @Override
    public List<Payment> findByInvoiceId(EntityManager em,Long invoiceId) {
        return em.createQuery("SELECT p FROM Payment p WHERE p.invoice.id = :invoiceId", Payment.class)
                .setParameter("invoiceId", invoiceId)
                .getResultList();
    }
}