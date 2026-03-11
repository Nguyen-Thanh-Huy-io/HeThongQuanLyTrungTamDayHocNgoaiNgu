package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Student;
import vn.ute.model.UserAccount;
import vn.ute.repo.StudentRepository;
import vn.ute.repo.UserAccountRepository;
import vn.ute.repo.jpa.JpaUserAccountRepository;
import vn.ute.service.StudentService;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;
import java.util.regex.Pattern;

public class StudentServiceImpl extends AbstractService<Student, Long> implements StudentService {
    private static final String DEFAULT_STUDENT_PASSWORD = "hs123456";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private final StudentRepository studentRepo;
    private final UserAccountRepository accountRepo;

    public StudentServiceImpl(StudentRepository studentRepo) {
        super(studentRepo);
        this.studentRepo = studentRepo;
        this.accountRepo = new JpaUserAccountRepository();
    }

    @Override
    public Long createStudent(Student s) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            validateStudentPayload(s);

            if (s.getEmail() != null && studentRepo.findByEmail(em, s.getEmail()).isPresent()) {
                throw new Exception("Email đã tồn tại!");
            }
            if (s.getPhone() != null && studentRepo.findByPhone(em, s.getPhone()).isPresent()) {
                throw new Exception("Số điện thoại đã tồn tại!");
            }

            Long studentId = studentRepo.insert(em, s);

            Student savedStudent = studentRepo.findById(em, studentId)
                    .orElseThrow(() -> new Exception("Không tìm thấy học viên vừa tạo!"));

            String username = buildUniqueUsername(em, savedStudent.getEmail(), "student");

            UserAccount account = new UserAccount();
            account.setUsername(username);
            account.setPasswordHash(BCrypt.hashpw(DEFAULT_STUDENT_PASSWORD, BCrypt.gensalt()));
            account.setRole(UserAccount.UserRole.Student);
            account.setStudent(savedStudent);
            account.setActive(savedStudent.getStatus() == Student.Status.Active);
            accountRepo.insert(em, account);

            return studentId;
        });
    }

    private String buildUniqueUsername(EntityManager em, String email, String fallbackPrefix) throws Exception {
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
    public void updateStudent(Student s) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            if (s == null || s.getId() <= 0) {
                throw new Exception("Không tìm thấy thông tin học viên để cập nhật!");
            }

            Student existing = studentRepo.findById(em, s.getId())
                    .orElseThrow(() -> new Exception("Không tìm thấy học viên ID: " + s.getId()));

            validateStudentPayload(s);

            if (s.getRegistrationDate() == null) {
                s.setRegistrationDate(existing.getRegistrationDate());
            }

            if (s.getEmail() != null) {
                Optional<Student> byEmail = studentRepo.findByEmail(em, s.getEmail());
                if (byEmail.isPresent() && byEmail.get().getId() != s.getId()) {
                    throw new Exception("Email đã tồn tại!");
                }
            }

            if (s.getPhone() != null) {
                Optional<Student> byPhone = studentRepo.findByPhone(em, s.getPhone());
                if (byPhone.isPresent() && byPhone.get().getId() != s.getId()) {
                    throw new Exception("Số điện thoại đã tồn tại!");
                }
            }

            studentRepo.update(em, s);

            Optional<UserAccount> accountOpt = accountRepo.findByStudentId(em, s.getId());
            if (accountOpt.isPresent()) {
                UserAccount account = accountOpt.get();
                account.setActive(s.getStatus() == Student.Status.Active);
                accountRepo.update(em, account);
            }
            return null;
        });
    }

    @Override
    public void deleteStudent(Long id) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            Student s = studentRepo.findById(em, id)
                    .orElseThrow(() -> new Exception("Không tìm thấy sinh viên ID: " + id));

            accountRepo.findByStudentId(em, id).ifPresent(account -> accountRepo.delete(em, account));
            studentRepo.delete(em, s);
            return null;
        });
    }

    @Override
    public Optional<Student> findByEmail(String email) throws Exception {
        return TransactionManager.execute((EntityManager em) -> {
            return studentRepo.findByEmail(em, normalize(email));
        });
    }

    @Override
    public Optional<Student> findByPhone(String phone) throws Exception {
        return TransactionManager.execute((EntityManager em) -> studentRepo.findByPhone(em, normalize(phone)));
    }

    private void validateStudentPayload(Student s) throws Exception {
        if (s == null) {
            throw new Exception("Dữ liệu học viên không hợp lệ!");
        }

        s.setFullName(normalize(s.getFullName()));
        s.setEmail(normalize(s.getEmail()));
        s.setPhone(normalize(s.getPhone()));
        s.setAddress(normalize(s.getAddress()));

        if (s.getFullName() == null) {
            throw new Exception("Họ tên học viên không được để trống!");
        }
        if (s.getEmail() != null && !EMAIL_PATTERN.matcher(s.getEmail()).matches()) {
            throw new Exception("Email học viên không hợp lệ!");
        }
        if (s.getDateOfBirth() != null && s.getDateOfBirth().isAfter(java.time.LocalDate.now())) {
            throw new Exception("Ngày sinh không hợp lệ!");
        }
        if (s.getRegistrationDate() != null && s.getRegistrationDate().isAfter(java.time.LocalDate.now())) {
            throw new Exception("Ngày đăng ký không hợp lệ!");
        }
        if (s.getStatus() == null) {
            s.setStatus(Student.Status.Active);
        }
        if (s.getRegistrationDate() == null) {
            s.setRegistrationDate(java.time.LocalDate.now());
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}