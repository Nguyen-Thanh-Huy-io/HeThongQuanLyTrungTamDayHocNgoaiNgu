package vn.ute.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "teachers")
public class Teacher extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id")
    private long id;

    @Column(nullable = false, length = 150)
    private String fullName;

    private String phone;
    private String email;
    private String specialty;
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private Status status = Status.Active;

    public enum Status { Active, Inactive }

    public Teacher() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}