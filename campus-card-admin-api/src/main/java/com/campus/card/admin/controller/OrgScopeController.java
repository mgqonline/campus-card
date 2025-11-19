package com.campus.card.admin.controller;

import com.campus.card.admin.domain.DataScope;
import com.campus.card.admin.repository.DataScopeRepository;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/org-scope")
public class OrgScopeController {

    private final DataScopeRepository dataScopeRepository;

    public OrgScopeController(DataScopeRepository dataScopeRepository) {
        this.dataScopeRepository = dataScopeRepository;
    }

    @GetMapping("/subject")
    public Result<List<DataScope>> listBySubject(@RequestParam String subjectType,
                                                 @RequestParam Long subjectId) {
        try {
            List<DataScope> list = dataScopeRepository.findBySubjectTypeAndSubjectId(subjectType, subjectId);
            return Result.ok(list);
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PutMapping("/subject")
    public Result<List<DataScope>> replaceSubjectScopes(@RequestParam String subjectType,
                                                        @RequestParam Long subjectId,
                                                        @RequestBody List<ScopeItemReq> scopes) {
        try {
            // 删除原有配置
            List<DataScope> old = dataScopeRepository.findBySubjectTypeAndSubjectId(subjectType, subjectId);
            if (old != null && !old.isEmpty()) {
                dataScopeRepository.deleteAll(old);
            }
            // 保存新配置
            List<DataScope> toSave = new ArrayList<>();
            if (scopes != null) {
                for (ScopeItemReq req : scopes) {
                    if (req == null || req.scopeType == null || req.scopeType.trim().isEmpty()) continue;
                    DataScope ds = new DataScope();
                    ds.setSubjectType(subjectType);
                    ds.setSubjectId(subjectId);
                    ds.setScopeType(req.scopeType.trim().toUpperCase(Locale.ROOT));
                    ds.setSchoolIds(nullIfBlank(req.schoolIds));
                    ds.setGradeIds(nullIfBlank(req.gradeIds));
                    ds.setClassIds(nullIfBlank(req.classIds));
                    ds.setStudentIds(nullIfBlank(req.studentIds));
                    ds.setStatus(req.status == null ? 1 : req.status);
                    toSave.add(ds);
                }
            }
            List<DataScope> saved = dataScopeRepository.saveAll(toSave);
            return Result.ok(saved);
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    private String nullIfBlank(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public static class ScopeItemReq {
        public String scopeType; // SCHOOL / GRADE / CLASS / PERSONAL
        public String schoolIds; // CSV
        public String gradeIds;  // CSV
        public String classIds;  // CSV
        public String studentIds;// CSV
        public Integer status;   // 1启用 0禁用

        public String getScopeType() { return scopeType; }
        public void setScopeType(String scopeType) { this.scopeType = scopeType; }
        public String getSchoolIds() { return schoolIds; }
        public void setSchoolIds(String schoolIds) { this.schoolIds = schoolIds; }
        public String getGradeIds() { return gradeIds; }
        public void setGradeIds(String gradeIds) { this.gradeIds = gradeIds; }
        public String getClassIds() { return classIds; }
        public void setClassIds(String classIds) { this.classIds = classIds; }
        public String getStudentIds() { return studentIds; }
        public void setStudentIds(String studentIds) { this.studentIds = studentIds; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
    }
}