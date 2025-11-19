package com.campus.card.wechat.repository;

import com.campus.card.wechat.model.AttendanceAlert;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceAlertRepository extends JpaRepository<AttendanceAlert, Long> {
    List<AttendanceAlert> findByChildIdOrderByTimeDesc(Long childId, Pageable pageable);
}