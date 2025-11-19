package com.campus.card.admin.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SyncTask {
    private String id;                 // 任务ID
    private String type;               // 任务类型：PERSONS/FACE_PHOTOS/...
    private String status;             // 状态：PENDING/RUNNING/SUCCESS/FAILED
    private String scope;              // 下发范围：如设备ID/学校/部门等
    private String payloadSummary;     // 载荷概要（数量、目标等简述）
    private Integer progress;          // 进度百分比 0-100
    private String message;            // 状态说明/错误信息
    private LocalDateTime createdAt;   // 创建时间
    private LocalDateTime updatedAt;   // 更新时间
}