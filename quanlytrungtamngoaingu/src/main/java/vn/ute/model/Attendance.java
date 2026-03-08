package vn.ute.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendances")
public class Attendance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @Column(nullable = false)
    private LocalDate attendDate;

    @Enumerated(EnumType.STRING)
    private AttendStatus status = AttendStatus.Present;

    private String note;

    public enum AttendStatus { Present, Absent, Late }

    public Attendance() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public ClassEntity getClassEntity() { return classEntity; }
    public void setClassEntity(ClassEntity classEntity) { this.classEntity = classEntity; }
    public LocalDate getAttendDate() { return attendDate; }
    public void setAttendDate(LocalDate attendDate) { this.attendDate = attendDate; }
    public AttendStatus getStatus() { return status; }
    public void setStatus(AttendStatus status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}