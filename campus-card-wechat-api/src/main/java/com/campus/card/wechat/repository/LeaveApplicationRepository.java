package com.campus.card.wechat.repository;

import com.campus.card.wechat.model.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {
    List<LeaveApplication> findByChildIdOrderByApplyTimeDesc(Long childId);
    List<LeaveApplication> findByClassIdAndStatusOrderByApplyTimeDesc(Long classId, String status);
    List<LeaveApplication> findByClassIdOrderByApplyTimeDesc(Long classId);
}