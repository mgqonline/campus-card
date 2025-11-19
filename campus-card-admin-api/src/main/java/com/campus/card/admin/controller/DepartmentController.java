package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Department;
import com.campus.card.admin.service.DepartmentService;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public Result<PageResult<Department>> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status
    ) {
        return Result.ok(departmentService.pageList(page, size, name, status));
    }

    @GetMapping("/{id}")
    public Result<Department> detail(@PathVariable Long id) {
        return departmentService.findById(id)
                .map(Result::ok)
                .orElse(Result.error("部门不存在", 404));
    }

    @PostMapping
    public Result<Department> create(@RequestBody Department body) {
        try {
            return Result.ok(departmentService.create(body));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PutMapping("/{id}")
    public Result<Department> update(@PathVariable Long id, @RequestBody Department body) {
        try {
            return Result.ok(departmentService.update(id, body));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.ok(null);
    }

    @PostMapping("/{id}/enable")
    public Result<Department> enable(@PathVariable Long id) {
        try {
            return Result.ok(departmentService.enable(id));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/{id}/disable")
    public Result<Department> disable(@PathVariable Long id) {
        try {
            return Result.ok(departmentService.disable(id));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }
}