package vn.ute.ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

import vn.ute.ui.UIUtils;

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
        JOptionPane.showMessageDialog(this, "Chức năng xóa chưa được triển khai");
    }
}