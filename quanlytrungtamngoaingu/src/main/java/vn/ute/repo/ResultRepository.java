package vn.ute.repo;

import jakarta.persistence.EntityManager;
import vn.ute.model.Result;
import java.util.List;
import java.util.Optional;

public interface ResultRepository extends Repository<Result, Long> {
    // Lấy toàn bộ bảng điểm của một lớp
    List<Result> findByClassId(EntityManager em,Long classId);
    
    // Lấy bảng điểm cá nhân của một sinh viên
    List<Result> findByStudentId(EntityManager em,Long studentId);
    
    // Tìm kết quả cụ thể của 1 sinh viên trong 1 lớp
    Optional<Result> findByStudentAndClass(EntityManager em,Long studentId, Long classId);
}