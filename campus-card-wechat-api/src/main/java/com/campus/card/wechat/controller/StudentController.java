package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student")
public class StudentController {

    private final com.campus.card.wechat.repository.StudentRepository studentRepository;
    // 添加班级和年级仓库
    private final com.campus.card.wechat.repository.ClazzRepository clazzRepository;
    private final com.campus.card.wechat.repository.GradeRepository gradeRepository;

    public StudentController(com.campus.card.wechat.repository.StudentRepository studentRepository,
                             com.campus.card.wechat.repository.ClazzRepository clazzRepository,
                             com.campus.card.wechat.repository.GradeRepository gradeRepository) {
        this.studentRepository = studentRepository;
        this.clazzRepository = clazzRepository;
        this.gradeRepository = gradeRepository;
    }

    @GetMapping("/info")
    public Result<StudentInfo> info(@RequestParam Long childId) {
        com.campus.card.wechat.model.Student s = studentRepository.findById(childId).orElse(null);
        StudentInfo resp = new StudentInfo();
        if (s != null) {
            resp.setId(s.getId());
            resp.setName(s.getName());
            resp.setClassId(s.getClassId());
            resp.setCardNo(s.getCardNo());
            resp.setFaceStatus(s.getFaceStatus());
            
            // 优先使用数据库中的年级信息，如果没有则通过关联查询获取
            String gradeName = s.getGrade();
            if (gradeName == null || gradeName.isEmpty()) {
                if (s.getClassId() != null) {
                    com.campus.card.wechat.model.Clazz clazz = clazzRepository.findById(s.getClassId()).orElse(null);
                    if (clazz != null && clazz.getGradeId() != null) {
                        com.campus.card.wechat.model.Grade grade = gradeRepository.findById(clazz.getGradeId()).orElse(null);
                        if (grade != null) {
                            gradeName = grade.getName();
                        }
                    }
                }
            }
            resp.setGrade(gradeName);
        } else {
            // 若数据库无记录，返回空壳，便于前端处理
            resp.setId(childId);
            resp.setName("未登记");
            resp.setClassId(0L);
            resp.setGrade("");
            resp.setCardNo("");
            resp.setFaceStatus("未采集");
        }
        return Result.ok(resp);
    }

    @Data
    public static class StudentInfo {
        private Long id;
        private String name;
        private Long classId;
        private String grade;
        private String cardNo;
        private String faceStatus;
    }
}