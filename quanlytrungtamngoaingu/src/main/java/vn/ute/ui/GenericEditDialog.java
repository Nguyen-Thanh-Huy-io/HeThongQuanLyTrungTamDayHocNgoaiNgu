package vn.ute.ui;

import jakarta.persistence.ManyToOne;
import vn.ute.db.TransactionManager;
import vn.ute.model.Student;
import vn.ute.model.Teacher;
import vn.ute.service.ServiceManager;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A very simple dialog that builds a form from a bean's fields using reflection.
 * When the user presses OK it does not actually save anything; you should
 * override onSave() or handle the returned values in the caller.
 */
public class GenericEditDialog<T> extends JDialog {
    private final Class<T> type;
    private final T instance;
    private final List<Field> editableFields = new ArrayList<>();
    private final List<JTextField> inputs = new ArrayList<>();
    private final Map<Field, JTextField> relationIdInputs = new HashMap<>();

    public GenericEditDialog(Window owner, Class<T> type) {
        this(owner, type, null);
    }

    public GenericEditDialog(Window owner, Class<T> type, T instance) {
        super(owner, "Chỉnh sửa " + type.getSimpleName(), ModalityType.APPLICATION_MODAL);
        this.type = type;
        this.instance = instance;
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;

        for (Field f : type.getDeclaredFields()) {
            f.setAccessible(true);
            if (f.getName().equals("id")) {
                continue;
            }

            if (f.isAnnotationPresent(ManyToOne.class)) {
                panel.add(new JLabel(f.getName() + "Id:"), gbc);
                gbc.gridx = 1;
                JTextField tf = new JTextField(20);
                if (instance != null) {
                    try {
                        Object relation = f.get(instance);
                        if (relation != null) {
                            Field relIdField = relation.getClass().getDeclaredField("id");
                            relIdField.setAccessible(true);
                            Object relId = relIdField.get(relation);
                            tf.setText(relId != null ? relId.toString() : "");
                        }
                    } catch (Exception ignored) {
                    }
                }
                panel.add(tf, gbc);
                relationIdInputs.put(f, tf);

                gbc.gridx = 0;
                gbc.gridy++;
                continue;
            }

            // Skip complex relation fields in generic dialog.
            if (!isSimpleField(f)) {
                continue;
            }

            panel.add(new JLabel(f.getName() + ":"), gbc);
            gbc.gridx = 1;
            JTextField tf = new JTextField(20);
            if (instance != null) {
                try {
                    Object value = f.get(instance);
                    tf.setText(value != null ? value.toString() : "");
                } catch (Exception ignored) {
                }
            }
            panel.add(tf, gbc);
            editableFields.add(f);
            inputs.add(tf);

            gbc.gridx = 0;
            gbc.gridy++;
        }

        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(e -> {
            if (onSave()) {
                dispose();
            }
        });
        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dispose());

        JPanel south = new JPanel();
        south.add(btnOk);
        south.add(btnCancel);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);
    }

    /**
     * Called when OK is pressed.  Default implementation does nothing; override
     * or attach a listener in the caller to persist the data.
     */
    protected boolean onSave() {
        try {
            T target = (instance != null) ? instance : type.getDeclaredConstructor().newInstance();
            for (int i = 0; i < editableFields.size(); i++) {
                Field field = editableFields.get(i);
                String raw = inputs.get(i).getText().trim();
                Object converted = convertValue(field.getType(), raw);
                field.setAccessible(true);
                field.set(target, converted);
            }

            Map<Field, Long> relationIds = readRelationIds();
            saveEntity(target, relationIds);
            JOptionPane.showMessageDialog(this, "Lưu dữ liệu thành công!");
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lưu dữ liệu thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean isSimpleField(Field f) {
        Class<?> t = f.getType();
        return t.isPrimitive()
                || t == String.class
                || t == Integer.class
                || t == Long.class
                || t == Double.class
                || t == Boolean.class
                || t == BigDecimal.class
                || t == LocalDate.class
                || t.isEnum();
    }

    private Object convertValue(Class<?> type, String raw) {
        if (type == String.class) {
            return raw;
        }
        if (raw.isEmpty()) {
            if (type.isPrimitive()) {
                if (type == boolean.class) return false;
                if (type == int.class) return 0;
                if (type == long.class) return 0L;
                if (type == double.class) return 0d;
            }
            return null;
        }
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(raw);
        }
        if (type == long.class || type == Long.class) {
            return Long.parseLong(raw);
        }
        if (type == double.class || type == Double.class) {
            return Double.parseDouble(raw);
        }
        if (type == boolean.class || type == Boolean.class) {
            return raw.equalsIgnoreCase("true") || raw.equals("1") || raw.equalsIgnoreCase("yes");
        }
        if (type == BigDecimal.class) {
            return new BigDecimal(raw);
        }
        if (type == LocalDate.class) {
            return LocalDate.parse(raw);
        }
        if (type.isEnum()) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Class<Enum> enumType = (Class<Enum>) type;
            for (Object constant : enumType.getEnumConstants()) {
                Enum<?> enumConstant = (Enum<?>) constant;
                if (enumConstant.name().equalsIgnoreCase(raw)) {
                    return enumConstant;
                }
            }
            throw new IllegalArgumentException("Giá trị enum không hợp lệ: " + raw);
        }
        return null;
    }

    private Map<Field, Long> readRelationIds() {
        Map<Field, Long> relationIds = new HashMap<>();
        for (Map.Entry<Field, JTextField> entry : relationIdInputs.entrySet()) {
            String raw = entry.getValue().getText().trim();
            if (!raw.isEmpty()) {
                relationIds.put(entry.getKey(), Long.parseLong(raw));
            }
        }
        return relationIds;
    }

    private void saveEntity(T target, Map<Field, Long> relationIds) throws Exception {
        boolean isNew = isNewEntity(target);
        ServiceManager sm = ServiceManager.getInstance();

        if (target instanceof Teacher teacher) {
            if (isNew) {
                sm.getTeacherService().createTeacher(teacher);
            } else {
                sm.getTeacherService().updateTeacher(teacher);
            }
            return;
        }

        if (target instanceof Student student) {
            if (isNew) {
                sm.getStudentService().createStudent(student);
            } else {
                sm.getStudentService().updateStudent(student);
            }
            return;
        }

        // Fallback generic persistence for other entities.
        TransactionManager.executeInTransaction(em -> {
            for (Map.Entry<Field, Long> relationEntry : relationIds.entrySet()) {
                Field relationField = relationEntry.getKey();
                Long relationId = relationEntry.getValue();
                Object reference = em.getReference(relationField.getType(), relationId);
                relationField.setAccessible(true);
                relationField.set(target, reference);
            }

            if (isNew) {
                em.persist(target);
            } else {
                em.merge(target);
            }
            return null;
        });
    }

    private boolean isNewEntity(T target) {
        try {
            Field idField = target.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object id = idField.get(target);
            if (id == null) return true;
            if (id instanceof Number n) return n.longValue() == 0L;
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
