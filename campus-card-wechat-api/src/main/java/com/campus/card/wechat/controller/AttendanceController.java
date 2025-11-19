package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {
    private final com.campus.card.wechat.repository.AttendanceEventRepository eventRepo;
    private final com.campus.card.wechat.repository.AttendanceAlertRepository alertRepo;

    public AttendanceController(com.campus.card.wechat.repository.AttendanceEventRepository eventRepo,
                                com.campus.card.wechat.repository.AttendanceAlertRepository alertRepo) {
        this.eventRepo = eventRepo;
        this.alertRepo = alertRepo;
    }

    /**
     * 打卡签到
     */
    @PostMapping("/checkin")
    public Result<CheckInResp> checkIn(@RequestParam(required = false) Long childId) {
        Long cid = childId != null ? childId : 2001L;
        
        // 创建新的考勤事件
        com.campus.card.wechat.model.AttendanceEvent event = new com.campus.card.wechat.model.AttendanceEvent();
        event.setChildId(cid);
        event.setType("进校");
        event.setGate("东门");
        event.setTime(LocalDateTime.now());
        event.setPhotoUrl(samplePhoto());
        
        // 判断是否迟到（假设8:30之后为迟到）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lateTime = now.toLocalDate().atTime(8, 30);
        event.setLate(now.isAfter(lateTime));
        event.setEarlyLeave(false);
        
        // 保存事件
        eventRepo.save(event);
        
        CheckInResp resp = new CheckInResp();
        resp.setSuccess(true);
        resp.setMessage(event.getLate() ? "打卡成功，但您已迟到" : "打卡成功");
        resp.setTime(event.getTime());
        resp.setLate(event.getLate());
        
        return Result.ok(resp);
    }

    /**
     * 实时考勤（进校/离校）最近事件，用于轮询通知
     */
    @GetMapping("/realtime")
    public Result<RealtimeResp> realtime(@RequestParam(required = false) Long childId) {
        Long cid = childId != null ? childId : 2001L;
        RealtimeResp r = new RealtimeResp();
        r.setChildId(cid);
        // 最新一条事件
        org.springframework.data.domain.Pageable p = org.springframework.data.domain.PageRequest.of(0, 1);
        java.util.List<com.campus.card.wechat.model.AttendanceEvent> latest = eventRepo.findTopByChildIdOrderByTimeDesc(cid, p);
        if (!latest.isEmpty()) {
            com.campus.card.wechat.model.AttendanceEvent e = latest.get(0);
            r.setHasNew(true);
            r.setEvent(new EventItem(e.getTime(), e.getType(), e.getGate(), e.getPhotoUrl(), Boolean.TRUE.equals(e.getLate()), Boolean.TRUE.equals(e.getEarlyLeave())));
        } else {
            r.setHasNew(false);
            r.setEvent(null);
        }
        return Result.ok(r);
    }

    /**
     * 考勤记录查询（分页）
     */
    @GetMapping("/records")
    public Result<List<EventItem>> records(@RequestParam(required = false) Long childId,
                                           @RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "20") Integer size) {
        Long cid = childId != null ? childId : 2001L;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(page - 1, 0), size);
        java.util.List<com.campus.card.wechat.model.AttendanceEvent> events;
        if (startDate != null && endDate != null) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            LocalDateTime endT = LocalDate.parse(endDate).atTime(23, 59, 59);
            events = eventRepo.findByChildIdAndTimeBetweenOrderByTimeDesc(cid, start, endT, pageable).getContent();
        } else {
            events = eventRepo.findByChildIdOrderByTimeDesc(cid, pageable).getContent();
        }
        List<EventItem> list = new ArrayList<>();
        for (com.campus.card.wechat.model.AttendanceEvent e : events) {
            list.add(new EventItem(e.getTime(), e.getType(), e.getGate(), e.getPhotoUrl(), Boolean.TRUE.equals(e.getLate()), Boolean.TRUE.equals(e.getEarlyLeave())));
        }
        return Result.ok(list);
    }

    /**
     * 考勤统计（日/月汇总）
     */
    @GetMapping("/stats")
    public Result<StatsResp> stats(@RequestParam(required = false) Long childId,
                                   @RequestParam(defaultValue = "month") String range) {
        Long cid = childId != null ? childId : 2001L;
        LocalDate startDate;
        LocalDate endDate = LocalDate.now();
        if ("day".equalsIgnoreCase(range)) {
            startDate = LocalDate.now();
        } else {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        java.util.List<com.campus.card.wechat.model.AttendanceEvent> events = eventRepo.findByChildIdAndTimeBetweenOrderByTimeDesc(cid, start, end,
                org.springframework.data.domain.PageRequest.of(0, 1000)).getContent();

        StatsResp s = new StatsResp();
        s.setChildId(cid);
        s.setRange(range);
        // 简易统计：当天按是否有进校事件视为到校
        Map<String, Integer> daily = new LinkedHashMap<>();
        int presentDays = 0;
        int lateCount = 0;
        int earlyCount = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate d = LocalDate.now().minusDays(i);
            boolean present = events.stream().anyMatch(e -> e.getTime().toLocalDate().equals(d) && "进校".equals(e.getType()));
            daily.put(d.toString(), present ? 1 : 0);
            if (present) presentDays++;
        }
        for (com.campus.card.wechat.model.AttendanceEvent e : events) {
            if (Boolean.TRUE.equals(e.getLate())) lateCount++;
            if (Boolean.TRUE.equals(e.getEarlyLeave())) earlyCount++;
        }
        s.setTotalDays(range.equalsIgnoreCase("day") ? 1 : endDate.lengthOfMonth());
        s.setPresentDays(presentDays);
        s.setLateCount(lateCount);
        s.setEarlyLeaveCount(earlyCount);
        s.setAbsentDays(s.getTotalDays() - presentDays);
        s.setDailyPresence(daily);
        return Result.ok(s);
    }

    /**
     * 考勤照片查看（按日）
     */
    @GetMapping("/photos")
    public Result<List<PhotoItem>> photos(@RequestParam(required = false) Long childId,
                                          @RequestParam(required = false) String date) {
        Long cid = childId != null ? childId : 2001L;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 100);
        LocalDateTime start = null, end = null;
        if (date != null && !date.isEmpty()) {
            LocalDate d = LocalDate.parse(date);
            start = d.atStartOfDay();
            end = d.atTime(23, 59, 59);
        }
        java.util.List<com.campus.card.wechat.model.AttendanceEvent> events = (start != null && end != null)
                ? eventRepo.findByChildIdAndTimeBetweenOrderByTimeDesc(cid, start, end, pageable).getContent()
                : eventRepo.findByChildIdOrderByTimeDesc(cid, pageable).getContent();
        List<PhotoItem> photos = new ArrayList<>();
        for (com.campus.card.wechat.model.AttendanceEvent e : events) {
            if (e.getPhotoUrl() != null) {
                photos.add(new PhotoItem(e.getTime(), e.getPhotoUrl(), e.getType(), e.getGate()));
            }
        }
        return Result.ok(photos);
    }

    /**
     * 迟到/早退/缺勤提醒（最近）
     */
    @GetMapping("/alerts")
    public Result<List<AlertItem>> alerts(@RequestParam(required = false) Long childId) {
        Long cid = childId != null ? childId : 2001L;
        org.springframework.data.domain.Pageable p = org.springframework.data.domain.PageRequest.of(0, 20);
        List<AlertItem> alerts = new ArrayList<>();
        try {
            java.util.List<com.campus.card.wechat.model.AttendanceAlert> list = alertRepo.findByChildIdOrderByTimeDesc(cid, p);
            for (com.campus.card.wechat.model.AttendanceAlert a : list) {
                alerts.add(new AlertItem(a.getTitle(), a.getTime(), a.getTxtdesc()));
            }
        } catch (Exception ex) {
            // 数据库未就绪或表缺失时，避免 500，返回空列表以保障前端正常显示
        }
        return Result.ok(alerts);
    }

    private String samplePhoto() { return "https://picsum.photos/seed/kq2/160/160"; }

    @Data
    public static class RealtimeResp {
        private Long childId;
        private boolean hasNew;
        private EventItem event;
    }

    @Data
    public static class EventItem {
        private LocalDateTime time;
        private String type; // 进校/离校
        private String gate;
        private String photoUrl;
        private boolean late;
        private boolean earlyLeave;
        public EventItem(LocalDateTime time, String type, String gate, String photoUrl, boolean late, boolean earlyLeave) {
            this.time = time; this.type = type; this.gate = gate; this.photoUrl = photoUrl; this.late = late; this.earlyLeave = earlyLeave;
        }
        public EventItem() {}
    }

    @Data
    public static class StatsResp {
        private Long childId;
        private String range; // day/month
        private Integer totalDays;
        private Integer presentDays;
        private Integer lateCount;
        private Integer earlyLeaveCount;
        private Integer absentDays;
        private Map<String, Integer> dailyPresence; // 日期 -> 是否到校(0/1)
    }

    @Data
    public static class PhotoItem {
        private LocalDateTime time;
        private String url;
        private String type;
        private String gate;
        public PhotoItem(LocalDateTime time, String url, String type, String gate) {
            this.time = time; this.url = url; this.type = type; this.gate = gate;
        }
        public PhotoItem() {}
    }

    @Data
    public static class AlertItem {
        private String title;
        private LocalDateTime time;
        private String txtdesc;
        public AlertItem(String title, LocalDateTime time, String txtdesc) {
            this.title = title; this.time = time; this.txtdesc = txtdesc;
        }
        public AlertItem() {}
    }

    @Data
    public static class CheckInResp {
        private boolean success;
        private String message;
        private LocalDateTime time;
        private boolean late;
    }
}