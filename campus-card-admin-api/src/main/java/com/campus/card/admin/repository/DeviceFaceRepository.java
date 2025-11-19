package com.campus.card.admin.repository;

import com.campus.card.admin.domain.DeviceFace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceFaceRepository extends JpaRepository<DeviceFace, Long> {
    List<DeviceFace> findByDeviceId(Long deviceId);
    Optional<DeviceFace> findByDeviceIdAndPersonId(Long deviceId, String personId);
    int deleteByDeviceIdAndPersonId(Long deviceId, String personId);
}