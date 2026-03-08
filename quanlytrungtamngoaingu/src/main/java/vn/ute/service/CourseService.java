package vn.ute.service;

import vn.ute.model.Course;
import java.util.Optional;

public interface CourseService extends Service<Course, Long> {
    Long createCourse(Course c) throws Exception;
    void updateCourse(Course c) throws Exception;
    void deleteCourse(Long id) throws Exception;
    Optional<Course> findByName(String name) throws Exception;
}