package vn.ute.service;

import vn.ute.model.Room;
import java.util.Optional;

public interface RoomService extends Service<Room, Long> {
    Long createRoom(Room r) throws Exception;
    void updateRoom(Room r) throws Exception;
    Optional<Room> findByRoomName(String name) throws Exception;
}