package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import vn.ute.model.Course;
import vn.ute.repo.CourseRepository;
import java.util.Optional;

public class JpaCourseRepository extends AbstractJpaRepository<Course, Long> implements CourseRepository {

    public JpaCourseRepository() {
        super(Course.class);
    }
    public JpaCourseRepository(EntityManager em) {
        super( Course.class);
    }

    @Override
    public Optional<Course> findByCourseName(EntityManager em,String name) {
        try {
            Course course = em.createQuery("SELECT c FROM Course c WHERE c.courseName = :name", Course.class)
                    .setParameter("name", name)
                    .getSingleResult();
            return Optional.of(course);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}