package com.campus.card.admin.repository;

import com.campus.card.admin.domain.DataChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataChangeLogRepository extends JpaRepository<DataChangeLog, Long> {
}