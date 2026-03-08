package vn.ute.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

public class GenericPanel<T> extends JPanel {
    private GenericTableModel<T> tableModel;
    private Supplier<List<T>> dataLoader;

    public GenericPanel(String[] cols, String[] fields, Supplier<List<T>> dataLoader) {
        this.dataLoader = dataLoader;
        this.tableModel = new GenericTableModel<>(cols, fields);
        
        setLayout(new BorderLayout());
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Toolbar gọn nhẹ
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> reload());
        toolbar.add(btnRefresh);
        add(toolbar, BorderLayout.NORTH);
        
        reload();
    }

    public void reload() {
        new Thread(() -> {
            List<T> list = dataLoader.get();
            SwingUtilities.invokeLater(() -> tableModel.setData(list));
        }).start();
    }
}