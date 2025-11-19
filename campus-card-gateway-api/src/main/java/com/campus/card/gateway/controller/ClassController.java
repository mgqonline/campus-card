package com.campus.card.gateway.controller;

import com.campus.card.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "班级数据", description = "班级基础数据查询接口")
@RestController
@RequestMapping("/api/v1/gw/classes")
public class ClassController {

    @Operation(summary = "班级列表")
    @GetMapping
    public Result<List<ClassVO>> list() {
        ClassVO c1 = new ClassVO(1L, 1L, "高一(1)班");
        ClassVO c2 = new ClassVO(2L, 1L, "高一(2)班");
        return Result.ok(Arrays.asList(c1, c2));
    }

    @Operation(summary = "班级详情")
    @GetMapping("/{id}")
    public Result<ClassVO> detail(@PathVariable Long id) {
        return Result.ok(new ClassVO(id, 1L, "示例班级"));
    }

    @Data
    public static class ClassVO {
        private Long id;
        private Long schoolId;
        private String name;
        public ClassVO(Long id, Long schoolId, String name) {
            this.id = id; this.schoolId = schoolId; this.name = name;
        }
    }
}