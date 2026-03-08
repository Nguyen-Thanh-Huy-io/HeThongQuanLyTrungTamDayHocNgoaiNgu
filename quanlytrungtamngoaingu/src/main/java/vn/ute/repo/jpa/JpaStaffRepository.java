package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.ute.model.Staff;
import vn.ute.repo.StaffRepository;
import java.util.List;
import java.util.Optional;

public class JpaStaffRepository extends AbstractJpaRepository<Staff, Long> implements StaffRepository {

    public JpaStaffRepository() {
        super(Staff.class);
    }
    
    public JpaStaffRepository(EntityManager em) {
        super(Staff.class);
    }

    @Override
    public Optional<Staff> findByEmail(EntityManager em,String email) {
        return em.createQuery("SELECT s FROM Staff s WHERE s.email = :email", Staff.class)
                .setParameter("email", email)
                .getResultStream().findFirst();
    }

    @Override
    public Optional<Staff> findByPhone(EntityManager em,String phone) {
        return em.createQuery("SELECT s FROM Staff s WHERE s.phone = :phone", Staff.class)
                .setParameter("phone", phone)
                .getResultStream().findFirst();
    }

    @Override
    public List<Staff> findByRole(EntityManager em,String role) {
        return em.createQuery("SELECT s FROM Staff s WHERE s.role = :role", Staff.class)
                .setParameter("role", role)
                .getResultList();
    }
}