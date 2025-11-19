package com.campus.card.admin.controller;

import com.campus.card.admin.domain.FaceConfig;
import com.campus.card.admin.service.FaceConfigService;
import com.campus.card.common.result.Result;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/face-config")
public class FaceConfigController {
    private final FaceConfigService service;

    public FaceConfigController(FaceConfigService service) {
        this.service = service;
    }

    @GetMapping
    public Result<FaceConfig> get() {
        return Result.ok(service.get());
    }

    @Data
    public static class UpdateReq {
        private Integer recognitionThreshold; // 0-100
        private String recognitionMode;       // ONE_TO_ONE / ONE_TO_MANY
        private Boolean livenessEnabled;      // true/false
        private Integer libraryCapacity;      // >0
    }

    @PutMapping
    public Result<FaceConfig> update(@RequestBody UpdateReq req) {
        try {
            FaceConfig c = service.update(
                req.getRecognitionThreshold(),
                req.getRecognitionMode(),
                req.getLivenessEnabled(),
                req.getLibraryCapacity()
            );
            return Result.ok(c);
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }
}