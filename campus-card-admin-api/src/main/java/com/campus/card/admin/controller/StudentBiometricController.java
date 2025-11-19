package com.campus.card.admin.controller;

import com.campus.card.admin.domain.StudentFace;
import com.campus.card.admin.domain.StudentInfo;
import com.campus.card.admin.repository.StudentFaceRepository;
import com.campus.card.admin.service.StudentService;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/students")
public class StudentBiometricController {

    private final StudentService studentService;
    private final StudentFaceRepository studentFaceRepository;

    public StudentBiometricController(StudentService studentService, StudentFaceRepository studentFaceRepository) {
        this.studentService = studentService;
        this.studentFaceRepository = studentFaceRepository;
    }

    @GetMapping("/{id}/face")
    public Result<String> getFace(@PathVariable Long id) {
        Optional<StudentFace> of = studentFaceRepository.findByStudentId(id);
        return Result.ok(of.map(StudentFace::getFaceToken).orElse(null));
    }

    @PutMapping("/{id}/face")
    public Result<StudentFace> setFace(@PathVariable Long id, @RequestBody FaceRequest req) {
        try {
            Optional<StudentInfo> os = studentService.findById(id);
            if (!os.isPresent()) return Result.error("学生不存在", 404);
            StudentFace face = studentFaceRepository.findByStudentId(id).orElseGet(StudentFace::new);
            face.setStudentId(id);
            face.setFaceToken(req.getFaceToken());
            face.setStatus(1);
            face = studentFaceRepository.save(face);
            return Result.ok(face);
        } catch (Exception e) {
            return Result.error("设置失败: " + e.getMessage(), 400);
        }
    }

    @DeleteMapping("/{id}/face")
    public Result<Void> deleteFace(@PathVariable Long id) {
        Optional<StudentFace> of = studentFaceRepository.findByStudentId(id);
        of.ifPresent(f -> studentFaceRepository.deleteById(f.getId()));
        return Result.ok(null);
    }

    public static class FaceRequest {
        private String faceToken;
        public String getFaceToken() { return faceToken; }
        public void setFaceToken(String faceToken) { this.faceToken = faceToken; }
    }
}