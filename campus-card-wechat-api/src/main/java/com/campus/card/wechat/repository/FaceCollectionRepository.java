package com.campus.card.wechat.repository;

import com.campus.card.wechat.model.FaceCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FaceCollectionRepository extends JpaRepository<FaceCollection, Long> {
    
    /**
     * 根据学生ID查找最新的人像采集记录
     */
    Optional<FaceCollection> findTopByChildIdOrderByCreatedTimeDesc(Long childId);
    
    /**
     * 根据学生ID查找所有人像采集记录
     */
    List<FaceCollection> findByChildIdOrderByCreatedTimeDesc(Long childId);
    
    /**
     * 根据状态查找人像采集记录（分页）
     */
    Page<FaceCollection> findByStatusOrderByCreatedTimeDesc(String status, Pageable pageable);
    
    /**
     * 根据学生ID和状态查找记录
     */
    List<FaceCollection> findByChildIdAndStatus(Long childId, String status);
    
    /**
     * 统计各状态的记录数量
     */
    @Query("SELECT f.status, COUNT(f) FROM FaceCollection f GROUP BY f.status")
    List<Object[]> countByStatus();
    
    /**
     * 查找待审核的记录
     */
    @Query("SELECT f FROM FaceCollection f WHERE f.status IN ('PENDING', 'PROCESSING') ORDER BY f.createdTime ASC")
    List<FaceCollection> findPendingRecords();
}