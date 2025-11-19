package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import com.campus.card.wechat.model.ChatMessage;
import com.campus.card.wechat.repository.ChatMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/teacher/messages")
public class TeacherMessageController {

    private final ChatMessageRepository repo;

    public TeacherMessageController(ChatMessageRepository repo) {
        this.repo = repo;
    }

    /** 家长消息列表（按子女） */
    @GetMapping("/list")
    public Result<Page<ChatMessage>> list(@RequestParam Long childId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        PageRequest pr = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        Page<ChatMessage> p = repo.findByChildIdOrderByCreatedAtDesc(childId, pr);
        return Result.ok(p);
    }

    /** 教师回复家长消息 */
    @PostMapping("/reply")
    public Result<ChatMessage> reply(@RequestBody ReplyReq req) {
        ChatMessage m = new ChatMessage();
        m.setChildId(req.getChildId());
        m.setSenderRole("TEACHER");
        m.setContent(req.getContent());
        m.setCreatedAt(LocalDateTime.now());
        repo.save(m);
        return Result.ok(m);
    }

    public static class ReplyReq {
        private Long childId;
        private String content;
        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}