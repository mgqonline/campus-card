package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "device_face", uniqueConstraints = {@UniqueConstraint(columnNames = {"device_id", "person_id"})})
public class DeviceFace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "person_type", length = 32, nullable = false)
    private String personType;

    @Column(name = "person_id", length = 64, nullable = false)
    private String personId;

    // 最近一次下发时间
    @Column
    private LocalDateTime dispatchedAt;

    // 状态 0: 未下发, 1: 已下发
    private Integer status = 1;
}