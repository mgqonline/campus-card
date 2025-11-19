package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "device")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 64, unique = true)
    private String code; // 设备唯一编码

    @Column(length = 128)
    private String location;

    /** 0: 离线, 1: 在线 */
    private Integer status = 1;

    @Column(length = 32)
    private String vendor; // 厂商，如 hikvision

    @Column(length = 64)
    private String ip;

    private Integer port;

    @Column(length = 64)
    private String username;

    @Column(length = 128)
    private String password;

    @Column(name = "group_type", length = 32)
    private String groupType; // TEACHER/STUDENT/ACCESS

    @Lob
    @Column(name = "param_json", columnDefinition = "LONGTEXT")
    private String paramJson; // 设备参数配置（JSON）

    @Column(name = "last_heartbeat_at")
    private LocalDateTime lastHeartbeatAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}