package vn.ute.db;

import jakarta.persistence.EntityManager;

@FunctionalInterface
public interface JpaWork<T> {
    T doWork(EntityManager em) throws Exception;
}
