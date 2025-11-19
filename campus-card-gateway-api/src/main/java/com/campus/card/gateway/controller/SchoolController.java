package com.campus.card.gateway.controller;

import com.campus.card.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "学校数据", description = "学校基础数据查询接口")
@RestController
@RequestMapping("/api/v1/gw/schools")
public class SchoolController {

    @Operation(summary = "学校列表")
    @GetMapping
    public Result<List<SchoolVO>> list() {
        SchoolVO s1 = new SchoolVO(1L, "第一中学");
        SchoolVO s2 = new SchoolVO(2L, "第二中学");
        return Result.ok(Arrays.asList(s1, s2));
    }

    @Operation(summary = "学校详情")
    @GetMapping("/{id}")
    public Result<SchoolVO> detail(@PathVariable Long id) {
        return Result.ok(new SchoolVO(id, "示例学校"));
    }

    @Data
    public static class SchoolVO {
        private Long id;
        private String name;
        public SchoolVO(Long id, String name) { this.id = id; this.name = name; }
    }
}