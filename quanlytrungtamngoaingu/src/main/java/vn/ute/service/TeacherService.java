package vn.ute.service;

import vn.ute.model.Teacher;
import java.util.Optional;

public interface TeacherService extends Service<Teacher, Long> {
    Long createTeacher(Teacher t) throws Exception;
    void updateTeacher(Teacher t) throws Exception;
    void deleteTeacher(Long id) throws Exception;
    Optional<Teacher> findByEmail(String email) throws Exception;
}