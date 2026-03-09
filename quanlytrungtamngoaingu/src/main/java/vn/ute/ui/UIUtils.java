package vn.ute.ui;

import javax.swing.*;
import java.awt.*;


/**
 * helper class để init theme và tạo các component có style thống nhất
 */
public class UIUtils {
    public static final Font DEFAULT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    public static final Color SECONDARY_COLOR = new Color(236, 240, 241);

    /**
     * Áp dụng Look&Feel và thiết lập một số thuộc tính chung
     */
    public static void initLookAndFeel() {
        try {
            // Nếu FlatLaf đã được thêm (tuỳ chọn) thì dùng nó, còn không thì dùng native
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // một số override mặc định cho mọi component
        UIManager.put("Button.font", DEFAULT_FONT);
        UIManager.put("Label.font", DEFAULT_FONT);
        UIManager.put("TextField.font", DEFAULT_FONT);
        UIManager.put("PasswordField.font", DEFAULT_FONT);
        UIManager.put("Table.font", DEFAULT_FONT);
        UIManager.put("TabbedPane.font", DEFAULT_FONT.deriveFont(Font.BOLD));
        UIManager.put("Menu.font", DEFAULT_FONT);
        UIManager.put("MenuItem.font", DEFAULT_FONT);
        UIManager.put("ComboBox.font", DEFAULT_FONT);
        UIManager.put("TextArea.font", DEFAULT_FONT);
    }

    public static JButton createPrimaryButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(DEFAULT_FONT.deriveFont(Font.BOLD));
        btn.setPreferredSize(new Dimension(120, 36));
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createPrimaryButton(text, null);
    }

    public static JButton createSecondaryButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY_COLOR);
        btn.setFocusPainted(false);
        btn.setFont(DEFAULT_FONT);
        btn.setPreferredSize(new Dimension(120, 36));
        btn.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        return createSecondaryButton(text, null);
    }

    public static void styleToolbar(JToolBar toolbar) {
        toolbar.setFloatable(false);
        toolbar.setBackground(SECONDARY_COLOR);
    }
}