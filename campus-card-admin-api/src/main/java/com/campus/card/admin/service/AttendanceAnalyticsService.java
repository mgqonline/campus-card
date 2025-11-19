package com.campus.card.admin.service;

import com.campus.card.admin.domain.AttendanceRecord;
import com.campus.card.admin.domain.AttendanceRule;
import com.campus.card.admin.repository.AttendanceRecordRepository;
import com.campus.card.common.result.Result;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceAnalyticsService {
    private final AttendanceRecordRepository recordRepo;
    private final AttendanceService attendanceService;

    public AttendanceAnalyticsService(AttendanceRecordRepository recordRepo, AttendanceService attendanceService) {
        this.recordRepo = recordRepo;
        this.attendanceService = attendanceService;
    }

    private List<AttendanceRecord> loadRecords(LocalDate startDate, LocalDate endDate, Long classId, Long studentId) {
        LocalDateTime start = (startDate != null ? startDate : LocalDate.now().minusDays(30)).atStartOfDay();
        LocalDateTime end = (endDate != null ? endDate : LocalDate.now()).atTime(LocalTime.MAX);
        List<AttendanceRecord> list = recordRepo.findByAttendanceTimeBetween(start, end);
        if (classId != null) list = list.stream().filter(r -> Objects.equals(r.getClassId(), classId)).collect(Collectors.toList());
        if (studentId != null) list = list.stream().filter(r -> Objects.equals(r.getStudentId(), studentId)).collect(Collectors.toList());
        return list;
    }

    private String computedStatus(AttendanceRule rule, AttendanceRecord r) {
        return attendanceService.classifyStatusByRule(rule, r);
    }

    private StatsPoint toStatsPoint(Collection<AttendanceRecord> records, AttendanceRule rule, String label) {
        long normal=0, late=0, early=0, absence=0;
        for (AttendanceRecord r : records) {
            String s = computedStatus(rule, r);
            if ("absence".equalsIgnoreCase(s)) absence++;
            else if ("late".equalsIgnoreCase(s)) late++;
            else if ("early".equalsIgnoreCase(s)) early++;
            else normal++;
        }
        long total = normal + late + early + absence;
        StatsPoint p = new StatsPoint();
        p.setLabel(label);
        p.setTotalCount(total);
        p.setNormalCount(normal);
        p.setLateCount(late);
        p.setEarlyCount(early);
        p.setAbsenceCount(absence);
        p.setAttendanceRate(total > 0 ? (double)(normal + late + early) / (double) total : 0d);
        return p;
    }

    @Data
    public static class StatsPoint {
        private String label; // 日期/月份/班级/个人标识
        private Long totalCount;
        private Long normalCount;
        private Long lateCount;
        private Long earlyCount;
        private Long absenceCount;
        private Double attendanceRate;
    }

    @Data
    public static class ClassStatsPoint extends StatsPoint {
        private Long classId;
        private String className;
    }

    @Data
    public static class PersonalStatsPoint extends StatsPoint {
        private Long studentId;
        private String studentName;
    }

    public List<StatsPoint> daily(LocalDate startDate, LocalDate endDate, Long classId) {
        AttendanceRule rule = attendanceService.getRuleOrDefault();
        List<AttendanceRecord> list = loadRecords(startDate, endDate, classId, null);
        Map<LocalDate, List<AttendanceRecord>> grouped = list.stream()
                .collect(Collectors.groupingBy(r -> r.getAttendanceTime().toLocalDate(), TreeMap::new, Collectors.toList()));
        return grouped.entrySet().stream()
                .map(e -> toStatsPoint(e.getValue(), rule, e.getKey().toString()))
                .collect(Collectors.toList());
    }

    public List<StatsPoint> monthly(LocalDate startDate, LocalDate endDate, Long classId) {
        AttendanceRule rule = attendanceService.getRuleOrDefault();
        List<AttendanceRecord> list = loadRecords(startDate, endDate, classId, null);
        Map<YearMonth, List<AttendanceRecord>> grouped = list.stream()
                .collect(Collectors.groupingBy(r -> YearMonth.from(r.getAttendanceTime()), TreeMap::new, Collectors.toList()));
        return grouped.entrySet().stream()
                .map(e -> toStatsPoint(e.getValue(), rule, e.getKey().toString()))
                .collect(Collectors.toList());
    }

    public List<StatsPoint> term(LocalDate startDate, LocalDate endDate, Long classId) {
        // 学期统计：按整个时间段聚合为单点
        AttendanceRule rule = attendanceService.getRuleOrDefault();
        List<AttendanceRecord> list = loadRecords(startDate, endDate, classId, null);
        return Collections.singletonList(toStatsPoint(list, rule, "TERM"));
    }

    public List<ClassStatsPoint> byClass(LocalDate startDate, LocalDate endDate) {
        AttendanceRule rule = attendanceService.getRuleOrDefault();
        List<AttendanceRecord> list = loadRecords(startDate, endDate, null, null);
        Map<Long, List<AttendanceRecord>> grouped = list.stream()
                .filter(r -> r.getClassId() != null)
                .collect(Collectors.groupingBy(AttendanceRecord::getClassId));
        List<ClassStatsPoint> resp = new ArrayList<>();
        for (Map.Entry<Long, List<AttendanceRecord>> e : grouped.entrySet()) {
            StatsPoint p = toStatsPoint(e.getValue(), rule, String.valueOf(e.getKey()));
            ClassStatsPoint cp = new ClassStatsPoint();
            cp.setLabel(p.getLabel());
            cp.setTotalCount(p.getTotalCount());
            cp.setNormalCount(p.getNormalCount());
            cp.setLateCount(p.getLateCount());
            cp.setEarlyCount(p.getEarlyCount());
            cp.setAbsenceCount(p.getAbsenceCount());
            cp.setAttendanceRate(p.getAttendanceRate());
            cp.setClassId(e.getKey());
            String name = e.getValue().stream().map(AttendanceRecord::getClassName).filter(Objects::nonNull).findFirst().orElse(null);
            cp.setClassName(name);
            resp.add(cp);
        }
        resp.sort(Comparator.comparing(ClassStatsPoint::getClassId));
        return resp;
    }

    public List<PersonalStatsPoint> byPersonal(LocalDate startDate, LocalDate endDate, Long classId) {
        AttendanceRule rule = attendanceService.getRuleOrDefault();
        List<AttendanceRecord> list = loadRecords(startDate, endDate, classId, null);
        Map<Long, List<AttendanceRecord>> grouped = list.stream()
                .filter(r -> r.getStudentId() != null)
                .collect(Collectors.groupingBy(AttendanceRecord::getStudentId));
        List<PersonalStatsPoint> resp = new ArrayList<>();
        for (Map.Entry<Long, List<AttendanceRecord>> e : grouped.entrySet()) {
            StatsPoint p = toStatsPoint(e.getValue(), rule, String.valueOf(e.getKey()));
            PersonalStatsPoint sp = new PersonalStatsPoint();
            sp.setLabel(p.getLabel());
            sp.setTotalCount(p.getTotalCount());
            sp.setNormalCount(p.getNormalCount());
            sp.setLateCount(p.getLateCount());
            sp.setEarlyCount(p.getEarlyCount());
            sp.setAbsenceCount(p.getAbsenceCount());
            sp.setAttendanceRate(p.getAttendanceRate());
            sp.setStudentId(e.getKey());
            String name = e.getValue().stream().map(AttendanceRecord::getStudentName).filter(Objects::nonNull).findFirst().orElse(null);
            sp.setStudentName(name);
            resp.add(sp);
        }
        resp.sort(Comparator.comparing(PersonalStatsPoint::getStudentId));
        return resp;
    }

    // 迟到早退统计（整体或按班级/个人过滤）
    public StatsPoint lateEarly(LocalDate startDate, LocalDate endDate, Long classId, Long studentId) {
        AttendanceRule rule = attendanceService.getRuleOrDefault();
        List<AttendanceRecord> list = loadRecords(startDate, endDate, classId, studentId);
        return toStatsPoint(list, rule, "LATE_EARLY");
    }

    // 缺勤统计（整体或按班级/个人过滤）
    public StatsPoint absence(LocalDate startDate, LocalDate endDate, Long classId, Long studentId) {
        AttendanceRule rule = attendanceService.getRuleOrDefault();
        List<AttendanceRecord> list = loadRecords(startDate, endDate, classId, studentId);
        // toStatsPoint 已包含缺勤计数
        return toStatsPoint(list, rule, "ABSENCE");
    }

    // 导出CSV：原始记录或指定聚合类型
    public String exportCsv(String type, LocalDate startDate, LocalDate endDate, Long classId, Long studentId) {
        StringBuilder sb = new StringBuilder();
        if ("records".equalsIgnoreCase(type)) {
            sb.append("id,studentId,studentName,classId,className,attendanceTime,attendanceType,checkType,status,remark\n");
            List<AttendanceRecord> list = loadRecords(startDate, endDate, classId, studentId);
            for (AttendanceRecord r : list) {
                sb.append(String.format(Locale.ROOT,
                    "%d,%d,%s,%d,%s,%s,%s,%s,%s,%s\n",
                    Optional.ofNullable(r.getId()).orElse(0L),
                    Optional.ofNullable(r.getStudentId()).orElse(0L),
                    safe(r.getStudentName()),
                    Optional.ofNullable(r.getClassId()).orElse(0L),
                    safe(r.getClassName()),
                    r.getAttendanceTime(),
                    safe(r.getAttendanceType()),
                    safe(r.getCheckType()),
                    safe(r.getStatus()),
                    safe(r.getRemark())
                ));
            }
        } else if ("daily".equalsIgnoreCase(type)) {
            sb.append("date,total,normal,late,early,absence,rate\n");
            for (StatsPoint p : daily(startDate, endDate, classId)) {
                sb.append(String.format(Locale.ROOT, "%s,%d,%d,%d,%d,%d,%.4f\n", p.getLabel(), p.getTotalCount(), p.getNormalCount(), p.getLateCount(), p.getEarlyCount(), p.getAbsenceCount(), p.getAttendanceRate()));
            }
        } else if ("monthly".equalsIgnoreCase(type)) {
            sb.append("month,total,normal,late,early,absence,rate\n");
            for (StatsPoint p : monthly(startDate, endDate, classId)) {
                sb.append(String.format(Locale.ROOT, "%s,%d,%d,%d,%d,%d,%.4f\n", p.getLabel(), p.getTotalCount(), p.getNormalCount(), p.getLateCount(), p.getEarlyCount(), p.getAbsenceCount(), p.getAttendanceRate()));
            }
        } else if ("class".equalsIgnoreCase(type)) {
            sb.append("classId,className,total,normal,late,early,absence,rate\n");
            for (ClassStatsPoint p : byClass(startDate, endDate)) {
                sb.append(String.format(Locale.ROOT, "%d,%s,%d,%d,%d,%d,%d,%.4f\n", p.getClassId(), safe(p.getClassName()), p.getTotalCount(), p.getNormalCount(), p.getLateCount(), p.getEarlyCount(), p.getAbsenceCount(), p.getAttendanceRate()));
            }
        } else if ("personal".equalsIgnoreCase(type)) {
            sb.append("studentId,studentName,total,normal,late,early,absence,rate\n");
            for (PersonalStatsPoint p : byPersonal(startDate, endDate, classId)) {
                sb.append(String.format(Locale.ROOT, "%d,%s,%d,%d,%d,%d,%d,%.4f\n", p.getStudentId(), safe(p.getStudentName()), p.getTotalCount(), p.getNormalCount(), p.getLateCount(), p.getEarlyCount(), p.getAbsenceCount(), p.getAttendanceRate()));
            }
        } else {
            sb.append("type not supported");
        }
        return sb.toString();
    }

    private String safe(String s) { return s == null ? "" : s.replaceAll(",", " "); }
}