package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Semester;
import com.campus.card.admin.service.SemesterService;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/semesters")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @GetMapping
    public Result<PageResult<Semester>> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long schoolId
    ) {
        return Result.ok(semesterService.pageList(page, size, name, schoolId));
    }

    @GetMapping("/{id}")
    public Result<Semester> detail(@PathVariable Long id) {
        return semesterService.findById(id)
                .map(Result::ok)
                .orElse(Result.error("学期不存在", 404));
    }

    @PostMapping
    public Result<Semester> create(@RequestBody Semester body) {
        try {
            return Result.ok(semesterService.create(body));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PutMapping("/{id}")
    public Result<Semester> update(@PathVariable Long id, @RequestBody Semester body) {
        return Result.ok(semesterService.update(id, body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        semesterService.delete(id);
        return Result.ok(null);
    }

    @PostMapping("/{id}/enable")
    public Result<Semester> enable(@PathVariable Long id) {
        return Result.ok(semesterService.enable(id));
    }

    @PostMapping("/{id}/disable")
    public Result<Semester> disable(@PathVariable Long id) {
        return Result.ok(semesterService.disable(id));
    }

    @PostMapping("/{id}/current")
    public Result<Semester> setCurrent(@PathVariable Long id) {
        return Result.ok(semesterService.setCurrent(id));
    }

    @GetMapping("/current")
    public Result<Semester> current(@RequestParam Long schoolId) {
        return semesterService.getCurrent(schoolId)
                .map(Result::ok)
                .orElse(Result.error("当前学期未设置", 404));
    }

    @GetMapping("/history")
    public Result<List<Semester>> history(
            @RequestParam Long schoolId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return Result.ok(semesterService.listHistory(schoolId, startDate, endDate));
    }
}