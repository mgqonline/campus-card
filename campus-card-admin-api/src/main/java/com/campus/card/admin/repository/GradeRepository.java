package com.campus.card.admin.repository;

import com.campus.card.admin.domain.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findBySchoolId(Long schoolId);
    List<Grade> findByNameContainingIgnoreCase(String name);
    Page<Grade> findBySchoolId(Long schoolId, Pageable pageable);
    Page<Grade> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Grade> findBySchoolIdAndNameContainingIgnoreCase(Long schoolId, String name, Pageable pageable);
    boolean existsBySchoolIdAndNameIgnoreCase(Long schoolId, String name);
    boolean existsBySchoolIdAndYear(Long schoolId, Integer year);
}