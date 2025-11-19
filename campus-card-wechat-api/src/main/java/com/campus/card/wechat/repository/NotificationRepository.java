package com.campus.card.wechat.repository;

import com.campus.card.wechat.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByChildIdOrderByCreatedAtDesc(Long childId, Pageable pageable);
    Page<Notification> findByChildIdAndCategoryOrderByCreatedAtDesc(Long childId, String category, Pageable pageable);
    Page<Notification> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);

    long countByChildIdAndReadFlagFalse(Long childId);
    long countByChildIdAndReadFlagFalseAndCategory(Long childId, String category);

    List<Notification> findTop5ByChildIdAndReadFlagFalseOrderByCreatedAtDesc(Long childId);

    Page<Notification> findByChildIdAndReadFlagFalseOrderByCreatedAtDesc(Long childId, Pageable pageable);
    Page<Notification> findByChildIdAndReadFlagFalseAndCategoryOrderByCreatedAtDesc(Long childId, String category, Pageable pageable);
}