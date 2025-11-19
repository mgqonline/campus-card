package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import com.campus.card.wechat.model.Feedback;
import com.campus.card.wechat.repository.FeedbackRepository;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    private final FeedbackRepository repo;

    public FeedbackController(FeedbackRepository repo) {
        this.repo = repo;
    }

    /** 提交意见反馈 */
    @PostMapping("/submit")
    public Result<Feedback> submit(@RequestBody SubmitReq req) {
        Feedback f = new Feedback();
        f.setChildId(req.getChildId() != null ? req.getChildId() : 2001L);
        f.setCategory(req.getCategory());
        f.setContent(req.getContent());
        f.setContact(req.getContact());
        f.setStatus("NEW");
        f.setCreatedAt(LocalDateTime.now());
        repo.save(f);
        return Result.ok(f);
    }

    /** 最近反馈列表（按子女） */
    @GetMapping("/list")
    public Result<List<Feedback>> list(@RequestParam(required = false) Long childId) {
        Long cid = childId != null ? childId : 2001L;
        List<Feedback> list = repo.findTop20ByChildIdOrderByCreatedAtDesc(cid);
        return Result.ok(list);
    }

    @Data
    public static class SubmitReq {
        private Long childId;
        private String category;
        private String content;
        private String contact;
    }
}