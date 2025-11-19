package com.campus.card.admin.controller;

import com.campus.card.admin.domain.Device;
import com.campus.card.admin.repository.DeviceRepository;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/attendance/devices")
public class AttendanceDeviceController {
    private final DeviceRepository deviceRepository;

    public AttendanceDeviceController(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    // 设备注册（海康）
    @PostMapping("/register")
    public Result<Device> register(@RequestBody Device body) {
        if (body.getCode() == null || body.getCode().isEmpty()) return Result.error("设备编码必填", 400);
        if (deviceRepository.existsByCode(body.getCode())) return Result.error("设备编码已存在", 400);
        body.setVendor(body.getVendor() == null ? "hikvision" : body.getVendor());
        body.setStatus(body.getStatus() == null ? 0 : body.getStatus());
        body.setCreatedAt(LocalDateTime.now());
        body.setUpdatedAt(LocalDateTime.now());
        Device saved = deviceRepository.save(body);
        return Result.ok(saved);
    }

    // 列表查询（支持按厂商/分组过滤）
    @GetMapping
    public Result<List<Device>> list(
            @RequestParam(required = false) String vendor,
            @RequestParam(required = false) String groupType,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        List<Device> all = deviceRepository.findAll();
        List<Device> filtered = all.stream()
                .filter(d -> vendor == null || vendor.equalsIgnoreCase(d.getVendor()))
                .filter(d -> groupType == null || groupType.equalsIgnoreCase(d.getGroupType()))
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .collect(Collectors.toList());
        System.out.println(all.size());
        int from = Math.min((page - 1) * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        return Result.ok(filtered.subList(from, to));
    }

    // 详情
    @GetMapping("/{id}")
    public Result<Device> detail(@PathVariable Long id) {
        Optional<Device> of = deviceRepository.findById(id);
        return of.map(Result::ok).orElse(Result.error("设备不存在", 404));
    }

    // 更新设备信息（基础信息维护）
    @PutMapping("/{id}")
    public Result<Device> update(@PathVariable Long id, @RequestBody Device body) {
        Optional<Device> of = deviceRepository.findById(id);
        if (!of.isPresent()) return Result.error("设备不存在", 404);
        Device d = of.get();
        if (body.getName() != null) d.setName(body.getName());
        if (body.getLocation() != null) d.setLocation(body.getLocation());
        if (body.getIp() != null) d.setIp(body.getIp());
        if (body.getPort() != null) d.setPort(body.getPort());
        if (body.getUsername() != null) d.setUsername(body.getUsername());
        if (body.getPassword() != null) d.setPassword(body.getPassword());
        if (body.getVendor() != null) d.setVendor(body.getVendor());
        if (body.getGroupType() != null) d.setGroupType(body.getGroupType());
        d.setUpdatedAt(LocalDateTime.now());
        return Result.ok(deviceRepository.save(d));
    }

    // 删除设备
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deviceRepository.deleteById(id);
        return Result.ok(null);
    }

    // 设备状态监控：获取在线/离线状态
    @GetMapping("/{id}/status")
    public Result<StatusResp> status(@PathVariable Long id) {
        Optional<Device> of = deviceRepository.findById(id);
        if (!of.isPresent()) return Result.error("设备不存在", 404);
        Device d = of.get();
        StatusResp resp = new StatusResp();
        resp.setStatus(d.getStatus() == null ? 0 : d.getStatus());
        resp.setLastHeartbeatAt(d.getLastHeartbeatAt());
        return Result.ok(resp);
    }

    // 设置在线状态
    @PutMapping("/{id}/online")
    public Result<Void> online(@PathVariable Long id) {
        Optional<Device> of = deviceRepository.findById(id);
        if (!of.isPresent()) return Result.error("设备不存在", 404);
        Device d = of.get();
        d.setStatus(1);
        d.setLastHeartbeatAt(LocalDateTime.now());
        d.setUpdatedAt(LocalDateTime.now());
        deviceRepository.save(d);
        return Result.ok(null);
    }

    // 设置离线状态
    @PutMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) {
        Optional<Device> of = deviceRepository.findById(id);
        if (!of.isPresent()) return Result.error("设备不存在", 404);
        Device d = of.get();
        d.setStatus(0);
        d.setUpdatedAt(LocalDateTime.now());
        deviceRepository.save(d);
        return Result.ok(null);
    }

    // 设备参数配置（JSON）
    @PutMapping("/{id}/params")
    public Result<Void> updateParams(@PathVariable Long id, @RequestBody ParamReq req) {
        Optional<Device> of = deviceRepository.findById(id);
        if (!of.isPresent()) return Result.error("设备不存在", 404);
        Device d = of.get();
        d.setParamJson(req.getParamJson());
        d.setUpdatedAt(LocalDateTime.now());
        deviceRepository.save(d);
        return Result.ok(null);
    }

    // 设备分组（教师/学生/门禁）
    @PutMapping("/{id}/group")
    public Result<Void> updateGroup(@PathVariable Long id, @RequestBody GroupReq req) {
        Optional<Device> of = deviceRepository.findById(id);
        if (!of.isPresent()) return Result.error("设备不存在", 404);
        Device d = of.get();
        d.setGroupType(req.getGroupType());
        d.setUpdatedAt(LocalDateTime.now());
        deviceRepository.save(d);
        return Result.ok(null);
    }

    @GetMapping("/groups")
    public Result<List<String>> groups() {
        return Result.ok(Arrays.asList("TEACHER", "STUDENT", "ACCESS"));
    }

    public static class StatusResp {
        private Integer status;
        private LocalDateTime lastHeartbeatAt;
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public LocalDateTime getLastHeartbeatAt() { return lastHeartbeatAt; }
        public void setLastHeartbeatAt(LocalDateTime lastHeartbeatAt) { this.lastHeartbeatAt = lastHeartbeatAt; }
    }

    public static class ParamReq {
        private String paramJson;
        public String getParamJson() { return paramJson; }
        public void setParamJson(String paramJson) { this.paramJson = paramJson; }
    }

    public static class GroupReq {
        private String groupType;
        public String getGroupType() { return groupType; }
        public void setGroupType(String groupType) { this.groupType = groupType; }
    }
}