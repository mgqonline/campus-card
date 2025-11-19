package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Clazz;
import com.campus.card.admin.service.ClazzService;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/classes")
public class ClazzController {

    private final ClazzService clazzService;

    public ClazzController(ClazzService clazzService) {
        this.clazzService = clazzService;
    }

    @GetMapping
    public Result<PageResult<Clazz>> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long schoolId
    ) {
        try {
            return Result.ok(clazzService.pageList(page, size, name, gradeId, schoolId));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @GetMapping("/{id}")
    public Result<Clazz> detail(@PathVariable Long id) {
        return clazzService.findById(id)
                .map(Result::ok)
                .orElse(Result.error("班级不存在", 404));
    }

    @PostMapping
    public Result<Clazz> create(@RequestBody Clazz body) {
        try {
            return Result.ok(clazzService.create(body));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PutMapping("/{id}")
    public Result<Clazz> update(@PathVariable Long id, @RequestBody Clazz body) {
        return Result.ok(clazzService.update(id, body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        clazzService.delete(id);
        return Result.ok(null);
    }

    @PostMapping("/{id}/enable")
    public Result<Clazz> enable(@PathVariable Long id) {
        try {
            return Result.ok(clazzService.enable(id));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/{id}/disable")
    public Result<Clazz> disable(@PathVariable Long id) {
        try {
            return Result.ok(clazzService.disable(id));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    // 与前端保持一致的班主任分配接口
    @PostMapping("/{id}/head-teacher")
    public Result<Clazz> assignHeadTeacher(@PathVariable Long id, @RequestBody AssignTeacherRequest request) {
        try {
            return Result.ok(clazzService.assignHeadTeacher(id, request.teacherId));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @DeleteMapping("/{id}/head-teacher")
    public Result<Clazz> removeHeadTeacher(@PathVariable Long id) {
        try {
            return Result.ok(clazzService.removeHeadTeacher(id));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    // 新增：班级学生人数统计
    @GetMapping("/{id}/students/count")
    public Result<Map<String, Integer>> studentCount(@PathVariable Long id) {
        int count = clazzService.countStudents(id);
        return Result.ok(Collections.singletonMap("count", count));
    }

    // 新增：班级升级
    @PostMapping("/{id}/upgrade")
    public Result<Clazz> upgrade(@PathVariable Long id, @RequestBody ChangeGradeRequest request) {
        try {
            return Result.ok(clazzService.changeGrade(id, request.targetGradeId));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    // 新增：班级降级（本质同升级，按目标年级变更）
    @PostMapping("/{id}/downgrade")
    public Result<Clazz> downgrade(@PathVariable Long id, @RequestBody ChangeGradeRequest request) {
        try {
            return Result.ok(clazzService.changeGrade(id, request.targetGradeId));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    // 新增：班级合并
    @PostMapping("/merge")
    public Result<Integer> merge(@RequestBody MergeRequest request) {
        try {
            boolean archive = request.archiveSources != null ? request.archiveSources : true;
            int moved = clazzService.mergeClasses(request.sourceClassIds, request.targetClassId, archive);
            return Result.ok(moved);
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    // 新增：班级拆分
    @PostMapping("/{id}/split")
    public Result<SplitResponse> split(@PathVariable Long id, @RequestBody SplitRequest request) {
        try {
            ClazzService.SplitResult r = clazzService.splitClass(id, request.newClassName, request.targetGradeId, request.headTeacherId, request.studentIds);
            SplitResponse resp = new SplitResponse();
            resp.newClass = r.getNewClass();
            resp.moved = r.getMovedCount();
            return Result.ok(resp);
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }
 
     public static class AssignTeacherRequest { public Long teacherId; }
     public static class ChangeGradeRequest { public Long targetGradeId; }
     public static class MergeRequest { public java.util.List<Long> sourceClassIds; public Long targetClassId; public Boolean archiveSources; }
     public static class SplitRequest { public String newClassName; public Long targetGradeId; public Long headTeacherId; public java.util.List<Long> studentIds; }
     public static class SplitResponse { public Clazz newClass; public int moved; }
}