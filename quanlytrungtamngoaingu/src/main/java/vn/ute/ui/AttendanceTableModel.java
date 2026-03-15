package vn.ute.ui;

import vn.ute.model.Attendance;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AttendanceTableModel extends MappedTableModel<vn.ute.model.Attendance> {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AttendanceTableModel() {
        super(
            new String[]{"Học viên", "Mã lớp", "Lớp học", "Khóa học", "Ngày học", "Thứ", "Trạng thái", "Ghi chú"},
            new String[]{
                "student.fullName",
                "classEntity.id",
                "classEntity.className",
                "classEntity.course.courseName",
                "attendDate",
                "attendDate",
                "status",
                "note"
            },
            null,
            value -> value != null ? "#" + value : "Chưa xác định",
            null,
            null,
            value -> formatDate((LocalDate) value),
            value -> formatWeekday((LocalDate) value),
            value -> formatStatus((Attendance.AttendStatus) value),
            null
        );
    }

    private static String formatDate(LocalDate date) {
        if (date == null) {
            return "Chưa xác định";
        }
        return date.format(DATE_FORMAT);
    }

    private static String formatWeekday(LocalDate date) {
        if (date == null) {
            return "Chưa xác định";
        }
        String english = date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH);
        return switch (english) {
            case "Monday" -> "Thứ 2";
            case "Tuesday" -> "Thứ 3";
            case "Wednesday" -> "Thứ 4";
            case "Thursday" -> "Thứ 5";
            case "Friday" -> "Thứ 6";
            case "Saturday" -> "Thứ 7";
            case "Sunday" -> "Chủ nhật";
            default -> english;
        };
    }

    private static String formatStatus(Attendance.AttendStatus status) {
        if (status == null) {
            return "Chưa xác định";
        }
        return switch (status) {
            case Present -> "Có mặt";
            case Absent -> "Vắng";
            case Late -> "Đi trễ";
        };
    }
}
