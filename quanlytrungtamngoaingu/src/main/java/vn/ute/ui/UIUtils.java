package vn.ute.ui;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;


/**
 * helper class để init theme và tạo các component có style thống nhất
 */
public class UIUtils {
    // ── Fonts ──────────────────────────────────────────────────────
    public static final Font DEFAULT_FONT     = new Font("Segoe UI", Font.PLAIN, 14);

    // ── Blue primary palette ───────────────────────────────────────
    /** #1565C0  deep professional blue — buttons, header, table header */
    public static final Color PRIMARY_COLOR   = new Color(21, 101, 192);
    /** #0D47A1  darker shade — pressed states, sidebar */
    public static final Color PRIMARY_DARK    = new Color(13,  71, 161);
    /** #42A5F5  vivid accent — active tab indicator, links */
    public static final Color PRIMARY_LIGHT   = new Color(187, 222, 251);
    /** #E3F2FD  very light blue — alternating table rows */
    public static final Color ACCENT_COLOR    = new Color(66,  165, 245);

    // ── Semantic colours ───────────────────────────────────────────
    /** Green — success / add */
    public static final Color SUCCESS_COLOR   = new Color(39, 174, 96);
    /** Red — danger / delete */
    public static final Color DANGER_COLOR    = new Color(192, 57,  43);
    /** Orange — warning */
    public static final Color WARNING_COLOR   = new Color(230, 81,   0);

    // ── Backgrounds & surfaces ────────────────────────────────────
    /** App-level background (very light blue tint) */
    public static final Color APP_BG          = new Color(235, 243, 252);
    /** Surface / card background */
    public static final Color SURFACE         = Color.WHITE;
    /** Alternating table row */
    public static final Color TABLE_ALT_ROW   = new Color(227, 242, 253);
    /** Toolbar background */
    public static final Color TOOLBAR_BG      = new Color(240, 248, 255);

    // ── Text ──────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY    = new Color(13,  71, 161);
    public static final Color TEXT_SECONDARY  = new Color(97,  97,  97);

    // ── Borders ──────────────────────────────────────────────────
    public static final Color BORDER_COLOR    = new Color(144, 202, 249);

    // ── Legacy aliases kept for backward compatibility ───────────
    /** @deprecated use PRIMARY_COLOR */
    public static final Color SECONDARY_COLOR = new Color(236, 240, 241);

    // ─────────────────────────────────────────────────────────────
    /**
     * Áp dụng Look&Feel và thiết lập một số thuộc tính chung
     */
    public static void initLookAndFeel() {
        try {
            FlatLightLaf.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ── Typography ──────────────────────────────────────────
        UIManager.put("defaultFont",           DEFAULT_FONT);
        UIManager.put("Button.font",           DEFAULT_FONT);
        UIManager.put("Label.font",            DEFAULT_FONT);
        UIManager.put("TextField.font",        DEFAULT_FONT);
        UIManager.put("PasswordField.font",    DEFAULT_FONT);
        UIManager.put("Table.font",            DEFAULT_FONT);
        UIManager.put("Menu.font",             DEFAULT_FONT);
        UIManager.put("MenuItem.font",         DEFAULT_FONT);
        UIManager.put("ComboBox.font",         DEFAULT_FONT);
        UIManager.put("TextArea.font",         DEFAULT_FONT);
        UIManager.put("TabbedPane.font",       DEFAULT_FONT.deriveFont(Font.BOLD));
        UIManager.put("TabbedPane.tabHeight",  36);

        // ── Table ───────────────────────────────────────────────
        UIManager.put("Table.rowHeight",               32);
        UIManager.put("Table.showHorizontalLines",     true);
        UIManager.put("Table.showVerticalLines",       false);
        UIManager.put("Table.gridColor",               new Color(213, 227, 245));
        UIManager.put("Table.selectionBackground",     PRIMARY_COLOR);
        UIManager.put("Table.selectionForeground",     Color.WHITE);
        UIManager.put("TableHeader.background",        PRIMARY_COLOR);
        UIManager.put("TableHeader.foreground",        Color.WHITE);
        UIManager.put("TableHeader.font",              DEFAULT_FONT.deriveFont(Font.BOLD));
        UIManager.put("TableHeader.separatorColor",    PRIMARY_DARK);

        // ── Tabbed pane ─────────────────────────────────────────
        UIManager.put("TabbedPane.underlineColor",          PRIMARY_COLOR);
        UIManager.put("TabbedPane.selectedForeground",      PRIMARY_COLOR);
        UIManager.put("TabbedPane.hoverColor",              PRIMARY_LIGHT);
        UIManager.put("TabbedPane.focusColor",              PRIMARY_LIGHT);

        // ── Buttons ─────────────────────────────────────────────
        UIManager.put("Button.default.background",     PRIMARY_COLOR);
        UIManager.put("Button.default.foreground",     Color.WHITE);
        UIManager.put("Button.hoverBorderColor",       PRIMARY_COLOR);
        UIManager.put("Component.focusColor",          ACCENT_COLOR);

        // ── Shape ───────────────────────────────────────────────
        UIManager.put("Component.arc",      14);
        UIManager.put("Button.arc",         18);
        UIManager.put("TextComponent.arc",  12);

        // ── Text field focus border ─────────────────────────────
        UIManager.put("Component.focusedBorderColor", PRIMARY_COLOR);
        UIManager.put("Component.borderColor",        BORDER_COLOR);

        // ── ScrollBar ───────────────────────────────────────────
        UIManager.put("ScrollBar.thumbArc",    999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width",       10);
    }

    // ─── Button factories ─────────────────────────────────────────

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
        btn.setBackground(SURFACE);
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

    /** Red outlined button — for destructive actions (Delete). */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(DANGER_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(DEFAULT_FONT.deriveFont(Font.BOLD));
        btn.setPreferredSize(new Dimension(120, 36));
        return btn;
    }

    // ─── Toolbar helper ──────────────────────────────────────────

    public static void styleToolbar(JToolBar toolbar) {
        toolbar.setFloatable(false);
        toolbar.setBackground(TOOLBAR_BG);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    // ─── Table styling helper ────────────────────────────────────

    /**
     * Applies a consistent blue header and alternating-row renderer to a JTable.
     */
    public static void styleTable(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setFont(DEFAULT_FONT.deriveFont(Font.BOLD, 13f));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_DARK));

        table.setShowGrid(true);
        table.setGridColor(new Color(213, 227, 245));
        table.setSelectionBackground(PRIMARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(32);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Alternating row colours
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? SURFACE : TABLE_ALT_ROW);
                    setForeground(new Color(33, 33, 33));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }
}