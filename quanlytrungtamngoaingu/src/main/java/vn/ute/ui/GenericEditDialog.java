package vn.ute.ui;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import vn.ute.db.TransactionManager;
import vn.ute.model.ClassEntity;
import vn.ute.model.Course;
import vn.ute.model.Enrollment;
import vn.ute.model.Invoice;
import vn.ute.model.Room;
import vn.ute.model.Staff;
import vn.ute.model.Student;
import vn.ute.model.Teacher;
import vn.ute.model.UserAccount;
import vn.ute.service.ServiceManager;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A very simple dialog that builds a form from a bean's fields using reflection.
 * When the user presses OK it does not actually save anything; you should
 * override onSave() or handle the returned values in the caller.
 */
public class GenericEditDialog<T> extends JDialog {
    private final Class<T> type;
    private final T instance;
    private final List<FieldInput> editableInputs = new ArrayList<>();
    private final List<RelationFieldInput> relationInputs = new ArrayList<>();

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
                panel.add(new JLabel(f.getName() + ":"), gbc);
                gbc.gridx = 1;
                JComboBox<Object> relationComboBox = createRelationComboBox(f);
                panel.add(relationComboBox, gbc);
                relationInputs.add(new RelationFieldInput(f, relationComboBox));

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
            JComponent input = createInputComponent(f);
            bindExistingValue(f, input);
            panel.add(input, gbc);
            editableInputs.add(new FieldInput(f, input));

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
            for (FieldInput input : editableInputs) {
                Field field = input.field;
                Object converted = readInputValue(field, input.component);
                field.setAccessible(true);
                field.set(target, converted);
            }

            for (RelationFieldInput relationInput : relationInputs) {
                relationInput.field.setAccessible(true);
                relationInput.field.set(target, relationInput.comboBox.getSelectedItem());
            }

            saveEntity(target);
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
                || t == LocalDateTime.class
                || t == LocalTime.class
                || t.isEnum();
    }

    private JComponent createInputComponent(Field field) {
        Class<?> fieldType = field.getType();

        if (fieldType.isEnum()) {
            JComboBox<Object> comboBox = new JComboBox<>(fieldType.getEnumConstants());
            comboBox.setRenderer(new DefaultListCellRenderer());
            return comboBox;
        }

        if (fieldType == boolean.class || fieldType == Boolean.class) {
            return new JCheckBox();
        }

        return new JTextField(20);
    }

    private void bindExistingValue(Field field, JComponent input) {
        if (instance == null) {
            return;
        }

        try {
            Object value = field.get(instance);
            if (input instanceof JTextField textField) {
                textField.setText(value != null ? value.toString() : "");
                return;
            }
            if (input instanceof JComboBox<?> comboBox) {
                comboBox.setSelectedItem(value);
                return;
            }
            if (input instanceof JCheckBox checkBox) {
                checkBox.setSelected(Boolean.TRUE.equals(value));
            }
        } catch (Exception ignored) {
        }
    }

    private Object readInputValue(Field field, JComponent component) {
        if (component instanceof JTextField textField) {
            return convertValue(field.getType(), textField.getText().trim());
        }
        if (component instanceof JComboBox<?> comboBox) {
            return comboBox.getSelectedItem();
        }
        if (component instanceof JCheckBox checkBox) {
            return checkBox.isSelected();
        }
        return null;
    }

    private JComboBox<Object> createRelationComboBox(Field relationField) {
        List<Object> relationItems = loadRelationItems(relationField.getType());
        JComboBox<Object> comboBox = new JComboBox<>();

        if (isNullableRelation(relationField)) {
            comboBox.addItem(null);
        }
        for (Object item : relationItems) {
            comboBox.addItem(item);
        }

        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(buildRelationLabel(value));
                return this;
            }
        });

        if (instance != null) {
            try {
                Object existingRelation = relationField.get(instance);
                selectRelationItem(comboBox, existingRelation);
            } catch (Exception ignored) {
            }
        }

        return comboBox;
    }

    private List<Object> loadRelationItems(Class<?> relationType) {
        try {
            List<Object> items = TransactionManager.executeInTransaction(em ->
                    em.createQuery("select e from " + relationType.getSimpleName() + " e", Object.class).getResultList()
            );
            items.sort(Comparator.comparing(this::buildRelationLabel, String.CASE_INSENSITIVE_ORDER));
            return items;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private boolean isNullableRelation(Field relationField) {
        JoinColumn joinColumn = relationField.getAnnotation(JoinColumn.class);
        return joinColumn == null || joinColumn.nullable();
    }

    private void selectRelationItem(JComboBox<Object> comboBox, Object existingRelation) {
        if (existingRelation == null) {
            comboBox.setSelectedItem(null);
            return;
        }

        Long existingId = extractEntityId(existingRelation);
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Object option = comboBox.getItemAt(i);
            Long optionId = extractEntityId(option);
            if (existingId != null && existingId.equals(optionId)) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private Long extractEntityId(Object entity) {
        if (entity == null) {
            return null;
        }
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object value = idField.get(entity);
            if (value instanceof Number number) {
                return number.longValue();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String buildRelationLabel(Object value) {
        if (value == null) {
            return "-- Chua chon --";
        }

        if (value instanceof Student student) {
            String email = student.getEmail() != null ? student.getEmail() : "khong co email";
            return student.getFullName() + " (" + email + ")";
        }
        if (value instanceof Teacher teacher) {
            return teacher.getFullName();
        }
        if (value instanceof Staff staff) {
            return staff.getFullName();
        }
        if (value instanceof ClassEntity classEntity) {
            return classEntity.getClassName();
        }
        if (value instanceof Course course) {
            return course.getCourseName();
        }
        if (value instanceof Room room) {
            return room.getRoomName();
        }
        if (value instanceof Invoice invoice) {
            return "HD#" + invoice.getId();
        }
        if (value instanceof Enrollment enrollment) {
            return "DK#" + enrollment.getId();
        }
        if (value instanceof UserAccount userAccount) {
            return userAccount.getUsername();
        }

        Long id = extractEntityId(value);
        if (id != null) {
            return value.getClass().getSimpleName() + " #" + id;
        }
        return value.toString();
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
        if (type == LocalDateTime.class) {
            String normalized = raw.replace(' ', 'T');
            if (normalized.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")) {
                normalized += ":00";
            }
            return LocalDateTime.parse(normalized);
        }
        if (type == LocalTime.class) {
            String normalized = raw;
            if (normalized.matches("^\\d{2}:\\d{2}$")) {
                normalized += ":00";
            }
            return LocalTime.parse(normalized);
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

    private void saveEntity(T target) throws Exception {
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
            if (isNew) {
                em.persist(target);
            } else {
                em.merge(target);
            }
            return null;
        });
    }

    private static class FieldInput {
        private final Field field;
        private final JComponent component;

        private FieldInput(Field field, JComponent component) {
            this.field = field;
            this.component = component;
        }
    }

    private static class RelationFieldInput {
        private final Field field;
        private final JComboBox<Object> comboBox;

        private RelationFieldInput(Field field, JComboBox<Object> comboBox) {
            this.field = field;
            this.comboBox = comboBox;
        }
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
