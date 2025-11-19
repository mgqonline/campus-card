package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Card;
import com.campus.card.admin.service.CardService;
import com.campus.card.admin.service.CardService.IssueCardReq;
import com.campus.card.admin.service.CardService.BalanceInfo;
import com.campus.card.admin.repository.CardRepository;
import com.campus.card.admin.domain.StudentInfo;
import com.campus.card.admin.service.StudentService;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/students")
public class StudentCardController {

    private final StudentService studentService;
    private final CardService cardService;
    private final CardRepository cardRepository;

    public StudentCardController(StudentService studentService, CardService cardService, CardRepository cardRepository) {
        this.studentService = studentService;
        this.cardService = cardService;
        this.cardRepository = cardRepository;
    }

    @GetMapping("/{id}/cards")
    public Result<List<Card>> listCards(@PathVariable Long id) {
        Optional<StudentInfo> os = studentService.findById(id);
        if (!os.isPresent()) return Result.error("学生不存在", 404);
        String holderId = os.get().getStudentNo();
        List<Card> list = cardRepository.findByHolderTypeAndHolderId("STUDENT", holderId);
        return Result.ok(list);
    }

    @PostMapping("/{id}/cards/issue")
    public Result<Card> issueCard(@PathVariable Long id, @RequestBody IssueReq req) {
        try {
            Optional<StudentInfo> os = studentService.findById(id);
            if (!os.isPresent()) return Result.error("学生不存在", 404);
            IssueCardReq icr = new IssueCardReq();
            icr.setTypeId(req.getTypeId());
            icr.setHolderType("STUDENT");
            icr.setHolderId(os.get().getStudentNo());
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
        Optional<StudentInfo> os = studentService.findById(id);
        if (!os.isPresent()) return Result.error("学生不存在", 404);
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