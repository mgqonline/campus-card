package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "face_config")
public class FaceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 识别阈值（0-100），默认75
    @Column(name = "recognition_threshold", nullable = false)
    private Integer recognitionThreshold = 75;

    // 识别模式：ONE_TO_ONE 或 ONE_TO_MANY
    public enum RecognitionMode { ONE_TO_ONE, ONE_TO_MANY }

    @Enumerated(EnumType.STRING)
    @Column(name = "recognition_mode", nullable = false, length = 32)
    private RecognitionMode recognitionMode = RecognitionMode.ONE_TO_ONE;

    // 活体检测开关
    @Column(name = "liveness_enabled", nullable = false)
    private Boolean livenessEnabled = false;

    // 人脸库容量（最大存储条目数）
    @Column(name = "library_capacity", nullable = false)
    private Integer libraryCapacity = 10000;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}