package com.campus.card.admin.service;

import com.campus.card.admin.domain.Teacher;
import com.campus.card.admin.repository.TeacherRepository;
import com.campus.card.common.result.PageResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeacherService {
    private final TeacherRepository repository;

    public TeacherService(TeacherRepository repository) {
        this.repository = repository;
    }

    public PageResult<Teacher> pageList(int page, int size, String name, Long schoolId) {
        List<Teacher> list = repository.findAll();
        
        if (name != null && !name.isEmpty()) {
            list = list.stream().filter(t -> t.getName() != null && t.getName().contains(name)).collect(Collectors.toList());
        }
        if (schoolId != null) {
            list = list.stream().filter(t -> schoolId.equals(t.getSchoolId())).collect(Collectors.toList());
        }
        
        int total = list.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(total, from + size);
        List<Teacher> pageList = from >= total ? java.util.Collections.emptyList() : list.subList(from, to);
        return PageResult.of(total, pageList);
    }

    public Optional<Teacher> findById(Long id) {
        return repository.findById(id);
    }

    public Teacher create(Teacher teacher) {
        if (teacher.getStatus() == null) teacher.setStatus(1);
        if (teacher.getTeacherNo() == null) throw new IllegalArgumentException("教师编号不能为空");
        if (repository.existsByTeacherNo(teacher.getTeacherNo())) {
            throw new IllegalStateException("教师编号已存在");
        }
        teacher.setCreateTime(LocalDateTime.now());
        teacher.setUpdateTime(LocalDateTime.now());
        return repository.save(teacher);
    }

    public Teacher update(Long id, Teacher body) {
        body.setId(id);
        body.setUpdateTime(LocalDateTime.now());
        return repository.save(body);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}