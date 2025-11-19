package com.campus.card.admin.repository;

import com.campus.card.admin.domain.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findBySchoolId(Long schoolId);
    List<Subject> findByNameContainingIgnoreCase(String name);
    boolean existsBySchoolIdAndNameIgnoreCase(Long schoolId, String name);
}