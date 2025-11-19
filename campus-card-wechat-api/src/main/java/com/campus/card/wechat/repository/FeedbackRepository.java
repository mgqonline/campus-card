package com.campus.card.wechat.repository;

import com.campus.card.wechat.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findTop20ByChildIdOrderByCreatedAtDesc(Long childId);
}