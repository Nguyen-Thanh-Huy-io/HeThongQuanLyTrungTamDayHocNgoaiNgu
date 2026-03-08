package vn.ute.ui;

import javax.swing.table.AbstractTableModel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GenericTableModel<T> extends AbstractTableModel {
    private final String[] columnNames;
    private final String[] fieldNames;
    private List<T> data = new ArrayList<>();

    public GenericTableModel(String[] columnNames, String[] fieldNames) {
        this.columnNames = columnNames;
        this.fieldNames = fieldNames;
    }

    public void setData(List<T> data) {
        this.data = (data != null) ? data : new ArrayList<>();
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return columnNames.length; }
    @Override public String getColumnName(int col) { return columnNames[col]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        T item = data.get(rowIndex);
        try {
            // Tự động lấy giá trị dựa trên tên biến (field)
            Field field = item.getClass().getDeclaredField(fieldNames[columnIndex]);
            field.setAccessible(true);
            Object value = field.get(item);
            return (value != null) ? value.toString() : "";
        } catch (Exception e) {
            return "N/A";
        }
    }
}