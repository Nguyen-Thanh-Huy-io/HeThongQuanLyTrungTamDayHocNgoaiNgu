package vn.ute.service;

import vn.ute.model.Enrollment;
import java.util.List;

public interface EnrollmentService extends Service<Enrollment, Long> {
    // Nghiệp vụ đăng ký mới
    Long enrollStudent(Enrollment enrollment) throws Exception;
    
    // Hủy đăng ký
    void cancelEnrollment(Long id) throws Exception;
    
    // Các hàm tìm kiếm dựa trên Repository của bạn
    List<Enrollment> getByStudent(Long studentId) throws Exception;
    List<Enrollment> getByClass(Long classId) throws Exception;
    List<Enrollment> getByStatus(String status) throws Exception;
}