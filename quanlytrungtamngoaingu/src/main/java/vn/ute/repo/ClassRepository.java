package vn.ute.repo;

import vn.ute.model.ClassEntity;
import jakarta.persistence.EntityManager;
import java.util.List;

public interface ClassRepository extends Repository<ClassEntity, Long> {
    // Tìm các lớp học thuộc một khóa học cụ thể
    List<ClassEntity> findByCourseId(EntityManager em,Long courseId);
    
    // Tìm các lớp do một giáo viên phụ trách
    List<ClassEntity> findByTeacherId(EntityManager em,Long teacherId);
    
    // Tìm các lớp đang học tại một phòng cụ thể
    List<ClassEntity> findByRoomId(EntityManager em,Long roomId);
}