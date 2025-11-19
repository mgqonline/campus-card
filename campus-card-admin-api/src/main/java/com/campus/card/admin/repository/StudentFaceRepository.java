package com.campus.card.admin.repository;

import com.campus.card.admin.domain.StudentFace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentFaceRepository extends JpaRepository<StudentFace, Long> {
    Optional<StudentFace> findByStudentId(Long studentId);
}