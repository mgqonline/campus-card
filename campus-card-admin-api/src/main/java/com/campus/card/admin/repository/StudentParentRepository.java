package com.campus.card.admin.repository;

import com.campus.card.admin.domain.StudentParent;
import com.campus.card.admin.domain.StudentParentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentParentRepository extends JpaRepository<StudentParent, StudentParentId> {
    List<StudentParent> findByStudentId(Long studentId);
    List<StudentParent> findByParentId(Long parentId);
    boolean existsByStudentIdAndParentId(Long studentId, Long parentId);
    long deleteByStudentIdAndParentId(Long studentId, Long parentId);
}