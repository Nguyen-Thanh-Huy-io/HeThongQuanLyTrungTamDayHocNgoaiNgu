package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Enrollment;
import vn.ute.model.Result;
import vn.ute.repo.EnrollmentRepository;
import vn.ute.repo.ResultRepository;
import vn.ute.service.ResultService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ResultServiceImpl extends AbstractService<Result, Long> implements ResultService {
    private final ResultRepository resultRepo;
    private final EnrollmentRepository enrollmentRepo;

    public ResultServiceImpl(ResultRepository resultRepo, EnrollmentRepository enrollmentRepo) {
        super(resultRepo);
        this.resultRepo = resultRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    @Override
    public void saveClassResults(List<Result> results) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            for (Result r : results) {
                validateResult(r);

                if (r.getId() == 0) {
                    resultRepo.insert(em, r);
                } else {
                    resultRepo.update(em, r);
                }

                syncEnrollmentResult(em, r);
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

    private void validateResult(Result result) throws Exception {
        if (result == null || result.getStudent() == null || result.getClassEntity() == null) {
            throw new Exception("Dữ liệu kết quả không hợp lệ!");
        }

        if (result.getScore() != null) {
            if (result.getScore().compareTo(BigDecimal.ZERO) < 0 || result.getScore().compareTo(BigDecimal.TEN) > 0) {
                throw new Exception("Điểm phải nằm trong khoảng từ 0 đến 10.");
            }
        }

        if (result.getGrade() != null) {
            result.setGrade(result.getGrade().trim());
            if (result.getGrade().isEmpty()) {
                result.setGrade(null);
            }
        }

        if (result.getComment() != null) {
            result.setComment(result.getComment().trim());
            if (result.getComment().isEmpty()) {
                result.setComment(null);
            }
        }
    }

    private void syncEnrollmentResult(EntityManager em, Result result) {
        enrollmentRepo.findByStudentAndClassId(em, result.getStudent().getId(), result.getClassEntity().getId())
                .ifPresent(enrollment -> {
                    if (enrollment.getStatus() != Enrollment.EnrollStatus.Dropped) {
                        enrollment.setStatus(Enrollment.EnrollStatus.Completed);
                        enrollment.setResult(determineResultType(result));
                        enrollmentRepo.update(em, enrollment);
                    }
                });
    }

    private Enrollment.ResultType determineResultType(Result result) {
        if (result.getGrade() != null) {
            String normalizedGrade = result.getGrade().trim().toLowerCase();
            if (normalizedGrade.equals("fail") || normalizedGrade.equals("f") || normalizedGrade.contains("khong dat")) {
                return Enrollment.ResultType.Fail;
            }
            if (normalizedGrade.equals("pass") || normalizedGrade.equals("p") || normalizedGrade.contains("dat")) {
                return Enrollment.ResultType.Pass;
            }
        }

        if (result.getScore() != null) {
            return result.getScore().compareTo(BigDecimal.valueOf(5)) >= 0
                    ? Enrollment.ResultType.Pass
                    : Enrollment.ResultType.Fail;
        }

        return Enrollment.ResultType.NA;
    }
}