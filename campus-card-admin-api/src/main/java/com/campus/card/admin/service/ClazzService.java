package com.campus.card.admin.service;

import com.campus.card.admin.domain.Clazz;
import com.campus.card.admin.domain.StudentInfo;
import com.campus.card.admin.domain.Teacher;
import com.campus.card.admin.repository.ClazzRepository;
import com.campus.card.admin.repository.StudentRepository;
import com.campus.card.admin.repository.TeacherRepository;
import com.campus.card.common.result.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClazzService {
    private final ClazzRepository repository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public ClazzService(ClazzRepository repository, StudentRepository studentRepository, TeacherRepository teacherRepository) {
        this.repository = repository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    public PageResult<Clazz> pageList(int page, int size, String name, Long gradeId, Long schoolId) {
        org.springframework.data.domain.Pageable pr = org.springframework.data.domain.PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1));
        org.springframework.data.jpa.domain.Specification<Clazz> spec = (root, query, cb) -> {
            java.util.List<javax.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (gradeId != null) {
                predicates.add(cb.equal(root.get("gradeId"), gradeId));
            }
            if (schoolId != null) {
                predicates.add(cb.equal(root.get("schoolId"), schoolId));
            }
            return cb.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
        org.springframework.data.domain.Page<Clazz> p = repository.findAll(spec, pr);
        List<Clazz> content = p.getContent();
        // 补充班主任姓名，供前端展示
        for (Clazz c : content) {
            Long tid = c.getHeadTeacherId();
            if (tid != null) {
                Optional<Teacher> ot = teacherRepository.findById(tid);
                c.setHeadTeacherName(ot.map(Teacher::getName).orElse(null));
            } else {
                c.setHeadTeacherName(null);
            }
        }
        return PageResult.of(p.getTotalElements(), content);
    }

    public Optional<Clazz> findById(Long id) { return repository.findById(id); }

    public Clazz create(Clazz body) {
        if (body.getStatus() == null) body.setStatus(1);
        body.setCreateTime(LocalDateTime.now());
        body.setUpdateTime(LocalDateTime.now());
        return repository.save(body);
    }

    public Clazz update(Long id, Clazz body) {
        body.setId(id);
        body.setUpdateTime(LocalDateTime.now());
        return repository.save(body);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Clazz enable(Long id) {
        Optional<Clazz> oc = repository.findById(id);
        if (!oc.isPresent()) throw new IllegalArgumentException("班级不存在");
        Clazz c = oc.get();
        c.setStatus(1);
        c.setUpdateTime(LocalDateTime.now());
        return repository.save(c);
    }

    public Clazz disable(Long id) {
        Optional<Clazz> oc = repository.findById(id);
        if (!oc.isPresent()) throw new IllegalArgumentException("班级不存在");
        Clazz c = oc.get();
        c.setStatus(0);
        c.setUpdateTime(LocalDateTime.now());
        return repository.save(c);
    }

    public Clazz assignHeadTeacher(Long classId, Long teacherId) {
        Optional<Clazz> oc = repository.findById(classId);
        if (!oc.isPresent()) throw new IllegalArgumentException("班级不存在");
        
        Clazz clazz = oc.get();
        clazz.setHeadTeacherId(teacherId);
        clazz.setUpdateTime(LocalDateTime.now());
        return repository.save(clazz);
    }

    public Clazz removeHeadTeacher(Long classId) {
        Optional<Clazz> oc = repository.findById(classId);
        if (!oc.isPresent()) throw new IllegalArgumentException("班级不存在");
        
        Clazz clazz = oc.get();
        clazz.setHeadTeacherId(null);
        clazz.setUpdateTime(LocalDateTime.now());
        return repository.save(clazz);
    }

    // 新增：统计班级学生人数
    public int countStudents(Long classId) {
        List<StudentInfo> list = studentRepository.findByClassId(classId);
        return list == null ? 0 : list.size();
    }

    // 新增：调整班级所属年级
    @Transactional
    public Clazz changeGrade(Long classId, Long targetGradeId) {
        Optional<Clazz> oc = repository.findById(classId);
        if (!oc.isPresent()) throw new IllegalArgumentException("班级不存在");
        Clazz clazz = oc.get();
        clazz.setGradeId(targetGradeId);
        clazz.setUpdateTime(LocalDateTime.now());
        return repository.save(clazz);
    }

    // 新增：班级合并（将源班学生转入目标班），可选择归档源班
    @Transactional
    public int mergeClasses(List<Long> sourceClassIds, Long targetClassId, boolean archiveSources) {
        if (sourceClassIds == null || sourceClassIds.isEmpty()) return 0;
        int moved = 0;
        for (Long sid : sourceClassIds) {
            if (sid == null || sid.equals(targetClassId)) continue;
            List<StudentInfo> list = studentRepository.findByClassId(sid);
            if (list != null) {
                for (StudentInfo s : list) {
                    s.setClassId(targetClassId);
                    studentRepository.save(s);
                    moved++;
                }
            }
            if (archiveSources) {
                Optional<Clazz> oc = repository.findById(sid);
                if (oc.isPresent()) {
                    Clazz c = oc.get();
                    c.setStatus(0);
                    c.setUpdateTime(LocalDateTime.now());
                    repository.save(c);
                }
            }
        }
        return moved;
    }

    // 新增：班级拆分（从源班选定学生转入新建班级，可指定目标年级与班主任）
    @Transactional
    public SplitResult splitClass(Long sourceClassId, String newClassName, Long targetGradeId, Long headTeacherId, List<Long> studentIds) {
        if (sourceClassId == null) throw new IllegalArgumentException("源班级ID不能为空");
        if (newClassName == null || newClassName.trim().isEmpty()) throw new IllegalArgumentException("新班级名称不能为空");
        if (studentIds == null || studentIds.isEmpty()) throw new IllegalArgumentException("需要选择至少一名学生");

        Clazz source = repository.findById(sourceClassId).orElseThrow(() -> new IllegalArgumentException("源班级不存在"));

        Clazz newClass = new Clazz();
        newClass.setName(newClassName.trim());
        newClass.setSchoolId(source.getSchoolId());
        newClass.setGradeId(targetGradeId != null ? targetGradeId : source.getGradeId());
        newClass.setHeadTeacherId(headTeacherId);
        newClass.setStatus(1);
        newClass.setCreateTime(LocalDateTime.now());
        newClass.setUpdateTime(LocalDateTime.now());
        newClass = repository.save(newClass);

        int moved = 0;
        List<StudentInfo> toMove = studentRepository.findAllById(studentIds);
        for (StudentInfo s : toMove) {
            // 仅移动当前源班的学生，避免误操作
            if (sourceClassId.equals(s.getClassId())) {
                s.setClassId(newClass.getId());
                studentRepository.save(s);
                moved++;
            }
        }
        SplitResult result = new SplitResult();
        result.setNewClass(newClass);
        result.setMovedCount(moved);
        return result;
    }

    public static class SplitResult {
        private Clazz newClass;
        private int movedCount;
        public Clazz getNewClass() { return newClass; }
        public void setNewClass(Clazz newClass) { this.newClass = newClass; }
        public int getMovedCount() { return movedCount; }
        public void setMovedCount(int movedCount) { this.movedCount = movedCount; }
    }
}