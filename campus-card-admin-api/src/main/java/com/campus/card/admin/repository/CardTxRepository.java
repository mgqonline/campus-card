package com.campus.card.admin.repository;

import com.campus.card.admin.domain.CardTx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CardTxRepository extends JpaRepository<CardTx, Long> {
    List<CardTx> findByCardNoOrderByOccurredAtDesc(String cardNo);
    List<CardTx> findByCardNoAndTypeOrderByOccurredAtDesc(String cardNo, String type);
    List<CardTx> findByCardNoAndOccurredAtBetweenOrderByOccurredAtDesc(String cardNo, LocalDateTime start, LocalDateTime end);
    // 新增：按类型与时间范围查询（全局），用于报表统计
    List<CardTx> findByTypeAndOccurredAtBetweenOrderByOccurredAtAsc(String type, LocalDateTime start, LocalDateTime end);
}