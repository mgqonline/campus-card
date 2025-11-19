package com.campus.card.admin.service;

import com.campus.card.admin.domain.Grade;
import com.campus.card.admin.repository.GradeRepository;
import com.campus.card.admin.repository.ClazzRepository;
import com.campus.card.admin.repository.SchoolRepository;
import com.campus.card.common.result.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class GradeService {
    private final GradeRepository repository;
    private final SchoolRepository schoolRepository;
    private final ClazzRepository clazzRepository;

    public GradeService(GradeRepository repository, SchoolRepository schoolRepository, ClazzRepository clazzRepository) {
        this.repository = repository;
        this.schoolRepository = schoolRepository;
        this.clazzRepository = clazzRepository;
    }

    public PageResult<Grade> pageList(int page, int size, String name, Long schoolId) {
        PageRequest pr = PageRequest.of(Math.max(page - 1, 0), size);
        Page<Grade> pg;
        if (schoolId != null && name != null && !name.isEmpty()) {
            pg = repository.findBySchoolIdAndNameContainingIgnoreCase(schoolId, name, pr);
        } else if (schoolId != null) {
            pg = repository.findBySchoolId(schoolId, pr);
        } else if (name != null && !name.isEmpty()) {
            pg = repository.findByNameContainingIgnoreCase(name, pr);
        } else {
            pg = repository.findAll(pr);
        }
        return PageResult.of((int) pg.getTotalElements(), pg.getContent());
    }

    public Optional<Grade> findById(Long id) {
        return repository.findById(id);
    }

    public Grade create(Grade grade) {
        if (grade.getSchoolId() == null) throw new IllegalArgumentException("schoolId不能为空");
        if (!schoolRepository.existsById(grade.getSchoolId())) throw new IllegalArgumentException("学校不存在");
        if (grade.getName() == null || grade.getName().trim().isEmpty()) throw new IllegalArgumentException("年级名称不能为空");
        String name = grade.getName().trim();
        if (repository.existsBySchoolIdAndNameIgnoreCase(grade.getSchoolId(), name)) throw new IllegalStateException("同一学校下年级名称已存在");
        if (grade.getYear() != null && repository.existsBySchoolIdAndYear(grade.getSchoolId(), grade.getYear())) throw new IllegalStateException("同一学校下该入学年份已存在");
        if (grade.getStatus() == null) grade.setStatus(1);
        grade.setName(name);
        grade.setCreateTime(LocalDateTime.now());
        grade.setUpdateTime(LocalDateTime.now());
        return repository.save(grade);
    }

    public Grade update(Long id, Grade body) {
        Grade origin = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("年级不存在"));
        // 不允许在存在班级时变更学校归属
        if (body.getSchoolId() != null && !body.getSchoolId().equals(origin.getSchoolId())) {
            if (clazzRepository.existsByGradeId(id)) throw new IllegalStateException("该年级下存在班级，禁止变更学校");
            if (!schoolRepository.existsById(body.getSchoolId())) throw new IllegalArgumentException("目标学校不存在");
            origin.setSchoolId(body.getSchoolId());
        }
        if (body.getName() != null) {
            String newName = body.getName().trim();
            if (!newName.equalsIgnoreCase(origin.getName()) && repository.existsBySchoolIdAndNameIgnoreCase(origin.getSchoolId(), newName)) {
                throw new IllegalStateException("同一学校下年级名称已存在");
            }
            origin.setName(newName);
        }
        if (body.getYear() != null) {
            Integer newYear = body.getYear();
            if (!newYear.equals(origin.getYear()) && repository.existsBySchoolIdAndYear(origin.getSchoolId(), newYear)) {
                throw new IllegalStateException("同一学校下该入学年份已存在");
            }
            origin.setYear(newYear);
        }
        if (body.getStatus() != null) origin.setStatus(body.getStatus());
        origin.setUpdateTime(LocalDateTime.now());
        return repository.save(origin);
    }

    public void delete(Long id) {
        if (clazzRepository.existsByGradeId(id)) throw new IllegalStateException("该年级下存在班级，无法删除");
        repository.deleteById(id);
    }

    public Grade enable(Long id) {
        Grade g = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("年级不存在"));
        g.setStatus(1);
        g.setUpdateTime(LocalDateTime.now());
        return repository.save(g);
    }

    public Grade disable(Long id) {
        Grade g = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("年级不存在"));
        g.setStatus(0);
        g.setUpdateTime(LocalDateTime.now());
        // 级联禁用其下班级
        // 出于简洁和安全考虑，这里只做标记，不删除
        java.util.List<com.campus.card.admin.domain.Clazz> classes = clazzRepository.findByGradeId(id);
        for (com.campus.card.admin.domain.Clazz c : classes) {
            if (c.getStatus() != null && c.getStatus() == 1) {
                c.setStatus(0);
                c.setUpdateTime(LocalDateTime.now());
                clazzRepository.save(c);
            }
        }
        return repository.save(g);
    }
}