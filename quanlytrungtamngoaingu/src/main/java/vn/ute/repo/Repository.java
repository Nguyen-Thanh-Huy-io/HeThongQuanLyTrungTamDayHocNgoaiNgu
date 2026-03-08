package vn.ute.repo;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    List<T> findAll(EntityManager em);
    
    Optional<T> findById(EntityManager em, ID id);
    
    ID insert(EntityManager em, T entity);
    
    void update(EntityManager em, T entity);
    
    void delete(EntityManager em, T entity);
}