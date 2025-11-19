package com.campus.card.gateway.controller;

import com.campus.card.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "学生数据", description = "学生基础数据查询接口")
@RestController
@RequestMapping("/api/v1/gw/students")
public class StudentController {

    @Operation(summary = "学生列表")
    @GetMapping
    public Result<List<StudentVO>> list() {
        StudentVO s1 = new StudentVO(1L, 1L, "张三", "S20240001");
        StudentVO s2 = new StudentVO(2L, 2L, "李四", "S20240002");
        return Result.ok(Arrays.asList(s1, s2));
    }

    @Operation(summary = "学生详情")
    @GetMapping("/{id}")
    public Result<StudentVO> detail(@PathVariable Long id) {
        return Result.ok(new StudentVO(id, 1L, "示例学生", "S000000"));
    }

    @Data
    public static class StudentVO {
        private Long id;
        private Long classId;
        private String name;
        private String studentNo;
        public StudentVO(Long id, Long classId, String name, String studentNo) {
            this.id = id; this.classId = classId; this.name = name; this.studentNo = studentNo;
        }
    }
}