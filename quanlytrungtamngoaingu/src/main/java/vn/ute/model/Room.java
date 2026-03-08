package vn.ute.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private long id;

    @Column(nullable = false, unique = true, length = 100)
    private String roomName;

    private int capacity;
    private String location;

    @Enumerated(EnumType.STRING)
    private Status status = Status.Active;

    public enum Status { Active, Inactive }

    public Room() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}