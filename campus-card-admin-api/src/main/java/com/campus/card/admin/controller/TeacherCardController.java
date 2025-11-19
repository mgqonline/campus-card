package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Card;
import com.campus.card.admin.domain.Teacher;
import com.campus.card.admin.repository.CardRepository;
import com.campus.card.admin.service.CardService;
import com.campus.card.admin.service.CardService.BalanceInfo;
import com.campus.card.admin.service.CardService.IssueCardReq;
import com.campus.card.admin.service.TeacherService;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherCardController {

    private final TeacherService teacherService;
    private final CardService cardService;
    private final CardRepository cardRepository;

    public TeacherCardController(TeacherService teacherService, CardService cardService, CardRepository cardRepository) {
        this.teacherService = teacherService;
        this.cardService = cardService;
        this.cardRepository = cardRepository;
    }

    @GetMapping("/{id}/cards")
    public Result<List<Card>> listCards(@PathVariable Long id) {
        Optional<Teacher> ot = teacherService.findById(id);
        if (!ot.isPresent()) return Result.error("教师不存在", 404);
        String holderId = ot.get().getTeacherNo();
        List<Card> list = cardRepository.findByHolderTypeAndHolderId("TEACHER", holderId);
        return Result.ok(list);
    }

    @PostMapping("/{id}/cards/issue")
    public Result<Card> issueCard(@PathVariable Long id, @RequestBody IssueReq req) {
        try {
            Optional<Teacher> ot = teacherService.findById(id);
            if (!ot.isPresent()) return Result.error("教师不存在", 404);
            IssueCardReq icr = new IssueCardReq();
            icr.setTypeId(req.getTypeId());
            icr.setHolderType("TEACHER");
            icr.setHolderId(ot.get().getTeacherNo());
            icr.setInitialBalance(req.getInitialBalance());
            icr.setNote(req.getNote());
            Card c = cardService.issueCard(icr);
            return Result.ok(c);
        } catch (Exception e) {
            return Result.error("发卡失败: " + e.getMessage(), 400);
        }
    }

    @GetMapping("/{id}/cards/{cardNo}/balance")
    public Result<BalanceInfo> getBalance(@PathVariable Long id, @PathVariable String cardNo) {
        Optional<Teacher> ot = teacherService.findById(id);
        if (!ot.isPresent()) return Result.error("教师不存在", 404);
        try {
            BalanceInfo info = cardService.getBalance(cardNo);
            return Result.ok(info);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage(), 400);
        }
    }

    public static class IssueReq {
        private Long typeId;
        private BigDecimal initialBalance;
        private String note;
        public Long getTypeId() { return typeId; }
        public void setTypeId(Long typeId) { this.typeId = typeId; }
        public BigDecimal getInitialBalance() { return initialBalance; }
        public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }
}