package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Permission;
import com.campus.card.admin.repository.PermissionRepository;
import com.campus.card.admin.security.RequiresPermission;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {
    private final PermissionRepository permissionRepository;

    public PermissionController(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    @RequiresPermission({"system:permission:list"})
    public Result<PageResult<Permission>> list(@RequestParam(defaultValue = "1") @Min(1) int page,
                                               @RequestParam(defaultValue = "10") @Min(1) int size,
                                               @RequestParam(required = false) String name,
                                               @RequestParam(required = false) String code) {
        List<Permission> all = permissionRepository.findAll();
        if (name != null && !name.trim().isEmpty()) {
            String kw = name.trim().toLowerCase();
            all = all.stream().filter(p -> p.getName() != null && p.getName().toLowerCase().contains(kw)).collect(java.util.stream.Collectors.toList());
        }
        if (code != null && !code.trim().isEmpty()) {
            String kwc = code.trim().toLowerCase();
            all = all.stream().filter(p -> p.getCode() != null && p.getCode().toLowerCase().contains(kwc)).collect(java.util.stream.Collectors.toList());
        }
        int total = all.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(total, from + size);
        List<Permission> pageList = from >= total ? java.util.Collections.emptyList() : all.subList(from, to);
        return Result.ok(PageResult.of(total, pageList));
    }

    @GetMapping("/{id}")
    public Result<Permission> detail(@PathVariable Long id) {
        Optional<Permission> op = permissionRepository.findById(id);
        if (!op.isPresent()) return Result.error("权限不存在", 404);
        return Result.ok(op.get());
    }

    @PostMapping
    public Result<Permission> create(@RequestBody CreatePermissionReq req) {
        if (req.getCode() == null || req.getCode().trim().isEmpty() || req.getName() == null || req.getName().trim().isEmpty()) {
            return Result.error("code和name不能为空", 400);
        }
        Permission p = new Permission();
        p.setCode(req.getCode().trim());
        p.setName(req.getName().trim());
        p.setDescription(req.getDescription());
        return Result.ok(permissionRepository.save(p));
    }

    @PutMapping("/{id}")
    @RequiresPermission({"system:permission:update"})
    public Result<Permission> update(@PathVariable Long id, @RequestBody UpdatePermissionReq req) {
        Optional<Permission> op = permissionRepository.findById(id);
        if (!op.isPresent()) return Result.error("权限不存在", 404);
        Permission p = op.get();
        if (req.getCode() != null && !req.getCode().trim().isEmpty()) p.setCode(req.getCode().trim());
        if (req.getName() != null && !req.getName().trim().isEmpty()) p.setName(req.getName().trim());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        return Result.ok(permissionRepository.save(p));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!permissionRepository.existsById(id)) return Result.error("权限不存在", 404);
        permissionRepository.deleteById(id);
        return Result.ok(null);
    }

    @Data
    public static class CreatePermissionReq {
        private String code;
        private String name;
        private String description;
    }

    @Data
    public static class UpdatePermissionReq {
        private String code;
        private String name;
        private String description;
    }
}