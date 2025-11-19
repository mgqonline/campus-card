package com.campus.card.admin.controller;

import com.campus.card.admin.domain.ParentInfo;
import com.campus.card.admin.domain.StudentInfo;
import com.campus.card.admin.domain.StudentParent;
import com.campus.card.admin.repository.ParentRepository;
import com.campus.card.admin.repository.StudentParentRepository;
import com.campus.card.admin.service.StudentService;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

// 新增导入
import com.campus.card.admin.domain.ParentWechat;
import com.campus.card.admin.domain.ParentPermission;
import com.campus.card.admin.repository.ParentWechatRepository;
import com.campus.card.admin.repository.ParentPermissionRepository;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/parents")
public class ParentController {

    private final ParentRepository parentRepository;
    private final StudentParentRepository studentParentRepository;
    private final StudentService studentService;
    // 新增仓库
    private final ParentWechatRepository parentWechatRepository;
    private final ParentPermissionRepository parentPermissionRepository;

    public ParentController(ParentRepository parentRepository,
                            StudentParentRepository studentParentRepository,
                            StudentService studentService,
                            ParentWechatRepository parentWechatRepository,
                            ParentPermissionRepository parentPermissionRepository) {
        this.parentRepository = parentRepository;
        this.studentParentRepository = studentParentRepository;
        this.studentService = studentService;
        this.parentWechatRepository = parentWechatRepository;
        this.parentPermissionRepository = parentPermissionRepository;
    }

    // 家长列表（分页、按姓名模糊）
    @GetMapping
    public Result<PageResult<ParentInfo>> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String name
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<ParentInfo> p;
        if (name != null && !name.trim().isEmpty()) {
            p = parentRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        } else {
            p = parentRepository.findAll(pageable);
        }
        PageResult<ParentInfo> result = new PageResult<>();
        result.setRecords(p.getContent());
        result.setTotal((int) p.getTotalElements());
        return Result.ok(result);
    }

    // 家长详情
    @GetMapping("/{id}")
    public Result<ParentInfo> detail(@PathVariable Long id) {
        return parentRepository.findById(id)
                .map(Result::ok)
                .orElse(Result.error("家长不存在", 404));
    }

    // 家长创建
    @PostMapping
    public Result<ParentInfo> create(@RequestBody ParentInfo body) {
        if (body.getName() == null || body.getName().trim().isEmpty()) {
            return Result.error("姓名不能为空", 400);
        }
        body.setCreateTime(LocalDateTime.now());
        body.setUpdateTime(LocalDateTime.now());
        ParentInfo saved = parentRepository.save(body);
        return Result.ok(saved);
    }

    // 家长更新
    @PutMapping("/{id}")
    public Result<ParentInfo> update(@PathVariable Long id, @RequestBody ParentInfo body) {
        Optional<ParentInfo> op = parentRepository.findById(id);
        if (!op.isPresent()) return Result.error("家长不存在", 404);
        ParentInfo p = op.get();
        if (body.getName() != null) p.setName(body.getName());
        if (body.getPhone() != null) p.setPhone(body.getPhone());
        if (body.getEmail() != null) p.setEmail(body.getEmail());
        if (body.getStatus() != null) p.setStatus(body.getStatus());
        p.setUpdateTime(LocalDateTime.now());
        return Result.ok(parentRepository.save(p));
    }

    // 新增：启用/禁用家长账号
    @PutMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable Long id) {
        Optional<ParentInfo> op = parentRepository.findById(id);
        if (!op.isPresent()) return Result.error("家长不存在", 404);
        ParentInfo p = op.get();
        p.setStatus(1);
        p.setUpdateTime(LocalDateTime.now());
        parentRepository.save(p);
        return Result.ok(null);
    }

    @PutMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        Optional<ParentInfo> op = parentRepository.findById(id);
        if (!op.isPresent()) return Result.error("家长不存在", 404);
        ParentInfo p = op.get();
        p.setStatus(0);
        p.setUpdateTime(LocalDateTime.now());
        parentRepository.save(p);
        return Result.ok(null);
    }

    // 新增：家长微信绑定管理
    @GetMapping("/{id}/wechat")
    public Result<ParentWechat> getWechat(@PathVariable Long id) {
        return parentWechatRepository.findById(id)
                .map(Result::ok)
                .orElse(Result.error("未绑定微信", 404));
    }

    @PutMapping("/{id}/wechat")
    public Result<ParentWechat> bindWechat(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String openId = (String) body.get("openId");
        String unionId = (String) body.get("unionId");
        String nickname = (String) body.get("nickname");
        String avatarUrl = (String) body.get("avatarUrl");
        if (openId == null || openId.trim().isEmpty()) {
            return Result.error("openId不能为空", 400);
        }
        Optional<ParentInfo> op = parentRepository.findById(id);
        if (!op.isPresent()) return Result.error("家长不存在", 404);
        ParentWechat pw = parentWechatRepository.findById(id).orElse(new ParentWechat());
        pw.setParentId(id);
        pw.setOpenId(openId);
        pw.setUnionId(unionId);
        pw.setNickname(nickname);
        pw.setAvatarUrl(avatarUrl);
        pw.setStatus(1);
        pw.setBindTime(LocalDateTime.now());
        return Result.ok(parentWechatRepository.save(pw));
    }

    @DeleteMapping("/{id}/wechat")
    public Result<Void> unbindWechat(@PathVariable Long id) {
        if (!parentWechatRepository.existsById(id)) {
            return Result.error("未绑定微信", 404);
        }
        parentWechatRepository.deleteById(id);
        return Result.ok(null);
    }

    // 新增：家长权限设置
    @GetMapping("/{id}/permissions")
    public Result<ParentPermission> getPermissions(@PathVariable Long id) {
        ParentPermission perm = parentPermissionRepository.findById(id).orElseGet(() -> {
            ParentPermission p = new ParentPermission();
            p.setParentId(id);
            return p;
        });
        return Result.ok(perm);
    }

    @PutMapping("/{id}/permissions")
    public Result<ParentPermission> updatePermissions(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        ParentPermission perm = parentPermissionRepository.findById(id).orElseGet(() -> {
            ParentPermission p = new ParentPermission();
            p.setParentId(id);
            return p;
        });
        if (body.containsKey("viewAttendance")) perm.setViewAttendance(Boolean.TRUE.equals(body.get("viewAttendance")) || "true".equals(String.valueOf(body.get("viewAttendance"))));
        if (body.containsKey("viewConsumption")) perm.setViewConsumption(Boolean.TRUE.equals(body.get("viewConsumption")) || "true".equals(String.valueOf(body.get("viewConsumption"))));
        if (body.containsKey("viewGrades")) perm.setViewGrades(Boolean.TRUE.equals(body.get("viewGrades")) || "true".equals(String.valueOf(body.get("viewGrades"))));
        if (body.containsKey("messageTeacher")) perm.setMessageTeacher(Boolean.TRUE.equals(body.get("messageTeacher")) || "true".equals(String.valueOf(body.get("messageTeacher"))));
        return Result.ok(parentPermissionRepository.save(perm));
    }

    // 家长删除（物理删除，谨慎使用）
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            parentRepository.deleteById(id);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage(), 400);
        }
    }

    // 家长-学生关联列表（家长视角）
    @GetMapping("/{id}/students")
    public Result<List<ParentStudentItem>> listStudents(@PathVariable Long id) {
        List<StudentParent> links = studentParentRepository.findByParentId(id);
        List<Long> sids = links.stream().map(StudentParent::getStudentId).distinct().collect(Collectors.toList());
        Map<Long, StudentInfo> stuMap = new HashMap<>();
        for (Long sid : sids) {
            studentService.findById(sid).ifPresent(s -> stuMap.put(sid, s));
        }
        List<ParentStudentItem> items = new ArrayList<>();
        for (StudentParent sp : links) {
            StudentInfo s = stuMap.get(sp.getStudentId());
            if (s == null) continue;
            ParentStudentItem item = new ParentStudentItem();
            item.setId(null); // 复合主键不再提供单独id
            item.setStudentId(sp.getStudentId());
            item.setParentId(sp.getParentId());
            item.setStudentName(s.getName());
            item.setRelation(sp.getRelation());
            item.setClassId(s.getClassId());
            item.setStatus(s.getStatus());
            items.add(item);
        }
        return Result.ok(items);
    }

    public static class ParentStudentItem {
        private Long id; // 关联ID
        private Long studentId;
        private Long parentId;
        private String studentName;
        private String relation;
        private Long classId;
        private Integer status;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getRelation() { return relation; }
        public void setRelation(String relation) { this.relation = relation; }
        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
    }
}