package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Staff;
import vn.ute.model.UserAccount;
import vn.ute.repo.StaffRepository;
import vn.ute.repo.UserAccountRepository;
import vn.ute.repo.jpa.JpaUserAccountRepository;
import vn.ute.service.StaffService;
import java.util.List;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

public class StaffServiceImpl extends AbstractService<Staff, Long> implements StaffService {
    private static final String DEFAULT_STAFF_PASSWORD = "nv123456";
    private final StaffRepository staffRepo;
    private final UserAccountRepository accountRepo;

    public StaffServiceImpl(StaffRepository staffRepo) {
        super(staffRepo);
        this.staffRepo = staffRepo;
        this.accountRepo = new JpaUserAccountRepository();
    }

    @Override
    public Long createStaff(Staff s) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            if (s == null || s.getFullName() == null || s.getFullName().isBlank()) {
                throw new Exception("Thông tin nhân viên không hợp lệ!");
            }

            s.setFullName(s.getFullName().trim());
            if (s.getEmail() != null) {
                s.setEmail(s.getEmail().trim());
                if (s.getEmail().isBlank()) {
                    s.setEmail(null);
                }
            }
            if (s.getPhone() != null) {
                s.setPhone(s.getPhone().trim());
                if (s.getPhone().isBlank()) {
                    s.setPhone(null);
                }
            }

            // 1. Kiểm tra trùng Email
            if (s.getEmail() != null && staffRepo.findByEmail(em, s.getEmail()).isPresent()) {
                throw new Exception("Email nhân viên này đã tồn tại trên hệ thống!");
            }
            
            // 2. Kiểm tra trùng Số điện thoại
            if (s.getPhone() != null && staffRepo.findByPhone(em, s.getPhone()).isPresent()) {
                throw new Exception("Số điện thoại này đã được sử dụng bởi nhân viên khác!");
            }

            Long staffId = staffRepo.insert(em, s);

            Staff savedStaff = staffRepo.findById(em, staffId)
                    .orElseThrow(() -> new Exception("Không tìm thấy nhân viên vừa tạo!"));

            String username = buildUniqueUsername(em, savedStaff.getEmail(), "staff");

            UserAccount account = new UserAccount();
            account.setUsername(username);
            account.setPasswordHash(BCrypt.hashpw(DEFAULT_STAFF_PASSWORD, BCrypt.gensalt()));
            account.setRole(savedStaff.getRole() == Staff.StaffRole.Admin ? UserAccount.UserRole.Admin : UserAccount.UserRole.Staff);
            account.setStaff(savedStaff);
            account.setActive(savedStaff.getStatus() == Staff.Status.Active);
            accountRepo.insert(em, account);

            return staffId;
        });
    }

    @Override
    public void updateStaff(Staff s) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            if (s == null || s.getId() <= 0) {
                throw new Exception("Không tìm thấy thông tin nhân viên để cập nhật!");
            }

            Staff existing = staffRepo.findById(em, s.getId())
                    .orElseThrow(() -> new Exception("Không tìm thấy nhân viên ID: " + s.getId()));

            if (s.getEmail() != null) {
                s.setEmail(s.getEmail().trim());
                if (s.getEmail().isBlank()) {
                    s.setEmail(null);
                }
            }
            if (s.getPhone() != null) {
                s.setPhone(s.getPhone().trim());
                if (s.getPhone().isBlank()) {
                    s.setPhone(null);
                }
            }

            if (s.getEmail() != null) {
                Optional<Staff> duplicatedEmail = staffRepo.findByEmail(em, s.getEmail());
                if (duplicatedEmail.isPresent() && duplicatedEmail.get().getId() != s.getId()) {
                    throw new Exception("Email nhân viên này đã tồn tại trên hệ thống!");
                }
            }

            if (s.getPhone() != null) {
                Optional<Staff> duplicatedPhone = staffRepo.findByPhone(em, s.getPhone());
                if (duplicatedPhone.isPresent() && duplicatedPhone.get().getId() != s.getId()) {
                    throw new Exception("Số điện thoại này đã được sử dụng bởi nhân viên khác!");
                }
            }

            if (s.getStatus() == null) {
                s.setStatus(existing.getStatus());
            }

            staffRepo.update(em, s);

            accountRepo.findByStaffId(em, s.getId()).ifPresent(account -> {
                account.setActive(s.getStatus() == Staff.Status.Active);
                account.setRole(s.getRole() == Staff.StaffRole.Admin ? UserAccount.UserRole.Admin : UserAccount.UserRole.Staff);
                accountRepo.update(em, account);
            });
            return null;
        });
    }

    @Override
    public void deleteStaff(Long id) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            Staff s = staffRepo.findById(em, id)
                    .orElseThrow(() -> new Exception("Không tìm thấy nhân viên ID: " + id));

            accountRepo.findByStaffId(em, id).ifPresent(account -> accountRepo.delete(em, account));
            staffRepo.delete(em, s);
            return null;
        });
    }

    private String buildUniqueUsername(EntityManager em, String email, String fallbackPrefix) {
        String base = toUsernameSeed(email, fallbackPrefix);
        String candidate = base;
        int suffix = 1;

        while (accountRepo.findByUsername(em, candidate).isPresent()) {
            candidate = base + suffix;
            suffix++;
        }

        return candidate;
    }

    private String toUsernameSeed(String email, String fallbackPrefix) {
        if (email == null || email.isBlank()) {
            return fallbackPrefix;
        }

        String localPart = email.split("@", 2)[0].trim().toLowerCase();
        String sanitized = localPart.replaceAll("[^a-z0-9._-]", "");
        return sanitized.isBlank() ? fallbackPrefix : sanitized;
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