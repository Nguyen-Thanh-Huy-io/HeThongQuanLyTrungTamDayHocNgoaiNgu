package vn.ute.ui;

import vn.ute.db.TransactionManager;
import vn.ute.model.Staff;
import vn.ute.model.Student;
import vn.ute.model.Teacher;
import vn.ute.service.ServiceManager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Supplier;

public class GenericPanel<T> extends BasePanel<T> {
    private Supplier<List<T>> dataLoader;
    private final Class<T> entityType;

    /**
     * Construct using column/field arrays.  The caller must supply the entity class
     * so that the "Thêm/Sửa" dialog can be built.
     */
    public GenericPanel(Class<T> entityType, String[] cols, String[] fields, Supplier<List<T>> dataLoader) {
        super(new GenericTableModel<>(cols, fields));
        this.entityType = entityType;
        this.dataLoader = dataLoader;
        reloadData();
    }

    /**
     * Construct with a prebuilt table model (usually a subclass of GenericTableModel).
     */
    public GenericPanel(Class<T> entityType, AbstractTableModel model, Supplier<List<T>> dataLoader) {
        super(model);
        this.entityType = entityType;
        this.dataLoader = dataLoader;
        reloadData();
    }

    @Override
    public void reloadData() {
        // tải lại về luồng khác để không chặn UI
        new Thread(() -> {
            List<T> list = dataLoader.get();
            SwingUtilities.invokeLater(() -> ((GenericTableModel<T>) tableModel).setData(list));
        }).start();
    }

    @Override
    protected void onAdd() {
        // open a blank edit dialog for the entity type
        new GenericEditDialog<>(SwingUtilities.getWindowAncestor(this), entityType)
                .setVisible(true);
        reloadData();
    }

    @Override
    protected void onEdit() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            @SuppressWarnings("unchecked")
            T item = ((GenericTableModel<T>) tableModel).getRow(row);
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

        @SuppressWarnings("unchecked")
        T item = ((GenericTableModel<T>) tableModel).getRow(row);
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
            deleteByType(id);
            JOptionPane.showMessageDialog(this, "Xóa dữ liệu thành công!");
            reloadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Xóa dữ liệu thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Long extractEntityId(T item) {
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

    private void deleteByType(Long id) throws Exception {
        if (entityType == Student.class) {
            ServiceManager.getInstance().getStudentService().deleteStudent(id);
            return;
        }

        if (entityType == Teacher.class) {
            ServiceManager.getInstance().getTeacherService().deleteTeacher(id);
            return;
        }

        if (entityType == Staff.class) {
            ServiceManager.getInstance().getStaffService().deleteStaff(id);
            return;
        }

        TransactionManager.executeInTransaction(em -> {
            Object entity = em.find(entityType, id);
            if (entity == null) {
                throw new Exception("Không tìm thấy bản ghi cần xóa (ID=" + id + ").");
            }
            em.remove(entity);
            return null;
        });
    }
}