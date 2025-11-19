package com.campus.card.admin.controller;

import com.campus.card.admin.domain.SystemConfig;
import com.campus.card.admin.repository.SystemConfigRepository;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/system/configs")
public class SystemConfigController {

    private final SystemConfigRepository repository;

    public SystemConfigController(SystemConfigRepository repository) {
        this.repository = repository;
    }

    private static final List<String> CATEGORIES = Arrays.asList(
            "PARAM", "INTERFACE", "PUSH", "STORAGE", "BACKUP"
    );

    @GetMapping("/categories")
    public Result<List<String>> categories() {
        return Result.ok(CATEGORIES);
    }

    @GetMapping("/{category}")
    public Result<List<SystemConfig>> listByCategory(@PathVariable String category) {
        if (!CATEGORIES.contains(category)) {
            return Result.error("未知配置分类", 400);
        }
        return Result.ok(repository.findByCategory(category));
    }

    public static class ConfigBody { public String value; }

    @PutMapping("/{category}/{key}")
    public Result<SystemConfig> setConfig(@PathVariable String category,
                                          @PathVariable String key,
                                          @RequestBody ConfigBody body) {
        if (!CATEGORIES.contains(category)) {
            return Result.error("未知配置分类", 400);
        }
        Optional<SystemConfig> opt = repository.findByKey(key);
        SystemConfig cfg = opt.orElseGet(SystemConfig::new);
        cfg.setKey(key);
        cfg.setCategory(category);
        cfg.setValue(body.value);
        cfg.setUpdatedAt(LocalDateTime.now());
        return Result.ok(repository.save(cfg));
    }
}