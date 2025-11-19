package com.campus.card.admin.controller;

import com.campus.card.admin.service.ReportService;
import com.campus.card.admin.service.ReportService.ClassConsumeResp;
import com.campus.card.admin.service.ReportService.DayStat;
import com.campus.card.admin.service.ReportService.MonthlyResp;
import com.campus.card.admin.service.ReportService.PersonalResp;
import com.campus.card.admin.service.ReportService.RankingItem;
import com.campus.card.admin.service.ReportService.StatsResp;
import com.campus.card.common.result.Result;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // 日消费统计
    @GetMapping("/consume/daily")
    public Result<StatsResp> daily(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.ok(reportService.dailyConsume(date));
    }

    // 月消费统计（含每日明细）
    @GetMapping("/consume/monthly")
    public Result<MonthlyResp> monthly(@RequestParam int year, @RequestParam int month) {
        return Result.ok(reportService.monthlyConsume(year, month));
    }

    // 班级消费统计（含个人明细）
    @GetMapping("/consume/class")
    public Result<ClassConsumeResp> classConsume(
            @RequestParam Long classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return Result.ok(reportService.classConsume(classId, startDate, endDate));
    }

    // 个人消费统计（含每日明细）
    @GetMapping("/consume/personal")
    public Result<PersonalResp> personal(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return Result.ok(reportService.personalConsume(studentId, studentNo, startDate, endDate));
    }

    // 消费排行榜（按学生总额）
    @GetMapping("/consume/ranking")
    public Result<List<RankingItem>> ranking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        return Result.ok(reportService.ranking(startDate, endDate, limit));
    }

    // 消费趋势分析（按日）
    @GetMapping("/consume/trend")
    public Result<List<DayStat>> trend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return Result.ok(reportService.trend(startDate, endDate));
    }
}