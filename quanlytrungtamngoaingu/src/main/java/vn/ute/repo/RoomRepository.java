package vn.ute.repo;

import jakarta.persistence.EntityManager;
import vn.ute.model.Room;
import java.util.Optional;

public interface RoomRepository extends Repository<Room, Long> {
    // Kiểm tra phòng học theo tên phòng (A101, B202,...)
    Optional<Room> findByRoomName(EntityManager em,String roomName);
}