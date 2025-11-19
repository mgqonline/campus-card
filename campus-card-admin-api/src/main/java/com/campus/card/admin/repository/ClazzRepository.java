package com.campus.card.admin.repository;

import com.campus.card.admin.domain.Clazz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClazzRepository extends JpaRepository<Clazz, Long>, JpaSpecificationExecutor<Clazz> {
    List<Clazz> findBySchoolId(Long schoolId);
    List<Clazz> findByGradeId(Long gradeId);
    List<Clazz> findByNameContainingIgnoreCase(String name);
    boolean existsByGradeId(Long gradeId);
    long countByGradeId(Long gradeId);
    // 新增：按班主任ID查询班级列表
    List<Clazz> findByHeadTeacherId(Long headTeacherId);
}