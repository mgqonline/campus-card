package com.campus.card.admin.service;

import com.campus.card.admin.domain.StudentInfo;
import com.campus.card.admin.repository.StudentRepository;
import com.campus.card.common.result.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.campus.card.admin.domain.Clazz;
import com.campus.card.admin.repository.ClazzRepository;

@Service
public class StudentService {
    private final StudentRepository repository;
    private final ClazzRepository clazzRepository;

    public StudentService(StudentRepository repository, ClazzRepository clazzRepository) {
        this.repository = repository;
        this.clazzRepository = clazzRepository;
    }

    public PageResult<StudentInfo> pageList(int page, int size, String name, Long classId, Long gradeId, Long schoolId,
                                             java.util.List<Long> allowedClassIds,
                                             java.util.Set<Long> allowedStudentIds) {
        // 预计算请求条件产生的班级ID集合
        java.util.List<Long> classIdsFilter = null;
        if (classId != null) {
            classIdsFilter = java.util.Collections.singletonList(classId);
        } else {
            java.util.List<com.campus.card.admin.domain.Clazz> classes = null;
            if (gradeId != null && schoolId != null) {
                java.util.List<com.campus.card.admin.domain.Clazz> byGrade = clazzRepository.findByGradeId(gradeId);
                classes = byGrade.stream().filter(c -> schoolId.equals(c.getSchoolId())).collect(java.util.stream.Collectors.toList());
            } else if (gradeId != null) {
                classes = clazzRepository.findByGradeId(gradeId);
            } else if (schoolId != null) {
                classes = clazzRepository.findBySchoolId(schoolId);
            }
            if (classes != null) {
                classIdsFilter = classes.stream().map(com.campus.card.admin.domain.Clazz::getId).collect(java.util.stream.Collectors.toList());
            }
        }
        // 与允许的班级集合做并/交（若请求中无条件则使用允许集合；若都有则取交集）
        java.util.List<Long> effectiveClassIds = null;
        if (allowedClassIds != null && !allowedClassIds.isEmpty()) {
            if (classIdsFilter == null) {
                effectiveClassIds = new java.util.ArrayList<>(allowedClassIds);
            } else {
                java.util.Set<Long> set = new java.util.HashSet<>(classIdsFilter);
                set.retainAll(new java.util.HashSet<>(allowedClassIds));
                effectiveClassIds = new java.util.ArrayList<>(set);
            }
        } else {
            effectiveClassIds = classIdsFilter;
        }
        final java.util.List<Long> classIdsFilterFinal = effectiveClassIds == null ? null : java.util.Collections.unmodifiableList(effectiveClassIds);
        final java.util.Set<Long> allowedStudentIdsFinal = (allowedStudentIds == null || allowedStudentIds.isEmpty()) ? null : allowedStudentIds;

        org.springframework.data.domain.Pageable pr = org.springframework.data.domain.PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1));
        org.springframework.data.jpa.domain.Specification<StudentInfo> spec = (root, query, cb) -> {
            java.util.List<javax.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (classIdsFilterFinal != null) {
                if (classIdsFilterFinal.isEmpty()) {
                    predicates.add(cb.equal(cb.literal(1), 0));
                } else {
                    predicates.add(root.get("classId").in(classIdsFilterFinal));
                }
            }
            if (allowedStudentIdsFinal != null) {
                if (allowedStudentIdsFinal.isEmpty()) {
                    predicates.add(cb.equal(cb.literal(1), 0));
                } else {
                    predicates.add(root.get("id").in(allowedStudentIdsFinal));
                }
            }
            return cb.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
        org.springframework.data.domain.Page<StudentInfo> p = repository.findAll(spec, pr);
        return PageResult.of(p.getTotalElements(), p.getContent());
    }

    public Optional<StudentInfo> findById(Long id) { return repository.findById(id); }

    public Optional<StudentInfo> findByStudentNo(String studentNo) {
        if (studentNo == null || studentNo.trim().isEmpty()) return java.util.Optional.empty();
        return repository.findByStudentNo(studentNo.trim());
    }

    public StudentInfo create(StudentInfo info) {
        if (info.getStatus() == null) info.setStatus(1);
        if (info.getStudentNo() == null) throw new IllegalArgumentException("学号不能为空");
        if (repository.existsByStudentNo(info.getStudentNo())) throw new IllegalStateException("学号已存在");
        return repository.save(info);
    }

    public StudentInfo update(Long id, StudentInfo body) {
        body.setId(id);
        return repository.save(body);
    }

    public void delete(Long id) { repository.deleteById(id); }

    public ImportResult importCsv(InputStream input, Long defaultClassId) {
        ImportResult result = new ImportResult();
        List<String> errors = new ArrayList<>();
        int success = 0;
        int skip = 0;
        int error = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                row++;
                line = line.trim();
                if (line.isEmpty()) continue;
                // Skip header line heuristically
                if (row == 1 && (line.toLowerCase().contains("studentno") || line.contains("学号"))) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 2) {
                    error++;
                    errors.add("第" + row + "行格式错误，至少需要学号,姓名两列");
                    continue;
                }
                String studentNo = parts[0].trim();
                String name = parts[1].trim();
                Long classId = defaultClassId;
                Integer status = 1;
                if (parts.length >= 3) {
                    try {
                        classId = parts[2].trim().isEmpty() ? defaultClassId : Long.parseLong(parts[2].trim());
                    } catch (Exception ex) {
                        // keep default
                    }
                }
                if (parts.length >= 4) {
                    try {
                        status = Integer.parseInt(parts[3].trim());
                    } catch (Exception ex) {
                        status = 1;
                    }
                }
                if (studentNo.isEmpty() || name.isEmpty()) {
                    error++;
                    errors.add("第" + row + "行学号或姓名为空");
                    continue;
                }
                if (repository.existsByStudentNo(studentNo)) {
                    skip++;
                    continue;
                }
                try {
                    StudentInfo info = new StudentInfo();
                    info.setStudentNo(studentNo);
                    info.setName(name);
                    info.setClassId(classId);
                    info.setStatus(status == null ? 1 : status);
                    repository.save(info);
                    success++;
                } catch (Exception ex) {
                    error++;
                    errors.add("第" + row + "行保存失败: " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            errors.add("读取文件失败: " + ex.getMessage());
        }
        result.setSuccessCount(success);
        result.setSkipCount(skip);
        result.setErrorCount(error);
        result.setErrors(errors);
        return result;
    }

    // 启用/禁用/毕业
    public StudentInfo enable(Long id) {
        Optional<StudentInfo> os = repository.findById(id);
        if (!os.isPresent()) throw new IllegalArgumentException("学生不存在");
        StudentInfo s = os.get();
        s.setStatus(1);
        return repository.save(s);
    }

    public StudentInfo disable(Long id) {
        Optional<StudentInfo> os = repository.findById(id);
        if (!os.isPresent()) throw new IllegalArgumentException("学生不存在");
        StudentInfo s = os.get();
        s.setStatus(0);
        return repository.save(s);
    }

    public StudentInfo graduate(Long id) {
        Optional<StudentInfo> os = repository.findById(id);
        if (!os.isPresent()) throw new IllegalArgumentException("学生不存在");
        StudentInfo s = os.get();
        s.setStatus(2); // 2: 毕业
        return repository.save(s);
    }

    // 批量转班
    public TransferResult transfer(List<Long> studentIds, Long targetClassId) {
        if (studentIds == null || studentIds.isEmpty()) {
            throw new IllegalArgumentException("学生ID列表不能为空");
        }
        if (targetClassId == null) {
            throw new IllegalArgumentException("目标班级ID不能为空");
        }
        Optional<Clazz> oc = clazzRepository.findById(targetClassId);
        if (!oc.isPresent()) {
            throw new IllegalArgumentException("目标班级不存在");
        }
        Clazz target = oc.get();
        if (target.getStatus() != null && target.getStatus() == 0) {
            throw new IllegalStateException("目标班级处于禁用状态");
        }
        int success = 0;
        java.util.List<String> errors = new java.util.ArrayList<>();
        for (Long sid : studentIds) {
            try {
                Optional<StudentInfo> os = repository.findById(sid);
                if (!os.isPresent()) {
                    errors.add("学生ID " + sid + " 不存在");
                    continue;
                }
                StudentInfo s = os.get();
                s.setClassId(targetClassId);
                repository.save(s);
                success++;
            } catch (Exception ex) {
                errors.add("学生ID " + sid + " 转班失败: " + ex.getMessage());
            }
        }
        TransferResult result = new TransferResult();
        result.setSuccessCount(success);
        result.setErrorCount(errors.size());
        result.setErrors(errors);
        return result;
    }

    public static class TransferResult {
        private int successCount;
        private int errorCount;
        private java.util.List<String> errors;
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
        public java.util.List<String> getErrors() { return errors; }
        public void setErrors(java.util.List<String> errors) { this.errors = errors; }
    }

    public static class ImportResult {
        private int successCount;
        private int skipCount;
        private int errorCount;
        private java.util.List<String> errors;
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getSkipCount() { return skipCount; }
        public void setSkipCount(int skipCount) { this.skipCount = skipCount; }
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
        public java.util.List<String> getErrors() { return errors; }
        public void setErrors(java.util.List<String> errors) { this.errors = errors; }
    }
}