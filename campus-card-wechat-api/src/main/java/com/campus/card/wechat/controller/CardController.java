package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import com.campus.card.wechat.model.CardRequest;
import com.campus.card.wechat.repository.CardRequestRepository;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/card")
public class CardController {

    private final CardRequestRepository repo;

    public CardController(CardRequestRepository repo) {
        this.repo = repo;
    }

    /** 提交卡服务申请：挂失/解挂/补卡 */
    @PostMapping("/request")
    public Result<CardRequest> request(@RequestBody CardReq req) {
        CardRequest r = new CardRequest();
        r.setChildId(req.getChildId() != null ? req.getChildId() : 2001L);
        r.setType(req.getType());
        r.setReason(req.getReason());
        r.setContact(req.getContact());
        r.setStatus("PENDING");
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        repo.save(r);
        return Result.ok(r);
    }

    /** 当前子女的申请列表（最近10条） */
    @GetMapping("/requests")
    public Result<List<CardRequest>> list(@RequestParam(required = false) Long childId) {
        Long cid = childId != null ? childId : 2001L;
        List<CardRequest> list = repo.findTop10ByChildIdOrderByCreatedAtDesc(cid);
        return Result.ok(list);
    }

    @Data
    public static class CardReq {
        private Long childId;
        private String type; // LOSS, UNLOSS, REPLACE
        private String reason;
        private String contact;
    }
}