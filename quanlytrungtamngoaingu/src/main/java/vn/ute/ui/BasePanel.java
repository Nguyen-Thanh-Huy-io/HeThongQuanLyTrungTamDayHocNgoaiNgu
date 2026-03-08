package vn.ute.ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;

public abstract class BasePanel<T> extends JPanel {
    protected JTable table;
    protected AbstractTableModel tableModel;
    protected JButton btnAdd, btnEdit, btnDelete, btnRefresh;

    public BasePanel(AbstractTableModel model) {
        this.tableModel = model;
        this.table = new JTable(model);
        setLayout(new BorderLayout(10, 10));
        buildUI();
    }

    private void buildUI() {
        // Toolbar chứa các nút bấm giống mẫu của thầy
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAdd = new JButton("Thêm");
        btnEdit = new JButton("Sửa");
        btnDelete = new JButton("Xóa");
        btnRefresh = new JButton("Làm mới");

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Gán sự kiện cơ bản
        btnRefresh.addActionListener(e -> reloadData());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
    }

    // Các hàm này sẽ được viết chi tiết ở từng Panel con
    public abstract void reloadData();
    protected abstract void onAdd();
    protected abstract void onEdit();
    protected abstract void onDelete();
}