package com.campus.card.admin.controller;

import com.campus.card.admin.domain.ParentInfo;
import com.campus.card.admin.domain.StudentParent;
import com.campus.card.admin.domain.StudentInfo;
import com.campus.card.admin.repository.ParentRepository;
import com.campus.card.admin.repository.StudentParentRepository;
import com.campus.card.admin.service.StudentService;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/students")
public class StudentRelationController {

    private final StudentService studentService;
    private final ParentRepository parentRepository;
    private final StudentParentRepository studentParentRepository;

    public StudentRelationController(StudentService studentService,
                                     ParentRepository parentRepository,
                                     StudentParentRepository studentParentRepository) {
        this.studentService = studentService;
        this.parentRepository = parentRepository;
        this.studentParentRepository = studentParentRepository;
    }

    // 学生-家长关联列表
    @GetMapping("/{id}/parents")
    public Result<List<StudentParentItem>> listParents(@PathVariable Long id) {
        List<StudentParent> links = studentParentRepository.findByStudentId(id);
        List<Long> pids = links.stream().map(StudentParent::getParentId).distinct().collect(Collectors.toList());
        Map<Long, ParentInfo> parentMap = parentRepository.findAllById(pids).stream().collect(Collectors.toMap(ParentInfo::getId, p -> p));
        List<StudentParentItem> items = new ArrayList<>();
        for (StudentParent sp : links) {
            ParentInfo p = parentMap.get(sp.getParentId());
            if (p == null) continue;
            StudentParentItem item = new StudentParentItem();
            item.setId(null);
            item.setParentId(p.getId());
            item.setName(p.getName());
            item.setPhone(p.getPhone());
            item.setEmail(p.getEmail());
            item.setRelation(sp.getRelation());
            item.setStatus(p.getStatus());
            items.add(item);
        }
        return Result.ok(items);
    }

    // 绑定家长（可新建或绑定已有）
    @PostMapping("/{id}/parents")
    public Result<StudentParentItem> addParent(@PathVariable Long id, @RequestBody ParentLinkRequest req) {
        try {
            Optional<StudentInfo> os = studentService.findById(id);
            if (!os.isPresent()) return Result.error("学生不存在", 404);
            Long parentId = req.getParentId();
            ParentInfo p;
            if (parentId == null) {
                p = new ParentInfo();
                p.setName(req.getName());
                p.setPhone(req.getPhone());
                p.setEmail(req.getEmail());
                p = parentRepository.save(p);
                parentId = p.getId();
            } else {
                p = parentRepository.findById(parentId).orElse(null);
                if (p == null) return Result.error("家长不存在", 404);
            }
            if (studentParentRepository.existsByStudentIdAndParentId(id, parentId)) {
                return Result.error("已存在关联", 400);
            }
            StudentParent sp = new StudentParent();
            sp.setStudentId(id);
            sp.setParentId(parentId);
            sp.setRelation(req.getRelation());
            sp = studentParentRepository.save(sp);
            StudentParentItem item = new StudentParentItem();
            item.setId(null);
            item.setParentId(p.getId());
            item.setName(p.getName());
            item.setPhone(p.getPhone());
            item.setEmail(p.getEmail());
            item.setRelation(sp.getRelation());
            item.setStatus(p.getStatus());
            return Result.ok(item);
        } catch (Exception e) {
            return Result.error("关联失败: " + e.getMessage(), 400);
        }
    }

    // 解除家长关联
    @DeleteMapping("/{id}/parents/{parentId}")
    @Transactional
    public Result<Void> removeParent(@PathVariable Long id, @PathVariable Long parentId) {
        try {
            studentParentRepository.deleteByStudentIdAndParentId(id, parentId);
             return Result.ok(null);
        } catch (Exception e) {
            return Result.error("解除失败: " + e.getMessage(), 400);
        }
    }

    // 更新家长关系
    @PutMapping("/{id}/parents/{parentId}")
    public Result<Void> updateRelation(@PathVariable Long id, @PathVariable Long parentId, @RequestBody Map<String, String> body) {
        try {
            String relation = body.get("relation");
            if (relation == null || relation.trim().isEmpty()) {
                return Result.error("关系不能为空", 400);
            }
            List<StudentParent> links = studentParentRepository.findByStudentId(id);
            boolean updated = false;
            for (StudentParent sp : links) {
                if (Objects.equals(sp.getParentId(), parentId)) {
                    sp.setRelation(relation);
                    studentParentRepository.save(sp);
                    updated = true;
                }
            }
            if (!updated) return Result.error("未找到关联", 404);
            return Result.ok(null);
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage(), 400);
        }
    }
    public static class ParentLinkRequest {
        private Long parentId; // 可选，存在时使用已有家长
        private String name;
        private String phone;
        private String email;
        private String relation;
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRelation() { return relation; }
        public void setRelation(String relation) { this.relation = relation; }
    }

    public static class StudentParentItem {
        private Long id; // 关联ID
        private Long parentId;
        private String name;
        private String phone;
        private String email;
        private String relation;
        private Integer status;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRelation() { return relation; }
        public void setRelation(String relation) { this.relation = relation; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
    }
}