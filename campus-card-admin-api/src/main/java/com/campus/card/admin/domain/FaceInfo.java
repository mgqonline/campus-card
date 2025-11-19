package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "face_info")
public class FaceInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联人员类型：STUDENT/TEACHER/STAFF/VISITOR
    @Column(nullable = false, length = 32)
    private String personType;

    // 关联人员ID：学号/工号/职工号/访客标识
    @Column(nullable = false, length = 64)
    private String personId;

    // 人脸照片（Base64 数据）
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String photoBase64;

    // 可选的人脸特征（预留字符串，后续可对接向量库）
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String features;

    // 质量分（0-100），基于基本校验的简易评估
    @Column
    private Integer qualityScore;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}