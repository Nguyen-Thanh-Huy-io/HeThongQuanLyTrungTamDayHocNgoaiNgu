package vn.ute.ui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;

import vn.ute.ui.UIUtils;

public abstract class BasePanel<T> extends JPanel {
    protected JTable table;
    protected AbstractTableModel tableModel;
    protected JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    protected JToolBar toolbar;
    protected JScrollPane tableScrollPane;

    public BasePanel(AbstractTableModel model) {
        this.tableModel = model;
        this.table = new JTable(model);
        setLayout(new BorderLayout(10, 10));
        buildUI();
    }

    private void buildUI() {
        toolbar = new JToolBar();
        UIUtils.styleToolbar(toolbar);

        btnAdd     = UIUtils.createPrimaryButton("Thêm");
        btnEdit    = UIUtils.createSecondaryButton("Sửa");
        btnDelete  = UIUtils.createDangerButton("Xóa");
        btnRefresh = UIUtils.createSecondaryButton("Làm mới");

        // đẩy các nút sang phải
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(btnAdd);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(btnEdit);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(btnDelete);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(btnRefresh);

        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR));
        tableScrollPane.getViewport().setBackground(UIUtils.SURFACE);
        UIUtils.styleTable(table);

        add(toolbar, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);

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

    /**
     * Ẩn nút bấm dựa trên role.
     */
    public void setButtonVisible(String buttonName, boolean visible) {
        switch (buttonName.toLowerCase()) {
            case "add" -> btnAdd.setVisible(visible);
            case "edit" -> btnEdit.setVisible(visible);
            case "delete" -> btnDelete.setVisible(visible);
            case "refresh" -> btnRefresh.setVisible(visible);
        }
    }

    /**
     * Vô hiệu hóa nút bấm (khiến nút không thể click).
     */
    public void setButtonEnabled(String buttonName, boolean enabled) {
        switch (buttonName.toLowerCase()) {
            case "add" -> btnAdd.setEnabled(enabled);
            case "edit" -> btnEdit.setEnabled(enabled);
            case "delete" -> btnDelete.setEnabled(enabled);
            case "refresh" -> btnRefresh.setEnabled(enabled);
        }
    }

    /**
     * Ẩn nhiều nút cùng lúc.
     */
    public void hideButtons(String... buttonNames) {
        for (String name : buttonNames) {
            setButtonVisible(name, false);
        }
    }

    /**
     * Tắt những nút nhất định.
     */
    public void disableButtons(String... buttonNames) {
        for (String name : buttonNames) {
            setButtonEnabled(name, false);
        }
    }

    protected void setMainContent(Component component) {
        if (tableScrollPane != null) {
            remove(tableScrollPane);
        }
        add(component, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}