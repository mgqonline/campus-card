package com.campus.card.admin.repository;

import com.campus.card.admin.domain.OpLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpLogRepository extends JpaRepository<OpLog, Long> {
}