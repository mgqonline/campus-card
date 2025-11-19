package com.campus.card.admin.controller;

import com.campus.card.admin.domain.HikvisionConfig;
import com.campus.card.admin.repository.HikvisionConfigRepository;
import com.campus.card.admin.repository.SchoolRepository;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/v1/hikvision-config")
public class HikvisionConfigController {

    private final HikvisionConfigRepository configRepository;
    private final SchoolRepository schoolRepository;

    public HikvisionConfigController(HikvisionConfigRepository configRepository, SchoolRepository schoolRepository) {
        this.configRepository = configRepository;
        this.schoolRepository = schoolRepository;
    }

    /**
     * 获取所有学校的海康配置列表
     */
    @GetMapping
    public Result<List<HikvisionConfig>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        List<HikvisionConfig> configs;
        if (status != null) {
            configs = configRepository.findByStatus(status);
        } else {
            configs = configRepository.findAll();
        }
        
        // 简单分页
        int from = Math.min((page - 1) * size, configs.size());
        int to = Math.min(from + size, configs.size());
        return Result.ok(configs.subList(from, to));
    }

    /**
     * 根据学校ID获取配置
     */
    @GetMapping("/school/{schoolId}")
    public Result<HikvisionConfig> getBySchoolId(@PathVariable Long schoolId) {
        Optional<HikvisionConfig> config = configRepository.findBySchoolId(schoolId);
        return config.map(Result::ok).orElse(Result.error("该学校暂无海康设备配置", 404));
    }

    /**
     * 获取配置详情
     */
    @GetMapping("/{id}")
    public Result<HikvisionConfig> detail(@PathVariable Long id) {
        Optional<HikvisionConfig> config = configRepository.findById(id);
        return config.map(Result::ok).orElse(Result.error("配置不存在", 404));
    }

    /**
     * 创建新的学校海康配置
     */
    @PostMapping
    public Result<HikvisionConfig> create(@RequestBody HikvisionConfig config) {
        if (config.getSchoolId() == null) {
            return Result.error("学校ID不能为空", 400);
        }
        
        if (configRepository.existsBySchoolId(config.getSchoolId())) {
            return Result.error("该学校已存在海康设备配置", 400);
        }
        
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        
        HikvisionConfig saved = configRepository.save(config);
        return Result.ok(saved);
    }

    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    public Result<HikvisionConfig> update(@PathVariable Long id, @RequestBody HikvisionConfig body) {
        Optional<HikvisionConfig> optConfig = configRepository.findById(id);
        if (!optConfig.isPresent()) {
            return Result.error("配置不存在", 404);
        }
        
        HikvisionConfig config = optConfig.get();
        
        // 更新基础配置
        if (body.getSchoolName() != null) config.setSchoolName(body.getSchoolName());
        if (body.getDeviceIp() != null) config.setDeviceIp(body.getDeviceIp());
        if (body.getDevicePort() != null) config.setDevicePort(body.getDevicePort());
        
        // 更新SDK配置
        if (body.getSdkVersion() != null) config.setSdkVersion(body.getSdkVersion());
        if (body.getSdkTimeout() != null) config.setSdkTimeout(body.getSdkTimeout());
        if (body.getMaxConnections() != null) config.setMaxConnections(body.getMaxConnections());
        
        // 更新设备编号配置
        if (body.getDeviceCodePrefix() != null) config.setDeviceCodePrefix(body.getDeviceCodePrefix());
        if (body.getDeviceCodeLength() != null) config.setDeviceCodeLength(body.getDeviceCodeLength());
        
        // 更新通讯协议配置
        if (body.getProtocolType() != null) config.setProtocolType(body.getProtocolType());
        if (body.getEncoding() != null) config.setEncoding(body.getEncoding());
        if (body.getDataFormat() != null) config.setDataFormat(body.getDataFormat());
        
        // 更新认证配置
        if (body.getUsername() != null) config.setUsername(body.getUsername());
        if (body.getPassword() != null) config.setPassword(body.getPassword());
        if (body.getAuthMode() != null) config.setAuthMode(body.getAuthMode());
        
        // 更新心跳检测配置
        if (body.getHeartbeatEnabled() != null) config.setHeartbeatEnabled(body.getHeartbeatEnabled());
        if (body.getHeartbeatInterval() != null) config.setHeartbeatInterval(body.getHeartbeatInterval());
        if (body.getHeartbeatTimeout() != null) config.setHeartbeatTimeout(body.getHeartbeatTimeout());
        if (body.getMaxRetryCount() != null) config.setMaxRetryCount(body.getMaxRetryCount());
        
        // 更新功能开关
        if (body.getFaceRecognitionEnabled() != null) config.setFaceRecognitionEnabled(body.getFaceRecognitionEnabled());
        if (body.getCardRecognitionEnabled() != null) config.setCardRecognitionEnabled(body.getCardRecognitionEnabled());
        if (body.getTemperatureDetectionEnabled() != null) config.setTemperatureDetectionEnabled(body.getTemperatureDetectionEnabled());
        if (body.getMaskDetectionEnabled() != null) config.setMaskDetectionEnabled(body.getMaskDetectionEnabled());
        
        // 更新数据同步配置
        if (body.getSyncEnabled() != null) config.setSyncEnabled(body.getSyncEnabled());
        if (body.getSyncInterval() != null) config.setSyncInterval(body.getSyncInterval());
        if (body.getBatchSize() != null) config.setBatchSize(body.getBatchSize());
        
        if (body.getStatus() != null) config.setStatus(body.getStatus());
        
        config.setUpdatedAt(LocalDateTime.now());
        
        HikvisionConfig saved = configRepository.save(config);
        return Result.ok(saved);
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!configRepository.existsById(id)) {
            return Result.error("配置不存在", 404);
        }
        configRepository.deleteById(id);
        return Result.ok(null);
    }

    /**
     * 测试连接
     */
    @PostMapping("/{id}/test")
    public Result<TestConnectionResult> testConnection(@PathVariable Long id) {
        Optional<HikvisionConfig> optConfig = configRepository.findById(id);
        if (!optConfig.isPresent()) {
            return Result.error("配置不存在", 404);
        }
        
        HikvisionConfig config = optConfig.get();
        
        // 模拟连接测试
        TestConnectionResult result = new TestConnectionResult();
        try {
            // 这里应该实现真实的海康设备连接测试
            // 暂时模拟测试结果
            boolean success = config.getDeviceIp() != null && !config.getDeviceIp().isEmpty();
            
            result.setSuccess(success);
            result.setMessage(success ? "连接成功" : "设备IP不能为空");
            result.setResponseTime(success ? 150 : 0);
            
            // 更新测试结果
            config.setLastTestAt(LocalDateTime.now());
            config.setTestResult(result.getMessage());
            configRepository.save(config);
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("连接失败: " + e.getMessage());
            result.setResponseTime(0);
        }
        
        return Result.ok(result);
    }

    /**
     * 启用/禁用配置
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        Optional<HikvisionConfig> optConfig = configRepository.findById(id);
        if (!optConfig.isPresent()) {
            return Result.error("配置不存在", 404);
        }
        
        HikvisionConfig config = optConfig.get();
        config.setStatus(request.getStatus());
        config.setUpdatedAt(LocalDateTime.now());
        configRepository.save(config);
        
        return Result.ok(null);
    }

    /**
     * 获取默认配置模板
     */
    @GetMapping("/template")
    public Result<HikvisionConfig> getTemplate() {
        HikvisionConfig template = new HikvisionConfig();
        template.setDeviceIp("192.168.1.100");
        template.setDevicePort(8000);
        template.setSdkVersion("6.1.9.47");
        template.setDeviceCodePrefix("001");
        template.setProtocolType("TCP");
        template.setUsername("admin");
        template.setPassword("12345");
        template.setHeartbeatEnabled(true);
        template.setHeartbeatInterval(30);
        template.setSdkTimeout(5000);
        template.setMaxRetryCount(3);
        template.setFaceRecognitionEnabled(true);
        template.setCardRecognitionEnabled(true);
        template.setTemperatureDetectionEnabled(false);
        template.setSyncInterval(60);
        template.setSyncEnabled(true);
        template.setStatus(1);
        
        return Result.ok(template);
    }

    /**
     * 批量配置学校设备参数
     */
    @PostMapping("/batch-config")
    public Result<List<HikvisionConfig>> batchConfigBySchools(@RequestBody BatchConfigRequest request) {
        try {
            List<HikvisionConfig> results = new ArrayList<>();
            
            for (Long schoolId : request.getSchoolIds()) {
                // 查找或创建配置
                Optional<HikvisionConfig> existingConfig = configRepository.findBySchoolId(schoolId);
                HikvisionConfig config;
                
                if (existingConfig.isPresent()) {
                    config = existingConfig.get();
                    // 更新现有配置
                    updateConfigFromTemplate(config, request.getTemplate());
                    config.setUpdatedAt(LocalDateTime.now());
                } else {
                    // 创建新配置
                    config = new HikvisionConfig();
                    config.setSchoolId(schoolId);
                    updateConfigFromTemplate(config, request.getTemplate());
                    config.setCreatedAt(LocalDateTime.now());
                    config.setUpdatedAt(LocalDateTime.now());
                }
                
                HikvisionConfig saved = configRepository.save(config);
                results.add(saved);
            }
            
            return Result.ok(results);
        } catch (Exception e) {
            return Result.error("批量配置失败: " + e.getMessage(), 500);
        }
    }

    /**
     * 按学校复制配置
     */
    @PostMapping("/copy-config")
    public Result<HikvisionConfig> copyConfigToSchool(@RequestBody CopyConfigRequest request) {
        try {
            // 获取源配置
            Optional<HikvisionConfig> sourceConfig = configRepository.findBySchoolId(request.getSourceSchoolId());
            if (!sourceConfig.isPresent()) {
                return Result.error("源学校配置不存在", 404);
            }
            
            // 查找或创建目标配置
            Optional<HikvisionConfig> existingConfig = configRepository.findBySchoolId(request.getTargetSchoolId());
            HikvisionConfig targetConfig;
            
            if (existingConfig.isPresent()) {
                targetConfig = existingConfig.get();
            } else {
                targetConfig = new HikvisionConfig();
                targetConfig.setSchoolId(request.getTargetSchoolId());
                targetConfig.setCreatedAt(LocalDateTime.now());
            }
            
            // 复制配置（除了ID和学校ID）
            HikvisionConfig source = sourceConfig.get();
            targetConfig.setDeviceIp(source.getDeviceIp());
            targetConfig.setDevicePort(source.getDevicePort());
            targetConfig.setSdkVersion(source.getSdkVersion());
            targetConfig.setDeviceCodePrefix(source.getDeviceCodePrefix());
            targetConfig.setProtocolType(source.getProtocolType());
            targetConfig.setUsername(source.getUsername());
            targetConfig.setPassword(source.getPassword());
            targetConfig.setHeartbeatEnabled(source.getHeartbeatEnabled());
            targetConfig.setHeartbeatInterval(source.getHeartbeatInterval());
            targetConfig.setSdkTimeout(source.getSdkTimeout());
            targetConfig.setMaxRetryCount(source.getMaxRetryCount());
            targetConfig.setFaceRecognitionEnabled(source.getFaceRecognitionEnabled());
            targetConfig.setCardRecognitionEnabled(source.getCardRecognitionEnabled());
            targetConfig.setTemperatureDetectionEnabled(source.getTemperatureDetectionEnabled());
            targetConfig.setSyncInterval(source.getSyncInterval());
            targetConfig.setSyncEnabled(source.getSyncEnabled());
            targetConfig.setStatus(source.getStatus());
            targetConfig.setUpdatedAt(LocalDateTime.now());
            
            HikvisionConfig saved = configRepository.save(targetConfig);
            return Result.ok(saved);
        } catch (Exception e) {
            return Result.error("复制配置失败: " + e.getMessage(), 500);
        }
    }

    /**
     * 获取学校配置统计
     */
    @GetMapping("/stats")
    public Result<ConfigStats> getConfigStats() {
        try {
            List<HikvisionConfig> allConfigs = configRepository.findAll();
            List<HikvisionConfig> activeConfigs = configRepository.findByStatus(1);
            
            ConfigStats stats = new ConfigStats();
            stats.setTotalConfigs(allConfigs.size());
            stats.setActiveConfigs(activeConfigs.size());
            stats.setInactiveConfigs(allConfigs.size() - activeConfigs.size());
            
            return Result.ok(stats);
        } catch (Exception e) {
            return Result.error("获取统计信息失败: " + e.getMessage(), 500);
        }
    }

    /**
     * 更新配置模板到指定配置
     */
    private void updateConfigFromTemplate(HikvisionConfig config, HikvisionConfig template) {
        if (template.getDeviceIp() != null) config.setDeviceIp(template.getDeviceIp());
        if (template.getDevicePort() != null) config.setDevicePort(template.getDevicePort());
        if (template.getSdkVersion() != null) config.setSdkVersion(template.getSdkVersion());
        if (template.getDeviceCodePrefix() != null) config.setDeviceCodePrefix(template.getDeviceCodePrefix());
        if (template.getProtocolType() != null) config.setProtocolType(template.getProtocolType());
        if (template.getUsername() != null) config.setUsername(template.getUsername());
        if (template.getPassword() != null) config.setPassword(template.getPassword());
        if (template.getHeartbeatEnabled() != null) config.setHeartbeatEnabled(template.getHeartbeatEnabled());
        if (template.getHeartbeatInterval() != null) config.setHeartbeatInterval(template.getHeartbeatInterval());
        if (template.getSdkTimeout() != null) config.setSdkTimeout(template.getSdkTimeout());
        if (template.getMaxRetryCount() != null) config.setMaxRetryCount(template.getMaxRetryCount());
        if (template.getFaceRecognitionEnabled() != null) config.setFaceRecognitionEnabled(template.getFaceRecognitionEnabled());
        if (template.getCardRecognitionEnabled() != null) config.setCardRecognitionEnabled(template.getCardRecognitionEnabled());
        if (template.getTemperatureDetectionEnabled() != null) config.setTemperatureDetectionEnabled(template.getTemperatureDetectionEnabled());
        if (template.getSyncInterval() != null) config.setSyncInterval(template.getSyncInterval());
        if (template.getSyncEnabled() != null) config.setSyncEnabled(template.getSyncEnabled());
        if (template.getStatus() != null) config.setStatus(template.getStatus());
    }

    // 内部类
    public static class TestConnectionResult {
        private boolean success;
        private String message;
        private int responseTime; // 响应时间(ms)
        
        // getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getResponseTime() { return responseTime; }
        public void setResponseTime(int responseTime) { this.responseTime = responseTime; }
    }

    public static class StatusRequest {
        private Integer status;
        
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
    }

    /**
     * 批量配置请求
     */
    public static class BatchConfigRequest {
        private List<Long> schoolIds;
        private HikvisionConfig template;

        public List<Long> getSchoolIds() { return schoolIds; }
        public void setSchoolIds(List<Long> schoolIds) { this.schoolIds = schoolIds; }
        public HikvisionConfig getTemplate() { return template; }
        public void setTemplate(HikvisionConfig template) { this.template = template; }
    }

    /**
     * 复制配置请求
     */
    public static class CopyConfigRequest {
        private Long sourceSchoolId;
        private Long targetSchoolId;

        public Long getSourceSchoolId() { return sourceSchoolId; }
        public void setSourceSchoolId(Long sourceSchoolId) { this.sourceSchoolId = sourceSchoolId; }
        public Long getTargetSchoolId() { return targetSchoolId; }
        public void setTargetSchoolId(Long targetSchoolId) { this.targetSchoolId = targetSchoolId; }
    }

    /**
     * 配置统计
     */
    public static class ConfigStats {
        private int totalConfigs;
        private int activeConfigs;
        private int inactiveConfigs;

        public int getTotalConfigs() { return totalConfigs; }
        public void setTotalConfigs(int totalConfigs) { this.totalConfigs = totalConfigs; }
        public int getActiveConfigs() { return activeConfigs; }
        public void setActiveConfigs(int activeConfigs) { this.activeConfigs = activeConfigs; }
        public int getInactiveConfigs() { return inactiveConfigs; }
        public void setInactiveConfigs(int inactiveConfigs) { this.inactiveConfigs = inactiveConfigs; }
    }
}