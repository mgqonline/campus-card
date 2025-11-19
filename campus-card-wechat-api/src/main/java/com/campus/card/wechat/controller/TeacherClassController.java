package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import com.campus.card.wechat.model.AttendanceEvent;
import com.campus.card.wechat.model.Student;
import com.campus.card.wechat.repository.AttendanceEventRepository;
import com.campus.card.wechat.repository.StudentRepository;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/t/class")
public class TeacherClassController {

    private final StudentRepository studentRepository;
    private final AttendanceEventRepository attendanceRepo;

    public TeacherClassController(StudentRepository studentRepository,
                                  AttendanceEventRepository attendanceRepo) {
        this.studentRepository = studentRepository;
        this.attendanceRepo = attendanceRepo;
    }

    /** 教师所带班级列表（与 TeacherAuthController.profile 保持一致） */
    @GetMapping("/list")
    public Result<List<ClassBrief>> list() {
        List<ClassBrief> classes = Arrays.asList(
                new ClassBrief(301L, "三年级一班"),
                new ClassBrief(302L, "三年级二班")
        );
        return Result.ok(classes);
    }

    /** 班级学生名单 */
    @GetMapping("/students")
    public Result<List<StudentItem>> students(@RequestParam Long classId) {
        List<Student> all = studentRepository.findAll();
        List<StudentItem> items = all.stream()
                .filter(s -> Objects.equals(s.getClassId(), classId))
                .map(s -> new StudentItem(s.getId(), s.getName(), s.getClassId(), s.getGrade(), s.getCardNo(), s.getFaceStatus()))
                .collect(Collectors.toList());
        return Result.ok(items);
    }

    /** 学生信息查看（复用 student 表） */
    @GetMapping("/student/info")
    public Result<StudentItem> studentInfo(@RequestParam Long childId) {
        Student s = studentRepository.findById(childId).orElse(null);
        if (s == null) return Result.error("student not found", 404);
        return Result.ok(new StudentItem(s.getId(), s.getName(), s.getClassId(), s.getGrade(), s.getCardNo(), s.getFaceStatus()));
    }

    /** 班级考勤统计（按日或月聚合，demo 计算） */
    @GetMapping("/attendance/stats")
    public Result<AttendanceStats> attendanceStats(@RequestParam Long classId,
                                                   @RequestParam(defaultValue = "day") String range,
                                                   @RequestParam(required = false) String date) {
        LocalDate base = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();
        List<Student> classStudents;
        try {
            classStudents = studentRepository.findAll().stream()
                    .filter(s -> Objects.equals(s.getClassId(), classId))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            // 数据源不可用时回退为空数据，避免 500 影响前端联调
            classStudents = Collections.emptyList();
        }

        AttendanceStats stats = new AttendanceStats();
        stats.setClassId(classId);
        stats.setRange(range);
        stats.setTotalStudents(classStudents.size());

        if ("day".equalsIgnoreCase(range)) {
            LocalDateTime start = base.atStartOfDay();
            LocalDateTime end = base.atTime(LocalTime.MAX);
            int present = 0, late = 0, early = 0;
            try {
                for (Student s : classStudents) {
                    // 查询当天该学生是否有考勤事件（取一条即可）
                    List<AttendanceEvent> events = attendanceRepo
                            .findByChildIdAndTimeBetweenOrderByTimeDesc(s.getId(), start, end, PageRequest.of(0, 20))
                            .getContent();
                    if (!events.isEmpty()) {
                        present++;
                        // 简单统计迟到/早退（若多条事件，则按任意一条标记）
                        boolean hasLate = events.stream().anyMatch(e -> Boolean.TRUE.equals(e.getLate()));
                        boolean hasEarly = events.stream().anyMatch(e -> Boolean.TRUE.equals(e.getEarlyLeave()));
                        if (hasLate) late++;
                        if (hasEarly) early++;
                    }
                }
            } catch (Exception ex) {
                // 回退为 0 统计，前端仍可展示基本结构
            }
            stats.setPresentCount(present);
            stats.setAbsentCount(Math.max(0, stats.getTotalStudents() - present));
            stats.setLateCount(late);
            stats.setEarlyLeaveCount(early);
            Map<String, Integer> dailyPresence = new LinkedHashMap<>();
            dailyPresence.put(base.toString(), present);
            stats.setDailyPresence(dailyPresence);
        } else { // month
            LocalDate first = base.withDayOfMonth(1);
            LocalDate last = base.withDayOfMonth(base.lengthOfMonth());
            int presentDaysSum = 0, lateSum = 0, earlySum = 0;
            Map<String, Integer> dailyPresence = new LinkedHashMap<>();
            for (LocalDate d = first; !d.isAfter(last); d = d.plusDays(1)) {
                LocalDateTime start = d.atStartOfDay();
                LocalDateTime end = d.atTime(LocalTime.MAX);
                int present = 0;
                int late = 0;
                int early = 0;
                try {
                    for (Student s : classStudents) {
                        List<AttendanceEvent> events = attendanceRepo
                                .findByChildIdAndTimeBetweenOrderByTimeDesc(s.getId(), start, end, PageRequest.of(0, 20))
                                .getContent();
                        if (!events.isEmpty()) {
                            present++;
                            boolean hasLate = events.stream().anyMatch(e -> Boolean.TRUE.equals(e.getLate()));
                            boolean hasEarly = events.stream().anyMatch(e -> Boolean.TRUE.equals(e.getEarlyLeave()));
                            if (hasLate) late++;
                            if (hasEarly) early++;
                        }
                    }
                } catch (Exception ex) {
                    // 回退当日统计为 0
                }
                dailyPresence.put(d.toString(), present);
                presentDaysSum += (present > 0 ? 1 : 0); // 天维度：是否有到校
                lateSum += late;
                earlySum += early;
            }
            stats.setPresentCount(null); // 月总在 dailyPresence
            stats.setAbsentCount(null);
            stats.setLateCount(lateSum);
            stats.setEarlyLeaveCount(earlySum);
            stats.setDailyPresence(dailyPresence);
        }
        return Result.ok(stats);
    }

    @Data
    public static class ClassBrief {
        private Long classId;
        private String className;
        public ClassBrief(Long classId, String className) { this.classId = classId; this.className = className; }
    }

    @Data
    public static class StudentItem {
        private Long id;
        private String name;
        private Long classId;
        private String grade;
        private String cardNo;
        private String faceStatus;
        public StudentItem(Long id, String name, Long classId, String grade, String cardNo, String faceStatus) {
            this.id = id; this.name = name; this.classId = classId; this.grade = grade; this.cardNo = cardNo; this.faceStatus = faceStatus;
        }
    }

    @Data
    public static class AttendanceStats {
        private Long classId;
        private String range; // day/month
        private Integer totalStudents;
        private Integer presentCount; // 日维度：到校人数
        private Integer absentCount;  // 日维度：缺勤人数
        private Integer lateCount;
        private Integer earlyLeaveCount;
        private Map<String, Integer> dailyPresence; // 日期 -> 到校人数（或布尔）
    }
}