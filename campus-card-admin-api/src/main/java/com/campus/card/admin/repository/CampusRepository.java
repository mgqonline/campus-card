package com.campus.card.admin.repository;

import com.campus.card.admin.domain.Campus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {
    List<Campus> findBySchoolId(Long schoolId);
    boolean existsByCode(String code);
    // 新增：分页与过滤
    Page<Campus> findBySchoolId(Long schoolId, Pageable pageable);
    Page<Campus> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Campus> findBySchoolIdAndNameContainingIgnoreCase(Long schoolId, String name, Pageable pageable);
    // 新增：唯一性校验（按学校）
    boolean existsBySchoolIdAndNameIgnoreCase(Long schoolId, String name);
    boolean existsBySchoolIdAndCode(Long schoolId, String code);
}