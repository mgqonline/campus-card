package com.campus.card.admin.repository;

import com.campus.card.admin.domain.StudentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<StudentInfo, Long>, JpaSpecificationExecutor<StudentInfo> {
    List<StudentInfo> findByNameContainingIgnoreCase(String name);
    List<StudentInfo> findByClassId(Long classId);
    boolean existsByStudentNo(String studentNo);
    Optional<StudentInfo> findByStudentNo(String studentNo);
}