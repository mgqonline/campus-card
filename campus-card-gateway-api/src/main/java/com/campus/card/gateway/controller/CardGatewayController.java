package com.campus.card.gateway.controller;

import com.campus.card.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Tag(name = "卡务数据", description = "卡片信息、余额、充值与消费记录接口")
@RestController
@RequestMapping("/api/v1/gw/cards")
public class CardGatewayController {

    @Operation(summary = "卡片信息接口")
    @GetMapping("/{cardNo}")
    public Result<CardInfo> cardInfo(@PathVariable String cardNo) {
        CardInfo info = new CardInfo();
        info.setCardNo(cardNo);
        info.setHolderType("STUDENT");
        info.setHolderId("S20250001");
        info.setStatus("ACTIVE");
        return Result.ok(info);
    }

    @Operation(summary = "账户余额接口")
    @GetMapping("/{cardNo}/balance")
    public Result<BalanceInfo> balance(@PathVariable String cardNo) {
        BalanceInfo b = new BalanceInfo();
        b.setCardNo(cardNo);
        b.setBalance(new BigDecimal("123.45"));
        b.setCurrency("CNY");
        return Result.ok(b);
    }

    @Operation(summary = "充值记录接口")
    @GetMapping("/{cardNo}/recharges")
    public Result<List<RechargeRecord>> rechargeRecords(
            @PathVariable String cardNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        RechargeRecord r1 = new RechargeRecord(cardNo, new BigDecimal("50.00"), "WX_PAY", LocalDateTime.now().minusDays(2));
        RechargeRecord r2 = new RechargeRecord(cardNo, new BigDecimal("30.00"), "ALIPAY", LocalDateTime.now().minusDays(1));
        return Result.ok(Arrays.asList(r1, r2));
    }

    @Operation(summary = "消费记录接口")
    @GetMapping("/{cardNo}/consumes")
    public Result<List<ConsumeRecord>> consumeRecords(
            @PathVariable String cardNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ConsumeRecord c1 = new ConsumeRecord(cardNo, new BigDecimal("12.50"), "CANTEEN", LocalDateTime.now().minusHours(5));
        ConsumeRecord c2 = new ConsumeRecord(cardNo, new BigDecimal("3.00"), "STORE", LocalDateTime.now().minusHours(3));
        return Result.ok(Arrays.asList(c1, c2));
    }

    @Data
    public static class CardInfo {
        private String cardNo;
        private String holderType;
        private String holderId;
        private String status;
    }

    @Data
    public static class BalanceInfo {
        private String cardNo;
        private BigDecimal balance;
        private String currency;
    }

    @Data
    public static class RechargeRecord {
        private String cardNo;
        private BigDecimal amount;
        private String method;
        private LocalDateTime time;
        public RechargeRecord(String cardNo, BigDecimal amount, String method, LocalDateTime time) {
            this.cardNo = cardNo; this.amount = amount; this.method = method; this.time = time;
        }
    }

    @Data
    public static class ConsumeRecord {
        private String cardNo;
        private BigDecimal amount;
        private String place;
        private LocalDateTime time;
        public ConsumeRecord(String cardNo, BigDecimal amount, String place, LocalDateTime time) {
            this.cardNo = cardNo; this.amount = amount; this.place = place; this.time = time;
        }
    }
}