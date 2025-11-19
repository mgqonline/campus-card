package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import com.campus.card.wechat.model.LeaveApplication;
import com.campus.card.wechat.model.Student;
import com.campus.card.wechat.repository.LeaveApplicationRepository;
import com.campus.card.wechat.repository.StudentRepository;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Optional;

/**
 * 教师端请假管理（老师帮请）
 * 路由前缀使用 /api/v1/t/leave 以确保前端自动附带教师端 token。
 */
@RestController
@RequestMapping("/api/v1/t/leave")
public class TeacherLeaveController {

    private final LeaveApplicationRepository leaveRepo;
    private final StudentRepository studentRepository;

    public TeacherLeaveController(LeaveApplicationRepository leaveRepo,
                                  StudentRepository studentRepository) {
        this.leaveRepo = leaveRepo;
        this.studentRepository = studentRepository;
    }

    /** 老师帮请：为指定学生创建请假记录，默认进入待审批 */
    @PostMapping("/help_apply")
    public Result<ApplyResp> helpApply(@RequestBody ApplyReq req) {
        if (req.getChildId() == null) {
            return Result.error("childId必填", 400);
        }
        Optional<Student> os = studentRepository.findById(req.getChildId());
        if (!os.isPresent()) {
            return Result.error("学生不存在", 404);
        }
        Student s = os.get();

        LeaveApplication entity = new LeaveApplication();
        entity.setChildId(req.getChildId());
        entity.setClassId(s.getClassId());
        entity.setType(req.getType() != null ? req.getType() : "OTHER");
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm[:ss]");
            if (req.getStartTime() != null) entity.setStartTime(LocalDateTime.parse(req.getStartTime(), fmt));
            if (req.getEndTime() != null) entity.setEndTime(LocalDateTime.parse(req.getEndTime(), fmt));
        } catch (DateTimeParseException e) {
            return Result.error("时间格式不正确，应为ISO日期时间", 400);
        }
        entity.setReason(req.getReason());
        entity.setAttachmentsJson(req.getAttachments() != null ? toJsonArray(req.getAttachments()) : toJsonArray(Collections.emptyList()));
        // 老师帮请：进入待审批，后续由教师审批
        entity.setStatus("PENDING");
        entity.setApplyTime(LocalDateTime.now());

        LeaveApplication saved = leaveRepo.save(entity);
        ApplyResp resp = new ApplyResp();
        resp.setId(saved.getId());
        resp.setStatus(saved.getStatus());
        return Result.ok(resp);
    }

    private static String toJsonArray(java.util.List<String> list) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < list.size(); i++) {
            sb.append('"').append(list.get(i).replace("\"", "\\\"")).append('"');
            if (i < list.size() - 1) sb.append(',');
        }
        sb.append(']');
        return sb.toString();
    }

    @Data
    public static class ApplyReq {
        private Long childId;
        private String type; // SICK/PERSONAL/OTHER
        private String startTime; // ISO 日期时间字符串
        private String endTime;
        private String reason;
        private java.util.List<String> attachments;
    }

    @Data
    public static class ApplyResp {
        private Long id;
        private String status; // PENDING/APPROVED/REJECTED/CANCELED
    }
}