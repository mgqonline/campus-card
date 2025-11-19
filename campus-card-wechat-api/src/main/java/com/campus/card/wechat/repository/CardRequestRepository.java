package com.campus.card.wechat.repository;

import com.campus.card.wechat.model.CardRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRequestRepository extends JpaRepository<CardRequest, Long> {
    Page<CardRequest> findByChildIdOrderByCreatedAtDesc(Long childId, Pageable pageable);
    List<CardRequest> findTop10ByChildIdOrderByCreatedAtDesc(Long childId);
}