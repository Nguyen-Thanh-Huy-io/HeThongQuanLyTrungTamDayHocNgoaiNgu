package vn.ute.repo;

import jakarta.persistence.EntityManager;
import vn.ute.model.Course;
import java.util.Optional;

public interface CourseRepository extends Repository<Course, Long> {
    // Tìm kiếm khóa học theo tên
    Optional<Course> findByCourseName(EntityManager em,String name);
}