package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import com.campus.card.wechat.model.Notification;
import com.campus.card.wechat.model.Student;
import com.campus.card.wechat.model.LeaveApplication;
import com.campus.card.wechat.repository.NotificationRepository;
import com.campus.card.wechat.repository.LeaveApplicationRepository;
import com.campus.card.wechat.repository.StudentRepository;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/leave")
public class LeaveController {

    private final StudentRepository studentRepository;
    private final NotificationRepository notificationRepository;
    private final LeaveApplicationRepository leaveRepo;
    // 当数据库不可用时的内存降级存储（仅用于保证前端演示不报错）
    private final Map<Long, List<LeaveRecord>> STORE = new HashMap<>();
    private long ID_SEQ = 10000L;

    public LeaveController(StudentRepository studentRepository,
                           NotificationRepository notificationRepository,
                           LeaveApplicationRepository leaveRepo) {
        this.studentRepository = studentRepository;
        this.notificationRepository = notificationRepository;
        this.leaveRepo = leaveRepo;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<UploadResp> upload(@RequestParam("file") MultipartFile file) {
        // 模拟上传，返回一个可用的占位URL
        String url = "https://example.com/uploads/" + (file != null ? Objects.requireNonNull(file.getOriginalFilename()) : ("file-" + System.currentTimeMillis()));
        UploadResp r = new UploadResp();
        r.setUrl(url);
        r.setName(file != null ? file.getOriginalFilename() : "附件");
        return Result.ok(r);
    }

    @PostMapping("/apply")
    public Result<ApplyResp> apply(@RequestBody ApplyReq req) {
        Long cid = req.getChildId() != null ? req.getChildId() : 2001L;
        Long clsId = studentRepository.findById(cid).map(Student::getClassId).orElse(null);
        LeaveApplication entity = new LeaveApplication();
        entity.setChildId(cid);
        entity.setClassId(clsId);
        entity.setType(req.getType());
        try {
            entity.setStartTime(LocalDateTime.parse(req.getStartTime()));
            entity.setEndTime(LocalDateTime.parse(req.getEndTime()));
        } catch (DateTimeParseException e) {
            return Result.error("时间格式不正确，应为ISO日期时间", 400);
        }
        entity.setReason(req.getReason());
        entity.setAttachmentsJson(req.getAttachments() != null ? toJsonArray(req.getAttachments()) : toJsonArray(Collections.emptyList()));
        entity.setStatus("PENDING");
        entity.setApplyTime(LocalDateTime.now());
        try {
            LeaveApplication saved = leaveRepo.save(entity);
            ApplyResp resp = new ApplyResp();
            resp.setId(saved.getId());
            resp.setStatus(saved.getStatus());
            return Result.ok(resp);
        } catch (Exception dbEx) {
            // 数据库异常时降级：写入内存，保证页面可用
            LeaveRecord rec = new LeaveRecord();
            rec.setId(++ID_SEQ);
            rec.setChildId(cid);
            rec.setType(entity.getType());
            rec.setStartTime(entity.getStartTime().toString());
            rec.setEndTime(entity.getEndTime().toString());
            rec.setReason(entity.getReason());
            rec.setAttachments(req.getAttachments() != null ? req.getAttachments() : new ArrayList<>());
            rec.setStatus("PENDING");
            rec.setApplyTime(entity.getApplyTime().toString());
            STORE.computeIfAbsent(cid, k -> new ArrayList<>()).add(rec);
            ApplyResp resp = new ApplyResp();
            resp.setId(rec.getId());
            resp.setStatus(rec.getStatus());
            return Result.ok(resp);
        }
    }

    @GetMapping("/records")
    public Result<List<LeaveRecord>> records(@RequestParam(required = false) Long childId,
                                             @RequestParam(required = false) String startDate,
                                             @RequestParam(required = false) String endDate,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "20") Integer size) {
        Long cid = childId != null ? childId : 2001L;
        try {
            List<LeaveApplication> items = leaveRepo.findByChildIdOrderByApplyTimeDesc(cid);
            List<LeaveRecord> list = new ArrayList<>();
            for (LeaveApplication a : items) list.add(toRecord(a));
            return Result.ok(list);
        } catch (Exception dbEx) {
            List<LeaveRecord> list = new ArrayList<>(STORE.getOrDefault(cid, Collections.emptyList()));
            list.sort(Comparator.comparing(LeaveRecord::getApplyTime).reversed());
            return Result.ok(list);
        }
    }

    @GetMapping("/status")
    public Result<List<StatusItem>> status(@RequestParam(required = false) Long childId) {
        Long cid = childId != null ? childId : 2001L;
        List<LeaveRecord> list = STORE.getOrDefault(cid, Collections.emptyList());
        List<StatusItem> res = new ArrayList<>();
        for (LeaveRecord r : list) {
            res.add(new StatusItem(r.getId(), r.getStatus()));
        }
        return Result.ok(res);
    }

    @PostMapping("/cancel")
    public Result<String> cancel(@RequestBody CancelReq req) {
        Long cid = req.getChildId() != null ? req.getChildId() : 2001L;
        Optional<LeaveApplication> found = leaveRepo.findById(req.getId());
        if (!found.isPresent() || !Objects.equals(found.get().getChildId(), cid)) {
            return Result.error("未找到记录", 404);
        }
        LeaveApplication a = found.get();
        if (!"PENDING".equals(a.getStatus())) {
            return Result.error("仅待审批的申请可撤销", 400);
        }
        a.setStatus("CANCELED");
        leaveRepo.save(a);
        return Result.ok("撤销成功");
    }

    @Data
    public static class UploadResp {
        private String url;
        private String name;
    }

    @Data
    public static class ApplyReq {
        private Long childId;
        private String type; // SICK/PERSONAL/OTHER
        private String startTime; // ISO 日期时间字符串
        private String endTime;
        private String reason;
        private List<String> attachments;
    }

    @Data
    public static class ApplyResp {
        private Long id;
        private String status; // PENDING/APPROVED/REJECTED/CANCELED
    }

    @Data
    public static class LeaveRecord {
        private Long id;
        private Long childId;
        private String type;
        private String startTime;
        private String endTime;
        private String reason;
        private List<String> attachments;
        private String status;
        private String applyTime;
    }

    @Data
    public static class StatusItem {
        private Long id;
        private String status;
        public StatusItem(Long id, String status) { this.id = id; this.status = status; }
    }

    @Data
    public static class CancelReq {
        private Long childId;
        private Long id;
    }

    /** 教师端：查询待审批列表（按班级） */
    @GetMapping("/teacher/pending")
    public Result<List<LeaveRecord>> teacherPending(@RequestParam Long classId) {
        try {
            List<LeaveApplication> items = leaveRepo.findByClassIdAndStatusOrderByApplyTimeDesc(classId, "PENDING");
            List<LeaveRecord> pending = new ArrayList<>();
            for (LeaveApplication a : items) pending.add(toRecord(a));
            return Result.ok(pending);
        } catch (Exception dbEx) {
            // 降级：从内存取
            List<Student> students = studentRepository.findAll();
            Set<Long> childIds = new HashSet<>();
            for (Student s : students) {
                if (s.getClassId() != null && s.getClassId().equals(classId)) {
                    childIds.add(s.getId());
                }
            }
            List<LeaveRecord> pending = new ArrayList<>();
            for (Long cid : childIds) {
                for (LeaveRecord r : STORE.getOrDefault(cid, Collections.emptyList())) {
                    if ("PENDING".equals(r.getStatus())) {
                        pending.add(r);
                    }
                }
            }
            pending.sort(Comparator.comparing(LeaveRecord::getApplyTime).reversed());
            return Result.ok(pending);
        }
    }

    /** 教师端：请假记录查询（按班级，含所有状态） */
    @GetMapping("/teacher/records")
    public Result<List<LeaveRecord>> teacherRecords(@RequestParam Long classId) {
        try {
            List<LeaveApplication> items = leaveRepo.findByClassIdOrderByApplyTimeDesc(classId);
            List<LeaveRecord> all = new ArrayList<>();
            for (LeaveApplication a : items) all.add(toRecord(a));
            return Result.ok(all);
        } catch (Exception dbEx) {
            List<Student> students = studentRepository.findAll();
            Set<Long> childIds = new HashSet<>();
            for (Student s : students) {
                if (s.getClassId() != null && s.getClassId().equals(classId)) {
                    childIds.add(s.getId());
                }
            }
            List<LeaveRecord> all = new ArrayList<>();
            for (Long cid : childIds) {
                all.addAll(STORE.getOrDefault(cid, Collections.emptyList()));
            }
            all.sort(Comparator.comparing(LeaveRecord::getApplyTime).reversed());
            return Result.ok(all);
        }
    }

    /** 测试造数：按班级批量生成待审批请假记录 */
    @GetMapping("/test/seed")
    public Result<List<LeaveRecord>> testSeed(@RequestParam Long classId,
                                              @RequestParam(defaultValue = "12") Integer count) {
        List<Student> students = studentRepository.findAll();
        List<Long> childIds = new ArrayList<>();
        for (Student s : students) {
            if (s.getClassId() != null && s.getClassId().equals(classId)) {
                childIds.add(s.getId());
            }
        }
        if (childIds.isEmpty()) {
            return Result.error("该班级无学生，无法造数", 404);
        }
        int n = Math.max(1, count != null ? count : 12);
        List<LeaveRecord> created = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Long cid = childIds.get(i % childIds.size());
            LocalDateTime base = LocalDateTime.now().minusDays(i % 7);
            LeaveApplication a = new LeaveApplication();
            a.setChildId(cid);
            a.setClassId(classId);
            a.setType((i % 2 == 0) ? "SICK" : "PERSONAL");
            a.setStartTime(base.minusHours(1));
            a.setEndTime(base.plusHours(2));
            a.setReason("示例请假 " + (i + 1));
            a.setAttachmentsJson(toJsonArray(Collections.singletonList("https://example.com/att-" + (i + 1) + ".jpg")));
            a.setStatus("PENDING");
            a.setApplyTime(base);
            try {
                a = leaveRepo.save(a);
                created.add(toRecord(a));
            } catch (Exception dbEx) {
                LeaveRecord r = new LeaveRecord();
                r.setId(++ID_SEQ);
                r.setChildId(cid);
                r.setType(a.getType());
                r.setStartTime(a.getStartTime().toString());
                r.setEndTime(a.getEndTime().toString());
                r.setReason(a.getReason());
                r.setAttachments(Collections.singletonList("https://example.com/att-" + (i + 1) + ".jpg"));
                r.setStatus("PENDING");
                r.setApplyTime(base.toString());
                STORE.computeIfAbsent(cid, k -> new ArrayList<>()).add(r);
                created.add(r);
            }
        }
        // 返回创建的记录，便于脚本后续审批
        created.sort(Comparator.comparing(LeaveRecord::getApplyTime).reversed());
        return Result.ok(created);
    }

    /** 教师端：审批单条申请 */
    @PostMapping("/teacher/approve")
    public Result<String> approve(@RequestBody ApproveReq req) {
        if (!"APPROVED".equals(req.getStatus()) && !"REJECTED".equals(req.getStatus())) {
            return Result.error("非法状态，仅支持 APPROVED/REJECTED", 400);
        }
        Optional<LeaveApplication> found = leaveRepo.findById(req.getId());
        if (!found.isPresent() || !Objects.equals(found.get().getChildId(), req.getChildId())) {
            return Result.error("未找到记录", 404);
        }
        LeaveApplication a = found.get();
        if (!"PENDING".equals(a.getStatus())) {
            return Result.error("仅待审批的申请可操作", 400);
        }
        a.setStatus(req.getStatus());
        a.setApproveTime(LocalDateTime.now());
        a.setApproverTeacherId(9001L);
        leaveRepo.save(a);
        notifyLeaveApproval(a.getChildId(), toRecord(a), req.getStatus());
        return Result.ok("审批成功");
    }

    /** 教师端：批量审批 */
    @PostMapping("/teacher/batch_approve")
    public Result<Integer> batchApprove(@RequestBody BatchApproveReq req) {
        if (!"APPROVED".equals(req.getStatus()) && !"REJECTED".equals(req.getStatus())) {
            return Result.error("非法状态，仅支持 APPROVED/REJECTED", 400);
        }
        int success = 0;
        for (Item it : req.getItems()) {
            Optional<LeaveApplication> found = leaveRepo.findById(it.getId());
            if (found.isPresent() && Objects.equals(found.get().getChildId(), it.getChildId())) {
                LeaveApplication a = found.get();
                if ("PENDING".equals(a.getStatus())) {
                    a.setStatus(req.getStatus());
                    a.setApproveTime(LocalDateTime.now());
                    a.setApproverTeacherId(9001L);
                    leaveRepo.save(a);
                    notifyLeaveApproval(a.getChildId(), toRecord(a), req.getStatus());
                    success++;
                }
            }
        }
        return Result.ok(success);
    }

    private void notifyLeaveApproval(Long childId, LeaveRecord r, String status) {
        try {
            Notification n = new Notification();
            n.setChildId(childId);
            n.setCategory("LEAVE_APPROVAL");
            n.setTitle("请假审批通知");
            n.setContent("学生" + childId + "的请假申请已" + ("APPROVED".equals(status) ? "通过" : "被驳回"));
            n.setExtra(String.format("{\"leaveId\":%d,\"type\":\"%s\"}", r.getId(), r.getType()));
            n.setReadFlag(false);
            n.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(n);
        } catch (Exception ignore) {
        }
    }

    private static LeaveRecord toRecord(LeaveApplication a) {
        LeaveRecord r = new LeaveRecord();
        r.setId(a.getId());
        r.setChildId(a.getChildId());
        r.setType(a.getType());
        r.setStartTime(a.getStartTime() != null ? a.getStartTime().toString() : "");
        r.setEndTime(a.getEndTime() != null ? a.getEndTime().toString() : "");
        r.setReason(a.getReason());
        r.setAttachments(fromJsonArray(a.getAttachmentsJson()));
        r.setStatus(a.getStatus());
        r.setApplyTime(a.getApplyTime() != null ? a.getApplyTime().toString() : "");
        return r;
    }

    private static String toJsonArray(List<String> list) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < list.size(); i++) {
            sb.append('"').append(list.get(i).replace("\"", "\\\"")).append('"');
            if (i < list.size() - 1) sb.append(',');
        }
        sb.append(']');
        return sb.toString();
    }

    private static List<String> fromJsonArray(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        String s = json.trim();
        if (!s.startsWith("[") || !s.endsWith("]")) return new ArrayList<>();
        s = s.substring(1, s.length() - 1).trim();
        if (s.isEmpty()) return new ArrayList<>();
        List<String> res = new ArrayList<>();
        String[] parts = s.split("\\s*,\\s*");
        for (String p : parts) {
            String t = p.trim();
            if (t.startsWith("\"") && t.endsWith("\"")) {
                res.add(t.substring(1, t.length() - 1).replace("\\\"", "\""));
            }
        }
        return res;
    }

    @Data
    public static class ApproveReq {
        private Long childId;
        private Long id;
        private String status; // APPROVED/REJECTED
    }

    @Data
    public static class BatchApproveReq {
        private List<Item> items; // [{childId,id}]
        private String status; // APPROVED/REJECTED
    }

    @Data
    public static class Item {
        private Long childId;
        private Long id;
    }
}