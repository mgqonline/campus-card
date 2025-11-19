package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import com.campus.card.wechat.model.AttendanceEvent;
import com.campus.card.wechat.model.Student;
import com.campus.card.wechat.repository.AttendanceEventRepository;
import com.campus.card.wechat.repository.StudentRepository;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教师端考勤管理：学生考勤查询、异常标记、报表导出
 */
@RestController
@RequestMapping("/api/v1/t/attendance")
public class TeacherAttendanceController {

    private final AttendanceEventRepository attendanceRepo;
    private final StudentRepository studentRepository;

    public TeacherAttendanceController(AttendanceEventRepository attendanceRepo,
                                       StudentRepository studentRepository) {
        this.attendanceRepo = attendanceRepo;
        this.studentRepository = studentRepository;
    }

    /**
     * 学生考勤记录查询（支持按学生或班级过滤，分页）
     */
    @GetMapping("/records")
    public Result<List<RecordItem>> getRecords(@RequestParam(required = false) Long childId,
                                               @RequestParam(required = false) Long classId,
                                               @RequestParam(required = false) String startDate,
                                               @RequestParam(required = false) String endDate,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "20") Integer size) {
        List<RecordItem> result = new ArrayList<>();
        // 计算查询范围
        LocalDateTime start = null, end = null;
        if (startDate != null && endDate != null) {
            start = LocalDate.parse(startDate).atStartOfDay();
            end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        }

        try {
            if (childId != null) {
                // 按单个学生查询
                org.springframework.data.domain.Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
                List<AttendanceEvent> events = (start != null && end != null)
                        ? attendanceRepo.findByChildIdAndTimeBetweenOrderByTimeDesc(childId, start, end, pageable).getContent()
                        : attendanceRepo.findByChildIdOrderByTimeDesc(childId, pageable).getContent();
                for (AttendanceEvent e : events) {
                    result.add(RecordItem.from(e));
                }
            } else if (classId != null) {
                // 按班级查询：聚合该班所有学生的事件（简单分页：按事件时间降序后截取）
                List<Student> students = studentRepository.findAll().stream()
                        .filter(s -> Objects.equals(s.getClassId(), classId))
                        .collect(Collectors.toList());
                List<RecordItem> all = new ArrayList<>();
                org.springframework.data.domain.Pageable pageable = PageRequest.of(0, Math.max(size, 20));
                for (Student s : students) {
                    List<AttendanceEvent> evs = (start != null && end != null)
                            ? attendanceRepo.findByChildIdAndTimeBetweenOrderByTimeDesc(s.getId(), start, end, pageable).getContent()
                            : attendanceRepo.findByChildIdOrderByTimeDesc(s.getId(), pageable).getContent();
                    for (AttendanceEvent e : evs) {
                        RecordItem item = RecordItem.from(e);
                        item.setChildId(s.getId());
                        item.setChildName(s.getName());
                        all.add(item);
                    }
                }
                // 合并后按时间降序并做分页切片
                all.sort((a, b) -> b.getTime().compareTo(a.getTime()));
                int from = Math.max((page - 1) * size, 0);
                int to = Math.min(from + size, all.size());
                if (from < to) result.addAll(all.subList(from, to));
            } else {
                // 无过滤参数，返回空列表，避免误查所有数据
                result = Collections.emptyList();
            }
        } catch (Exception ex) {
            // 数据源不可用时回退为空列表
            result = Collections.emptyList();
        }
        return Result.ok(result);
    }

    /**
     * 考勤异常处理（标记迟到/早退/恢复正常）
     */
    @PutMapping("/records/{id}/exception")
    public Result<String> markException(@PathVariable Long id, @RequestBody MarkReq req) {
        Optional<AttendanceEvent> oe = attendanceRepo.findById(id);
        if (!oe.isPresent()) return Result.error("记录不存在", 404);
        AttendanceEvent e = oe.get();
        String status = Optional.ofNullable(req.getStatus()).orElse("normal").toLowerCase(Locale.ROOT);
        switch (status) {
            case "late":
                e.setLate(true);
                e.setEarlyLeave(false);
                break;
            case "early":
                e.setLate(false);
                e.setEarlyLeave(true);
                break;
            case "normal":
            default:
                e.setLate(false);
                e.setEarlyLeave(false);
                break;
        }
        attendanceRepo.save(e);
        return Result.ok("updated");
    }

    /**
     * 班级考勤报表导出（CSV）
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam Long classId,
                                         @RequestParam(required = false) String startDate,
                                         @RequestParam(required = false) String endDate) {
        LocalDate baseStart = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : LocalDate.now();
        LocalDate baseEnd = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : baseStart;
        LocalDateTime start = baseStart.atStartOfDay();
        LocalDateTime end = baseEnd.atTime(LocalTime.MAX);

        List<Student> students;
        try {
            students = studentRepository.findAll().stream()
                    .filter(s -> Objects.equals(s.getClassId(), classId))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            students = Collections.emptyList();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("studentId,studentName,time,type,gate,late,earlyLeave\n");
        org.springframework.data.domain.Pageable p = PageRequest.of(0, 500);
        try {
            for (Student s : students) {
                List<AttendanceEvent> evs = attendanceRepo.findByChildIdAndTimeBetweenOrderByTimeDesc(s.getId(), start, end, p).getContent();
                for (AttendanceEvent e : evs) {
                    sb.append(s.getId()).append(',')
                            .append(s.getName() != null ? s.getName() : "").append(',')
                            .append(e.getTime()).append(',')
                            .append(e.getType()).append(',')
                            .append(e.getGate()).append(',')
                            .append(Boolean.TRUE.equals(e.getLate())).append(',')
                            .append(Boolean.TRUE.equals(e.getEarlyLeave()))
                            .append('\n');
                }
            }
        } catch (Exception ex) {
            // 保留只有表头的 CSV
        }

        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        String filename = String.format("attendance_class_%d_%s_to_%s.csv", classId, baseStart, baseEnd);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(data);
    }

    @Data
    public static class RecordItem {
        private Long id;
        private Long childId;
        private String childName;
        private LocalDateTime time;
        private String type;
        private String gate;
        private Boolean late;
        private Boolean earlyLeave;

        public static RecordItem from(AttendanceEvent e) {
            RecordItem r = new RecordItem();
            r.setId(e.getId());
            r.setChildId(e.getChildId());
            r.setTime(e.getTime());
            r.setType(e.getType());
            r.setGate(e.getGate());
            r.setLate(Boolean.TRUE.equals(e.getLate()));
            r.setEarlyLeave(Boolean.TRUE.equals(e.getEarlyLeave()));
            return r;
        }
    }

    @Data
    public static class MarkReq {
        private String status; // late/early/normal
        private String reason; // 预留
    }
}