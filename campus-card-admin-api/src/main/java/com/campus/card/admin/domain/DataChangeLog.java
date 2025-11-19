package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "data_change_log")
public class DataChangeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Column(length = 64)
    private String entity;

    @Column(length = 64)
    private String entityId;

    @Column(length = 16)
    private String changeType; // INSERT/UPDATE/DELETE

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String beforeJson;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String afterJson;

    @Column(length = 255)
    private String changedFields;

    private Long operatorId;

    @Column(length = 255)
    private String remark;
}