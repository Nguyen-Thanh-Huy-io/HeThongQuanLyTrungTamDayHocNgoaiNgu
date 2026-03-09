package vn.ute.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "enrollments")
public class Enrollment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    private LocalDate enrollmentDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private EnrollStatus status = EnrollStatus.Enrolled;

    @Enumerated(EnumType.STRING)
    private ResultType result = ResultType.NA;

    public enum EnrollStatus { Enrolled, Dropped, Completed }
    public enum ResultType { Pass, Fail, NA }

    public Enrollment() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public ClassEntity getClassEntity() { return classEntity; }
    public void setClassEntity(ClassEntity classEntity) { this.classEntity = classEntity; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public EnrollStatus getStatus() { return status; }
    public void setStatus(EnrollStatus status) { this.status = status; }
    public ResultType getResult() { return result; }
    public void setResult(ResultType result) { this.result = result; }
}