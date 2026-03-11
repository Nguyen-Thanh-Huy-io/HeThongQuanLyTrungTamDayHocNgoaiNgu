package vn.ute.ui;

import vn.ute.model.ClassEntity;
import vn.ute.model.Enrollment;
import vn.ute.model.Result;
import vn.ute.model.Student;
import vn.ute.service.ServiceManager;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class ResultPanel extends BasePanel<Result> {
    private final ServiceManager serviceManager;
    private final SessionManager sessionManager;
    private final Supplier<List<Result>> dataLoader;

    public ResultPanel(ServiceManager serviceManager, SessionManager sessionManager, Supplier<List<Result>> dataLoader) {
        super(new ResultTableModel());
        this.serviceManager = serviceManager;
        this.sessionManager = sessionManager;
        this.dataLoader = dataLoader;

        if (sessionManager.isTeacher()) {
            btnAdd.setText("Nhập kết quả");
            btnEdit.setText("Cập nhật kết quả");
        }

        reloadData();
    }

    @Override
    public void reloadData() {
        new Thread(() -> {
            List<Result> list = dataLoader.get();
            SwingUtilities.invokeLater(() -> ((GenericTableModel<Result>) tableModel).setData(list));
        }).start();
    }

    @Override
    protected void onAdd() {
        if (sessionManager.isTeacher()) {
            openResultDialog(null);
            return;
        }

        new GenericEditDialog<>(SwingUtilities.getWindowAncestor(this), Result.class).setVisible(true);
        reloadData();
    }

    @Override
    protected void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn kết quả cần chỉnh sửa.");
            return;
        }

        Result selected = ((GenericTableModel<Result>) tableModel).getRow(row);
        if (sessionManager.isTeacher()) {
            openResultDialog(selected);
            return;
        }

        new GenericEditDialog<>(SwingUtilities.getWindowAncestor(this), Result.class, selected).setVisible(true);
        reloadData();
    }

    @Override
    protected void onDelete() {
        JOptionPane.showMessageDialog(this, "Chức năng xóa kết quả không được hỗ trợ để đảm bảo tính toàn vẹn dữ liệu.");
    }

    private void openResultDialog(Result existingResult) {
        try {
            List<ClassEntity> teacherClasses = loadTeacherClasses();
            if (teacherClasses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Bạn chưa được phân công lớp nào để nhập kết quả.");
                return;
            }

            JComboBox<ClassEntity> classComboBox = new JComboBox<>(teacherClasses.toArray(new ClassEntity[0]));
            classComboBox.setRenderer(new ClassRenderer());

            JComboBox<Student> studentComboBox = new JComboBox<>();
            studentComboBox.setRenderer(new StudentRenderer());

            JTextField txtScore = new JTextField(20);
            JTextField txtGrade = new JTextField(20);
            JTextArea txtComment = new JTextArea(4, 20);
            txtComment.setLineWrap(true);
            txtComment.setWrapStyleWord(true);

            classComboBox.addActionListener(e -> reloadStudents(studentComboBox, (ClassEntity) classComboBox.getSelectedItem()));
            reloadStudents(studentComboBox, (ClassEntity) classComboBox.getSelectedItem());

            if (existingResult != null) {
                selectClass(classComboBox, existingResult.getClassEntity());
                reloadStudents(studentComboBox, (ClassEntity) classComboBox.getSelectedItem());
                selectStudent(studentComboBox, existingResult.getStudent());
                txtScore.setText(existingResult.getScore() != null ? existingResult.getScore().toPlainString() : "");
                txtGrade.setText(existingResult.getGrade() != null ? existingResult.getGrade() : "");
                txtComment.setText(existingResult.getComment() != null ? existingResult.getComment() : "");
            }

            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 4, 4, 4);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;

            form.add(new JLabel("Lớp học:"), gbc);
            gbc.gridx = 1;
            form.add(classComboBox, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            form.add(new JLabel("Học viên:"), gbc);
            gbc.gridx = 1;
            form.add(studentComboBox, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            form.add(new JLabel("Điểm số:"), gbc);
            gbc.gridx = 1;
            form.add(txtScore, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            form.add(new JLabel("Xếp loại:"), gbc);
            gbc.gridx = 1;
            form.add(txtGrade, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            form.add(new JLabel("Nhận xét:"), gbc);
            gbc.gridx = 1;
            form.add(new JScrollPane(txtComment), gbc);

            int option = JOptionPane.showConfirmDialog(
                    this,
                    form,
                    existingResult == null ? "Nhập kết quả học tập" : "Cập nhật kết quả học tập",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            ClassEntity selectedClass = (ClassEntity) classComboBox.getSelectedItem();
            Student selectedStudent = (Student) studentComboBox.getSelectedItem();
            if (selectedClass == null || selectedStudent == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn lớp học và học viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Result target = existingResult != null ? existingResult : new Result();
            if (existingResult == null) {
                serviceManager.getResultService().getSpecificResult(selectedStudent.getId(), selectedClass.getId())
                        .ifPresent(found -> target.setId(found.getId()));
            }

            target.setClassEntity(selectedClass);
            target.setStudent(selectedStudent);
            target.setScore(parseScore(txtScore.getText()));
            target.setGrade(normalize(txtGrade.getText()));
            target.setComment(normalize(txtComment.getText()));

            serviceManager.getResultService().saveClassResults(List.of(target));
            JOptionPane.showMessageDialog(this, target.getId() > 0 ? "Lưu kết quả thành công." : "Nhập kết quả thành công.");
            reloadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể lưu kết quả: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<ClassEntity> loadTeacherClasses() throws Exception {
        Long teacherId = sessionManager.getCurrentTeacherId();
        if (teacherId == null) {
            return Collections.emptyList();
        }

        List<ClassEntity> classes = serviceManager.getClassService().findByTeacher(teacherId);
        return classes.stream()
                .sorted(Comparator.comparing(ClassEntity::getClassName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private void reloadStudents(JComboBox<Student> studentComboBox, ClassEntity selectedClass) {
        studentComboBox.removeAllItems();
        if (selectedClass == null) {
            return;
        }

        try {
            List<Enrollment> enrollments = serviceManager.getEnrollmentService().getByClass(selectedClass.getId());
            List<Student> students = new ArrayList<>();
            for (Enrollment enrollment : enrollments) {
                if (enrollment.getStudent() != null && enrollment.getStatus() != Enrollment.EnrollStatus.Dropped) {
                    students.add(enrollment.getStudent());
                }
            }
            students.sort(Comparator.comparing(Student::getFullName, Comparator.nullsLast(String::compareToIgnoreCase)));
            for (Student student : students) {
                studentComboBox.addItem(student);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách học viên: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectClass(JComboBox<ClassEntity> comboBox, ClassEntity selectedClass) {
        if (selectedClass == null) {
            return;
        }
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            ClassEntity item = comboBox.getItemAt(i);
            if (item != null && item.getId() == selectedClass.getId()) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectStudent(JComboBox<Student> comboBox, Student selectedStudent) {
        if (selectedStudent == null) {
            return;
        }
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Student item = comboBox.getItemAt(i);
            if (item != null && item.getId() == selectedStudent.getId()) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private BigDecimal parseScore(String rawValue) throws Exception {
        String normalized = normalize(rawValue);
        if (normalized == null) {
            return null;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            throw new Exception("Điểm số không hợp lệ.");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static class ClassRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ClassEntity classEntity) {
                setText(classEntity.getClassName());
            }
            return this;
        }
    }

    private static class StudentRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Student student) {
                setText(student.getFullName() + " - " + (student.getEmail() != null ? student.getEmail() : "Chưa có email"));
            }
            return this;
        }
    }
}