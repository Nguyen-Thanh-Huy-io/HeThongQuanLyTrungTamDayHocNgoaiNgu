package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Teacher;
import vn.ute.repo.TeacherRepository;
import vn.ute.service.TeacherService;
import java.util.Optional;

public class TeacherServiceImpl extends AbstractService<Teacher, Long> implements TeacherService {
    private final TeacherRepository teacherRepo;

    public TeacherServiceImpl(TeacherRepository teacherRepo) {
        super(teacherRepo);
        this.teacherRepo = teacherRepo;
    }

    @Override
    public Long createTeacher(Teacher t) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            // Sử dụng repo có tham số em như bạn đã định nghĩa
            if (teacherRepo.findByEmail(em, t.getEmail()).isPresent()) {
                throw new Exception("Email giáo viên này đã tồn tại!");
            }
            return teacherRepo.insert(em, t);
        });
    }

    @Override
    public void updateTeacher(Teacher t) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            teacherRepo.update(em, t);
            return null;
        });
    }

    @Override
    public void deleteTeacher(Long id) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            Teacher t = teacherRepo.findById(em, id)
                    .orElseThrow(() -> new Exception("Không tìm thấy giáo viên có ID: " + id));
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