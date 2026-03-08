package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.ute.model.UserAccount;
import vn.ute.repo.UserAccountRepository;
import java.util.Optional;

public class JpaUserAccountRepository extends AbstractJpaRepository<UserAccount, Long> implements UserAccountRepository {

    public JpaUserAccountRepository() {
        super(UserAccount.class);
    }
    
    public JpaUserAccountRepository(EntityManager em) {
        super(UserAccount.class);
    }

    @Override
    public Optional<UserAccount> findByUsername(EntityManager em,String username) {
        return em.createQuery("SELECT u FROM UserAccount u WHERE u.username = :username", UserAccount.class)
                .setParameter("username", username)
                .getResultStream().findFirst();
    }

    @Override
    public Optional<UserAccount> findByStudentId(EntityManager em,Long studentId) {
        return em.createQuery("SELECT u FROM UserAccount u WHERE u.student.id = :studentId", UserAccount.class)
                .setParameter("studentId", studentId)
                .getResultStream().findFirst();
    }

    @Override
    public Optional<UserAccount> findByTeacherId(EntityManager em,Long teacherId) {
        return em.createQuery("SELECT u FROM UserAccount u WHERE u.teacher.id = :teacherId", UserAccount.class)
                .setParameter("teacherId", teacherId)
                .getResultStream().findFirst();
    }

    @Override
    public boolean isAccountActive(EntityManager em,String username) {
        return em.createQuery("SELECT u.isActive FROM UserAccount u WHERE u.username = :username", Boolean.class)
                .setParameter("username", username)
                .getSingleResult();
    }
}