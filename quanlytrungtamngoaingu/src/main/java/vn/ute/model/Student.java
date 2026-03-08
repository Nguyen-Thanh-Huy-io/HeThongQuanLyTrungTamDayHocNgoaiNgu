package vn.ute.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "students")
public class Student extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private long id;

    @Column(nullable = false, length = 150)
    private String fullName;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(unique = true, length = 150)
    private String email;

    private String address;

    @Column(nullable = false)
    private LocalDate registrationDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.Active;

    public enum Gender { Male, Female, Other }
    public enum Status { Active, Inactive }

    public Student() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}