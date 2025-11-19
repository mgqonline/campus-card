package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Card;
import com.campus.card.admin.domain.CardType;
import com.campus.card.admin.domain.CardTx;
import com.campus.card.admin.service.CardService;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    // 卡种管理
    @GetMapping("/types")
    public Result<List<CardType>> listCardTypes() {
        return Result.ok(cardService.listCardTypes());
    }

    @PostMapping("/types")
    public Result<CardType> createCardType(@RequestBody CardType body) {
        return Result.ok(cardService.createCardType(body));
    }

    @PutMapping("/types/{id}")
    public Result<CardType> updateCardType(@PathVariable Long id, @RequestBody CardType body) {
        return Result.ok(cardService.updateCardType(id, body));
    }

    @DeleteMapping("/types/{id}")
    public Result<Void> deleteCardType(@PathVariable Long id) {
        cardService.deleteCardType(id);
        return Result.ok(null);
    }

    // 卡片管理
    @GetMapping
    public Result<PageResult<Card>> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String cardNo,
            @RequestParam(required = false) String holderType,
            @RequestParam(required = false) String holderId,
            @RequestParam(required = false) String status
    ) {
        return Result.ok(cardService.pageList(page, size, cardNo, holderType, holderId, status));
    }

    @PostMapping("/issue")
    public Result<Card> issueCard(@RequestBody CardService.IssueCardReq req) {
        try {
            return Result.ok(cardService.issueCard(req));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/issue/batch")
    public Result<CardService.BatchIssueResult> issueBatch(@RequestBody CardService.BatchIssueReq req) {
        try {
            return Result.ok(cardService.batchIssue(req));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/loss")
    public Result<Void> reportLoss(@RequestBody CardService.LossReq req) {
        try {
            cardService.reportLoss(req.getCardNo());
            return Result.ok(null);
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    // 新增：冻结
    @PostMapping("/freeze")
    public Result<Void> freeze(@RequestBody CardService.FreezeReq req) {
        try {
            cardService.freeze(req.getCardNo());
            return Result.ok(null);
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/unloss")
    public Result<Void> unloss(@RequestBody CardService.LossReq req) {
        try {
            cardService.unloss(req.getCardNo());
            return Result.ok(null);
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    // 新增：解冻
    @PostMapping("/unfreeze")
    public Result<Void> unfreeze(@RequestBody CardService.UnfreezeReq req) {
        try {
            cardService.unfreeze(req.getCardNo());
            return Result.ok(null);
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody CardService.CancelReq req) {
        try {
            cardService.cancel(req);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/consume")
    public Result<CardService.ConsumeResult> consume(@RequestBody CardService.ConsumeReq req) {
        try {
            return Result.ok(cardService.consume(req));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @GetMapping("/{cardNo}/balance")
    public Result<CardService.BalanceInfo> getBalance(@PathVariable String cardNo) {
        try {
            return Result.ok(cardService.getBalance(cardNo));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 404);
        }
    }

    @GetMapping("/{cardNo}/transactions")
    public Result<List<CardTx>> getTransactions(
            @PathVariable String cardNo,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        return Result.ok(cardService.getTransactions(cardNo, type, startTime, endTime));
    }

    @PostMapping("/recharge")
    public Result<CardService.RechargeResult> recharge(@RequestBody CardService.RechargeReq req) {
        try {
            return Result.ok(cardService.recharge(req));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/replace")
    public Result<CardService.ReplaceResult> replace(@RequestBody CardService.ReplaceReq req) {
        try {
            return Result.ok(cardService.replaceCard(req));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }
}