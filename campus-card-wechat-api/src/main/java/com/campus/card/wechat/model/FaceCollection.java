package com.campus.card.wechat.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "face_collection")
public class FaceCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "child_id", nullable = false)
    private Long childId;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "collection_type", length = 20)
    private String collectionType; // UPLOAD, CAMERA

    @Column(name = "status", length = 20)
    private String status; // PENDING, PROCESSING, APPROVED, REJECTED

    @Column(name = "quality_score")
    private Double qualityScore; // 0-100 质量评分

    @Column(name = "quality_issues", length = 500)
    private String qualityIssues; // 质量问题描述，JSON格式

    @Column(name = "audit_comment", length = 500)
    private String auditComment; // 审核意见

    @Column(name = "auditor_id")
    private Long auditorId; // 审核员ID

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    // 构造函数
    public FaceCollection() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getCollectionType() { return collectionType; }
    public void setCollectionType(String collectionType) { this.collectionType = collectionType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        this.updatedTime = LocalDateTime.now();
    }

    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }

    public String getQualityIssues() { return qualityIssues; }
    public void setQualityIssues(String qualityIssues) { this.qualityIssues = qualityIssues; }

    public String getAuditComment() { return auditComment; }
    public void setAuditComment(String auditComment) { this.auditComment = auditComment; }

    public Long getAuditorId() { return auditorId; }
    public void setAuditorId(Long auditorId) { this.auditorId = auditorId; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }

    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }
}