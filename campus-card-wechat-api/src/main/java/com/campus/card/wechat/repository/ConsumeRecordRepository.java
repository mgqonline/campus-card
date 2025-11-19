package com.campus.card.wechat.repository;

import com.campus.card.wechat.model.ConsumeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ConsumeRecordRepository extends JpaRepository<ConsumeRecord, Long> {
    Page<ConsumeRecord> findByChildIdAndDateBetweenOrderByDateDesc(Long childId, LocalDate start, LocalDate end, Pageable pageable);
    List<ConsumeRecord> findByChildIdAndDateBetween(Long childId, LocalDate start, LocalDate end);
    Page<ConsumeRecord> findByChildIdOrderByDateDesc(Long childId, Pageable pageable);
    List<ConsumeRecord> findByChildId(Long childId);
}