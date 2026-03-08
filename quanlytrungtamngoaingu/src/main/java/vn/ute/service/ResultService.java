package vn.ute.service;

import vn.ute.model.Result;
import java.util.List;
import java.util.Optional;

public interface ResultService extends Service<Result, Long> {
    // Lưu hoặc cập nhật điểm hàng loạt (dùng cho giáo viên nhập điểm cả lớp)
    void saveClassResults(List<Result> results) throws Exception;
    
    // Các hàm truy vấn dựa trên Repository của bạn
    List<Result> getByClass(Long classId) throws Exception;
    List<Result> getByStudent(Long studentId) throws Exception;
    Optional<Result> getSpecificResult(Long studentId, Long classId) throws Exception;
}