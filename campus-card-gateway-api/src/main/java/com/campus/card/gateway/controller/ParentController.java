package com.campus.card.gateway.controller;

import com.campus.card.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "家长数据", description = "家长基础数据查询接口")
@RestController
@RequestMapping("/api/v1/gw/parents")
public class ParentController {

    @Operation(summary = "家长列表")
    @GetMapping
    public Result<List<ParentVO>> list() {
        ParentVO p1 = new ParentVO(1L, "张三父亲", "13800000001");
        ParentVO p2 = new ParentVO(2L, "李四母亲", "13800000002");
        return Result.ok(Arrays.asList(p1, p2));
    }

    @Operation(summary = "家长详情")
    @GetMapping("/{id}")
    public Result<ParentVO> detail(@PathVariable Long id) {
        return Result.ok(new ParentVO(id, "示例家长", "13800000000"));
    }

    @Data
    public static class ParentVO {
        private Long id;
        private String name;
        private String phone;
        public ParentVO(Long id, String name, String phone) {
            this.id = id; this.name = name; this.phone = phone;
        }
    }
}