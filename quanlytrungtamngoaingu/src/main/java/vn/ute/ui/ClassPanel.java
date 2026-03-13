package vn.ute.ui;

import vn.ute.model.ClassEntity;
import vn.ute.model.Course;
import vn.ute.model.Room;
import vn.ute.model.Teacher;
import vn.ute.service.ServiceManager;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class ClassPanel extends BasePanel<ClassEntity> {
    private final ServiceManager serviceManager;
    private final Supplier<List<ClassEntity>> dataLoader;
    private final SchedulePanel schedulePanel;

    public ClassPanel(ServiceManager serviceManager, Supplier<List<ClassEntity>> dataLoader, SchedulePanel schedulePanel) {
        super(new ClassTableModel());
        this.serviceManager = serviceManager;
        this.dataLoader = dataLoader;
        this.schedulePanel = schedulePanel;
        btnAdd.setText("Tạo lớp");
        reloadData();
    }

    @Override
    public void reloadData() {
        new Thread(() -> {
            List<ClassEntity> classes = dataLoader.get();
            SwingUtilities.invokeLater(() -> ((GenericTableModel<ClassEntity>) tableModel).setData(classes));
        }).start();
    }

    @Override
    protected void onAdd() {
        try {
            ClassEntity created = openClassDialog(null);
            if (created != null) {
                JOptionPane.showMessageDialog(this, "Tạo lớp học thành công.");
                reloadData();
                int createSchedule = JOptionPane.showConfirmDialog(
                        this,
                        "Bạn có muốn tạo lịch học ngay cho lớp vừa tạo không?",
                        "Tạo lịch học",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (createSchedule == JOptionPane.YES_OPTION && schedulePanel != null) {
                    schedulePanel.openCreateForClass(created);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Tạo lớp học thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lớp học cần chỉnh sửa.");
            return;
        }

        ClassEntity selected = ((GenericTableModel<ClassEntity>) tableModel).getRow(row);
        try {
            ClassEntity updated = openClassDialog(selected);
            if (updated != null) {
                JOptionPane.showMessageDialog(this, "Cập nhật lớp học thành công.");
                reloadData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cập nhật lớp học thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lớp học cần xóa.");
            return;
        }

        ClassEntity selected = ((GenericTableModel<ClassEntity>) tableModel).getRow(row);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa lớp học đã chọn?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            serviceManager.getClassService().deleteClass(selected.getId());
            JOptionPane.showMessageDialog(this, "Xóa lớp học thành công.");
            reloadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Xóa lớp học thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ClassEntity openClassDialog(ClassEntity existingClass) throws Exception {
        List<Course> courses = serviceManager.getCourseService().findAll().stream()
                .filter(course -> course.getStatus() == Course.Status.Active)
                .sorted(Comparator.comparing(Course::getCourseName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
        List<Teacher> teachers = serviceManager.getTeacherService().findAll().stream()
                .filter(teacher -> teacher.getStatus() == Teacher.Status.Active)
                .sorted(Comparator.comparing(Teacher::getFullName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
        List<Room> rooms = serviceManager.getRoomService().findAll().stream()
                .filter(room -> room.getStatus() == Room.Status.Active)
                .sorted(Comparator.comparing(Room::getRoomName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        if (courses.isEmpty() || rooms.isEmpty()) {
            throw new Exception("Cần có khóa học và phòng học hoạt động trước khi tạo lớp.");
        }

        JTextField classNameField = new JTextField(20);
        JComboBox<Course> courseComboBox = new JComboBox<>(courses.toArray(new Course[0]));
        courseComboBox.setRenderer(new CourseRenderer());
        JComboBox<Teacher> teacherComboBox = new JComboBox<>(teachers.toArray(new Teacher[0]));
        teacherComboBox.setRenderer(new TeacherRenderer());
        teacherComboBox.insertItemAt(null, 0);
        teacherComboBox.setSelectedIndex(0);
        JTextField startDateField = new JTextField(20);
        JTextField endDateField = new JTextField(20);
        JTextField maxStudentField = new JTextField(20);
        JComboBox<Room> roomComboBox = new JComboBox<>(rooms.toArray(new Room[0]));
        roomComboBox.setRenderer(new RoomRenderer());
        JComboBox<ClassEntity.ClassStatus> statusComboBox = new JComboBox<>(ClassEntity.ClassStatus.values());

        if (existingClass != null) {
            classNameField.setText(existingClass.getClassName());
            selectCourse(courseComboBox, existingClass.getCourse());
            selectTeacher(teacherComboBox, existingClass.getTeacher());
            startDateField.setText(existingClass.getStartDate() != null ? existingClass.getStartDate().toString() : "");
            endDateField.setText(existingClass.getEndDate() != null ? existingClass.getEndDate().toString() : "");
            maxStudentField.setText(String.valueOf(existingClass.getMaxStudent()));
            selectRoom(roomComboBox, existingClass.getRoom());
            statusComboBox.setSelectedItem(existingClass.getStatus());
        }

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        form.add(new JLabel("Tên lớp:"), gbc);
        gbc.gridx = 1;
        form.add(classNameField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Khóa học:"), gbc);
        gbc.gridx = 1;
        form.add(courseComboBox, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Giảng viên:"), gbc);
        gbc.gridx = 1;
        form.add(teacherComboBox, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Ngày bắt đầu (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        form.add(startDateField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Ngày kết thúc (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1;
        form.add(endDateField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Sĩ số tối đa:"), gbc);
        gbc.gridx = 1;
        form.add(maxStudentField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Phòng học:"), gbc);
        gbc.gridx = 1;
        form.add(roomComboBox, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1;
        form.add(statusComboBox, gbc);

        int option = JOptionPane.showConfirmDialog(
                this,
                form,
                existingClass == null ? "Tạo lớp học" : "Cập nhật lớp học",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (option != JOptionPane.OK_OPTION) {
            return null;
        }

        Course selectedCourse = (Course) courseComboBox.getSelectedItem();
        Room selectedRoom = (Room) roomComboBox.getSelectedItem();
        if (selectedCourse == null || selectedRoom == null) {
            throw new Exception("Vui lòng chọn khóa học và phòng học.");
        }

        ClassEntity target = existingClass != null ? existingClass : new ClassEntity();
        target.setClassName(classNameField.getText().trim());
        target.setCourse(selectedCourse);
        target.setTeacher((Teacher) teacherComboBox.getSelectedItem());
        target.setStartDate(LocalDate.parse(startDateField.getText().trim()));
        String endDateRaw = endDateField.getText().trim();
        target.setEndDate(endDateRaw.isEmpty() ? null : LocalDate.parse(endDateRaw));
        target.setMaxStudent(Integer.parseInt(maxStudentField.getText().trim()));
        target.setRoom(selectedRoom);
        target.setStatus((ClassEntity.ClassStatus) statusComboBox.getSelectedItem());

        if (target.getClassName() == null || target.getClassName().isBlank()) {
            throw new Exception("Tên lớp không được để trống.");
        }
        if (target.getEndDate() != null && target.getEndDate().isBefore(target.getStartDate())) {
            throw new Exception("Ngày kết thúc không được trước ngày bắt đầu.");
        }

        if (existingClass == null) {
            Long id = serviceManager.getClassService().createClass(target);
            return serviceManager.getClassService().findById(id)
                    .orElseThrow(() -> new Exception("Không tìm thấy lớp học vừa tạo."));
        }

        serviceManager.getClassService().updateClass(target);
        return target;
    }

    private void selectCourse(JComboBox<Course> comboBox, Course course) {
        if (course == null) {
            return;
        }
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Course item = comboBox.getItemAt(i);
            if (item != null && item.getId() == course.getId()) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectTeacher(JComboBox<Teacher> comboBox, Teacher teacher) {
        if (teacher == null) {
            comboBox.setSelectedIndex(0);
            return;
        }
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Teacher item = comboBox.getItemAt(i);
            if (item != null && item.getId() == teacher.getId()) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectRoom(JComboBox<Room> comboBox, Room room) {
        if (room == null) {
            return;
        }
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Room item = comboBox.getItemAt(i);
            if (item != null && item.getId() == room.getId()) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private static class CourseRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Course course) {
                setText(course.getCourseName());
            }
            return this;
        }
    }

    private static class TeacherRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Teacher teacher) {
                setText(teacher.getFullName());
            } else if (value == null) {
                setText("Chưa phân công");
            }
            return this;
        }
    }

    private static class RoomRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Room room) {
                setText(room.getRoomName());
            }
            return this;
        }
    }
}
