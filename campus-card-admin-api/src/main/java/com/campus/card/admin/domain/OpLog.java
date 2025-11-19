package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "op_log")
public class OpLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    private Long userId;
    @Column(length = 32)
    private String subjectType; // USER/ROLE

    @Column(length = 16)
    private String method;

    @Column(length = 255)
    private String uri;

    @Column(length = 64)
    private String action;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String params;

    @Column(length = 64)
    private String clientIp;

    private Integer resultCode;

    private Integer durationMs;
}