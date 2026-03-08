package vn.ute.repo;

import jakarta.persistence.EntityManager;
import vn.ute.model.Staff;
import java.util.Optional;
import java.util.List;

public interface StaffRepository extends Repository<Staff, Long> {
    Optional<Staff> findByEmail(EntityManager em,String email);
    Optional<Staff> findByPhone(EntityManager em,String phone);
    List<Staff> findByRole(EntityManager em,String role);
}