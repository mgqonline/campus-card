package com.campus.card.admin.repository;

import com.campus.card.admin.domain.ClassSubjectTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassSubjectTeacherRepository extends JpaRepository<ClassSubjectTeacher, Long> {
    List<ClassSubjectTeacher> findByClassId(Long classId);
    Optional<ClassSubjectTeacher> findByClassIdAndSubjectId(Long classId, Long subjectId);
    boolean existsByClassIdAndSubjectId(Long classId, Long subjectId);
    // 新增：根据教师ID查询其任教班级-学科关联
    List<ClassSubjectTeacher> findByTeacherId(Long teacherId);
}