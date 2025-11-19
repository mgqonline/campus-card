package com.campus.card.admin.repository;

import com.campus.card.admin.domain.FaceRecognitionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FaceRecognitionLogRepository extends JpaRepository<FaceRecognitionLog, Long> {
    List<FaceRecognitionLog> findByOccurredAtBetween(LocalDateTime start, LocalDateTime end);
    long countByOccurredAtBetween(LocalDateTime start, LocalDateTime end);
    long countBySuccessIsTrueAndOccurredAtBetween(LocalDateTime start, LocalDateTime end);
}