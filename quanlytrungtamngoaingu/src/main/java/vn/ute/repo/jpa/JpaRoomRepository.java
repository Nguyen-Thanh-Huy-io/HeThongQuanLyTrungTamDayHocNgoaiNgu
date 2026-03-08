package vn.ute.repo.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import vn.ute.model.Room;
import vn.ute.repo.RoomRepository;
import java.util.Optional;

public class JpaRoomRepository extends AbstractJpaRepository<Room, Long> implements RoomRepository {

    public JpaRoomRepository() {
        super(Room.class);
    }

    public JpaRoomRepository(EntityManager em) {
        super( Room.class);
    }

    @Override
    public Optional<Room> findByRoomName(EntityManager em,String roomName) {
        try {
            Room room = em.createQuery("SELECT r FROM Room r WHERE r.roomName = :roomName", Room.class)
                    .setParameter("roomName", roomName)
                    .getSingleResult();
            return Optional.of(room);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}