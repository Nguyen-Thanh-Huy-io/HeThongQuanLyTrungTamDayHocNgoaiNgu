package vn.ute.stream;

import vn.ute.model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


public class OperationStreamQueries {

    /** 7. Tìm các phòng học đang trống (không có lịch) vào một ngày cụ thể */
    public static List<Room> availableRooms(List<Room> allRooms, List<Schedule> schedules, LocalDate date) {
        Set<Long> busyRoomIds = schedules.stream()
                .filter(s -> s.getStudyDate().equals(date))
                .map(s -> s.getRoom().getId())
                .collect(Collectors.toSet());

        return allRooms.stream()
                .filter(r -> !busyRoomIds.contains(r.getId()))
                .collect(Collectors.toList());
    }

    /** 8. Đếm số lượng tài khoản theo vai trò (Admin, Teacher, Student, Staff) */
    public static Map<String, Long> countAccountsByRole(List<UserAccount> accounts) {
        return accounts.stream()
                .collect(Collectors.groupingBy(
                    acc -> acc.getRole().toString(), Collectors.counting()));
    }

    /** 9. Lấy danh sách Giáo viên dạy nhiều lớp nhất (Top 1 Teacher) */
    public static Optional<Teacher> busiestTeacher(List<ClassEntity> classes) {
        return classes.stream()
                .collect(Collectors.groupingBy(ClassEntity::getTeacher, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }
}