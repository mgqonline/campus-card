package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import com.campus.card.wechat.model.ConsumeRecord;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/consume")
public class ConsumeController {

    private final com.campus.card.wechat.repository.ConsumeRecordRepository consumeRepo;

    public ConsumeController(com.campus.card.wechat.repository.ConsumeRecordRepository consumeRepo) {
        this.consumeRepo = consumeRepo;
    }

    @GetMapping("/list")
    public Result<List<ConsumeItem>> list(@RequestParam(required = false) Long childId,
                                          @RequestParam(required = false) String startDate,
                                          @RequestParam(required = false) String endDate,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "20") Integer size) {
        Long cid = childId != null ? childId : 2001L;
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        ensureSampleData(cid, start, end);
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        java.util.List<ConsumeRecord> records = consumeRepo.findByChildIdAndDateBetweenOrderByDateDesc(cid, start, end, pageable).getContent();
        List<ConsumeItem> items = new ArrayList<>();
        for (ConsumeRecord r : records) {
            items.add(new ConsumeItem(r.getDate().toString(), r.getMerchant(), r.getAmount().doubleValue(), r.getDetail(), r.getChannel(), r.getTxId()));
        }
        return Result.ok(items);
    }

    @GetMapping("/stats")
    public Result<ConsumeStats> stats(@RequestParam(required = false) Long childId,
                                      @RequestParam(required = false) String startDate,
                                      @RequestParam(required = false) String endDate) {
        Long cid = childId != null ? childId : 2001L;
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        ensureSampleData(cid, start, end);
        List<ConsumeRecord> records = consumeRepo.findByChildIdAndDateBetween(cid, start, end);
        double total = records.stream().map(ConsumeRecord::getAmount).mapToDouble(BigDecimal::doubleValue).sum();
        int txCount = records.size();
        long days = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1);
        double avgPerDay = total / days;
        ConsumeStats s = new ConsumeStats();
        s.setTotalAmount(round2(total));
        s.setTxCount(txCount);
        s.setAvgPerDay(round2(avgPerDay));
        return Result.ok(s);
    }

    @GetMapping("/calendar")
    public Result<List<DailyStat>> calendar(@RequestParam(required = false) Long childId,
                                            @RequestParam String month) {
        Long cid = childId != null ? childId : 2001L;
        LocalDate first = LocalDate.parse(month + "-01");
        LocalDate last = first.withDayOfMonth(first.lengthOfMonth());
        ensureSampleData(cid, first, last);
        List<ConsumeRecord> records = consumeRepo.findByChildIdAndDateBetween(cid, first, last);
        Map<String, DailyStat> map = new LinkedHashMap<>();
        for (int d = 1; d <= first.lengthOfMonth(); d++) {
            String dateStr = first.withDayOfMonth(d).toString();
            DailyStat stat = new DailyStat();
            stat.setDate(dateStr);
            stat.setTotalAmount(0.0);
            stat.setTxCount(0);
            map.put(dateStr, stat);
        }
        for (ConsumeRecord r : records) {
            DailyStat s = map.get(r.getDate().toString());
            s.setTotalAmount(round2(s.getTotalAmount() + r.getAmount().doubleValue()));
            s.setTxCount(s.getTxCount() + 1);
        }
        return Result.ok(new ArrayList<>(map.values()));
    }

    @GetMapping("/trend")
    public Result<List<TrendPoint>> trend(@RequestParam(required = false) Long childId,
                                          @RequestParam(required = false) String startDate,
                                          @RequestParam(required = false) String endDate) {
        Long cid = childId != null ? childId : 2001L;
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(6);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        ensureSampleData(cid, start, end);
        List<ConsumeRecord> records = consumeRepo.findByChildIdAndDateBetween(cid, start, end);
        Map<String, Double> dailyTotal = new LinkedHashMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            dailyTotal.put(d.toString(), 0.0);
        }
        for (ConsumeRecord r : records) {
            String ds = r.getDate().toString();
            dailyTotal.put(ds, round2(dailyTotal.get(ds) + r.getAmount().doubleValue()));
        }
        List<TrendPoint> series = new ArrayList<>();
        for (Map.Entry<String, Double> e : dailyTotal.entrySet()) {
            series.add(new TrendPoint(e.getKey(), e.getValue()));
        }
        return Result.ok(series);
    }

    private void ensureSampleData(Long childId, LocalDate start, LocalDate end) {
        // 若区间内无数据，生成示例数据
        List<ConsumeRecord> exists = consumeRepo.findByChildIdAndDateBetween(childId, start, end);
        if (exists != null && !exists.isEmpty()) return;
        String[] merchants = {"食堂A", "食堂B", "校园超市", "文具店"};
        java.util.Random rand = new java.util.Random(12345);
        List<ConsumeRecord> batch = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            int n = rand.nextInt(5); // 每日0-4笔
            for (int i = 0; i < n; i++) {
                ConsumeRecord r = new ConsumeRecord();
                r.setChildId(childId);
                r.setDate(d);
                r.setMerchant(merchants[rand.nextInt(merchants.length)]);
                double amt = 5 + rand.nextDouble() * 45; // 5-50元
                r.setAmount(java.math.BigDecimal.valueOf(round2(amt)));
                r.setDetail(i % 2 == 0 ? "餐饮" : "购物");
                r.setChannel("CARD");
                r.setTxId("TX" + childId + d.toString().replace("-", "") + i);
                batch.add(r);
            }
        }
        if (!batch.isEmpty()) consumeRepo.saveAll(batch);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    @Data
    public static class ConsumeItem {
        private String date;
        private String merchant;
        private Double amount;
        private String detail;
        private String channel;
        private String txId;
        public ConsumeItem(String date, String merchant, Double amount, String detail, String channel, String txId) {
            this.date = date; this.merchant = merchant; this.amount = amount; this.detail = detail; this.channel = channel; this.txId = txId;
        }
    }

    @Data
    public static class ConsumeStats {
        private Double totalAmount;
        private Integer txCount;
        private Double avgPerDay;
    }

    @Data
    public static class DailyStat {
        private String date;
        private Double totalAmount;
        private Integer txCount;
    }

    @Data
    public static class TrendPoint {
        private String date;
        private double value;
        public TrendPoint(String date, double value) { this.date = date; this.value = value; }
    }
}