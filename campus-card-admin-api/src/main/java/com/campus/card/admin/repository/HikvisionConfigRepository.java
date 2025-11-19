package com.campus.card.admin.repository;

import com.campus.card.admin.domain.HikvisionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HikvisionConfigRepository extends JpaRepository<HikvisionConfig, Long> {
    
    /**
     * 根据学校ID查找配置
     */
    Optional<HikvisionConfig> findBySchoolId(Long schoolId);
    
    /**
     * 检查学校是否已有配置
     */
    boolean existsBySchoolId(Long schoolId);
    
    /**
     * 根据状态查找配置
     */
    java.util.List<HikvisionConfig> findByStatus(Integer status);
}