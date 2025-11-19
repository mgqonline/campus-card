package com.campus.card.admin.repository;

import com.campus.card.admin.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByCode(String code);
    boolean existsByCode(String code);
}