package vn.ute.service;

import vn.ute.model.ClassEntity;
import java.util.List;

public interface ClassService extends Service<ClassEntity, Long> {
    Long createClass(ClassEntity clazz) throws Exception;
    void updateClass(ClassEntity clazz) throws Exception;
    void deleteClass(Long id) throws Exception;
    List<ClassEntity> findByCourse(Long courseId) throws Exception;
    List<ClassEntity> findByTeacher(Long teacherId) throws Exception;
}