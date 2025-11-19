package com.campus.card.admin.service;

import com.campus.card.admin.domain.Subject;
import com.campus.card.admin.repository.SubjectRepository;
import com.campus.card.common.result.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SubjectService {
    private final SubjectRepository repository;

    public SubjectService(SubjectRepository repository) {
        this.repository = repository;
    }

    public PageResult<Subject> pageList(int page, int size, String name, Long schoolId) {
        PageRequest pr = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1));
        Page<Subject> pg;
        if (schoolId != null && name != null && !name.isEmpty()) {
            pg = repository.findAll(pr).map(s -> s); // fallback simple, then filter
            java.util.List<Subject> filtered = pg.getContent();
            filtered = filtered.stream().filter(s -> schoolId.equals(s.getSchoolId()) && s.getName() != null && s.getName().toLowerCase().contains(name.toLowerCase())).collect(java.util.stream.Collectors.toList());
            return PageResult.of(filtered.size(), filtered);
        } else if (schoolId != null) {
            java.util.List<Subject> list = repository.findBySchoolId(schoolId);
            return PageResult.of(list.size(), list);
        } else if (name != null && !name.isEmpty()) {
            java.util.List<Subject> list = repository.findByNameContainingIgnoreCase(name);
            return PageResult.of(list.size(), list);
        } else {
            pg = repository.findAll(pr);
            return PageResult.of((int) pg.getTotalElements(), pg.getContent());
        }
    }

    public Optional<Subject> findById(Long id) { return repository.findById(id); }

    public Subject create(Subject subject) {
        if (subject.getSchoolId() == null) throw new IllegalArgumentException("schoolId不能为空");
        if (subject.getName() == null || subject.getName().trim().isEmpty()) throw new IllegalArgumentException("学科名称不能为空");
        String name = subject.getName().trim();
        if (repository.existsBySchoolIdAndNameIgnoreCase(subject.getSchoolId(), name)) throw new IllegalStateException("同一学校下学科名称已存在");
        if (subject.getStatus() == null) subject.setStatus(1);
        subject.setName(name);
        subject.setCreateTime(LocalDateTime.now());
        subject.setUpdateTime(LocalDateTime.now());
        return repository.save(subject);
    }

    public Subject update(Long id, Subject body) {
        Subject origin = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("学科不存在"));
        if (body.getSchoolId() != null) origin.setSchoolId(body.getSchoolId());
        if (body.getName() != null) {
            String newName = body.getName().trim();
            if (!newName.equalsIgnoreCase(origin.getName()) && repository.existsBySchoolIdAndNameIgnoreCase(origin.getSchoolId(), newName)) {
                throw new IllegalStateException("同一学校下学科名称已存在");
            }
            origin.setName(newName);
        }
        if (body.getStatus() != null) origin.setStatus(body.getStatus());
        origin.setUpdateTime(LocalDateTime.now());
        return repository.save(origin);
    }

    public void delete(Long id) { repository.deleteById(id); }
}