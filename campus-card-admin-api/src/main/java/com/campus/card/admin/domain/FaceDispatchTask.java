package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "face_dispatch_task")
public class FaceDispatchTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // BATCH, INCREMENTAL, DELETE
    @Column(length = 32, nullable = false)
    private String taskType;

    // 目标设备ID列表（逗号分隔）
    @Column(length = 512)
    private String deviceIds;

    // 过滤条件或说明（JSON或文本）
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    // 统计
    private Integer totalItems = 0;
    private Integer successItems = 0;
    private Integer failedItems = 0;

    // 失败项（逗号分隔 personId 列表）
    @Column(length = 2000)
    private String failedPersonIds;

    // 状态：PENDING, RUNNING, SUCCESS, PARTIAL_FAILED, FAILED
    @Column(length = 32)
    private String status = "PENDING";

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}