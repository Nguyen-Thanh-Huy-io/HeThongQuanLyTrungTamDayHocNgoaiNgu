package vn.ute.ui;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * A very simple dialog that builds a form from a bean's fields using reflection.
 * When the user presses OK it does not actually save anything; you should
 * override onSave() or handle the returned values in the caller.
 */
public class GenericEditDialog<T> extends JDialog {
    private final Class<T> type;
    private final T instance;
    private final List<JTextField> inputs = new ArrayList<>();

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
            inputs.add(tf);

            gbc.gridx = 0;
            gbc.gridy++;
        }

        JButton btnOk = new JButton("OK");
        btnOk.addActionListener(e -> {
            onSave();
            dispose();
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
    protected void onSave() {
        // reflection could copy values back to instance, but left empty
    }
}
