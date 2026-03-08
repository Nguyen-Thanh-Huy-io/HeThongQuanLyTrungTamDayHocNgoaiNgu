package vn.ute.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "courses")
public class Course extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private long id;

    @Column(nullable = false, length = 200)
    private String courseName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Level level;

    private Integer duration;

    @Enumerated(EnumType.STRING)
    private DurationUnit durationUnit = DurationUnit.Week;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private Status status = Status.Active;

    public enum Level { Beginner, Intermediate, Advanced }
    public enum DurationUnit { Hour, Week }
    public enum Status { Active, Inactive }

    public Course() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public DurationUnit getDurationUnit() { return durationUnit; }
    public void setDurationUnit(DurationUnit durationUnit) { this.durationUnit = durationUnit; }
    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}