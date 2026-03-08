package vn.ute.model;

import jakarta.persistence.*;

@Entity
@Table(name = "staffs")
public class Staff extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private long id;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Enumerated(EnumType.STRING)
    private StaffRole role = StaffRole.Other;

    private String phone;
    private String email;

    @Enumerated(EnumType.STRING)
    private Status status = Status.Active;

    public enum StaffRole { Admin, Consultant, Accountant, Manager, Other }
    public enum Status { Active, Inactive }

    public Staff() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public StaffRole getRole() { return role; }
    public void setRole(StaffRole role) { this.role = role; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}