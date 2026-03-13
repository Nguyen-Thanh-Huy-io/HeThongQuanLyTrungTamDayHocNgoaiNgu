package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Teacher;
import vn.ute.model.UserAccount;
import vn.ute.repo.TeacherRepository;
import vn.ute.repo.UserAccountRepository;
import vn.ute.repo.jpa.JpaUserAccountRepository;
import vn.ute.service.TeacherService;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

public class TeacherServiceImpl extends AbstractService<Teacher, Long> implements TeacherService {
    private static final String DEFAULT_TEACHER_PASSWORD = "gv123456";
    private final TeacherRepository teacherRepo;
    private final UserAccountRepository accountRepo;

    public TeacherServiceImpl(TeacherRepository teacherRepo) {
        super(teacherRepo);
        this.teacherRepo = teacherRepo;
        this.accountRepo = new JpaUserAccountRepository();
    }

    @Override
    public Long createTeacher(Teacher t) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            // Sử dụng repo có tham số em như bạn đã định nghĩa
            if (teacherRepo.findByEmail(em, t.getEmail()).isPresent()) {
                throw new Exception("Email giáo viên này đã tồn tại!");
            }

            Long teacherId = teacherRepo.insert(em, t);

            Teacher savedTeacher = teacherRepo.findById(em, teacherId)
                    .orElseThrow(() -> new Exception("Không tìm thấy giáo viên vừa tạo!"));

            String username = buildUniqueUsername(em, savedTeacher.getEmail(), "teacher");

            UserAccount account = new UserAccount();
            account.setUsername(username);
            account.setPasswordHash(BCrypt.hashpw(DEFAULT_TEACHER_PASSWORD, BCrypt.gensalt()));
            account.setRole(UserAccount.UserRole.Teacher);
            account.setTeacher(savedTeacher);
            account.setActive(savedTeacher.getStatus() == Teacher.Status.Active);
            accountRepo.insert(em, account);

            return teacherId;
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
    public void updateTeacher(Teacher t) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            if (t == null || t.getId() <= 0) {
                throw new Exception("Không tìm thấy thông tin giáo viên để cập nhật!");
            }

            Teacher existing = teacherRepo.findById(em, t.getId())
                    .orElseThrow(() -> new Exception("Không tìm thấy giáo viên có ID: " + t.getId()));

            if (t.getEmail() != null && !t.getEmail().isBlank()) {
                Optional<Teacher> duplicatedEmail = teacherRepo.findByEmail(em, t.getEmail().trim());
                if (duplicatedEmail.isPresent() && duplicatedEmail.get().getId() != t.getId()) {
                    throw new Exception("Email giáo viên này đã tồn tại!");
                }
            }

            if (t.getHireDate() == null) {
                t.setHireDate(existing.getHireDate());
            }
            if (t.getStatus() == null) {
                t.setStatus(existing.getStatus());
            }

            teacherRepo.update(em, t);

            accountRepo.findByTeacherId(em, t.getId()).ifPresent(account -> {
                account.setActive(t.getStatus() == Teacher.Status.Active);
                accountRepo.update(em, account);
            });
            return null;
        });
    }

    @Override
    public void deleteTeacher(Long id) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            Teacher t = teacherRepo.findById(em, id)
                    .orElseThrow(() -> new Exception("Không tìm thấy giáo viên có ID: " + id));

            accountRepo.findByTeacherId(em, id).ifPresent(account -> accountRepo.delete(em, account));
            teacherRepo.delete(em, t);
            return null;
        });
    }

    @Override
    public Optional<Teacher> findByEmail(String email) throws Exception {
        return TransactionManager.execute((EntityManager em) -> {
            return teacherRepo.findByEmail(em, email);
        });
    }
}