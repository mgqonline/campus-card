package com.campus.card.admin.controller;

import com.campus.card.admin.domain.AttendanceRecord;
import com.campus.card.admin.domain.AttendanceRule;
import com.campus.card.admin.service.AttendanceService;
import com.campus.card.admin.service.AttendanceStreamService;
import com.campus.card.admin.queue.AttendanceQueueProducer;
import com.campus.card.admin.service.OrgScopeService;
import com.campus.card.admin.repository.UserRepository;
import com.campus.card.admin.domain.User;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceStreamService attendanceStreamService;
    private final com.campus.card.admin.queue.AttendanceQueueProducer queueProducer;
    private final OrgScopeService orgScopeService;
    private final UserRepository userRepository;

    public AttendanceController(AttendanceService attendanceService, AttendanceStreamService attendanceStreamService) {
        this.attendanceService = attendanceService;
        this.attendanceStreamService = attendanceStreamService;
        this.queueProducer = null; // will be autowired via another constructor
        this.orgScopeService = null;
        this.userRepository = null;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AttendanceController(AttendanceService attendanceService, AttendanceStreamService attendanceStreamService,
                                com.campus.card.admin.queue.AttendanceQueueProducer queueProducer,
                                OrgScopeService orgScopeService,
                                UserRepository userRepository) {
        this.attendanceService = attendanceService;
        this.attendanceStreamService = attendanceStreamService;
        this.queueProducer = queueProducer;
        this.orgScopeService = orgScopeService;
        this.userRepository = userRepository;
    }

    @GetMapping("/records")
    public Result<PageResult<AttendanceRecord>> listRecords(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "Authorization", required = false) String auth
    ) {
        // 解析当前用户并计算数据范围
        Long userId = null;
        if (auth != null && !auth.isEmpty()) {
            String token = auth.trim();
            if (token.toLowerCase(java.util.Locale.ROOT).startsWith("bearer ")) token = token.substring(7).trim();
            if (token.startsWith("admin-token-")) {
                String tail = token.substring("admin-token-".length());
                try { userId = Long.parseLong(tail); } catch (Exception ignored) {}
            }
        }
        java.util.List<Long> allowedClassIds = null;
        java.util.Set<Long> allowedStudentIds = null;
        if (userId != null && orgScopeService != null && userRepository != null) {
            try {
                java.util.Optional<User> ou = userRepository.findByIdWithRolesPermsMenus(userId);
                if (ou.isPresent()) {
                    OrgScopeService.EffectiveScope eff = orgScopeService.resolveForUser(ou.get());
                    if (eff != null) {
                        if (eff.classIds != null && !eff.classIds.isEmpty()) {
                            allowedClassIds = new java.util.ArrayList<>(eff.classIds);
                        }
                        if (eff.studentIds != null && !eff.studentIds.isEmpty()) {
                            allowedStudentIds = eff.studentIds;
                        }
                    }
                }
            } catch (Exception ignored) {
                // 数据范围表可能尚未创建或查询异常，忽略以保证列表正常返回
            }
        }
        return Result.ok(attendanceService.pageList(page, size, studentId, classId, startDate, endDate, status,
                allowedClassIds, allowedStudentIds));
    }

    @GetMapping("/statistics")
    public Result<AttendanceService.RuleStatistics> getStatistics(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String checkType
    ) {
        return Result.ok(attendanceService.getStatistics(classId, startDate, endDate, status, checkType));
    }

    @GetMapping("/students/{studentId}/records")
    public Result<List<AttendanceRecord>> getStudentRecords(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return Result.ok(attendanceService.getStudentRecords(studentId, startDate, endDate));
    }

    @GetMapping("/rules")
    public Result<AttendanceRule> getRule() {
        return Result.ok(attendanceService.getRuleOrDefault());
    }

    @PostMapping("/rules")
    public Result<AttendanceRule> saveRule(@RequestBody AttendanceRule rule) {
        return Result.ok(attendanceService.saveRule(rule));
    }

    // 新增：考勤设置（节假日/调休/时段/请假类型/旷课规则）
    @GetMapping("/settings")
    public Result<AttendanceService.AttendanceSettings> getSettings() {
        return Result.ok(attendanceService.getSettingsOrDefault());
    }

    @PutMapping("/settings")
    public Result<AttendanceService.AttendanceSettings> saveSettings(@RequestBody AttendanceService.AttendanceSettings settings) {
        return Result.ok(attendanceService.saveSettings(settings));
    }

    @PostMapping("/ingest/card")
    public Result<AttendanceRecord> ingestCard(@RequestBody CardIngestReq req) {
        AttendanceRecord record = attendanceService.ingestCard(req);
        attendanceStreamService.emit(record);
        return Result.ok(record);
    }

    @PostMapping("/ingest/queue/card")
    public Result<String> enqueueCard(@RequestBody CardIngestReq req) {
        queueProducer.enqueueCard(req);
        return Result.ok("queued");
    }

    @PostMapping("/ingest/queue/card/bulk")
    public Result<String> enqueueCardBulk(@RequestBody List<CardIngestReq> reqs) {
        if (reqs != null) {
            for (CardIngestReq r : reqs) {
                queueProducer.enqueueCard(r);
            }
        }
        return Result.ok("queued:" + (reqs == null ? 0 : reqs.size()));
    }

    @PostMapping("/ingest/face")
    public Result<FaceIngestResp> ingestFace(@RequestBody FaceIngestReq req) {
        FaceIngestResp resp = attendanceService.ingestFace(req);
        if (resp.getRecord() != null) {
            attendanceStreamService.emit(resp.getRecord());
        } else {
            attendanceStreamService.emit(resp);
        }
        return Result.ok(resp);
    }

    @PostMapping("/ingest/queue/face")
    public Result<String> enqueueFace(@RequestBody FaceIngestReq req) {
        queueProducer.enqueueFace(req);
        return Result.ok("queued");
    }

    @PostMapping("/ingest/queue/face/bulk")
    public Result<String> enqueueFaceBulk(@RequestBody List<FaceIngestReq> reqs) {
        if (reqs != null) {
            for (FaceIngestReq r : reqs) {
                queueProducer.enqueueFace(r);
            }
        }
        return Result.ok("queued:" + (reqs == null ? 0 : reqs.size()));
    }

    @GetMapping("/stream")
    public SseEmitter stream() {
        return attendanceStreamService.subscribe();
    }

    public static class CardIngestReq {
        private Long deviceId;
        private String cardNo;
        private String attendanceType; // in/out
        private java.time.LocalDateTime attendanceTime;
        private String remark;
        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
        public String getCardNo() { return cardNo; }
        public void setCardNo(String cardNo) { this.cardNo = cardNo; }
        public String getAttendanceType() { return attendanceType; }
        public void setAttendanceType(String attendanceType) { this.attendanceType = attendanceType; }
        public java.time.LocalDateTime getAttendanceTime() { return attendanceTime; }
        public void setAttendanceTime(java.time.LocalDateTime attendanceTime) { this.attendanceTime = attendanceTime; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    public static class FaceIngestReq {
        private Long deviceId;
        private String personType; // STUDENT/TEACHER
        private String personId; // 学号或工号
        private String attendanceType; // in/out
        private java.time.LocalDateTime attendanceTime;
        private Double score;
        private Boolean success;
        private String photoUrl;
        private String remark;
        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
        public String getPersonType() { return personType; }
        public void setPersonType(String personType) { this.personType = personType; }
        public String getPersonId() { return personId; }
        public void setPersonId(String personId) { this.personId = personId; }
        public String getAttendanceType() { return attendanceType; }
        public void setAttendanceType(String attendanceType) { this.attendanceType = attendanceType; }
        public java.time.LocalDateTime getAttendanceTime() { return attendanceTime; }
        public void setAttendanceTime(java.time.LocalDateTime attendanceTime) { this.attendanceTime = attendanceTime; }
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        public String getPhotoUrl() { return photoUrl; }
        public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    public static class FaceIngestResp {
        private boolean logged;
        private AttendanceRecord record;
        public boolean isLogged() { return logged; }
        public void setLogged(boolean logged) { this.logged = logged; }
        public AttendanceRecord getRecord() { return record; }
        public void setRecord(AttendanceRecord record) { this.record = record; }
    }

    @GetMapping("/face/success-rate")
    public FaceSuccessRateResp getFaceSuccessRate(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                  @RequestParam(required = false) Long classId) {
        com.campus.card.admin.service.AttendanceService.FaceSuccessRate rate = attendanceService.getFaceSuccessRate(startDate, endDate, classId);
        FaceSuccessRateResp resp = new FaceSuccessRateResp();
        resp.setTotalAttempts(rate.getTotalAttempts());
        resp.setSuccessCount(rate.getSuccessCount());
        resp.setSuccessRate(rate.getSuccessRate());
        return resp;
    }

    public static class FaceSuccessRateResp {
        private long totalAttempts;
        private long successCount;
        private double successRate;
        public long getTotalAttempts() { return totalAttempts; }
        public void setTotalAttempts(long totalAttempts) { this.totalAttempts = totalAttempts; }
        public long getSuccessCount() { return successCount; }
        public void setSuccessCount(long successCount) { this.successCount = successCount; }
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
    }

    @PutMapping("/api/v1/attendance/records/{id}/exception")
    public AttendanceRecord markException(@PathVariable Long id, @RequestBody MarkExceptionReq req) {
        return attendanceService.markException(id, req.status, req.reason);
    }

    @PutMapping("/api/v1/attendance/records/{id}/correct")
    public AttendanceRecord correctRecord(@PathVariable Long id, @RequestBody CorrectReq req) {
        return attendanceService.correctRecord(id, req.attendanceTime, req.attendanceType, req.status, req.reason);
    }

    @PostMapping("/api/v1/attendance/records/supplement")
    public AttendanceRecord supplementRecord(@RequestBody SupplementReq req) {
        return attendanceService.supplementRecord(req.studentId, req.studentNo, req.attendanceType, req.attendanceTime, req.checkType, req.deviceId, req.remark);
    }

    static class MarkExceptionReq {
        public String status; // late/early/absence/normal
        public String reason;
    }
    static class CorrectReq {
        public java.time.LocalDateTime attendanceTime;
        public String attendanceType; // in/out
        public String status; // late/early/absence/normal
        public String reason;
    }
    static class SupplementReq {
        public Long studentId;
        public String studentNo;
        public String attendanceType; // in/out
        public java.time.LocalDateTime attendanceTime;
        public String checkType; // manual/card/face
        public Long deviceId;
        public String remark;
    }
}