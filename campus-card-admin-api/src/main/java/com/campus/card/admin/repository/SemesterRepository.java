package com.campus.card.admin.repository;

import com.campus.card.admin.domain.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {
    List<Semester> findBySchoolId(Long schoolId);
    Optional<Semester> findBySchoolIdAndCurrentTrue(Long schoolId);
    List<Semester> findBySchoolIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(Long schoolId, LocalDate start, LocalDate end);
    boolean existsBySchoolIdAndCode(Long schoolId, String code);
}