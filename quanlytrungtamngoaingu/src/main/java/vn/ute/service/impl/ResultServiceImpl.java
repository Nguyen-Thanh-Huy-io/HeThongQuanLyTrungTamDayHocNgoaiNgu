package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Result;
import vn.ute.repo.ResultRepository;
import vn.ute.service.ResultService;
import java.util.List;
import java.util.Optional;

public class ResultServiceImpl extends AbstractService<Result, Long> implements ResultService {
    private final ResultRepository resultRepo;

    public ResultServiceImpl(ResultRepository resultRepo) {
        super(resultRepo);
        this.resultRepo = resultRepo;
    }

    @Override
    public void saveClassResults(List<Result> results) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            for (Result r : results) {
                // Kiểm tra ID kiểu long primitive (== 0 là chưa có trong DB)
                if (r.getId() == 0) {
                    resultRepo.insert(em, r);
                } else {
                    resultRepo.update(em, r);
                }
            }
            return null;
        });
    }

    @Override
    public List<Result> getByClass(Long classId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> resultRepo.findByClassId(em, classId));
    }

    @Override
    public List<Result> getByStudent(Long studentId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> resultRepo.findByStudentId(em, studentId));
    }

    @Override
    public Optional<Result> getSpecificResult(Long studentId, Long classId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> 
            resultRepo.findByStudentAndClass(em, studentId, classId)
        );
    }
}