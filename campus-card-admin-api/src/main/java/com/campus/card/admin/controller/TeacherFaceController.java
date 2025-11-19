package com.campus.card.admin.controller;

import com.campus.card.admin.domain.FaceInfo;
import com.campus.card.admin.domain.Teacher;
import com.campus.card.admin.repository.FaceInfoRepository;
import com.campus.card.admin.service.TeacherService;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherFaceController {

    private final TeacherService teacherService;
    private final FaceInfoRepository faceInfoRepository;

    public TeacherFaceController(TeacherService teacherService, FaceInfoRepository faceInfoRepository) {
        this.teacherService = teacherService;
        this.faceInfoRepository = faceInfoRepository;
    }

    @GetMapping("/{id}/faces")
    public Result<List<FaceInfo>> listFaces(@PathVariable Long id) {
        Optional<Teacher> ot = teacherService.findById(id);
        if (!ot.isPresent()) return Result.error("教师不存在", 404);
        String personId = ot.get().getTeacherNo();
        List<FaceInfo> list = faceInfoRepository.findByPersonTypeAndPersonId("TEACHER", personId);
        return Result.ok(list);
    }
}