package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Subject;
import com.campus.card.admin.service.SubjectService;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public Result<PageResult<Subject>> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long schoolId
    ) {
        return Result.ok(subjectService.pageList(page, size, name, schoolId));
    }

    @GetMapping("/{id}")
    public Result<Subject> detail(@PathVariable Long id) {
        return subjectService.findById(id)
                .map(Result::ok)
                .orElse(Result.error("学科不存在", 404));
    }

    @PostMapping
    public Result<Subject> create(@RequestBody Subject body) {
        try {
            return Result.ok(subjectService.create(body));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PutMapping("/{id}")
    public Result<Subject> update(@PathVariable Long id, @RequestBody Subject body) {
        return Result.ok(subjectService.update(id, body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return Result.ok(null);
    }
}