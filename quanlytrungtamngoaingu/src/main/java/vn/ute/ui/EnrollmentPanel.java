package vn.ute.ui;

import vn.ute.model.ClassEntity;
import vn.ute.model.Course;
import vn.ute.model.Enrollment;
import vn.ute.model.Student;
import vn.ute.service.ServiceManager;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class EnrollmentPanel extends BasePanel<Enrollment> {
    private final ServiceManager serviceManager;
    private final SessionManager sessionManager;
    private final Supplier<List<Enrollment>> dataLoader;

    public EnrollmentPanel(ServiceManager serviceManager, SessionManager sessionManager, Supplier<List<Enrollment>> dataLoader) {
        super(sessionManager.isStudent() ? new StudentEnrollmentTableModel() : new EnrollmentTableModel());
        this.serviceManager = serviceManager;
        this.sessionManager = sessionManager;
        this.dataLoader = dataLoader;

        if (sessionManager.isStudent()) {
            btnAdd.setText("Đăng ký lớp");
            btnDelete.setText("Hủy đăng ký");
        }

        reloadData();
    }

    @Override
    public void reloadData() {
        new Thread(() -> {
            List<Enrollment> list = dataLoader.get();
            SwingUtilities.invokeLater(() -> ((GenericTableModel<Enrollment>) tableModel).setData(list));
        }).start();
    }

    @Override
    protected void onAdd() {
        if (sessionManager.isStudent()) {
            registerCurrentStudent();
            return;
        }

        new GenericEditDialog<>(SwingUtilities.getWindowAncestor(this), Enrollment.class).setVisible(true);
        reloadData();
    }

    @Override
    protected void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để chỉnh sửa.");
            return;
        }

        Enrollment item = ((GenericTableModel<Enrollment>) tableModel).getRow(row);
        new GenericEditDialog<>(SwingUtilities.getWindowAncestor(this), Enrollment.class, item).setVisible(true);
        reloadData();
    }

    @Override
    protected void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đăng ký lớp cần hủy.");
            return;
        }

        Enrollment enrollment = ((GenericTableModel<Enrollment>) tableModel).getRow(row);
        if (enrollment == null || enrollment.getId() <= 0) {
            JOptionPane.showMessageDialog(this, "Không xác định được đăng ký lớp cần hủy.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (enrollment.getStatus() == Enrollment.EnrollStatus.Dropped) {
            JOptionPane.showMessageDialog(this, "Đăng ký lớp này đã được hủy trước đó.");
            return;
        }

        String message = sessionManager.isStudent()
                ? "Bạn có chắc chắn muốn hủy đăng ký lớp đã chọn?"
                : "Bạn có chắc chắn muốn cập nhật đăng ký này sang trạng thái hủy?";
        int confirm = JOptionPane.showConfirmDialog(
                this,
                message,
                "Xác nhận hủy đăng ký",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            serviceManager.getEnrollmentService().cancelEnrollment(enrollment.getId());
            JOptionPane.showMessageDialog(this, "Hủy đăng ký lớp thành công.");
            reloadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Hủy đăng ký lớp thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerCurrentStudent() {
        Long studentId = sessionManager.getCurrentStudentId();
        if (studentId == null) {
            JOptionPane.showMessageDialog(this, "Không xác định được học viên đang đăng nhập.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            List<ClassEntity> availableClasses = loadAvailableClassesForStudent(studentId);
            if (availableClasses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Hiện không có lớp phù hợp để đăng ký.");
                return;
            }

            JComboBox<ClassEntity> classComboBox = new JComboBox<>(availableClasses.toArray(new ClassEntity[0]));
            classComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof ClassEntity classEntity) {
                        setText(formatClassLabel(classEntity));
                    }
                    return this;
                }
            });

            int option = JOptionPane.showConfirmDialog(
                    this,
                    classComboBox,
                    "Chọn lớp muốn đăng ký",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            ClassEntity selectedClass = (ClassEntity) classComboBox.getSelectedItem();
            if (selectedClass == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn lớp học.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Enrollment enrollment = new Enrollment();
            Student student = new Student();
            student.setId(studentId);
            enrollment.setStudent(student);
            enrollment.setClassEntity(selectedClass);

            serviceManager.getEnrollmentService().enrollStudent(enrollment);
            JOptionPane.showMessageDialog(this, "Đăng ký lớp thành công.");
            reloadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đăng ký lớp thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<ClassEntity> loadAvailableClassesForStudent(Long studentId) throws Exception {
        List<ClassEntity> classes = serviceManager.getClassService().findAll();
        List<Enrollment> enrollments = serviceManager.getEnrollmentService().getByStudent(studentId);

        Set<Long> enrolledClassIds = new HashSet<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStatus() != Enrollment.EnrollStatus.Dropped && enrollment.getClassEntity() != null) {
                enrolledClassIds.add(enrollment.getClassEntity().getId());
            }
        }

        List<ClassEntity> available = classes.stream()
                .filter(clazz -> clazz.getId() > 0)
                .filter(clazz -> !enrolledClassIds.contains(clazz.getId()))
                .filter(clazz -> clazz.getStatus() == ClassEntity.ClassStatus.Open
                        || clazz.getStatus() == ClassEntity.ClassStatus.Planned
                        || clazz.getStatus() == ClassEntity.ClassStatus.Ongoing)
                .filter(clazz -> clazz.getCourse() == null || clazz.getCourse().getStatus() == Course.Status.Active)
                .sorted(Comparator.comparing(ClassEntity::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ClassEntity::getClassName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        return available.isEmpty() ? Collections.emptyList() : available;
    }

    private String formatClassLabel(ClassEntity classEntity) {
        String courseName = classEntity.getCourse() != null ? classEntity.getCourse().getCourseName() : "Chưa gán khóa học";
        String teacherName = classEntity.getTeacher() != null ? classEntity.getTeacher().getFullName() : "Chưa phân công GV";
        String startDate = classEntity.getStartDate() != null ? classEntity.getStartDate().toString() : "Chưa có ngày khai giảng";
        return classEntity.getClassName() + " | " + courseName + " | GV: " + teacherName + " | Bắt đầu: " + startDate;
    }
}