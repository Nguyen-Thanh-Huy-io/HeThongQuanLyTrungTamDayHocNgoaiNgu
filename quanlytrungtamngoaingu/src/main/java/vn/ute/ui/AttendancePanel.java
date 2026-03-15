package vn.ute.ui;

import vn.ute.db.TransactionManager;
import vn.ute.model.Attendance;
import vn.ute.service.ServiceManager;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Attendance panel with class filter to avoid mixing records when users belong to multiple classes.
 */
public class AttendancePanel extends BasePanel<Attendance> {
    private static final String ALL_CLASSES_LABEL = "Tất cả lớp";

    private final Class<Attendance> entityType = Attendance.class;
    private final Supplier<List<Attendance>> dataLoader;
    private final List<Attendance> sourceData = new ArrayList<>();

    private final JComboBox<ClassFilterOption> classFilterCombo = new JComboBox<>();

    public AttendancePanel(Supplier<List<Attendance>> dataLoader) {
        super(new AttendanceTableModel());
        this.dataLoader = dataLoader;
        initFilterToolbar();
        reloadData();
    }

    private void initFilterToolbar() {
        toolbar.removeAll();

        JLabel filterLabel = new JLabel("Lọc theo lớp:");
        classFilterCombo.setPrototypeDisplayValue(new ClassFilterOption(-1L, "#999 - Lớp mẫu | Khóa học mẫu"));
        classFilterCombo.addActionListener(e -> applyFilter());

        JButton clearFilterButton = UIUtils.createSecondaryButton("Bỏ lọc");
        clearFilterButton.setPreferredSize(new java.awt.Dimension(95, 36));
        clearFilterButton.addActionListener(e -> classFilterCombo.setSelectedIndex(0));

        toolbar.add(filterLabel);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(classFilterCombo);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(clearFilterButton);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(btnAdd);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(btnEdit);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(btnDelete);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(btnRefresh);

        toolbar.revalidate();
        toolbar.repaint();
    }

    @Override
    public void reloadData() {
        new Thread(() -> {
            List<Attendance> list = dataLoader.get();
            SwingUtilities.invokeLater(() -> {
                sourceData.clear();
                if (list != null) {
                    sourceData.addAll(list);
                }
                refreshClassFilterOptions();
                applyFilter();
            });
        }).start();
    }

    private void refreshClassFilterOptions() {
        Object previousSelection = classFilterCombo.getSelectedItem();
        Long previousClassId = null;
        if (previousSelection instanceof ClassFilterOption option) {
            previousClassId = option.classId;
        }

        Map<Long, ClassFilterOption> options = new LinkedHashMap<>();
        options.put(0L, new ClassFilterOption(0L, ALL_CLASSES_LABEL));

        sourceData.stream()
                .filter(a -> a.getClassEntity() != null)
                .sorted(Comparator
                        .comparing((Attendance a) -> a.getClassEntity().getClassName(), Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(a -> a.getClassEntity().getId()))
                .forEach(a -> {
                    Long classId = a.getClassEntity().getId();
                    options.putIfAbsent(classId, new ClassFilterOption(classId, buildClassLabel(a)));
                });

        classFilterCombo.removeAllItems();
        options.values().forEach(classFilterCombo::addItem);

        int targetIndex = 0;
        if (previousClassId != null) {
            for (int i = 0; i < classFilterCombo.getItemCount(); i++) {
                ClassFilterOption option = classFilterCombo.getItemAt(i);
                if (option != null && option.classId.equals(previousClassId)) {
                    targetIndex = i;
                    break;
                }
            }
        }
        classFilterCombo.setSelectedIndex(targetIndex);
    }

    private String buildClassLabel(Attendance attendance) {
        String className = attendance.getClassEntity().getClassName() != null
                ? attendance.getClassEntity().getClassName()
                : "Chưa rõ tên lớp";
        String courseName = attendance.getClassEntity().getCourse() != null
                && attendance.getClassEntity().getCourse().getCourseName() != null
                ? attendance.getClassEntity().getCourse().getCourseName()
                : "Chưa rõ khóa học";

        return "#" + attendance.getClassEntity().getId() + " - " + className + " | " + courseName;
    }

    private void applyFilter() {
        ClassFilterOption selected = (ClassFilterOption) classFilterCombo.getSelectedItem();
        Long selectedClassId = selected != null ? selected.classId : 0L;

        List<Attendance> filtered = sourceData.stream()
                .filter(a -> selectedClassId == 0L
                        || (a.getClassEntity() != null && a.getClassEntity().getId() == selectedClassId))
                .toList();

        ((GenericTableModel<Attendance>) tableModel).setData(filtered);
    }

    @Override
    protected void onAdd() {
        new GenericEditDialog<>(SwingUtilities.getWindowAncestor(this), entityType)
                .setVisible(true);
        reloadData();
    }

    @Override
    protected void onEdit() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            Attendance item = ((GenericTableModel<Attendance>) tableModel).getRow(row);
            new GenericEditDialog<>(SwingUtilities.getWindowAncestor(this), entityType, item)
                    .setVisible(true);
            reloadData();
        }
    }

    @Override
    protected void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng để xóa.");
            return;
        }

        Attendance item = ((GenericTableModel<Attendance>) tableModel).getRow(row);
        Long id = extractEntityId(item);
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Không xác định được ID để xóa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa bản ghi đã chọn?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            TransactionManager.executeInTransaction(em -> {
                Object entity = em.find(entityType, id);
                if (entity == null) {
                    throw new Exception("Không tìm thấy bản ghi cần xóa (ID=" + id + ").");
                }
                em.remove(entity);
                return null;
            });
            JOptionPane.showMessageDialog(this, "Xóa dữ liệu thành công!");
            reloadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Xóa dữ liệu thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Long extractEntityId(Attendance item) {
        try {
            Field idField = item.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object value = idField.get(item);
            if (value instanceof Number n) {
                return n.longValue();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static class ClassFilterOption {
        private final Long classId;
        private final String label;

        private ClassFilterOption(Long classId, String label) {
            this.classId = classId;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
