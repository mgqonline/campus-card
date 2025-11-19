package com.campus.card.admin.repository;

import com.campus.card.admin.domain.FaceInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaceInfoRepository extends JpaRepository<FaceInfo, Long> {
    List<FaceInfo> findByPersonTypeAndPersonId(String personType, String personId);
    List<FaceInfo> findByPersonType(String personType);
    List<FaceInfo> findByPersonId(String personId);
}