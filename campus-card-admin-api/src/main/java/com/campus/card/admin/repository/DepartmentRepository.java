package com.campus.card.admin.repository;

import com.campus.card.admin.domain.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findBySchoolId(Long schoolId);
    List<Department> findByNameContainingIgnoreCase(String name);
    Page<Department> findBySchoolId(Long schoolId, Pageable pageable);
    Page<Department> findByStatus(Integer status, Pageable pageable);
    Page<Department> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Department> findBySchoolIdAndNameContainingIgnoreCase(Long schoolId, String name, Pageable pageable);
    Page<Department> findByNameContainingIgnoreCaseAndStatus(String name, Integer status, Pageable pageable);
    boolean existsByNameIgnoreCase(String name);
    boolean existsBySchoolIdAndNameIgnoreCase(Long schoolId, String name);
}