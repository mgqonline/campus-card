package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Campus;
import com.campus.card.admin.domain.School;
import com.campus.card.admin.domain.SchoolConfig;
import com.campus.card.admin.domain.SchoolLogo;
import com.campus.card.admin.service.SchoolService;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schools")
public class SchoolController {

    private final SchoolService schoolService;

    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping
    public Result<PageResult<School>> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code
    ) {
        return Result.ok(schoolService.pageList(page, size, name, code));
    }

    @GetMapping("/{id}")
    public Result<School> detail(@PathVariable Long id) {
        return schoolService.findById(id)
                .map(Result::ok)
                .orElse(Result.error("学校不存在", 404));
    }

    @PostMapping
    public Result<School> create(@RequestBody School body) {
        try {
            return Result.ok(schoolService.create(body));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PutMapping("/{id}")
    public Result<School> update(@PathVariable Long id, @RequestBody School body) {
        return Result.ok(schoolService.update(id, body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        schoolService.delete(id);
        return Result.ok(null);
    }

    @PostMapping("/{id}/enable")
    public Result<School> enable(@PathVariable Long id) {
        try {
            return Result.ok(schoolService.enable(id));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/{id}/disable")
    public Result<School> disable(@PathVariable Long id) {
        try {
            return Result.ok(schoolService.disable(id));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    // 校区管理
    @GetMapping("/{schoolId}/campuses")
    public Result<List<Campus>> listCampuses(@PathVariable Long schoolId) {
        return Result.ok(schoolService.listCampuses(schoolId));
    }

    @GetMapping("/{schoolId}/campuses/page")
    public Result<PageResult<Campus>> pageCampuses(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String name
    ) {
        return Result.ok(schoolService.pageCampuses(schoolId, page, size, name));
    }

    @PostMapping("/{schoolId}/campuses")
    public Result<Campus> addCampus(@PathVariable Long schoolId, @RequestBody Campus body) {
        try {
            return Result.ok(schoolService.addCampus(schoolId, body));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PutMapping("/{schoolId}/campuses/{campusId}")
    public Result<Campus> updateCampus(@PathVariable Long schoolId, @PathVariable Long campusId, @RequestBody Campus body) {
        try {
            return Result.ok(schoolService.updateCampus(schoolId, campusId, body));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @DeleteMapping("/{schoolId}/campuses/{campusId}")
    public Result<Void> deleteCampus(@PathVariable Long schoolId, @PathVariable Long campusId) {
        try {
            schoolService.deleteCampus(campusId);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/{schoolId}/campuses/{campusId}/enable")
    public Result<Campus> enableCampus(@PathVariable Long schoolId, @PathVariable Long campusId) {
        try {
            return Result.ok(schoolService.enableCampus(schoolId, campusId));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/{schoolId}/campuses/{campusId}/disable")
    public Result<Campus> disableCampus(@PathVariable Long schoolId, @PathVariable Long campusId) {
        try {
            return Result.ok(schoolService.disableCampus(schoolId, campusId));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    // 学校参数配置
    @GetMapping("/{schoolId}/configs")
    public Result<List<SchoolConfig>> listConfigs(@PathVariable Long schoolId) {
        return Result.ok(schoolService.listConfigs(schoolId));
    }

    public static class ConfigBody {
        public String value;
    }

    @PutMapping("/{schoolId}/configs/{key}")
    public Result<SchoolConfig> setConfig(@PathVariable Long schoolId, @PathVariable String key, @RequestBody ConfigBody body) {
        return Result.ok(schoolService.setConfig(schoolId, key, body.value));
    }

    // 学校Logo
    @GetMapping("/{schoolId}/logo")
    public Result<SchoolLogo> getLogo(@PathVariable Long schoolId) {
        return schoolService.getLogo(schoolId)
                .map(Result::ok)
                .orElse(Result.error("未设置Logo", 404));
    }

    public static class LogoBody {
        public String url;
    }

    @PutMapping("/{schoolId}/logo")
    public Result<SchoolLogo> setLogo(@PathVariable Long schoolId, @RequestBody LogoBody body) {
        return Result.ok(schoolService.setLogo(schoolId, body.url));
    }
}