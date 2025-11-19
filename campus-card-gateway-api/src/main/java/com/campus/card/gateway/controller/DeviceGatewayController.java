package com.campus.card.gateway.controller;

import com.campus.card.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "设备对接", description = "设备注册、状态、数据上传、参数配置与命令下发")
@RestController
@RequestMapping("/api/v1/gw/devices")
public class DeviceGatewayController {

    @Operation(summary = "设备注册接口")
    @PostMapping("/register")
    public Result<DeviceRegisterResult> register(@RequestBody DeviceRegisterRequest body) {
        DeviceRegisterResult r = new DeviceRegisterResult();
        r.setDeviceId(body.getDeviceId());
        r.setRegistered(true);
        r.setDeviceKey(UUID.randomUUID().toString());
        r.setRegisteredAt(LocalDateTime.now());
        r.setMessage("REGISTERED");
        return Result.ok(r);
    }

    @Operation(summary = "设备状态接口")
    @GetMapping("/{deviceId}/status")
    public Result<DeviceStatus> status(@PathVariable String deviceId) {
        DeviceStatus s = new DeviceStatus();
        s.setDeviceId(deviceId);
        s.setOnline(true);
        s.setLastHeartbeat(LocalDateTime.now().minusSeconds(30));
        s.setFirmwareVersion("v1.2.3");
        s.setBattery(88);
        s.setNetwork("WIFI");
        return Result.ok(s);
    }

    @Operation(summary = "设备数据上传接口")
    @PostMapping("/{deviceId}/data")
    public Result<DataUploadResult> uploadData(@PathVariable String deviceId, @RequestBody DataUploadRequest body) {
        DataUploadResult r = new DataUploadResult();
        r.setDeviceId(deviceId);
        r.setType(body.getType());
        r.setAccepted(true);
        r.setAckId("ack-" + UUID.randomUUID());
        r.setReceivedAt(LocalDateTime.now());
        return Result.ok(r);
    }

    @Operation(summary = "设备参数配置接口")
    @PostMapping("/{deviceId}/config")
    public Result<ConfigApplyResult> applyConfig(@PathVariable String deviceId, @RequestBody DeviceConfigRequest body) {
        ConfigApplyResult r = new ConfigApplyResult();
        r.setDeviceId(deviceId);
        r.setApplied(true);
        r.setMessage("APPLIED");
        r.setAppliedAt(LocalDateTime.now());
        r.setAppliedItems(body.getParams());
        return Result.ok(r);
    }

    @Operation(summary = "设备命令下发接口")
    @PostMapping("/{deviceId}/commands")
    public Result<CommandIssueResult> issueCommand(@PathVariable String deviceId, @RequestBody CommandIssueRequest body) {
        CommandIssueResult r = new CommandIssueResult();
        r.setDeviceId(deviceId);
        r.setCommandId("cmd-" + UUID.randomUUID());
        r.setStatus("QUEUED");
        r.setCommandType(body.getCommandType());
        r.setIssuedAt(LocalDateTime.now());
        return Result.ok(r);
    }

    @Data
    public static class DeviceRegisterRequest {
        private String deviceId;
        private String deviceType; // e.g. FACE_TERMINAL, POS, GATE
        private String model;
        private String location;
        private String secret; // 可选注册校验码
    }

    @Data
    public static class DeviceRegisterResult {
        private String deviceId;
        private boolean registered;
        private String deviceKey;
        private LocalDateTime registeredAt;
        private String message;
    }

    @Data
    public static class DeviceStatus {
        private String deviceId;
        private boolean online;
        private LocalDateTime lastHeartbeat;
        private String firmwareVersion;
        private Integer battery;
        private String network;
    }

    @Data
    public static class DataUploadRequest {
        private String type; // e.g. FACE_RECOGNITION, CARD_CONSUME, HEARTBEAT
        private Map<String, Object> payload = new HashMap<>();
    }

    @Data
    public static class DataUploadResult {
        private String deviceId;
        private String type;
        private boolean accepted;
        private String ackId;
        private LocalDateTime receivedAt;
    }

    @Data
    public static class DeviceConfigRequest {
        private Map<String, Object> params = new HashMap<>();
    }

    @Data
    public static class ConfigApplyResult {
        private String deviceId;
        private boolean applied;
        private String message;
        private LocalDateTime appliedAt;
        private Map<String, Object> appliedItems = new HashMap<>();
    }

    @Data
    public static class CommandIssueRequest {
        private String commandType; // e.g. REBOOT, UPDATE_FIRMWARE, SYNC_TIME
        private Map<String, Object> params = new HashMap<>();
    }

    @Data
    public static class CommandIssueResult {
        private String deviceId;
        private String commandId;
        private String status; // QUEUED/SENT/FAILED
        private String commandType;
        private LocalDateTime issuedAt;
    }
}