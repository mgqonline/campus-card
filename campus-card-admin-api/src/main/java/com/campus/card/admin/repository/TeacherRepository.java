package com.campus.card.admin.repository;

import com.campus.card.admin.domain.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    List<Teacher> findBySchoolId(Long schoolId);
    List<Teacher> findByNameContainingIgnoreCase(String name);
    Optional<Teacher> findByTeacherNo(String teacherNo);
    boolean existsByTeacherNo(String teacherNo);
}