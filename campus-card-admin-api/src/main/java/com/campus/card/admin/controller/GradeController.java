package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Grade;
import com.campus.card.admin.service.GradeService;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping
    public Result<PageResult<Grade>> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long schoolId
    ) {
        return Result.ok(gradeService.pageList(page, size, name, schoolId));
    }

    @GetMapping("/{id}")
    public Result<Grade> detail(@PathVariable Long id) {
        return gradeService.findById(id)
                .map(Result::ok)
                .orElse(Result.error("年级不存在", 404));
    }

    @PostMapping
    public Result<Grade> create(@RequestBody Grade body) {
        try {
            return Result.ok(gradeService.create(body));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PutMapping("/{id}")
    public Result<Grade> update(@PathVariable Long id, @RequestBody Grade body) {
        return Result.ok(gradeService.update(id, body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        gradeService.delete(id);
        return Result.ok(null);
    }

    @PostMapping("/{id}/enable")
    public Result<Grade> enable(@PathVariable Long id) {
        try {
            return Result.ok(gradeService.enable(id));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/{id}/disable")
    public Result<Grade> disable(@PathVariable Long id) {
        try {
            return Result.ok(gradeService.disable(id));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }
}