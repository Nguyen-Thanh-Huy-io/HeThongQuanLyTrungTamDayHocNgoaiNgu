package vn.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.ute.db.TransactionManager;
import vn.ute.model.ClassEntity;
import vn.ute.model.Room;
import vn.ute.repo.ClassRepository;
import vn.ute.repo.RoomRepository;
import vn.ute.service.ClassService;
import java.util.List;

public class ClassServiceImpl extends AbstractService<ClassEntity, Long> implements ClassService {
    private final ClassRepository classRepo;
    private final RoomRepository roomRepo;

    public ClassServiceImpl(ClassRepository classRepo, RoomRepository roomRepo) {
        super(classRepo);
        this.classRepo = classRepo;
        this.roomRepo = roomRepo;
    }

    @Override
    public Long createClass(ClassEntity clazz) throws Exception {
        return TransactionManager.executeInTransaction((EntityManager em) -> {
            // Logic: Kiểm tra sức chứa phòng trước khi tạo lớp
            Room room = roomRepo.findById(em, clazz.getRoom().getId())
                    .orElseThrow(() -> new Exception("Phòng học không tồn tại!"));

            if (clazz.getMaxStudent() > room.getCapacity()) {
                throw new Exception("Sức chứa phòng không đủ cho " + clazz.getMaxStudent() + " học viên!");
            }

            return classRepo.insert(em, clazz);
        });
    }

    @Override
    public void updateClass(ClassEntity clazz) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            classRepo.update(em, clazz);
            return null;
        });
    }

    @Override
    public void deleteClass(Long id) throws Exception {
        TransactionManager.executeInTransaction((EntityManager em) -> {
            ClassEntity clazz = classRepo.findById(em, id)
                    .orElseThrow(() -> new Exception("Không tìm thấy lớp học ID: " + id));
            classRepo.delete(em, clazz);
            return null;
        });
    }

    @Override
    public List<ClassEntity> findByCourse(Long courseId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> classRepo.findByCourseId(em, courseId));
    }

    @Override
    public List<ClassEntity> findByTeacher(Long teacherId) throws Exception {
        return TransactionManager.execute((EntityManager em) -> classRepo.findByTeacherId(em, teacherId));
    }
}