package com.campus.card.admin.repository;

import com.campus.card.admin.domain.SchoolConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolConfigRepository extends JpaRepository<SchoolConfig, Long> {
    List<SchoolConfig> findBySchoolId(Long schoolId);
    Optional<SchoolConfig> findBySchoolIdAndKey(Long schoolId, String key);
}