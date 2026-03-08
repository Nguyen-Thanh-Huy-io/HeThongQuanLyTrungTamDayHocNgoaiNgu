package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Course;
import vn.ute.repo.CourseRepository;
import vn.ute.service.CourseService;
import java.util.Optional;

public class CourseServiceImpl extends AbstractService<Course, Long> implements CourseService {
    private final CourseRepository courseRepo;

    public CourseServiceImpl(CourseRepository courseRepo) {
        super(courseRepo);
        this.courseRepo = courseRepo;
    }

    @Override
    public Long createCourse(Course c) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            if (courseRepo.findByCourseName(em, c.getCourseName()).isPresent()) {
                throw new Exception("Tên khóa học này đã tồn tại!");
            }
            return courseRepo.insert(em, c);
        });
    }

    @Override
    public void updateCourse(Course c) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            courseRepo.update(em, c);
            return null;
        });
    }

    @Override
    public void deleteCourse(Long id) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            Course c = courseRepo.findById(em, id)
                    .orElseThrow(() -> new Exception("Không tìm thấy khóa học để xóa!"));
            courseRepo.delete(em, c);
            return null;
        });
    }

    @Override
    public Optional<Course> findByName(String name) throws Exception {
        return TransactionManager.execute((EntityManager em) -> courseRepo.findByCourseName(em, name));
    }
}