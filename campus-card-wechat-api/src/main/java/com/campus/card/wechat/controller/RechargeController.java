package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/recharge")
public class RechargeController {

    @GetMapping("/options")
    public Result<RechargeOptions> options() {
        RechargeOptions opt = new RechargeOptions();
        opt.setSuggested(Arrays.asList(50, 100, 200, 300));
        opt.setMin(10);
        opt.setMax(1000);
        return Result.ok(opt);
    }

    @PostMapping("/create")
    public Result<CreateOrderResp> create(@RequestBody CreateOrderReq req) {
        // 模拟创建订单并返回预支付参数
        String orderId = "ORDER" + System.currentTimeMillis();
        CreateOrderResp resp = new CreateOrderResp();
        resp.setOrderId(orderId);
        resp.setAmount(req.getAmount());
        resp.setChildId(req.getChildId());
        Map<String, String> payParams = new HashMap<>();
        payParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        payParams.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
        payParams.put("package", "prepay_id=mock123456");
        payParams.put("signType", "RSA");
        payParams.put("paySign", "mock-signature");
        resp.setPayParams(payParams);
        return Result.ok(resp);
    }

    @GetMapping("/records")
    public Result<List<RechargeRecord>> records(@RequestParam(required = false) Long childId,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "20") Integer size) {
        List<RechargeRecord> list = new ArrayList<>();
        list.add(new RechargeRecord("ORDER10001", childId != null ? childId : 2001L, 100.0, "SUCCESS", LocalDateTime.now().minusDays(1)));
        list.add(new RechargeRecord("ORDER10002", childId != null ? childId : 2001L, 50.0, "PENDING", LocalDateTime.now().minusHours(5)));
        return Result.ok(list);
    }

    @PostMapping("/invoice")
    public Result<String> applyInvoice(@RequestBody InvoiceReq req) {
        return Result.ok("申请已提交，订单:" + req.getOrderId());
    }

    @Data
    public static class RechargeOptions {
        private List<Integer> suggested;
        private Integer min;
        private Integer max;
    }

    @Data
    public static class CreateOrderReq {
        private Long childId;
        private Double amount;
    }

    @Data
    public static class CreateOrderResp {
        private String orderId;
        private Long childId;
        private Double amount;
        private Map<String, String> payParams;
    }

    @Data
    public static class RechargeRecord {
        private String orderId;
        private Long childId;
        private Double amount;
        private String status;
        private String time;
        public RechargeRecord(String orderId, Long childId, Double amount, String status, LocalDateTime time) {
            this.orderId = orderId; this.childId = childId; this.amount = amount; this.status = status; this.time = time.toString();
        }
    }

    @Data
    public static class InvoiceReq {
        private String orderId;
        private String email;
    }
}