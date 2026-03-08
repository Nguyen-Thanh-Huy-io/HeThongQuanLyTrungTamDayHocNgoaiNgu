package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.repo.Repository;
import vn.ute.service.Service;
import java.util.List;
import java.util.Optional;

public abstract class AbstractService<T, ID> implements Service<T, ID> {
    protected final Repository<T, ID> repo;

    public AbstractService(Repository<T, ID> repo) {
        this.repo = repo;
    }

    @Override
    public List<T> findAll() throws Exception {
        // Dùng static method execute và truyền em vào repo
        return TransactionManager.execute((EntityManager em) -> repo.findAll(em));
    }

    @Override
    public Optional<T> findById(ID id) throws Exception {
        return TransactionManager.execute((EntityManager em) -> repo.findById(em, id));
    }
}