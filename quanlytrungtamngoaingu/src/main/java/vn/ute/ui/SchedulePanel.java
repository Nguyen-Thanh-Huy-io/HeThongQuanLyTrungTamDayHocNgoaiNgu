package vn.ute.ui;

import vn.ute.model.ClassEntity;
import vn.ute.model.Schedule;
import vn.ute.service.ServiceManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class SchedulePanel extends BasePanel<Schedule> {
    private static final String[] SLOT_LABELS = {"Sáng", "Chiều", "Tối"};
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi-VN");

    private final ServiceManager serviceManager;
    private final Supplier<List<Schedule>> dataLoader;
    private final JPanel timetableGrid;
    private final JLabel weekLabel;
    private final JLabel hintLabel;
    private final JLabel detailLabel;

    private List<Schedule> currentSchedules = new ArrayList<>();
    private Schedule selectedSchedule;
    private LocalDate weekReference = LocalDate.now();
    private final Map<Long, JPanel> scheduleCards = new HashMap<>();

    public SchedulePanel(ServiceManager serviceManager, Supplier<List<Schedule>> dataLoader) {
        this(serviceManager, dataLoader, new ScheduleTableModel());
    }

    public SchedulePanel(ServiceManager serviceManager, Supplier<List<Schedule>> dataLoader, AbstractTableModel model) {
        super(model);
        this.serviceManager = serviceManager;
        this.dataLoader = dataLoader;
        this.weekLabel = new JLabel();
        this.hintLabel = new JLabel("Chọn một thẻ lịch học để xem chi tiết hoặc thao tác sửa/xóa.");
        this.detailLabel = new JLabel(" ");
        this.timetableGrid = new JPanel(new GridBagLayout());

        btnAdd.setText("Tạo lịch");
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        setMainContent(buildTimetableContent());
        reloadData();
    }

    @Override
    public void reloadData() {
        new Thread(() -> {
            List<Schedule> schedules = dataLoader.get();
            List<Schedule> normalized = schedules == null ? new ArrayList<>() : new ArrayList<>(schedules);
            normalized.sort(Comparator
                    .comparing(Schedule::getStudyDate, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Schedule::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));
            SwingUtilities.invokeLater(() -> {
                currentSchedules = normalized;
                ((GenericTableModel<Schedule>) tableModel).setData(normalized);
                if (selectedSchedule != null) {
                    selectedSchedule = currentSchedules.stream()
                            .filter(item -> item.getId() == selectedSchedule.getId())
                            .findFirst()
                            .orElse(null);
                }
                renderTimetable();
            });
        }).start();
    }

    @Override
    protected void onAdd() {
        boolean saved = new ScheduleDialog(SwingUtilities.getWindowAncestor(this), serviceManager, null, null).open();
        if (saved) {
            JOptionPane.showMessageDialog(this, "Tạo lịch học thành công.");
            reloadData();
        }
    }

    public void openCreateForClass(ClassEntity classEntity) {
        boolean saved = new ScheduleDialog(SwingUtilities.getWindowAncestor(this), serviceManager, null, classEntity).open();
        if (saved) {
            JOptionPane.showMessageDialog(this, "Tạo lịch học thành công.");
            reloadData();
        }
    }

    @Override
    protected void onEdit() {
        if (selectedSchedule == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lịch học cần chỉnh sửa.");
            return;
        }

        boolean saved = new ScheduleDialog(SwingUtilities.getWindowAncestor(this), serviceManager, selectedSchedule, null).open();
        if (saved) {
            JOptionPane.showMessageDialog(this, "Cập nhật lịch học thành công.");
            reloadData();
        }
    }

    @Override
    protected void onDelete() {
        if (selectedSchedule == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lịch học cần xóa.");
            return;
        }

        if (selectedSchedule.getId() <= 0) {
            JOptionPane.showMessageDialog(this, "Không xác định được lịch học cần xóa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa lịch học đã chọn?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            serviceManager.getScheduleService().deleteSchedule(selectedSchedule.getId());
            selectedSchedule = null;
            JOptionPane.showMessageDialog(this, "Xóa lịch học thành công.");
            reloadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Xóa lịch học thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Component buildTimetableContent() {
        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setOpaque(false);

        JPanel navigation = new JPanel(new BorderLayout());
        navigation.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        JButton prevWeekButton = UIUtils.createSecondaryButton("Tuan truoc");
        JButton currentWeekButton = UIUtils.createSecondaryButton("Tuan nay");
        JButton nextWeekButton = UIUtils.createSecondaryButton("Tuan sau");
        prevWeekButton.setPreferredSize(new Dimension(108, 34));
        currentWeekButton.setPreferredSize(new Dimension(100, 34));
        nextWeekButton.setPreferredSize(new Dimension(108, 34));

        prevWeekButton.addActionListener(e -> {
            weekReference = weekReference.minusWeeks(1);
            renderTimetable();
        });
        currentWeekButton.addActionListener(e -> {
            weekReference = LocalDate.now();
            renderTimetable();
        });
        nextWeekButton.addActionListener(e -> {
            weekReference = weekReference.plusWeeks(1);
            renderTimetable();
        });

        leftActions.add(prevWeekButton);
        leftActions.add(currentWeekButton);
        leftActions.add(nextWeekButton);

        weekLabel.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 18f));
        weekLabel.setForeground(new Color(27, 54, 93));

        hintLabel.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.PLAIN, 13f));
        hintLabel.setForeground(new Color(110, 123, 145));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(weekLabel);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(hintLabel);

        navigation.add(leftActions, BorderLayout.WEST);
        navigation.add(titleBox, BorderLayout.CENTER);

        timetableGrid.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(timetableGrid);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(242, 244, 247));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(true);
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 220, 228)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        detailLabel.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.PLAIN, 13f));
        detailLabel.setForeground(new Color(61, 79, 99));
        footer.add(detailLabel, BorderLayout.CENTER);

        container.add(navigation, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(footer, BorderLayout.SOUTH);
        return container;
    }

    private void renderTimetable() {
        LocalDate weekStart = weekReference.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        weekLabel.setText("Lich hoc tuan " + DATE_FORMAT.format(weekStart) + " - " + DATE_FORMAT.format(weekEnd));

        timetableGrid.removeAll();
        scheduleCards.clear();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 1, 1, 1);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 0;

        addHeaderCell(gbc, 0, 0, "");
        for (int column = 0; column < SLOT_LABELS.length; column++) {
            addHeaderCell(gbc, column + 1, 0, SLOT_LABELS[column]);
        }

        LocalDate today = LocalDate.now();
        for (int row = 0; row < 7; row++) {
            LocalDate day = weekStart.plusDays(row);
            addDayLabelCell(gbc, day, row + 1, day.equals(today));
            for (TimeSlot slot : TimeSlot.values()) {
                addScheduleCell(gbc, row + 1, slot, day, day.equals(today));
            }
        }

        timetableGrid.revalidate();
        timetableGrid.repaint();
        updateSelectionDetail();
    }

    private void addHeaderCell(GridBagConstraints gbc, int gridx, int gridy, String text) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.weightx = gridx == 0 ? 0.22 : 0.78;
        gbc.weighty = 0;

        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(19, 83, 127));
        label.setForeground(Color.WHITE);
        label.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 15f));
        label.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
        timetableGrid.add(label, gbc);
    }

    private void addDayLabelCell(GridBagConstraints gbc, LocalDate day, int row, boolean isToday) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.22;
        gbc.weighty = 1;

        JPanel dayPanel = new JPanel(new BorderLayout());
        dayPanel.setOpaque(true);
        dayPanel.setBackground(isToday ? new Color(242, 231, 73) : new Color(235, 195, 148));
        dayPanel.setBorder(BorderFactory.createLineBorder(new Color(176, 167, 157)));
        dayPanel.setPreferredSize(new Dimension(170, 132));

        String label = formatVietnameseDay(day);
        JLabel dayLabel = new JLabel(isToday ? "<html><div style='text-align:center;'>" + label + "<br/>(Hom nay)</div></html>" : label, JLabel.CENTER);
        dayLabel.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 15f));
        dayLabel.setForeground(isToday ? new Color(198, 36, 32) : Color.BLACK);
        dayPanel.add(dayLabel, BorderLayout.CENTER);

        timetableGrid.add(dayPanel, gbc);
    }

    private void addScheduleCell(GridBagConstraints gbc, int row, TimeSlot slot, LocalDate day, boolean isToday) {
        gbc.gridx = slot.ordinal() + 1;
        gbc.gridy = row;
        gbc.weightx = 0.78;
        gbc.weighty = 1;

        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setOpaque(true);
        cell.setBackground(isToday ? new Color(245, 247, 250) : new Color(236, 237, 239));
        cell.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        cell.setPreferredSize(new Dimension(320, 132));

        List<Schedule> schedules = getSchedulesForCell(day, slot);
        if (schedules.isEmpty()) {
            cell.add(Box.createVerticalGlue());
            JLabel empty = new JLabel(" ", JLabel.CENTER);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            cell.add(empty);
            cell.add(Box.createVerticalGlue());
        } else {
            for (Schedule schedule : schedules) {
                JPanel card = createScheduleCard(schedule);
                scheduleCards.put(schedule.getId(), card);
                cell.add(card);
                cell.add(Box.createVerticalStrut(8));
            }
        }

        timetableGrid.add(cell, gbc);
    }

    private List<Schedule> getSchedulesForCell(LocalDate day, TimeSlot slot) {
        return currentSchedules.stream()
                .filter(schedule -> day.equals(schedule.getStudyDate()))
                .filter(schedule -> TimeSlot.from(schedule) == slot)
                .sorted(Comparator.comparing(Schedule::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private JPanel createScheduleCard(Schedule schedule) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(new Color(165, 193, 214));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 216, 224), 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        String courseName = schedule.getClassEntity() != null && schedule.getClassEntity().getCourse() != null
                ? schedule.getClassEntity().getCourse().getCourseName()
                : "Chua xac dinh";
        String className = schedule.getClassEntity() != null ? schedule.getClassEntity().getClassName() : "Chua xac dinh";
        String teacherName = schedule.getClassEntity() != null && schedule.getClassEntity().getTeacher() != null
                ? schedule.getClassEntity().getTeacher().getFullName()
                : "Chua phan cong";
        String roomName = schedule.getRoom() != null ? schedule.getRoom().getRoomName() : "Chua xac dinh";
        String timeText = formatTimeRange(schedule);

        JLabel title = new JLabel("Mon: " + courseName, JLabel.CENTER);
        title.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 15f));
        title.setForeground(new Color(224, 43, 37));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(createDetailLabel("Lop: " + className, new Color(75, 96, 114)));
        card.add(createDetailLabel("Tiet: " + timeText, new Color(75, 96, 114)));
        card.add(createDetailLabel("Phong: " + roomName, new Color(36, 135, 51)));
        card.add(createDetailLabel("GV: " + teacherName, new Color(39, 76, 227)));

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                setSelectedSchedule(schedule);
                if (e.getClickCount() == 2 && btnEdit.isVisible() && btnEdit.isEnabled()) {
                    Window owner = SwingUtilities.getWindowAncestor(SchedulePanel.this);
                    boolean saved = new ScheduleDialog(owner, serviceManager, schedule, null).open();
                    if (saved) {
                        reloadData();
                    }
                }
            }
        });

        return card;
    }

    private JLabel createDetailLabel(String text, Color color) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.PLAIN, 13f));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void setSelectedSchedule(Schedule schedule) {
        selectedSchedule = schedule;
        updateCardSelection();
        updateSelectionDetail();
    }

    private void updateCardSelection() {
        for (Map.Entry<Long, JPanel> entry : scheduleCards.entrySet()) {
            boolean isSelected = selectedSchedule != null && selectedSchedule.getId() == entry.getKey();
            entry.getValue().setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(isSelected ? new Color(8, 84, 186) : new Color(206, 216, 224), isSelected ? 3 : 2),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
        }
    }

    private void updateSelectionDetail() {
        if (selectedSchedule == null) {
            detailLabel.setText("Tuan hien tai hien thi theo dinh dang sang, chieu, toi. Bam vao mot the de xem chi tiet.");
            return;
        }

        String className = selectedSchedule.getClassEntity() != null ? selectedSchedule.getClassEntity().getClassName() : "Chua xac dinh";
        String roomName = selectedSchedule.getRoom() != null ? selectedSchedule.getRoom().getRoomName() : "Chua xac dinh";
        String teacherName = selectedSchedule.getClassEntity() != null && selectedSchedule.getClassEntity().getTeacher() != null
                ? selectedSchedule.getClassEntity().getTeacher().getFullName()
                : "Chua phan cong";

        detailLabel.setText("Dang chon: " + className + " | " + DATE_FORMAT.format(selectedSchedule.getStudyDate())
                + " | " + formatTimeRange(selectedSchedule) + " | Phong " + roomName + " | GV: " + teacherName);
    }

    private String formatTimeRange(Schedule schedule) {
        if (schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return "Chua xac dinh";
        }
        return schedule.getStartTime() + " - " + schedule.getEndTime();
    }

    private String formatVietnameseDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "Thu 2";
            case TUESDAY -> "Thu 3";
            case WEDNESDAY -> "Thu 4";
            case THURSDAY -> "Thu 5";
            case FRIDAY -> "Thu 6";
            case SATURDAY -> "Thu 7";
            case SUNDAY -> "Chu nhat";
        };
    }

    private enum TimeSlot {
        MORNING,
        AFTERNOON,
        EVENING;

        private static TimeSlot from(Schedule schedule) {
            if (schedule == null || schedule.getStartTime() == null) {
                return MORNING;
            }

            int hour = schedule.getStartTime().getHour();
            if (hour < 12) {
                return MORNING;
            }
            if (hour < 18) {
                return AFTERNOON;
            }
            return EVENING;
        }
    }
}
