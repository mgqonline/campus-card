package com.campus.card.admin.controller;

import com.campus.card.admin.service.AttendanceAnalyticsService;
import com.campus.card.admin.service.AttendanceAnalyticsService.ClassStatsPoint;
import com.campus.card.admin.service.AttendanceAnalyticsService.PersonalStatsPoint;
import com.campus.card.admin.service.AttendanceAnalyticsService.StatsPoint;
import com.campus.card.common.result.Result;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance/analytics")
public class AttendanceAnalyticsController {

    private final AttendanceAnalyticsService analyticsService;

    public AttendanceAnalyticsController(AttendanceAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/daily")
    public Result<List<StatsPoint>> daily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long classId
    ) {
        return Result.ok(analyticsService.daily(startDate, endDate, classId));
    }

    @GetMapping("/monthly")
    public Result<List<StatsPoint>> monthly(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long classId
    ) {
        return Result.ok(analyticsService.monthly(startDate, endDate, classId));
    }

    @GetMapping("/term")
    public Result<List<StatsPoint>> term(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long classId
    ) {
        return Result.ok(analyticsService.term(startDate, endDate, classId));
    }

    @GetMapping("/classes")
    public Result<List<ClassStatsPoint>> byClass(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return Result.ok(analyticsService.byClass(startDate, endDate));
    }

    @GetMapping("/personal")
    public Result<List<PersonalStatsPoint>> byPersonal(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long classId
    ) {
        return Result.ok(analyticsService.byPersonal(startDate, endDate, classId));
    }

    @GetMapping("/late-early")
    public Result<StatsPoint> lateEarly(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long studentId
    ) {
        return Result.ok(analyticsService.lateEarly(startDate, endDate, classId, studentId));
    }

    @GetMapping("/absence")
    public Result<StatsPoint> absence(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long studentId
    ) {
        return Result.ok(analyticsService.absence(startDate, endDate, classId, studentId));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> export(
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long studentId
    ) {
        String csv = analyticsService.exportCsv(type, startDate, endDate, classId, studentId);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance-" + type + ".csv");
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}