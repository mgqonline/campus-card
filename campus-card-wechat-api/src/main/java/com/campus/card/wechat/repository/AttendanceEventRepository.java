package com.campus.card.wechat.repository;

import com.campus.card.wechat.model.AttendanceEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {
    Page<AttendanceEvent> findByChildIdOrderByTimeDesc(Long childId, Pageable pageable);
    Page<AttendanceEvent> findByChildIdAndTimeBetweenOrderByTimeDesc(Long childId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("select e from AttendanceEvent e where e.childId = ?1 order by e.time desc")
    List<AttendanceEvent> findTopByChildIdOrderByTimeDesc(Long childId, Pageable pageable);
}