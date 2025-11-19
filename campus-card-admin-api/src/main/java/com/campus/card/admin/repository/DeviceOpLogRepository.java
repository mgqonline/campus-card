package com.campus.card.admin.repository;

import com.campus.card.admin.domain.DeviceOpLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceOpLogRepository extends JpaRepository<DeviceOpLog, Long> {
}