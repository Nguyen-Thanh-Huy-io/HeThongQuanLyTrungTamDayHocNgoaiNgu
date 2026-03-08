package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Student;
import vn.ute.repo.StudentRepository;
import vn.ute.service.StudentService;
import java.util.Optional;

public class StudentServiceImpl extends AbstractService<Student, Long> implements StudentService {
    private final StudentRepository studentRepo;

    public StudentServiceImpl(StudentRepository studentRepo) {
        super(studentRepo);
        this.studentRepo = studentRepo;
    }

    @Override
    public Long createStudent(Student s) throws Exception {
        // executeInTransaction sẽ nhận Lambda và ném lỗi ngược ra ngoài createStudent
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            if (studentRepo.findByEmail(em, s.getEmail()).isPresent()) {
                // Lỗi này chỉ ném được nếu JpaWork.doWork có "throws Exception"
                throw new Exception("Email đã tồn tại!");
            }
            return studentRepo.insert(em, s);
        });
    }

    @Override
    public void updateStudent(Student s) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            studentRepo.update(em, s);
            return null;
        });
    }

    @Override
    public void deleteStudent(Long id) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            // findById trả về Optional, ta dùng orElseThrow để ném lỗi nếu không tìm thấy
            Student s = studentRepo.findById(em, id)
                    .orElseThrow(() -> new Exception("Không tìm thấy sinh viên ID: " + id));
            
            studentRepo.delete(em, s);
            return null;
        });
    }

    @Override
    public Optional<Student> findByEmail(String email) throws Exception {
        return TransactionManager.execute((EntityManager em) -> {
            return studentRepo.findByEmail(em, email);
        });
    }
}