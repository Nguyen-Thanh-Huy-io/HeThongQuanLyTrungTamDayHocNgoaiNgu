package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.ute.model.Invoice;
import vn.ute.repo.InvoiceRepository;
import java.util.List;

public class JpaInvoiceRepository extends AbstractJpaRepository<Invoice, Long> implements InvoiceRepository {

    public JpaInvoiceRepository() {
        super(Invoice.class);
    }

    public JpaInvoiceRepository(EntityManager em) {
        super(Invoice.class);
    }

    @Override
    public List<Invoice> findByStudentId(EntityManager em,Long studentId) {
        return em.createQuery("SELECT i FROM Invoice i WHERE i.student.id = :studentId", Invoice.class)
                .setParameter("studentId", studentId)
                .getResultList();
    }

    @Override
    public List<Invoice> findByStatus(EntityManager em,String status) {
        return em.createQuery("SELECT i FROM Invoice i WHERE i.status = :status", Invoice.class)
                .setParameter("status", status)
                .getResultList();
    }
}