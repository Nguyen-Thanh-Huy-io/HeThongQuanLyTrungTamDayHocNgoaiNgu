package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.Room;
import vn.ute.repo.RoomRepository;
import vn.ute.service.RoomService;
import java.util.Optional;

public class RoomServiceImpl extends AbstractService<Room, Long> implements RoomService {
    private final RoomRepository roomRepo;

    public RoomServiceImpl(RoomRepository roomRepo) {
        super(roomRepo);
        this.roomRepo = roomRepo;
    }

    @Override
    public Long createRoom(Room r) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            if (roomRepo.findByRoomName(em, r.getRoomName()).isPresent()) {
                throw new Exception("Tên phòng học đã tồn tại!");
            }
            return roomRepo.insert(em, r);
        });
    }

    @Override
    public void updateRoom(Room r) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            roomRepo.update(em, r);
            return null;
        });
    }

    @Override
    public Optional<Room> findByRoomName(String name) throws Exception {
        return TransactionManager.execute((EntityManager em) -> roomRepo.findByRoomName(em, name));
    }
}