package vn.ute.stream;

import vn.ute.model.Course;
import vn.ute.model.Enrollment;
import vn.ute.model.Result;
import vn.ute.model.Student;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

public class AcademicStreamQueries {

    /** 1. Tìm Course có học phí cao nhất (Max) */
    public static Optional<Course> mostExpensiveCourse(List<Course> courses) {
        return courses.stream()
                .max(Comparator.comparing(Course::getFee));
    }

    /** 2. Lấy danh sách Sinh viên xuất sắc của một lớp (Điểm > 8.5) */
    public static List<Student> excellentStudentsInClass(List<Result> results, long classId) {
        return results.stream()
                .filter(r -> r.getClassEntity().getId() == classId)
                .filter(r -> r.getScore() != null && r.getScore().compareTo(new BigDecimal("8.5")) >= 0)
                .map(Result::getStudent)
                .collect(Collectors.toList());
    }

    /** 3. Thống kê số lượng học viên theo từng trình độ (Beginner, Intermediate...) */
    public static Map<String, Long> countStudentsByCourseLevel(List<Enrollment> enrollments) {
    return enrollments.stream()
            .collect(Collectors.groupingBy(
                e -> String.valueOf(e.getClassEntity().getCourse().getLevel()), 
                Collectors.counting()
            ));
}
}
