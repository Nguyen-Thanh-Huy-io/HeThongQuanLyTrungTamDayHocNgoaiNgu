package vn.ute.service;

import vn.ute.model.Staff;
import java.util.List;
import java.util.Optional;

public interface StaffService extends Service<Staff, Long> {
    Long createStaff(Staff s) throws Exception;
    void updateStaff(Staff s) throws Exception;
    void deleteStaff(Long id) throws Exception;
    
    // Các hàm nghiệp vụ dựa trên Repository của bạn
    Optional<Staff> getByEmail(String email) throws Exception;
    Optional<Staff> getByPhone(String phone) throws Exception;
    List<Staff> getByRole(String role) throws Exception;
}