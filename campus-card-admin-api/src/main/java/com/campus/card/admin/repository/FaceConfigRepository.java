package com.campus.card.admin.repository;

import com.campus.card.admin.domain.FaceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FaceConfigRepository extends JpaRepository<FaceConfig, Long> {
    Optional<FaceConfig> findTopByOrderByIdAsc();
}