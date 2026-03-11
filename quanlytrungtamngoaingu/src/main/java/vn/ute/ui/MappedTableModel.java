package vn.ute.ui;

import vn.ute.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced TableModel với automatic mapping từ Object relationships
 * Thay vì hiểu Student.id, nó sẽ show Student.fullName
 */
public class MappedTableModel<T> extends GenericTableModel<T> {
    private final List<T> data;
    private final String[] columnNames;
    private final String[] fieldNames;
    private final java.util.function.Function<Object, String>[] mappers;

    @SuppressWarnings("unchecked")
    public MappedTableModel(String[] columnNames, String[] fieldNames, 
                           java.util.function.Function<Object, String>... mappers) {
        super(columnNames, fieldNames);
        this.columnNames = columnNames;
        this.fieldNames = fieldNames;
        this.mappers = mappers;
        this.data = new ArrayList<>();
    }

    @Override
    public void setData(List<T> newData) {
        this.data.clear();
        if (newData != null) {
            this.data.addAll(newData);
        }
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= data.size()) {
            return "";
        }

        T item = data.get(rowIndex);

        try {
            // Nếu có mapper cho cột này, dùng mapper
            if (columnIndex < mappers.length && mappers[columnIndex] != null) {
                return mappers[columnIndex].apply(getFieldValue(item, fieldNames[columnIndex]));
            }

            // Nếu không, dùng value trực tiếp
            Object value = getFieldValue(item, fieldNames[columnIndex]);
            if (value == null) {
                return "Chưa xác định";
            }

            // Nếu là object khác, gọi toString()
            if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                return value;
            }

            // Để trống nếu là object complex không biết xử lý
            return value.toString();

        } catch (Exception e) {
            return "Chưa xác định";
        }
    }

    /**
     * Lấy giá trị từ field (hỗ trợ nested: "student.fullName" -> Student.fullName)
     */
    private Object getFieldValue(T item, String fieldPath) throws Exception {
        String[] parts = fieldPath.split("\\.");
        Object current = item;

        for (String part : parts) {
            if (current == null) {
                return null;
            }

            try {
                java.lang.reflect.Field field = current.getClass().getDeclaredField(part);
                field.setAccessible(true);
                current = field.get(current);
            } catch (NoSuchFieldException e) {
                throw e;
            }
        }

        return current;
    }

    public T getRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < data.size()) {
            return data.get(rowIndex);
        }
        return null;
    }
}
