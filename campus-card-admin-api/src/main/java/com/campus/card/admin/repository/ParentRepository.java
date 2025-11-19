package com.campus.card.admin.repository;

import com.campus.card.admin.domain.ParentInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParentRepository extends JpaRepository<ParentInfo, Long> {
  // 按姓名模糊查询
  Page<ParentInfo> findByNameContainingIgnoreCase(String name, Pageable pageable);
}