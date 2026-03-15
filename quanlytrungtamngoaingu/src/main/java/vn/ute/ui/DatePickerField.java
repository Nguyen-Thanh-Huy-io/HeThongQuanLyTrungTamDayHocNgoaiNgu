package vn.ute.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * A compact day/month/year spinner component that returns a LocalDate.
 * Displayed as:  [DD] / [MM] / [YYYY]
 */
public class DatePickerField extends JPanel {

    private final JSpinner daySpinner;
    private final JSpinner monthSpinner;
    private final JSpinner yearSpinner;

    public DatePickerField() {
        this(LocalDate.now());
    }

    public DatePickerField(LocalDate initialDate) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        setOpaque(false);

        LocalDate date = initialDate != null ? initialDate : LocalDate.now();

        daySpinner   = new JSpinner(new SpinnerNumberModel(date.getDayOfMonth(), 1, 31, 1));
        monthSpinner = new JSpinner(new SpinnerNumberModel(date.getMonthValue(),  1, 12, 1));
        yearSpinner  = new JSpinner(new SpinnerNumberModel(date.getYear(), 2000, 2100, 1));

        daySpinner  .setEditor(new JSpinner.NumberEditor(daySpinner,   "00"));
        monthSpinner.setEditor(new JSpinner.NumberEditor(monthSpinner, "00"));
        yearSpinner .setEditor(new JSpinner.NumberEditor(yearSpinner,  "0000"));

        Dimension dayDim  = new Dimension(58, 30);
        Dimension monDim  = new Dimension(58, 30);
        Dimension yearDim = new Dimension(76, 30);
        daySpinner  .setPreferredSize(dayDim);
        monthSpinner.setPreferredSize(monDim);
        yearSpinner .setPreferredSize(yearDim);

        JLabel sep1 = new JLabel("/");
        JLabel sep2 = new JLabel("/");
        sep1.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 15f));
        sep2.setFont(UIUtils.DEFAULT_FONT.deriveFont(Font.BOLD, 15f));

        add(daySpinner);
        add(sep1);
        add(monthSpinner);
        add(sep2);
        add(yearSpinner);
    }

    /** Returns the currently selected date, clamping the day to a valid range. */
    public LocalDate getValue() {
        int day   = (int) daySpinner.getValue();
        int month = (int) monthSpinner.getValue();
        int year  = (int) yearSpinner.getValue();
        int maxDay = YearMonth.of(year, month).lengthOfMonth();
        if (day > maxDay) day = maxDay;
        return LocalDate.of(year, month, day);
    }

    /** Sets the displayed date. Does nothing if {@code date} is null. */
    public void setValue(LocalDate date) {
        if (date == null) return;
        yearSpinner .setValue(date.getYear());
        monthSpinner.setValue(date.getMonthValue());
        daySpinner  .setValue(date.getDayOfMonth());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        daySpinner  .setEnabled(enabled);
        monthSpinner.setEnabled(enabled);
        yearSpinner .setEnabled(enabled);
    }
}
