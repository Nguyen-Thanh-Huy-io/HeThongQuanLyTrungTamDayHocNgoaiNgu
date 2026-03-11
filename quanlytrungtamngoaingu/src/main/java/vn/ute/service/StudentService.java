package vn.ute.service;

import vn.ute.model.Student;
import java.util.Optional;

public interface StudentService extends Service<Student, Long> {
    Long createStudent(Student s) throws Exception;
    void updateStudent(Student s) throws Exception;
    void deleteStudent(Long id) throws Exception;
    Optional<Student> findByEmail(String email) throws Exception;
    Optional<Student> findByPhone(String phone) throws Exception;
}