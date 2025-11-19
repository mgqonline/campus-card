package com.campus.card.gateway.controller;

import com.campus.card.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "教师数据", description = "教师基础数据查询接口")
@RestController
@RequestMapping("/api/v1/gw/teachers")
public class TeacherController {

    @Operation(summary = "教师列表")
    @GetMapping
    public Result<List<TeacherVO>> list() {
        TeacherVO t1 = new TeacherVO(1L, "王老师", "数学");
        TeacherVO t2 = new TeacherVO(2L, "李老师", "语文");
        return Result.ok(Arrays.asList(t1, t2));
    }

    @Operation(summary = "教师详情")
    @GetMapping("/{id}")
    public Result<TeacherVO> detail(@PathVariable Long id) {
        return Result.ok(new TeacherVO(id, "示例教师", "示例科目"));
    }

    @Data
    public static class TeacherVO {
        private Long id;
        private String name;
        private String subject;
        public TeacherVO(Long id, String name, String subject) {
            this.id = id; this.name = name; this.subject = subject;
        }
    }
}