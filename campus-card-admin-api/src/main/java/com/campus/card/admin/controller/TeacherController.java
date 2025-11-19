package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Teacher;
import com.campus.card.admin.domain.Clazz;
import com.campus.card.admin.domain.ClassSubjectTeacher;
import com.campus.card.admin.service.TeacherService;
import com.campus.card.admin.repository.ClazzRepository;
import com.campus.card.admin.repository.ClassSubjectTeacherRepository;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {

    private final TeacherService teacherService;
    private final ClazzRepository clazzRepository;
    private final ClassSubjectTeacherRepository classSubjectTeacherRepository;

    public TeacherController(TeacherService teacherService, ClazzRepository clazzRepository, ClassSubjectTeacherRepository classSubjectTeacherRepository) {
        this.teacherService = teacherService;
        this.clazzRepository = clazzRepository;
        this.classSubjectTeacherRepository = classSubjectTeacherRepository;
    }

    @GetMapping
    public Result<PageResult<Teacher>> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long schoolId
    ) {
        return Result.ok(teacherService.pageList(page, size, name, schoolId));
    }

    @GetMapping("/{id}")
    public Result<Teacher> detail(@PathVariable Long id) {
        return teacherService.findById(id)
                .map(Result::ok)
                .orElse(Result.error("教师不存在", 404));
    }

    @PostMapping
    public Result<Teacher> create(@RequestBody Teacher body) {
        try {
            return Result.ok(teacherService.create(body));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PutMapping("/{id}")
    public Result<Teacher> update(@PathVariable Long id, @RequestBody Teacher body) {
        return Result.ok(teacherService.update(id, body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return Result.ok(null);
    }

    // 新增：查询教师担任班主任的班级列表
    @GetMapping("/{id}/head-classes")
    public Result<List<Clazz>> listHeadClasses(@PathVariable Long id) {
        List<Clazz> classes = clazzRepository.findByHeadTeacherId(id);
        return Result.ok(classes);
    }

    // 新增：查询教师的班级-学科任教关联列表
    @GetMapping("/{id}/subject-classes")
    public Result<List<ClassSubjectTeacher>> listSubjectClasses(@PathVariable Long id) {
        List<ClassSubjectTeacher> list = classSubjectTeacherRepository.findByTeacherId(id);
        return Result.ok(list);
    }
}