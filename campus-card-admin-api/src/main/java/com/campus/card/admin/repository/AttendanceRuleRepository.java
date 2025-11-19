package com.campus.card.admin.repository;

import com.campus.card.admin.domain.AttendanceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRuleRepository extends JpaRepository<AttendanceRule, Long> {
}