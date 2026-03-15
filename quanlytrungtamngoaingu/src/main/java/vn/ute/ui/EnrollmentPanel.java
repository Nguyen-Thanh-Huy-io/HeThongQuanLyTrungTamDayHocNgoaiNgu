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
            List<Course> availableCourses = loadAvailableCoursesForStudent(studentId);
            if (availableCourses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Hiện không có khóa học phù hợp để đăng ký.");
                return;
            }

            JComboBox<Course> courseComboBox = new JComboBox<>(availableCourses.toArray(new Course[0]));
            courseComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Course course) {
                        setText(course.getCourseName() + " - " + course.getFee() + " VND");
                    }
                    return this;
                }
            });

            int option = JOptionPane.showConfirmDialog(
                    this,
                    courseComboBox,
                    "Chọn khóa học muốn đăng ký",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            Course selectedCourse = (Course) courseComboBox.getSelectedItem();
            if (selectedCourse == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khóa học.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            serviceManager.getEnrollmentService().enrollStudentInCourse(studentId, selectedCourse.getId());
            JOptionPane.showMessageDialog(this, "Đăng ký khóa học thành công. Lịch học và hóa đơn đã được tạo tự động.");
            reloadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đăng ký khóa học thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<Course> loadAvailableCoursesForStudent(Long studentId) throws Exception {
        List<Course> courses = serviceManager.getCourseService().findAll();
        List<Enrollment> enrollments = serviceManager.getEnrollmentService().getByStudent(studentId);

        Set<Long> enrolledCourseIds = new HashSet<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStatus() != Enrollment.EnrollStatus.Dropped && enrollment.getClassEntity() != null && enrollment.getClassEntity().getCourse() != null) {
                enrolledCourseIds.add(enrollment.getClassEntity().getCourse().getId());
            }
        }

        List<Course> available = courses.stream()
                .filter(course -> course.getId() > 0)
                .filter(course -> !enrolledCourseIds.contains(course.getId()))
                .filter(course -> course.getStatus() == Course.Status.Active)
                .sorted(Comparator.comparing(Course::getCourseName))
                .toList();

        return available.isEmpty() ? Collections.emptyList() : available;
    }
}