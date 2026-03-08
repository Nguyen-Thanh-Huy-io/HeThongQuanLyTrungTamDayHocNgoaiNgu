package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Staff;
import vn.ute.repo.StaffRepository;
import vn.ute.service.StaffService;
import java.util.List;
import java.util.Optional;

public class StaffServiceImpl extends AbstractService<Staff, Long> implements StaffService {
    private final StaffRepository staffRepo;

    public StaffServiceImpl(StaffRepository staffRepo) {
        super(staffRepo);
        this.staffRepo = staffRepo;
    }

    @Override
    public Long createStaff(Staff s) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            // 1. Kiểm tra trùng Email
            if (staffRepo.findByEmail(em, s.getEmail()).isPresent()) {
                throw new Exception("Email nhân viên này đã tồn tại trên hệ thống!");
            }
            
            // 2. Kiểm tra trùng Số điện thoại
            if (staffRepo.findByPhone(em, s.getPhone()).isPresent()) {
                throw new Exception("Số điện thoại này đã được sử dụng bởi nhân viên khác!");
            }

            return staffRepo.insert(em, s);
        });
    }

    @Override
    public void updateStaff(Staff s) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            staffRepo.update(em, s);
            return null;
        });
    }

    @Override
    public void deleteStaff(Long id) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            Staff s = staffRepo.findById(em, id)
                    .orElseThrow(() -> new Exception("Không tìm thấy nhân viên ID: " + id));
            staffRepo.delete(em, s);
            return null;
        });
    }

    @Override
    public Optional<Staff> getByEmail(String email) throws Exception {
        return TransactionManager.execute((EntityManager em) -> staffRepo.findByEmail(em, email));
    }

    @Override
    public Optional<Staff> getByPhone(String phone) throws Exception {
        return TransactionManager.execute((EntityManager em) -> staffRepo.findByPhone(em, phone));
    }

    @Override
    public List<Staff> getByRole(String role) throws Exception {
        return TransactionManager.execute((EntityManager em) -> staffRepo.findByRole(em, role));
    }
}