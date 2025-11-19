package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    @GetMapping("/balance")
    public Result<BalanceResp> balance(@RequestParam(required = false) Long childId) {
        BalanceResp b = new BalanceResp();
        b.setChildId(childId != null ? childId : 2001L);
        b.setBalance(256.78);
        b.setCurrency("CNY");
        return Result.ok(b);
    }

    public static class BalanceResp {
        private Long childId;
        private Double balance;
        private String currency;
        public Long getChildId() { return childId; }
        public void setChildId(Long childId) { this.childId = childId; }
        public Double getBalance() { return balance; }
        public void setBalance(Double balance) { this.balance = balance; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}