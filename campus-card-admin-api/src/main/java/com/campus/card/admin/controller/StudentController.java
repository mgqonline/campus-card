package com.campus.card.admin.controller;

import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import com.campus.card.admin.domain.StudentInfo;
import com.campus.card.admin.service.StudentService;
import com.campus.card.admin.service.OrgScopeService;
import com.campus.card.admin.repository.UserRepository;
import com.campus.card.admin.domain.User;
import com.campus.card.admin.domain.Clazz;
import com.campus.card.admin.domain.Grade;
import com.campus.card.admin.domain.School;
import com.campus.card.admin.repository.ClazzRepository;
import com.campus.card.admin.repository.GradeRepository;
import com.campus.card.admin.repository.SchoolRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import javax.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;
    private final ClazzRepository clazzRepository;
    private final GradeRepository gradeRepository;
    private final SchoolRepository schoolRepository;
    private final OrgScopeService orgScopeService;
    private final UserRepository userRepository;


    public StudentController(StudentService studentService, ClazzRepository clazzRepository, GradeRepository gradeRepository, SchoolRepository schoolRepository) {
        this.studentService = studentService;
        this.clazzRepository = clazzRepository;
        this.gradeRepository = gradeRepository;
        this.schoolRepository = schoolRepository;
        this.orgScopeService = null;
        this.userRepository = null;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public StudentController(StudentService studentService, ClazzRepository clazzRepository, GradeRepository gradeRepository, SchoolRepository schoolRepository,
                             OrgScopeService orgScopeService,
                             UserRepository userRepository) {
        this.studentService = studentService;
        this.clazzRepository = clazzRepository;
        this.gradeRepository = gradeRepository;
        this.schoolRepository = schoolRepository;
        this.orgScopeService = orgScopeService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public Result<PageResult<StudentListItem>> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long schoolId,
            @RequestHeader(value = "Authorization", required = false) String auth
    ) {
        try {
            // 解析当前用户并计算数据范围
            Long userId = null;
            if (auth != null && !auth.isEmpty()) {
                String token = auth.trim();
                if (token.toLowerCase(java.util.Locale.ROOT).startsWith("bearer ")) token = token.substring(7).trim();
                if (token.startsWith("admin-token-")) {
                    String tail = token.substring("admin-token-".length());
                    try { userId = Long.parseLong(tail); } catch (Exception ignored) {}
                }
            }
            java.util.List<Long> allowedClassIds = null;
            java.util.Set<Long> allowedStudentIds = null;
            if (userId != null && orgScopeService != null && userRepository != null) {
                try {
                    java.util.Optional<User> ou = userRepository.findByIdWithRolesPermsMenus(userId);
                    if (ou.isPresent()) {
                        OrgScopeService.EffectiveScope eff = orgScopeService.resolveForUser(ou.get());
                        if (eff != null) {
                            if (eff.classIds != null && !eff.classIds.isEmpty()) {
                                allowedClassIds = new java.util.ArrayList<>(eff.classIds);
                            }
                            if (eff.studentIds != null && !eff.studentIds.isEmpty()) {
                                allowedStudentIds = eff.studentIds;
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // 数据范围表可能尚未创建或加载失败，忽略以保证列表正常返回
                }
            }

            PageResult<StudentInfo> pr = studentService.pageList(page, size, name, classId, gradeId, schoolId, allowedClassIds, allowedStudentIds);
            List<StudentInfo> students = pr.getRecords();
            List<Long> classIds = students.stream().map(StudentInfo::getClassId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            Map<Long, Clazz> clazzMap = classIds.isEmpty() ? java.util.Collections.emptyMap() : clazzRepository.findAllById(classIds).stream().collect(Collectors.toMap(Clazz::getId, c -> c));
            Set<Long> gradeIds = clazzMap.isEmpty() ? java.util.Collections.emptySet() : clazzMap.values().stream().map(Clazz::getGradeId).filter(Objects::nonNull).collect(Collectors.toSet());
            Set<Long> schoolIds = clazzMap.isEmpty() ? java.util.Collections.emptySet() : clazzMap.values().stream().map(Clazz::getSchoolId).filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Long, Grade> gradeMap = gradeIds.isEmpty() ? java.util.Collections.emptyMap() : gradeRepository.findAllById(gradeIds).stream().collect(Collectors.toMap(Grade::getId, g -> g));
            Map<Long, School> schoolMap = schoolIds.isEmpty() ? java.util.Collections.emptyMap() : schoolRepository.findAllById(schoolIds).stream().collect(Collectors.toMap(School::getId, s -> s));
            List<StudentListItem> items = new ArrayList<>();
            for (StudentInfo s : students) {
                StudentListItem item = new StudentListItem();
                item.setId(s.getId());
                item.setName(s.getName());
                item.setStudentNo(s.getStudentNo());
                item.setClassId(s.getClassId());
                item.setStatus(s.getStatus());
                Clazz clazz = s.getClassId() != null ? clazzMap.get(s.getClassId()) : null;
                if (clazz != null) {
                    item.setClassName(clazz.getName());
                    item.setGradeId(clazz.getGradeId());
                    item.setSchoolId(clazz.getSchoolId());
                    Grade g = clazz.getGradeId() != null ? gradeMap.get(clazz.getGradeId()) : null;
                    if (g != null) item.setGradeName(g.getName());
                    School sch = clazz.getSchoolId() != null ? schoolMap.get(clazz.getSchoolId()) : null;
                    if (sch != null) item.setSchoolName(sch.getName());
                }
                items.add(item);
            }
            return Result.ok(PageResult.of(pr.getTotal(), items));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @GetMapping("/{id}")
    public Result<StudentInfo> detail(@PathVariable Long id) {
        return studentService.findById(id)
                .map(Result::ok)
                .orElse(Result.error("学生不存在", 404));
    }

    // 按学号查询学生详情（便于前端通过学号进行绑定等操作）
    @GetMapping("/by-no/{studentNo}")
    public Result<StudentInfo> getByStudentNo(@PathVariable String studentNo) {
        try {
            return studentService.findByStudentNo(studentNo)
                    .map(Result::ok)
                    .orElse(Result.error("学生不存在", 404));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping
    public Result<StudentInfo> create(@RequestBody StudentInfo body) {
        try {
            return Result.ok(studentService.create(body));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PutMapping("/{id}")
    public Result<StudentInfo> update(@PathVariable Long id, @RequestBody StudentInfo body) {
        return Result.ok(studentService.update(id, body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return Result.ok(null);
    }

    // 启用/禁用/毕业
    @PostMapping("/{id}/enable")
    public Result<StudentInfo> enable(@PathVariable Long id) {
        try {
            return Result.ok(studentService.enable(id));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/{id}/disable")
    public Result<StudentInfo> disable(@PathVariable Long id) {
        try {
            return Result.ok(studentService.disable(id));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/{id}/graduate")
    public Result<StudentInfo> graduate(@PathVariable Long id) {
        try {
            return Result.ok(studentService.graduate(id));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/import")
    public Result<StudentService.ImportResult> importStudents(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "classId", required = false) Long classId) {
        try {
            StudentService.ImportResult report = studentService.importCsv(file.getInputStream(), classId);
            return Result.ok(report);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage(), 400);
        }
    }

    // 批量转班
    @PostMapping("/transfer")
    public Result<StudentService.TransferResult> transfer(@RequestBody BatchTransferRequest req) {
        try {
            StudentService.TransferResult r = studentService.transfer(req.getStudentIds(), req.getClassId());
            return Result.ok(r);
        } catch (Exception e) {
            return Result.error("转班失败: " + e.getMessage(), 400);
        }
    }

    // 单个学生转班（便于前端在学生详情页直接转班）
    @PostMapping("/{id}/transfer")
    public Result<StudentService.TransferResult> transferSingle(@PathVariable Long id, @RequestBody SingleTransferRequest req) {
        try {
            java.util.List<Long> ids = java.util.Collections.singletonList(id);
            StudentService.TransferResult r = studentService.transfer(ids, req.getClassId());
            return Result.ok(r);
        } catch (Exception e) {
            return Result.error("转班失败: " + e.getMessage(), 400);
        }
    }

    public static class SingleTransferRequest {
        private Long classId;
        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }
    }

    public static class BatchTransferRequest {
        private java.util.List<Long> studentIds;
        private Long classId;
        public java.util.List<Long> getStudentIds() { return studentIds; }
        public void setStudentIds(java.util.List<Long> studentIds) { this.studentIds = studentIds; }
        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }
    }

    // 学生照片管理
    @PostMapping("/{id}/photo")
    public Result<StudentInfo> uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Optional<StudentInfo> os = studentService.findById(id);
            if (!os.isPresent()) return Result.error("学生不存在", 404);
            StudentInfo s = os.get();
            // 保存到 uploads/students/{id}/ 原始文件名
            Path base = Paths.get("uploads", "students", String.valueOf(id));
            Files.createDirectories(base);
            String original = file.getOriginalFilename();
            String filename = (original == null || original.isEmpty()) ? ("photo-" + System.currentTimeMillis() + ".jpg") : original;
            Path target = base.resolve(filename);
            Files.write(target, file.getBytes());
            s.setPhotoPath(target.toString());
            return Result.ok(studentService.update(id, s));
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage(), 400);
        }
    }

    @GetMapping(value = "/{id}/photo", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {
        try {
            Optional<StudentInfo> os = studentService.findById(id);
            if (!os.isPresent()) return ResponseEntity.status(404).build();
            StudentInfo s = os.get();
            if (s.getPhotoPath() == null || s.getPhotoPath().isEmpty()) return ResponseEntity.status(404).build();
            Path p = Paths.get(s.getPhotoPath());
            if (!Files.exists(p)) return ResponseEntity.status(404).build();
            byte[] data = Files.readAllBytes(p);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}/photo")
    public Result<StudentInfo> deletePhoto(@PathVariable Long id) {
        try {
            Optional<StudentInfo> os = studentService.findById(id);
            if (!os.isPresent()) return Result.error("学生不存在", 404);
            StudentInfo s = os.get();
            if (s.getPhotoPath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(s.getPhotoPath()));
                } catch (Exception ignore) {}
            }
            s.setPhotoPath(null);
            return Result.ok(studentService.update(id, s));
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage(), 400);
        }
    }

    // 学生档案管理
    @GetMapping("/{id}/archive")
    public Result<String> getArchive(@PathVariable Long id) {
        Optional<StudentInfo> os = studentService.findById(id);
        if (!os.isPresent()) return Result.error("学生不存在", 404);
        return Result.ok(os.get().getArchive());
    }

    @PutMapping("/{id}/archive")
    public Result<StudentInfo> updateArchive(@PathVariable Long id, @RequestBody String archive) {
        try {
            Optional<StudentInfo> os = studentService.findById(id);
            if (!os.isPresent()) return Result.error("学生不存在", 404);
            StudentInfo s = os.get();
            s.setArchive(archive);
            return Result.ok(studentService.update(id, s));
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage(), 400);
        }
    }

    // 学生列表项（包含学校/年级/班级关联信息）
    public static class StudentListItem {
        private Long id;
        private String name;
        private String studentNo;
        private Long classId;
        private String className;
        private Long gradeId;
        private String gradeName;
        private Long schoolId;
        private String schoolName;
        private Integer status;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public Long getGradeId() { return gradeId; }
        public void setGradeId(Long gradeId) { this.gradeId = gradeId; }
        public String getGradeName() { return gradeName; }
        public void setGradeName(String gradeName) { this.gradeName = gradeName; }
        public Long getSchoolId() { return schoolId; }
        public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
        public String getSchoolName() { return schoolName; }
        public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
    }
}