package vn.ute.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class TransactionManager {

    public static <T> T execute(JpaWork<T> work) {
        EntityManager em = Jpa.getEntityManagerFactory().createEntityManager();
        try {
            return work.doWork(em);
        } catch (Exception e) {
            // Chuyển đổi thành RuntimeException để không bắt buộc try-catch ở tầng trên
            throw new RuntimeException("Lỗi truy vấn dữ liệu: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public static <T> T executeInTransaction(JpaWork<T> work) {
        EntityManager em = Jpa.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.doWork(em);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RuntimeException("Lỗi nghiệp vụ (Transaction): " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}