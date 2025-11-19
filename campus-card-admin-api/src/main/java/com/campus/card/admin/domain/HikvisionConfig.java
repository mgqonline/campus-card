package com.campus.card.admin.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "hikvision_config", uniqueConstraints = {@UniqueConstraint(columnNames = {"school_id"})})
public class HikvisionConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId; // 学校ID，按学校配置

    @Column(name = "school_name", length = 128)
    private String schoolName; // 学校名称

    // 海康设备IP配置
    @Column(name = "device_ip", length = 64)
    private String deviceIp;

    @Column(name = "device_port")
    private Integer devicePort = 8000; // 默认8000端口

    // SDK对接配置
    @Column(name = "sdk_version", length = 32)
    private String sdkVersion = "6.1.9.47"; // SDK版本

    @Column(name = "sdk_timeout")
    private Integer sdkTimeout = 5000; // SDK超时时间(ms)

    @Column(name = "max_connections")
    private Integer maxConnections = 10; // 最大连接数

    // 设备编号配置
    @Column(name = "device_code_prefix", length = 16)
    private String deviceCodePrefix = "HK"; // 设备编号前缀

    @Column(name = "device_code_length")
    private Integer deviceCodeLength = 8; // 设备编号长度

    // 通讯协议配置
    @Column(name = "protocol_type", length = 16)
    private String protocolType = "TCP"; // 协议类型: TCP/UDP

    @Column(name = "encoding", length = 16)
    private String encoding = "UTF-8"; // 编码格式

    @Column(name = "data_format", length = 16)
    private String dataFormat = "JSON"; // 数据格式: JSON/XML

    // 认证配置
    @Column(name = "username", length = 64)
    private String username = "admin"; // 默认用户名

    @Column(name = "password", length = 128)
    private String password; // 密码

    @Column(name = "auth_mode", length = 16)
    private String authMode = "DIGEST"; // 认证模式: BASIC/DIGEST

    // 心跳检测配置
    @Column(name = "heartbeat_enabled")
    private Boolean heartbeatEnabled = true; // 是否启用心跳检测

    @Column(name = "heartbeat_interval")
    private Integer heartbeatInterval = 30; // 心跳间隔(秒)

    @Column(name = "heartbeat_timeout")
    private Integer heartbeatTimeout = 10; // 心跳超时(秒)

    @Column(name = "max_retry_count")
    private Integer maxRetryCount = 3; // 最大重试次数

    // 功能开关
    @Column(name = "face_recognition_enabled")
    private Boolean faceRecognitionEnabled = true; // 人脸识别功能

    @Column(name = "card_recognition_enabled")
    private Boolean cardRecognitionEnabled = true; // 刷卡功能

    @Column(name = "temperature_detection_enabled")
    private Boolean temperatureDetectionEnabled = false; // 体温检测

    @Column(name = "mask_detection_enabled")
    private Boolean maskDetectionEnabled = false; // 口罩检测

    // 数据同步配置
    @Column(name = "sync_enabled")
    private Boolean syncEnabled = true; // 是否启用数据同步

    @Column(name = "sync_interval")
    private Integer syncInterval = 300; // 同步间隔(秒)

    @Column(name = "batch_size")
    private Integer batchSize = 100; // 批量处理大小

    // 状态和时间
    @Column(name = "status")
    private Integer status = 1; // 0: 禁用, 1: 启用

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_test_at")
    private LocalDateTime lastTestAt; // 最后测试连接时间

    @Column(name = "test_result", length = 512)
    private String testResult; // 测试结果
}