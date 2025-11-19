package com.campus.card.wechat.controller;

import com.campus.card.wechat.model.Notification;
import com.campus.card.wechat.repository.NotificationRepository;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/notify")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /** 通知列表 */
    @GetMapping("/list")
    public Resp<Page<Notification>> list(@RequestParam Long childId,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(defaultValue = "false") boolean unreadOnly) {
        PageRequest pr = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        Page<Notification> p;
        if (unreadOnly) {
            p = (category == null || category.trim().isEmpty())
                    ? notificationRepository.findByChildIdAndReadFlagFalseOrderByCreatedAtDesc(childId, pr)
                    : notificationRepository.findByChildIdAndReadFlagFalseAndCategoryOrderByCreatedAtDesc(childId, category, pr);
        } else {
            p = (category == null || category.trim().isEmpty())
                    ? notificationRepository.findByChildIdOrderByCreatedAtDesc(childId, pr)
                    : notificationRepository.findByChildIdAndCategoryOrderByCreatedAtDesc(childId, category, pr);
        }
        return Resp.ok(p);
    }

    /** 未读数量 */
    @GetMapping("/unread_count")
    public Resp<Long> unreadCount(@RequestParam Long childId,
                                  @RequestParam(required = false) String category) {
        long cnt = (category == null || category.trim().isEmpty())
                ? notificationRepository.countByChildIdAndReadFlagFalse(childId)
                : notificationRepository.countByChildIdAndReadFlagFalseAndCategory(childId, category);
        return Resp.ok(cnt);
    }

    /** 未读统计：总数与各分类分布 */
    @GetMapping("/stats")
    public Resp<StatsResp> stats(@RequestParam Long childId) {
        StatsResp s = new StatsResp();
        long total = notificationRepository.countByChildIdAndReadFlagFalse(childId);
        s.setTotalUnread(total);
        java.util.Map<String, Long> dist = new java.util.HashMap<>();
        String[] cats = new String[]{"ATTENDANCE","CONSUME","LOW_BALANCE","LEAVE_APPROVAL","ANNOUNCEMENT","CLASS_NOTICE"};
        for (String c : cats) {
            try { dist.put(c, notificationRepository.countByChildIdAndReadFlagFalseAndCategory(childId, c)); } catch (Exception ignore) {}
        }
        s.setCategoryUnread(dist);
        return Resp.ok(s);
    }

    @Data
    public static class StatsResp {
        private long totalUnread;
        private java.util.Map<String, Long> categoryUnread;
    }

    /** 标记已读 */
    @PostMapping("/mark_read")
    public Resp<Boolean> markRead(@RequestBody MarkReadReq req) {
        if (req.getId() != null) {
            Optional<Notification> opt = notificationRepository.findById(req.getId());
            opt.ifPresent(n -> { n.setReadFlag(true); notificationRepository.save(n); });
            return Resp.ok(opt.isPresent());
        }
        if (req.getIds() != null && !req.getIds().isEmpty()) {
            List<Notification> list = notificationRepository.findAllById(req.getIds());
            list.forEach(n -> n.setReadFlag(true));
            notificationRepository.saveAll(list);
            return Resp.ok(true);
        }
        return Resp.ok(false);
    }

    /** 实时拉取：返回最近5条未读 */
    @GetMapping("/realtime")
    public Resp<List<Notification>> realtime(@RequestParam Long childId) {
        List<Notification> list = notificationRepository.findTop5ByChildIdAndReadFlagFalseOrderByCreatedAtDesc(childId);
        return Resp.ok(list);
    }

    /** 单条查询：根据ID获取通知详情 */
    @GetMapping("/item")
    public Resp<Notification> item(@RequestParam Long id) {
        Optional<Notification> opt = notificationRepository.findById(id);
        if (opt.isPresent()) return Resp.ok(opt.get());
        return Resp.error("notification not found", 404);
    }

    /** 联调用：生成模拟通知 */
    @PostMapping("/mock/generate")
    public Resp<Integer> mockGenerate(@RequestBody MockGenerateReq req) {
        Long childId = req.getChildId();
        int count = 0;
        count += save(childId, "ATTENDANCE", "考勤提醒", "今日早读已签到", null);
        count += save(childId, "CONSUME", "消费提醒", "食堂消费 12.00 元", "{\"amount\":12.00,\"place\":\"一食堂\"}");
        count += save(childId, "LOW_BALANCE", "余额不足", "卡片余额低于 10 元，请尽快充值", "{\"threshold\":10}");
        count += save(childId, "LEAVE_APPROVAL", "请假审批通知", "学生请假已审批通过", null);
        count += save(childId, "ANNOUNCEMENT", "学校通知公告", "本周五举行运动会，请合理安排", null);
        return Resp.ok(count);
    }

    /** 发布通知：面向指定 childId 发布一条消息并持久化 */
    @PostMapping("/publish")
    public Resp<Long> publish(@RequestBody PublishReq req) {
        if (req == null || req.getChildId() == null || req.getChildId() <= 0) {
            return Resp.error("childId invalid", 400);
        }
        if (req.getCategory() == null || req.getCategory().trim().isEmpty()) {
            return Resp.error("category required", 400);
        }
        Notification n = new Notification();
        n.setChildId(req.getChildId());
        n.setCategory(req.getCategory().trim());
        n.setTitle(req.getTitle() == null ? "" : req.getTitle().trim());
        n.setContent(req.getContent() == null ? "" : req.getContent().trim());
        n.setExtra(req.getExtra());
        n.setReadFlag(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
        return Resp.ok(n.getId());
    }

    private int save(Long childId, String category, String title, String content, String extra) {
        Notification n = new Notification();
        n.setChildId(childId);
        n.setCategory(category);
        n.setTitle(title);
        n.setContent(content);
        n.setExtra(extra);
        n.setReadFlag(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
        return 1;
    }

    @Data
    public static class MarkReadReq {
        private Long id;
        private List<Long> ids;
    }

    @Data
    public static class MockGenerateReq {
        private Long childId;
    }

    @Data
    public static class PublishReq {
        private Long childId;
        private String category;
        private String title;
        private String content;
        private String extra;
    }

    /** 轻量响应包装，保持与前端约定的结构一致 */
    public static class Resp<T> {
        private int code;
        private String message;
        private T data;

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }

        public static <T> Resp<T> ok(T data) {
            Resp<T> r = new Resp<>();
            r.setCode(0);
            r.setMessage("success");
            r.setData(data);
            return r;
        }

        public static <T> Resp<T> error(String message, int code) {
            Resp<T> r = new Resp<>();
            r.setCode(code);
            r.setMessage(message);
            r.setData(null);
            return r;
        }
    }
}