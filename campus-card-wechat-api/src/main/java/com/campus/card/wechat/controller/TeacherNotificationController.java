package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import com.campus.card.wechat.model.Notification;
import com.campus.card.wechat.model.Student;
import com.campus.card.wechat.repository.NotificationRepository;
import com.campus.card.wechat.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/teacher/notify")
public class TeacherNotificationController {

    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;

    public TeacherNotificationController(NotificationRepository notificationRepository,
                                         StudentRepository studentRepository) {
        this.notificationRepository = notificationRepository;
        this.studentRepository = studentRepository;
    }

    /** 学校通知接收：按分类 ANNOUNCEMENT 拉取（不限定 childId） */
    @GetMapping("/school_list")
    public Result<Page<Notification>> schoolList(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        PageRequest pr = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        Page<Notification> p = notificationRepository.findByCategoryOrderByCreatedAtDesc("ANNOUNCEMENT", pr);
        return Result.ok(p);
    }

    /** 班级通知发送：向班级学生家长下发 CLASS_NOTICE */
    @PostMapping("/class_send")
    public Result<Integer> classSend(@RequestBody ClassSendReq req) {
        List<Student> list = studentRepository.findAll();
        int count = 0;
        for (Student s : list) {
            if (s.getClassId() != null && s.getClassId().equals(req.getClassId())) {
                Notification n = new Notification();
                n.setChildId(s.getId());
                n.setCategory("CLASS_NOTICE");
                n.setTitle(req.getTitle());
                n.setContent(req.getContent());
                n.setReadFlag(false);
                n.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(n);
                count++;
            }
        }
        return Result.ok(count);
    }

    public static class ClassSendReq {
        private Long classId;
        private String title;
        private String content;
        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}