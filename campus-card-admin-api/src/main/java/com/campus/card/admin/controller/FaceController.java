package com.campus.card.admin.controller;

import com.campus.card.admin.domain.FaceInfo;
import com.campus.card.admin.service.FaceService;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/faces")
public class FaceController {

    private final FaceService faceService;

    public FaceController(FaceService faceService) {
        this.faceService = faceService;
    }

    @GetMapping
    public Result<PageResult<FaceInfo>> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String personType,
            @RequestParam(required = false) String personId
    ) {
        return Result.ok(faceService.list(page, size, personType, personId));
    }

    @GetMapping("/{id}")
    public Result<FaceInfo> detail(@PathVariable Long id) {
        Optional<FaceInfo> of = faceService.get(id);
        return of.map(Result::ok).orElse(Result.error("人脸记录不存在", 404));
    }

    @PostMapping
    public Result<FaceInfo> create(@RequestBody FaceService.CreateReq req) {
        try {
            return Result.ok(faceService.create(req));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PutMapping("/{id}")
    public Result<FaceInfo> update(@PathVariable Long id, @RequestBody FaceService.UpdateReq req) {
        try {
            req.setId(id);
            return Result.ok(faceService.update(req));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        faceService.delete(id);
        return Result.ok(null);
    }

    @PostMapping("/quality")
    public Result<FaceService.QualityResult> quality(@RequestBody FaceService.QualityReq req) {
        return Result.ok(faceService.quality(req));
    }

    @GetMapping("/{id}/photo")
    public Result<Map<String, String>> photo(@PathVariable Long id) {
        Optional<FaceInfo> of = faceService.get(id);
        if (!of.isPresent()) return Result.error("人脸记录不存在", 404);
        Map<String, String> resp = new HashMap<>();
        resp.put("photoBase64", of.get().getPhotoBase64());
        return Result.ok(resp);
    }
}