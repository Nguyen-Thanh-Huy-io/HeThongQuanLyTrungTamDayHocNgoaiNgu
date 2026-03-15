package vn.ute.ui;

import vn.ute.model.ClassEntity;
import vn.ute.model.Room;
import vn.ute.model.Schedule;
import vn.ute.service.ServiceManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Dialog cho phép tạo hàng loạt lịch học theo khoảng ngày + các thứ trong tuần.
 * Luồng sử dụng:
 *   1. Chọn lớp, phòng, giờ bắt đầu, giờ kết thúc
 *   2. Chọn khoảng ngày (từ – đến) và các thứ được dạy
 *   3. Nhấn "Xem trước" để xem danh sách ngày sẽ được tạo
 *   4. Nhấn "Tạo tất cả" để lưu
 */
public class BulkScheduleDialog extends JDialog {

    private static final DateTimeFormatter DISP_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] DAY_NAMES = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
    private static final DayOfWeek[] DAYS = {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    };

    private final ServiceManager serviceManager;
    private final ClassEntity preselectedClass;

    private JComboBox<ClassEntity> classCombo;
    private JComboBox<Room> roomCombo;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private DatePickerField fromDatePicker;
    private DatePickerField toDatePicker;
    private final JCheckBox[] dayCheckBoxes = new JCheckBox[7];
    private final PreviewTableModel previewModel = new PreviewTableModel();
    private JButton btnCreate;
    private boolean saved = false;

    public BulkScheduleDialog(Window owner, ServiceManager serviceManager, ClassEntity preselectedClass) {
        super(owner, "Tạo nhiều lịch học cùng lúc", ModalityType.APPLICATION_MODAL);
        this.serviceManager = serviceManager;
        this.preselectedClass = preselectedClass;
        buildUI();
        pack();
        setMinimumSize(new Dimension(780, 620));
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        List<ClassEntity> classes = loadClasses();
        List<Room> rooms = loadRooms();

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // ─── TOP: input form ────────────────────────────────────────────────
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(titledBorder("Thông tin lịch học"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(5, 8, 5, 8);
        g.anchor  = GridBagConstraints.WEST;
        g.fill    = GridBagConstraints.HORIZONTAL;

        // Row 0: class + room
        classCombo = new JComboBox<>(classes.toArray(new ClassEntity[0]));
        classCombo.setRenderer(new ClassRenderer());
        roomCombo = new JComboBox<>(rooms.toArray(new Room[0]));
        roomCombo.setRenderer(new RoomRenderer());

        addRow(topPanel, g, 0, 0, "Lớp học:", classCombo, "Phòng học:", roomCombo);

        // Row 1: start/end time
        startTimeField = new JTextField("08:00", 8);
        endTimeField   = new JTextField("10:00", 8);
        startTimeField.setToolTipText("HH:mm — ví dụ 08:00");
        endTimeField  .setToolTipText("HH:mm — ví dụ 10:00");
        addRow(topPanel, g, 1, 0, "Giờ bắt đầu:", startTimeField, "Giờ kết thúc:", endTimeField);

        // Row 2: from–to date
        fromDatePicker = new DatePickerField(LocalDate.now());
        toDatePicker   = new DatePickerField(LocalDate.now().plusMonths(3));
        addRow(topPanel, g, 2, 0, "Từ ngày:", fromDatePicker, "Đến ngày:", toDatePicker);

        // Row 3: day-of-week checkboxes
        g.gridx = 0; g.gridy = 3; g.gridwidth = 1;
        topPanel.add(new JLabel("Các thứ dạy:"), g);

        JPanel dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        dayPanel.setOpaque(false);
        for (int i = 0; i < 7; i++) {
            dayCheckBoxes[i] = new JCheckBox(DAY_NAMES[i]);
            dayCheckBoxes[i].setFont(UIUtils.DEFAULT_FONT);
            // Default: Mon-Fri checked
            dayCheckBoxes[i].setSelected(i < 5);
            dayPanel.add(dayCheckBoxes[i]);
        }
        g.gridx = 1; g.gridwidth = 3;
        topPanel.add(dayPanel, g);

        // Row 4: preview button
        JButton btnPreview = UIUtils.createSecondaryButton("Xem trước danh sách ngày");
        btnPreview.addActionListener(e -> generatePreview());
        g.gridx = 0; g.gridy = 4; g.gridwidth = 4; g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.CENTER;
        g.insets = new Insets(10, 8, 5, 8);
        topPanel.add(btnPreview, g);

        // ─── CENTER: preview table ───────────────────────────────────────────
        JTable previewTable = new JTable(previewModel);
        previewTable.setRowHeight(26);
        previewTable.setFont(UIUtils.DEFAULT_FONT);
        previewTable.getTableHeader().setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD));
        previewTable.setAutoCreateRowSorter(true);
        previewTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        previewTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        previewTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        previewTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        previewTable.getColumnModel().getColumn(4).setPreferredWidth(140);

        JScrollPane scroll = new JScrollPane(previewTable);
        scroll.setBorder(titledBorder("Danh sách lịch sẽ được tạo (0 ngày)"));
        scroll.setPreferredSize(new Dimension(700, 260));

        // Keep a reference to the scroll border title to update count
        previewTableScroll = scroll;

        // ─── BOTTOM: action buttons ──────────────────────────────────────────
        btnCreate = UIUtils.createPrimaryButton("Tạo tất cả (0 lịch)");
        btnCreate.setEnabled(false);
        btnCreate.addActionListener(e -> doCreate());

        JButton btnCancel = UIUtils.createSecondaryButton("Hủy");
        btnCancel.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.add(btnCancel);
        footer.add(btnCreate);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        setContentPane(root);

        // pre-select class if given
        if (preselectedClass != null) {
            for (int i = 0; i < classCombo.getItemCount(); i++) {
                ClassEntity item = classCombo.getItemAt(i);
                if (item != null && item.getId() == preselectedClass.getId()) {
                    classCombo.setSelectedIndex(i);
                    break;
                }
            }
            if (preselectedClass.getRoom() != null) {
                for (int i = 0; i < roomCombo.getItemCount(); i++) {
                    Room r = roomCombo.getItemAt(i);
                    if (r != null && r.getId() == preselectedClass.getRoom().getId()) {
                        roomCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
            if (preselectedClass.getStartDate() != null) fromDatePicker.setValue(preselectedClass.getStartDate());
            if (preselectedClass.getEndDate() != null) toDatePicker.setValue(preselectedClass.getEndDate());
        }
    }

    private JScrollPane previewTableScroll;

    // ─── Helper: add two label+field pairs on one row ──────────────────────

    private void addRow(JPanel panel, GridBagConstraints g, int row, int startCol,
                        String label1, JComponent field1, String label2, JComponent field2) {
        g.gridy = row; g.gridwidth = 1; g.fill = GridBagConstraints.HORIZONTAL; g.anchor = GridBagConstraints.WEST;
        g.gridx = startCol;
        panel.add(new JLabel(label1), g);
        g.gridx = startCol + 1; g.weightx = 1;
        panel.add(field1, g);
        g.gridx = startCol + 2; g.weightx = 0;
        panel.add(new JLabel(label2), g);
        g.gridx = startCol + 3; g.weightx = 1;
        panel.add(field2, g);
        g.weightx = 0;
    }

    // ─── Generate preview ───────────────────────────────────────────────────

    private void generatePreview() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to   = toDatePicker  .getValue();

        if (!to.isAfter(from) && !to.isEqual(from)) {
            showError("Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.");
            return;
        }
        if (to.minusDays(from.toEpochDay()).toEpochDay() > 730) {
            showError("Khoảng thời gian tối đa là 2 năm (730 ngày).");
            return;
        }

        List<DayOfWeek> selectedDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            if (dayCheckBoxes[i].isSelected()) selectedDays.add(DAYS[i]);
        }
        if (selectedDays.isEmpty()) {
            showError("Vui lòng chọn ít nhất một thứ trong tuần.");
            return;
        }

        ClassEntity cls  = (ClassEntity) classCombo.getSelectedItem();
        Room room        = (Room) roomCombo.getSelectedItem();
        if (cls == null || room == null) {
            showError("Vui lòng chọn lớp học và phòng học.");
            return;
        }

        try {
            LocalTime startTime = parseTime(startTimeField.getText());
            LocalTime endTime   = parseTime(endTimeField.getText());
            if (!startTime.isBefore(endTime)) {
                showError("Giờ bắt đầu phải trước giờ kết thúc.");
                return;
            }
        } catch (DateTimeParseException ex) {
            showError("Giờ không hợp lệ. Định dạng: HH:mm (ví dụ 08:00)");
            return;
        }

        // Build list of dates
        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            if (selectedDays.contains(cursor.getDayOfWeek())) {
                dates.add(cursor);
            }
            cursor = cursor.plusDays(1);
        }

        previewModel.setData(dates, cls, room,
                parseTime(startTimeField.getText()), parseTime(endTimeField.getText()));

        // Update titles
        String title = "Danh sách lịch sẽ được tạo (" + dates.size() + " ngày)";
        previewTableScroll.setBorder(titledBorder(title));
        previewTableScroll.revalidate();

        btnCreate.setText("Tạo tất cả (" + dates.size() + " lịch)");
        btnCreate.setEnabled(!dates.isEmpty());
    }

    // ─── Persist all schedules ──────────────────────────────────────────────

    private void doCreate() {
        List<PreviewRow> rows = previewModel.getRows();
        if (rows.isEmpty()) {
            showError("Chưa có lịch nào để tạo. Hãy nhấn \"Xem trước\" trước.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tạo " + rows.size() + " lịch học?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int created = 0;
        int failed  = 0;
        StringBuilder errors = new StringBuilder();

        for (PreviewRow row : rows) {
            try {
                Schedule sc = new Schedule();
                sc.setClassEntity(row.classEntity);
                sc.setRoom(row.room);
                sc.setStudyDate(row.date);
                sc.setStartTime(row.startTime);
                sc.setEndTime(row.endTime);
                serviceManager.getScheduleService().createSchedule(sc);
                created++;
            } catch (Exception ex) {
                failed++;
                if (errors.length() < 400) {
                    errors.append("• ").append(DISP_FMT.format(row.date)).append(": ").append(ex.getMessage()).append("\n");
                }
            }
        }

        if (failed == 0) {
            JOptionPane.showMessageDialog(this, "Tạo thành công " + created + " lịch học!");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Tạo " + created + " lịch thành công.\n" + failed + " lịch thất bại:\n" + errors,
                    "Kết quả", JOptionPane.WARNING_MESSAGE);
        }

        saved = created > 0;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    // ─── Utils ──────────────────────────────────────────────────────────────

    private List<ClassEntity> loadClasses() {
        try {
            return serviceManager.getClassService().findAll().stream()
                    .sorted(Comparator.comparing(ClassEntity::getClassName,
                            Comparator.nullsLast(String::compareToIgnoreCase)))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<Room> loadRooms() {
        try {
            return serviceManager.getRoomService().findAll().stream()
                    .filter(r -> r.getStatus() == Room.Status.Active)
                    .sorted(Comparator.comparing(Room::getRoomName,
                            Comparator.nullsLast(String::compareToIgnoreCase)))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private LocalTime parseTime(String raw) {
        String t = raw.trim();
        if (t.matches("^\\d{2}:\\d{2}$")) t = t + ":00";
        return LocalTime.parse(t);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private static TitledBorder titledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 13f),
                UIUtils.PRIMARY_COLOR);
    }

    // ─── Preview Table Model ─────────────────────────────────────────────────

    private static class PreviewRow {
        final LocalDate   date;
        final ClassEntity classEntity;
        final Room        room;
        final LocalTime   startTime;
        final LocalTime   endTime;

        PreviewRow(LocalDate date, ClassEntity classEntity, Room room,
                   LocalTime startTime, LocalTime endTime) {
            this.date        = date;
            this.classEntity = classEntity;
            this.room        = room;
            this.startTime   = startTime;
            this.endTime     = endTime;
        }
    }

    private static class PreviewTableModel extends AbstractTableModel {
        private static final String[] COLS = {"#", "Ngày học", "Thứ", "Lớp học", "Phòng"};
        private final List<PreviewRow> rows = new ArrayList<>();

        void setData(List<LocalDate> dates, ClassEntity cls, Room room,
                     LocalTime startTime, LocalTime endTime) {
            rows.clear();
            for (LocalDate d : dates) {
                rows.add(new PreviewRow(d, cls, room, startTime, endTime));
            }
            fireTableDataChanged();
        }

        List<PreviewRow> getRows() {
            return rows;
        }

        @Override public int getRowCount()    { return rows.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int col) { return COLS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            PreviewRow r = rows.get(row);
            return switch (col) {
                case 0 -> row + 1;
                case 1 -> DISP_FMT.format(r.date);
                case 2 -> formatDay(r.date.getDayOfWeek());
                case 3 -> r.classEntity != null ? r.classEntity.getClassName() : "—";
                case 4 -> r.room != null ? r.room.getRoomName() : "—";
                default -> "";
            };
        }

        private String formatDay(DayOfWeek dow) {
            return switch (dow) {
                case MONDAY    -> "Thứ 2";
                case TUESDAY   -> "Thứ 3";
                case WEDNESDAY -> "Thứ 4";
                case THURSDAY  -> "Thứ 5";
                case FRIDAY    -> "Thứ 6";
                case SATURDAY  -> "Thứ 7";
                case SUNDAY    -> "Chủ nhật";
            };
        }
    }

    // ─── Renderers ──────────────────────────────────────────────────────────

    private static class ClassRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ClassEntity c) {
                String courseName = c.getCourse() != null ? " | " + c.getCourse().getCourseName() : "";
                setText("[#" + c.getId() + "] " + c.getClassName() + courseName);
            }
            return this;
        }
    }

    private static class RoomRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Room r) setText(r.getRoomName());
            return this;
        }
    }
}
