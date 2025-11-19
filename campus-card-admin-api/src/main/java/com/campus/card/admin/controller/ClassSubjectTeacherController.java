package com.campus.card.admin.controller;

import com.campus.card.admin.domain.ClassSubjectTeacher;
import com.campus.card.admin.service.ClassSubjectTeacherService;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes/{classId}/subject-teachers")
public class ClassSubjectTeacherController {

    private final ClassSubjectTeacherService service;

    public ClassSubjectTeacherController(ClassSubjectTeacherService service) {
        this.service = service;
    }

    @GetMapping
    public Result<List<ClassSubjectTeacher>> list(@PathVariable Long classId) {
        return Result.ok(service.listByClass(classId));
    }

    @PostMapping
    public Result<ClassSubjectTeacher> assign(@PathVariable Long classId, @RequestBody AssignRequest req) {
        try {
            return Result.ok(service.assign(classId, req.subjectId, req.teacherId));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long classId, @PathVariable Long id) {
        service.remove(id);
        return Result.ok(null);
    }

    @DeleteMapping
    public Result<Void> removeBySubject(@PathVariable Long classId, @RequestParam Long subjectId) {
        service.removeByClassAndSubject(classId, subjectId);
        return Result.ok(null);
    }

    public static class AssignRequest {
        public Long subjectId;
        public Long teacherId;
    }
}