package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.ute.repo.Repository;
import java.util.List;
import java.util.Optional;

public abstract class AbstractJpaRepository<T, ID> implements Repository<T, ID> {
    protected Class<T> entityClass;

    // Constructor này sẽ được gọi bởi JpaStudentRepository
    public AbstractJpaRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public List<T> findAll(EntityManager em) {
        return em.createQuery("FROM " + entityClass.getSimpleName(), entityClass).getResultList();
    }

    @Override
    public Optional<T> findById(EntityManager em, ID id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    @Override
    public ID insert(EntityManager em, T entity) {
        em.persist(entity);
        return (ID) em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
    }

    @Override
    public void update(EntityManager em, T entity) {
        em.merge(entity);
    }

    @Override
    public void delete(EntityManager em, T entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }
}