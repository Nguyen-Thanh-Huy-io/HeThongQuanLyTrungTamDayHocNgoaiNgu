package vn.ute.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "classes")
public class ClassEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private long id;

    @Column(nullable = false, length = 150)
    private String className;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;
    private int maxStudent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id")
    private Room room;

    @Enumerated(EnumType.STRING)
    private ClassStatus status = ClassStatus.Planned;

    public enum ClassStatus { Planned, Open, Ongoing, Completed, Cancelled }

    public ClassEntity() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public int getMaxStudent() { return maxStudent; }
    public void setMaxStudent(int maxStudent) { this.maxStudent = maxStudent; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public ClassStatus getStatus() { return status; }
    public void setStatus(ClassStatus status) { this.status = status; }
}