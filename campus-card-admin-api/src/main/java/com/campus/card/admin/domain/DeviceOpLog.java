package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "device_op_log")
public class DeviceOpLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    private Long deviceId;

    @Column(length = 64)
    private String deviceCode;

    @Column(length = 64)
    private String action;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String params;

    @Column(length = 128)
    private String result;

    private Long operatorId;
}