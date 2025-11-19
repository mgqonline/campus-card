package com.campus.card.admin.service;

import com.campus.card.admin.domain.Department;
import com.campus.card.admin.repository.DepartmentRepository;
import com.campus.card.common.result.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DepartmentService {
    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    public PageResult<Department> pageList(int page, int size, String name, Integer status) {
        PageRequest pr = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1));
        Page<Department> pg;
        boolean hasName = name != null && !name.trim().isEmpty();
        boolean hasStatus = status != null;
        if (hasName && hasStatus) {
            pg = repository.findByNameContainingIgnoreCaseAndStatus(name.trim(), status, pr);
        } else if (hasName) {
            pg = repository.findByNameContainingIgnoreCase(name.trim(), pr);
        } else if (hasStatus) {
            pg = repository.findByStatus(status, pr);
        } else {
            pg = repository.findAll(pr);
        }
        return PageResult.of((int) pg.getTotalElements(), pg.getContent());
    }

    public Optional<Department> findById(Long id) {
        return repository.findById(id);
    }

    public Department create(Department d) {
        if (d.getName() == null || d.getName().trim().isEmpty()) throw new IllegalArgumentException("部门名称不能为空");
        String name = d.getName().trim();
        // 简化：名称全局唯一（如需按学校唯一，可改为 existsBySchoolIdAndNameIgnoreCase）
        if (repository.existsByNameIgnoreCase(name)) throw new IllegalStateException("部门名称已存在");
        if (d.getStatus() == null) d.setStatus(1);
        d.setName(name);
        d.setCreateTime(LocalDateTime.now());
        d.setUpdateTime(LocalDateTime.now());
        return repository.save(d);
    }

    public Department update(Long id, Department body) {
        Department origin = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("部门不存在"));
        if (body.getName() != null) {
            String newName = body.getName().trim();
            if (!newName.equalsIgnoreCase(origin.getName()) && repository.existsByNameIgnoreCase(newName)) {
                throw new IllegalStateException("部门名称已存在");
            }
            origin.setName(newName);
        }
        if (body.getStatus() != null) origin.setStatus(body.getStatus());
        // 如果前端后续支持 schoolId 变更，也可在此校验
        if (body.getSchoolId() != null) origin.setSchoolId(body.getSchoolId());
        origin.setUpdateTime(LocalDateTime.now());
        return repository.save(origin);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Department enable(Long id) {
        Department d = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("部门不存在"));
        d.setStatus(1);
        d.setUpdateTime(LocalDateTime.now());
        return repository.save(d);
    }

    public Department disable(Long id) {
        Department d = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("部门不存在"));
        d.setStatus(0);
        d.setUpdateTime(LocalDateTime.now());
        return repository.save(d);
    }
}