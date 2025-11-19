package com.campus.card.admin.service;

import com.campus.card.admin.domain.AttendanceRecord;
import com.campus.card.admin.domain.AttendanceRule;
import com.campus.card.admin.repository.AttendanceRecordRepository;
import com.campus.card.admin.repository.AttendanceRuleRepository;
import com.campus.card.admin.repository.CardRepository;
import com.campus.card.admin.repository.StudentRepository;
import com.campus.card.admin.repository.DeviceRepository;
import com.campus.card.admin.repository.FaceRecognitionLogRepository;
import com.campus.card.admin.domain.FaceRecognitionLog;
import com.campus.card.common.result.PageResult;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class AttendanceService {
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceRuleRepository attendanceRuleRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private FaceRecognitionLogRepository faceRecognitionLogRepository;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String KEY_SETTINGS = "attendance:settings";

    public AttendanceService(AttendanceRecordRepository attendanceRecordRepository,
                             AttendanceRuleRepository attendanceRuleRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.attendanceRuleRepository = attendanceRuleRepository;
    }

    public PageResult<AttendanceRecord> pageList(int page, int size, Long studentId, Long classId,
                                                LocalDate startDate, LocalDate endDate, String status,
                                                java.util.List<Long> allowedClassIds,
                                                java.util.Set<Long> allowedStudentIds) {
        List<AttendanceRecord> list = attendanceRecordRepository.findAll();

        // 过滤条件
        if (studentId != null) {
            list = list.stream().filter(r -> studentId.equals(r.getStudentId())).collect(java.util.stream.Collectors.toList());
        }
        if (classId != null) {
            list = list.stream().filter(r -> classId.equals(r.getClassId())).collect(java.util.stream.Collectors.toList());
        }
        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            list = list.stream().filter(r -> r.getAttendanceTime() != null && !r.getAttendanceTime().isBefore(startDateTime)).collect(java.util.stream.Collectors.toList());
        }
        if (endDate != null) {
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            list = list.stream().filter(r -> r.getAttendanceTime() != null && !r.getAttendanceTime().isAfter(endDateTime)).collect(java.util.stream.Collectors.toList());
        }
        if (status != null && !status.isEmpty()) {
            list = list.stream().filter(r -> status.equals(r.getStatus())).collect(java.util.stream.Collectors.toList());
        }
        // 数据范围过滤：班级
        if (allowedClassIds != null && !allowedClassIds.isEmpty()) {
            list = list.stream().filter(r -> r.getClassId() != null && allowedClassIds.contains(r.getClassId()))
                    .collect(java.util.stream.Collectors.toList());
        }
        // 数据范围过滤：个人
        if (allowedStudentIds != null && !allowedStudentIds.isEmpty()) {
            list = list.stream().filter(r -> r.getStudentId() != null && allowedStudentIds.contains(r.getStudentId()))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        int total = list.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(total, from + size);
        List<AttendanceRecord> pageList = from >= total ? java.util.Collections.emptyList() : list.subList(from, to);
        return PageResult.of(total, pageList);
    }

    // 读取规则（若不存在则返回默认规则）
    public AttendanceRule getRuleOrDefault() {
        return attendanceRuleRepository.findAll().stream().findFirst().orElseGet(AttendanceRule::new);
    }

    // 保存或更新规则（系统维持一条规则记录）
    public AttendanceRule saveRule(AttendanceRule incoming) {
        AttendanceRule rule = attendanceRuleRepository.findAll().stream().findFirst().orElse(new AttendanceRule());
        rule.setScenario(incoming.getScenario());
        rule.setWorkDays(incoming.getWorkDays());
        rule.setWorkStart(incoming.getWorkStart());
        rule.setWorkEnd(incoming.getWorkEnd());
        rule.setLateGraceMin(incoming.getLateGraceMin());
        rule.setEarlyGraceMin(incoming.getEarlyGraceMin());
        rule.setEnabled(incoming.getEnabled());
        return attendanceRuleRepository.save(rule);
    }

    // 根据规则判定记录状态（迟到/早退/正常），缺勤沿用原状态
    public String classifyStatusByRule(AttendanceRule rule, AttendanceRecord r) {
        if (r.getAttendanceTime() == null) return "normal";
        // 读取综合设置（节假日/调休/时段）
        AttendanceSettings settings = getSettingsOrDefault();
    
        java.time.LocalDate date = r.getAttendanceTime().toLocalDate();
        DayOfWeek dow = r.getAttendanceTime().getDayOfWeek();
        Set<DayOfWeek> workDays = parseWorkDays(rule);
        boolean working = workDays.contains(dow);
    
        // 假期与调休覆盖工作日判断
        if (settings != null) {
            // 节假日：在任一节假日区间内则视为休息日
            if (settings.getHolidays() != null) {
                for (Holiday h : settings.getHolidays()) {
                    if (h.getStartDate() != null && h.getEndDate() != null) {
                        java.time.LocalDate s = java.time.LocalDate.parse(h.getStartDate());
                        java.time.LocalDate e = java.time.LocalDate.parse(h.getEndDate());
                        if ((date.isEqual(s) || date.isAfter(s)) && (date.isEqual(e) || date.isBefore(e))) {
                            working = false;
                            break;
                        }
                    }
                }
            }
            // 调休：按当日调整覆盖休息/工作
            if (settings.getAdjustments() != null) {
                for (Adjustment a : settings.getAdjustments()) {
                    if (a.getDate() != null && date.equals(java.time.LocalDate.parse(a.getDate()))) {
                        if ("WORK".equalsIgnoreCase(a.getType())) working = true;
                        else if ("OFF".equalsIgnoreCase(a.getType())) working = false;
                    }
                }
            }
        }
    
        // 非工作日不判定迟到/早退
        if (!working) return "normal";
    
        java.time.LocalTime time = r.getAttendanceTime().toLocalTime();
        Integer lateGrace = rule.getLateGraceMin() != null ? rule.getLateGraceMin() : 0;
        Integer earlyGrace = rule.getEarlyGraceMin() != null ? rule.getEarlyGraceMin() : 0;
    
        if ("absence".equalsIgnoreCase(String.valueOf(r.getStatus()))) {
            return "absence";
        }
    
        // 计算参考开始/结束时间：优先使用时段配置，否则用规则上下学时间
        java.time.LocalTime startRef = rule.getWorkStart();
        java.time.LocalTime endRef = rule.getWorkEnd();
        if (settings != null && settings.getPeriods() != null && !settings.getPeriods().isEmpty()) {
            java.util.Optional<java.time.LocalTime> minStart = settings.getPeriods().stream()
                .filter(p -> p.getStart() != null)
                .map(p -> java.time.LocalTime.parse(p.getStart()))
                .min(java.util.Comparator.naturalOrder());
            java.util.Optional<java.time.LocalTime> maxEnd = settings.getPeriods().stream()
                .filter(p -> p.getEnd() != null)
                .map(p -> java.time.LocalTime.parse(p.getEnd()))
                .max(java.util.Comparator.naturalOrder());
            if (minStart.isPresent()) startRef = minStart.get();
            if (maxEnd.isPresent()) endRef = maxEnd.get();
        }
    
        if ("in".equalsIgnoreCase(String.valueOf(r.getAttendanceType()))) {
            if (startRef != null) {
                java.time.LocalTime threshold = startRef.plusMinutes(lateGrace);
                if (time.isAfter(threshold)) return "late";
            }
            return "normal";
        } else if ("out".equalsIgnoreCase(String.valueOf(r.getAttendanceType()))) {
            if (endRef != null) {
                java.time.LocalTime threshold = endRef.minusMinutes(earlyGrace);
                if (time.isBefore(threshold)) return "early";
            }
            return "normal";
        }
        return "normal";
    }

    // 统计：返回与前端字段一致的结构（支持规则驱动判定）
    public RuleStatistics getStatistics(Long classId, LocalDate startDate, LocalDate endDate, String status, String checkType) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30);
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();

        List<AttendanceRecord> records;
        if (classId != null) {
            records = attendanceRecordRepository.findByClassIdAndAttendanceTimeBetween(classId, startDateTime, endDateTime);
        } else {
            records = attendanceRecordRepository.findByAttendanceTimeBetween(startDateTime, endDateTime);
        }

        if (checkType != null && !checkType.isEmpty()) {
            records = records.stream().filter(r -> checkType.equals(r.getCheckType())).collect(Collectors.toList());
        }

        AttendanceRule rule = getRuleOrDefault();
        boolean ruleEnabled = rule.getEnabled() == null || rule.getEnabled();

        long normal = 0L, late = 0L, early = 0L, absence = 0L;
        for (AttendanceRecord r : records) {
            String computed = ruleEnabled ? classifyStatusByRule(rule, r) : String.valueOf(r.getStatus());
            // 根据查询参数过滤（使用计算后的状态）
            if (status != null && !status.isEmpty() && !status.equalsIgnoreCase(computed)) {
                continue;
            }
            if ("absence".equalsIgnoreCase(computed)) absence++;
            else if ("late".equalsIgnoreCase(computed)) late++;
            else if ("early".equalsIgnoreCase(computed)) early++;
            else normal++;
        }
        long total = normal + late + early + absence;

        RuleStatistics stats = new RuleStatistics();
        stats.setTotalCount(total);
        stats.setNormalCount(normal);
        stats.setLateCount(late);
        stats.setEarlyCount(early);
        stats.setAbsenceCount(absence);
        stats.setAttendanceRate(total > 0 ? (double)(normal + late + early) / (double) total : 0d);
        return stats;
    }

    // 学生记录查询（保留原有接口功能）
    public List<AttendanceRecord> getStudentRecords(Long studentId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(30);
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        return attendanceRecordRepository.findByStudentIdAndAttendanceTimeBetween(studentId, startDateTime, endDateTime);
    }

    @Data
    public static class RuleStatistics {
        private Long totalCount;
        private Long normalCount;
        private Long lateCount;
        private Long earlyCount;
        private Long absenceCount;
        private Double attendanceRate;
    }

    public AttendanceRecord ingestCard(com.campus.card.admin.controller.AttendanceController.CardIngestReq req) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String cardNo = req.getCardNo();
        com.campus.card.admin.domain.Card card = cardRepository.findByCardNo(cardNo).orElse(null);
        if (card == null) throw new IllegalArgumentException("卡片不存在: " + cardNo);
        if (card.getHolderType() == null || !"STUDENT".equalsIgnoreCase(card.getHolderType())) {
            throw new IllegalArgumentException("当前仅支持学生刷卡考勤");
        }
        // 修复：holderId存储为学号(student_no)，应按学号查询学生
        com.campus.card.admin.domain.StudentInfo student = studentRepository.findByStudentNo(card.getHolderId()).orElse(null);
        if (student == null) throw new IllegalStateException("未找到持卡学生学号: " + card.getHolderId());
        com.campus.card.admin.domain.AttendanceRecord record = new com.campus.card.admin.domain.AttendanceRecord();
        record.setStudentId(student.getId());
        record.setStudentName(student.getName());
        record.setStudentNo(student.getStudentNo());
        record.setClassId(student.getClassId());
        record.setAttendanceTime(req.getAttendanceTime() != null ? req.getAttendanceTime() : now);
        record.setAttendanceType(req.getAttendanceType() != null ? req.getAttendanceType() : "in");
        record.setCheckType("card");
        record.setRemark(req.getRemark());
        if (req.getDeviceId() != null) {
            java.util.Optional<com.campus.card.admin.domain.Device> od = deviceRepository.findById(req.getDeviceId());
            od.ifPresent(d -> { record.setDeviceId(d.getId()); record.setDeviceName(d.getName()); });
        }
        record.setStatus("normal");
        return attendanceRecordRepository.save(record);
    }

    @Data
    public static class FaceSuccessRate {
        private long totalAttempts;
        private long successCount;
        private double successRate;
    }

    public FaceSuccessRate getFaceSuccessRate(LocalDate startDate, LocalDate endDate, Long classId) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(7);
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();
        FaceSuccessRate rate = new FaceSuccessRate();
        if (classId == null) {
            long total = faceRecognitionLogRepository.countByOccurredAtBetween(start, end);
            long succ = faceRecognitionLogRepository.countBySuccessIsTrueAndOccurredAtBetween(start, end);
            rate.setTotalAttempts(total);
            rate.setSuccessCount(succ);
            rate.setSuccessRate(total > 0 ? (double) succ / (double) total : 0d);
        } else {
            java.util.List<com.campus.card.admin.domain.StudentInfo> students = studentRepository.findByClassId(classId);
            java.util.Set<String> studentNos = students.stream().map(com.campus.card.admin.domain.StudentInfo::getStudentNo).collect(java.util.stream.Collectors.toSet());
            java.util.List<FaceRecognitionLog> logs = faceRecognitionLogRepository.findByOccurredAtBetween(start, end);
            long total = logs.stream().filter(l -> "STUDENT".equalsIgnoreCase(l.getPersonType()) && studentNos.contains(l.getPersonId())).count();
            long succ = logs.stream().filter(l -> Boolean.TRUE.equals(l.getSuccess()) && "STUDENT".equalsIgnoreCase(l.getPersonType()) && studentNos.contains(l.getPersonId())).count();
            rate.setTotalAttempts(total);
            rate.setSuccessCount(succ);
            rate.setSuccessRate(total > 0 ? (double) succ / (double) total : 0d);
        }
        return rate;
    }
    public com.campus.card.admin.controller.AttendanceController.FaceIngestResp ingestFace(com.campus.card.admin.controller.AttendanceController.FaceIngestReq req) {
        // 记录人脸识别尝试
        FaceRecognitionLog log = new FaceRecognitionLog();
        log.setDeviceId(req.getDeviceId());
        log.setPersonType(req.getPersonType());
        log.setPersonId(req.getPersonId());
        log.setScore(req.getScore());
        log.setSuccess(Boolean.TRUE.equals(req.getSuccess()));
        log.setPhotoUrl(req.getPhotoUrl());
        log.setOccurredAt(req.getAttendanceTime() != null ? req.getAttendanceTime() : java.time.LocalDateTime.now());
        log.setRemark(req.getRemark());
        faceRecognitionLogRepository.save(log);
        // 原有成功入库逻辑
        com.campus.card.admin.controller.AttendanceController.FaceIngestResp resp = new com.campus.card.admin.controller.AttendanceController.FaceIngestResp();
        resp.setLogged(true);
        if (Boolean.TRUE.equals(req.getSuccess()) && "STUDENT".equalsIgnoreCase(req.getPersonType())) {
            com.campus.card.admin.domain.StudentInfo student = studentRepository.findByStudentNo(req.getPersonId()).orElse(null);
            if (student != null) {
                com.campus.card.admin.domain.AttendanceRecord record = new com.campus.card.admin.domain.AttendanceRecord();
                record.setStudentId(student.getId());
                record.setStudentName(student.getName());
                record.setStudentNo(student.getStudentNo());
                record.setClassId(student.getClassId());
                record.setAttendanceTime(req.getAttendanceTime() != null ? req.getAttendanceTime() : java.time.LocalDateTime.now());
                record.setAttendanceType(req.getAttendanceType() != null ? req.getAttendanceType() : "in");
                record.setCheckType("face");
                record.setPhotoUrl(req.getPhotoUrl());
                record.setRemark(req.getRemark());
                if (req.getDeviceId() != null) {
                    java.util.Optional<com.campus.card.admin.domain.Device> od = deviceRepository.findById(req.getDeviceId());
                    od.ifPresent(d -> { record.setDeviceId(d.getId()); record.setDeviceName(d.getName()); });
                }
                record.setStatus("normal");
                resp.setRecord(attendanceRecordRepository.save(record));
            }
        }
        return resp;
    }

    /**
     * 解析工作日字符串为DayOfWeek集合
     * @param rule 考勤规则
     * @return 工作日集合
     */
    private Set<DayOfWeek> parseWorkDays(AttendanceRule rule) {
        Set<DayOfWeek> workDays = new HashSet<>();
        if (rule == null || rule.getWorkDays() == null || rule.getWorkDays().trim().isEmpty()) {
            // 默认工作日：周一到周五
            workDays.add(DayOfWeek.MONDAY);
            workDays.add(DayOfWeek.TUESDAY);
            workDays.add(DayOfWeek.WEDNESDAY);
            workDays.add(DayOfWeek.THURSDAY);
            workDays.add(DayOfWeek.FRIDAY);
            return workDays;
        }

        String[] days = rule.getWorkDays().split(",");
        for (String day : days) {
            String trimmedDay = day.trim().toUpperCase();
            switch (trimmedDay) {
                case "MON":
                case "MONDAY":
                    workDays.add(DayOfWeek.MONDAY);
                    break;
                case "TUE":
                case "TUESDAY":
                    workDays.add(DayOfWeek.TUESDAY);
                    break;
                case "WED":
                case "WEDNESDAY":
                    workDays.add(DayOfWeek.WEDNESDAY);
                    break;
                case "THU":
                case "THURSDAY":
                    workDays.add(DayOfWeek.THURSDAY);
                    break;
                case "FRI":
                case "FRIDAY":
                    workDays.add(DayOfWeek.FRIDAY);
                    break;
                case "SAT":
                case "SATURDAY":
                    workDays.add(DayOfWeek.SATURDAY);
                    break;
                case "SUN":
                case "SUNDAY":
                    workDays.add(DayOfWeek.SUNDAY);
                    break;
                default:
                    // 忽略无效的工作日配置
                    break;
            }
        }

        // 如果解析后没有有效的工作日，返回默认的周一到周五
        if (workDays.isEmpty()) {
            workDays.add(DayOfWeek.MONDAY);
            workDays.add(DayOfWeek.TUESDAY);
            workDays.add(DayOfWeek.WEDNESDAY);
            workDays.add(DayOfWeek.THURSDAY);
            workDays.add(DayOfWeek.FRIDAY);
        }

        return workDays;
    }

    // 新增：考勤设置结构（节假日、调休、时段、请假类型、旷课规则）
    @Data
    public static class AttendanceSettings {
        private List<TimePeriod> periods; // 考勤时段设置
        private Integer absenceThresholdMin; // 旷课阈值（超过该分钟视为缺勤）
        private List<String> leaveTypes; // 请假类型列表
        private List<Holiday> holidays; // 节假日
        private List<Adjustment> adjustments; // 调休设置
    }

    @Data
    public static class TimePeriod {
        private String name; // 例如 早晨/下午
        private String start; // HH:mm:ss
        private String end;   // HH:mm:ss
    }

    @Data
    public static class Holiday {
        private String name; // 节假日名称
        private String startDate; // yyyy-MM-dd
        private String endDate;   // yyyy-MM-dd
    }

    @Data
    public static class Adjustment {
        private String date; // yyyy-MM-dd
        private String type; // WORK 或 OFF
        private String name; // 说明（可选）
    }

    public AttendanceSettings getSettingsOrDefault() {
        String json = stringRedisTemplate.opsForValue().get(KEY_SETTINGS);
        if (json == null || json.isEmpty()) {
            AttendanceSettings s = new AttendanceSettings();
            s.setAbsenceThresholdMin(30);
            s.setLeaveTypes(Arrays.asList("事假", "病假"));
            s.setPeriods(Collections.emptyList());
            s.setHolidays(Collections.emptyList());
            s.setAdjustments(Collections.emptyList());
            return s;
        }
        try {
            return objectMapper.readValue(json, AttendanceSettings.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // 若解析失败，返回默认
            AttendanceSettings s = new AttendanceSettings();
            s.setAbsenceThresholdMin(30);
            s.setLeaveTypes(Arrays.asList("事假", "病假"));
            s.setPeriods(Collections.emptyList());
            s.setHolidays(Collections.emptyList());
            s.setAdjustments(Collections.emptyList());
            return s;
        }
    }

    public AttendanceSettings saveSettings(AttendanceSettings settings) {
        try {
            String json = objectMapper.writeValueAsString(settings);
            stringRedisTemplate.opsForValue().set(KEY_SETTINGS, json);
            return settings;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("保存考勤设置失败", e);
        }
    }

    public AttendanceRecord markException(Long id, String status, String reason) {
        java.util.Optional<AttendanceRecord> or = attendanceRecordRepository.findById(id);
        if (!or.isPresent()) throw new IllegalArgumentException("考勤记录不存在: " + id);
        AttendanceRecord r = or.get();
        if (status != null && !status.isEmpty()) {
            r.setStatus(status);
        }
        String remark = r.getRemark();
        String append = (reason != null && !reason.isEmpty()) ? ("[异常标记] " + reason) : "[异常标记]";
        r.setRemark(remark == null || remark.isEmpty() ? append : (remark + " | " + append));
        return attendanceRecordRepository.save(r);
    }

    public AttendanceRecord correctRecord(Long id, java.time.LocalDateTime attendanceTime, String attendanceType, String status, String reason) {
        java.util.Optional<AttendanceRecord> or = attendanceRecordRepository.findById(id);
        if (!or.isPresent()) throw new IllegalArgumentException("考勤记录不存在: " + id);
        AttendanceRecord r = or.get();
        if (attendanceTime != null) r.setAttendanceTime(attendanceTime);
        if (attendanceType != null && !attendanceType.isEmpty()) r.setAttendanceType(attendanceType);
        if (status != null && !status.isEmpty()) r.setStatus(status);
        String remark = r.getRemark();
        String append = (reason != null && !reason.isEmpty()) ? ("[修正] " + reason) : "[修正]";
        r.setRemark(remark == null || remark.isEmpty() ? append : (remark + " | " + append));
        return attendanceRecordRepository.save(r);
    }

    public AttendanceRecord supplementRecord(Long studentId, String studentNo, String attendanceType, java.time.LocalDateTime attendanceTime, String checkType, Long deviceId, String remark) {
        com.campus.card.admin.domain.StudentInfo student = null;
        if (studentId != null) {
            student = studentRepository.findById(studentId).orElse(null);
        }
        if (student == null && studentNo != null && !studentNo.isEmpty()) {
            student = studentRepository.findByStudentNo(studentNo).orElse(null);
        }
        if (student == null) {
            throw new IllegalArgumentException("未找到学生信息");
        }
        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(student.getId());
        record.setStudentName(student.getName());
        record.setStudentNo(student.getStudentNo());
        record.setClassId(student.getClassId());
        record.setAttendanceTime(attendanceTime != null ? attendanceTime : java.time.LocalDateTime.now());
        record.setAttendanceType(attendanceType != null ? attendanceType : "in");
        record.setCheckType((checkType != null && !checkType.isEmpty()) ? checkType : "manual");
        if (deviceId != null) {
            java.util.Optional<com.campus.card.admin.domain.Device> od = deviceRepository.findById(deviceId);
            od.ifPresent(d -> { record.setDeviceId(d.getId()); record.setDeviceName(d.getName()); });
        } else {
            record.setDeviceName("手工补录");
        }
        String append = "[补录]" + (remark != null && !remark.isEmpty() ? (" " + remark) : "");
        record.setRemark(append);
        record.setStatus("normal");
        return attendanceRecordRepository.save(record);
    }
}