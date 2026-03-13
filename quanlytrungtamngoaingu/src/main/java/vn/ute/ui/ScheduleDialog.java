package vn.ute.ui;

import vn.ute.model.ClassEntity;
import vn.ute.model.Room;
import vn.ute.model.Schedule;
import vn.ute.service.ServiceManager;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScheduleDialog extends JDialog {
    private final ServiceManager serviceManager;
    private final Schedule existingSchedule;
    private final ClassEntity preselectedClass;

    public ScheduleDialog(Window owner, ServiceManager serviceManager, Schedule existingSchedule, ClassEntity preselectedClass) {
        super(owner, existingSchedule == null ? "Tạo lịch học" : "Cập nhật lịch học", ModalityType.APPLICATION_MODAL);
        this.serviceManager = serviceManager;
        this.existingSchedule = existingSchedule;
        this.preselectedClass = preselectedClass;
    }

    public boolean open() {
        try {
            List<ClassEntity> classes = serviceManager.getClassService().findAll().stream()
                    .sorted(Comparator.comparing(ClassEntity::getClassName, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .toList();
            List<Room> rooms = serviceManager.getRoomService().findAll().stream()
                    .filter(room -> room.getStatus() == Room.Status.Active)
                    .sorted(Comparator.comparing(Room::getRoomName, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .toList();

            if (classes.isEmpty()) {
                JOptionPane.showMessageDialog(getOwner(), "Chưa có lớp học để tạo lịch.", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (rooms.isEmpty()) {
                JOptionPane.showMessageDialog(getOwner(), "Chưa có phòng học hoạt động để tạo lịch.", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            JComboBox<ClassEntity> classComboBox = new JComboBox<>(classes.toArray(new ClassEntity[0]));
            classComboBox.setRenderer(new ClassRenderer());
            JComboBox<Room> roomComboBox = new JComboBox<>(rooms.toArray(new Room[0]));
            roomComboBox.setRenderer(new RoomRenderer());
            JTextField dateField = new JTextField(20);
            JTextField startTimeField = new JTextField(20);
            JTextField endTimeField = new JTextField(20);

            if (preselectedClass != null) {
                selectClass(classComboBox, preselectedClass.getId());
                if (preselectedClass.getRoom() != null) {
                    selectRoom(roomComboBox, preselectedClass.getRoom().getId());
                }
            }

            if (existingSchedule != null) {
                selectClass(classComboBox, existingSchedule.getClassEntity() != null ? existingSchedule.getClassEntity().getId() : 0L);
                selectRoom(roomComboBox, existingSchedule.getRoom() != null ? existingSchedule.getRoom().getId() : 0L);
                dateField.setText(existingSchedule.getStudyDate() != null ? existingSchedule.getStudyDate().toString() : "");
                startTimeField.setText(existingSchedule.getStartTime() != null ? existingSchedule.getStartTime().toString() : "");
                endTimeField.setText(existingSchedule.getEndTime() != null ? existingSchedule.getEndTime().toString() : "");
            } else {
                ClassEntity selectedClass = (ClassEntity) classComboBox.getSelectedItem();
                if (selectedClass != null) {
                    dateField.setText(selectedClass.getStartDate() != null ? selectedClass.getStartDate().toString() : "");
                }
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
            form.add(new JLabel("Phòng học:"), gbc);
            gbc.gridx = 1;
            form.add(roomComboBox, gbc);
            gbc.gridx = 0;
            gbc.gridy++;
            form.add(new JLabel("Ngày học (yyyy-MM-dd):"), gbc);
            gbc.gridx = 1;
            form.add(dateField, gbc);
            gbc.gridx = 0;
            gbc.gridy++;
            form.add(new JLabel("Giờ bắt đầu (HH:mm):"), gbc);
            gbc.gridx = 1;
            form.add(startTimeField, gbc);
            gbc.gridx = 0;
            gbc.gridy++;
            form.add(new JLabel("Giờ kết thúc (HH:mm):"), gbc);
            gbc.gridx = 1;
            form.add(endTimeField, gbc);

            int option = JOptionPane.showConfirmDialog(
                    getOwner(),
                    form,
                    existingSchedule == null ? "Tạo lịch học" : "Cập nhật lịch học",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (option != JOptionPane.OK_OPTION) {
                return false;
            }

            ClassEntity selectedClass = (ClassEntity) classComboBox.getSelectedItem();
            Room selectedRoom = (Room) roomComboBox.getSelectedItem();
            if (selectedClass == null || selectedRoom == null) {
                throw new Exception("Vui lòng chọn lớp học và phòng học.");
            }

            LocalDate studyDate = LocalDate.parse(dateField.getText().trim());
            LocalTime startTime = LocalTime.parse(normalizeTime(startTimeField.getText()));
            LocalTime endTime = LocalTime.parse(normalizeTime(endTimeField.getText()));
            if (!startTime.isBefore(endTime)) {
                throw new Exception("Giờ bắt đầu phải trước giờ kết thúc.");
            }

            Schedule target = existingSchedule != null ? existingSchedule : new Schedule();
            target.setClassEntity(selectedClass);
            target.setRoom(selectedRoom);
            target.setStudyDate(studyDate);
            target.setStartTime(startTime);
            target.setEndTime(endTime);

            if (existingSchedule == null) {
                serviceManager.getScheduleService().createSchedule(target);
            } else {
                serviceManager.getScheduleService().updateSchedule(target);
            }
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(getOwner(), "Không thể lưu lịch học: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private String normalizeTime(String value) {
        String trimmed = value.trim();
        if (trimmed.matches("^\\d{2}:\\d{2}$")) {
            return trimmed + ":00";
        }
        return trimmed;
    }

    private void selectClass(JComboBox<ClassEntity> comboBox, long classId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            ClassEntity item = comboBox.getItemAt(i);
            if (item != null && item.getId() == classId) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectRoom(JComboBox<Room> comboBox, long roomId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Room item = comboBox.getItemAt(i);
            if (item != null && item.getId() == roomId) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
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
