package com.campus.card.admin.service;

import com.campus.card.admin.domain.SyncTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SyncService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_TASKS = "sync:tasks";       // 列表，记录任务ID（最新在左）
    private static final String KEY_TASK_PREFIX = "sync:task:";  // 具体任务内容，值为JSON
    private static final String KEY_SEQ = "sync:task:seq";       // 自增序列

    public enum SyncType {
        PERSONS,
        FACE_PHOTOS,
        FACE_FEATURES,
        CARDS,
        PERMISSIONS,
        TIME_GROUPS,
        ATTENDANCE_RULES
    }

    public enum SyncStatus { PENDING, RUNNING, SUCCESS, FAILED }

    public SyncTask createTask(SyncType type, String scope, String payloadSummary) {
        Long seq = redisTemplate.opsForValue().increment(KEY_SEQ);
        String id = (seq == null ? "0" : String.valueOf(seq)) + "-" + System.currentTimeMillis();
        SyncTask task = new SyncTask();
        task.setId(id);
        task.setType(type.name());
        task.setStatus(SyncStatus.PENDING.name());
        task.setScope(scope);
        task.setPayloadSummary(payloadSummary);
        task.setProgress(0);
        task.setMessage("任务已创建");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        saveTask(task);
        return task;
    }

    public void saveTask(SyncTask task) {
        try {
            String json = objectMapper.writeValueAsString(task);
            redisTemplate.opsForValue().set(KEY_TASK_PREFIX + task.getId(), json);
            // 新任务放到队列左侧
            redisTemplate.opsForList().leftPush(KEY_TASKS, task.getId());
            // 只保留最近1000条
            redisTemplate.opsForList().trim(KEY_TASKS, 0, 999);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("保存任务失败", e);
        }
    }

    public SyncTask getTask(String id) {
        String json = redisTemplate.opsForValue().get(KEY_TASK_PREFIX + id);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, SyncTask.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("读取任务失败", e);
        }
    }

    public void updateTask(SyncTask task) {
        task.setUpdatedAt(LocalDateTime.now());
        try {
            String json = objectMapper.writeValueAsString(task);
            redisTemplate.opsForValue().set(KEY_TASK_PREFIX + task.getId(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("更新任务失败", e);
        }
    }

    public void updateTaskStatus(String id, SyncStatus status, Integer progress, String message) {
        SyncTask task = getTask(id);
        if (task == null) return;
        task.setStatus(status.name());
        if (progress != null) task.setProgress(progress);
        if (message != null) task.setMessage(message);
        updateTask(task);
    }

    public List<SyncTask> listTasks(int limit) {
        List<String> ids = redisTemplate.opsForList().range(KEY_TASKS, 0, Math.max(limit - 1, 0));
        List<SyncTask> result = new ArrayList<>();
        if (ids == null) return result;
        for (String id : ids) {
            SyncTask t = getTask(id);
            if (t != null) result.add(t);
        }
        return result;
    }

    // 以下为各类下发的占位实现：创建任务->RUNNING->SUCCESS（可后续替换为实际集成）
    public SyncTask dispatchPersons(String scope, String payloadSummary) {
        SyncTask task = createTask(SyncType.PERSONS, scope, payloadSummary);
        updateTaskStatus(task.getId(), SyncStatus.RUNNING, 10, "开始下发人员信息");
        // TODO 接入具体设备/平台API
        updateTaskStatus(task.getId(), SyncStatus.SUCCESS, 100, "人员信息下发完成");
        return getTask(task.getId());
    }

    public SyncTask dispatchFacePhotos(String scope, String payloadSummary) {
        SyncTask task = createTask(SyncType.FACE_PHOTOS, scope, payloadSummary);
        updateTaskStatus(task.getId(), SyncStatus.RUNNING, 10, "开始下发人脸照片");
        // TODO 实现单个/批量照片同步
        updateTaskStatus(task.getId(), SyncStatus.SUCCESS, 100, "人脸照片下发完成");
        return getTask(task.getId());
    }

    public SyncTask dispatchFaceFeatures(String scope, String payloadSummary) {
        SyncTask task = createTask(SyncType.FACE_FEATURES, scope, payloadSummary);
        updateTaskStatus(task.getId(), SyncStatus.RUNNING, 10, "开始下发人脸特征");
        // TODO 下发模板/特征到设备
        updateTaskStatus(task.getId(), SyncStatus.SUCCESS, 100, "人脸特征下发完成");
        return getTask(task.getId());
    }

    public SyncTask dispatchCards(String scope, String payloadSummary) {
        SyncTask task = createTask(SyncType.CARDS, scope, payloadSummary);
        updateTaskStatus(task.getId(), SyncStatus.RUNNING, 10, "开始下发卡片信息");
        // TODO 下发卡号/状态到控制器
        updateTaskStatus(task.getId(), SyncStatus.SUCCESS, 100, "卡片信息下发完成");
        return getTask(task.getId());
    }

    public SyncTask dispatchPermissions(String scope, String payloadSummary) {
        SyncTask task = createTask(SyncType.PERMISSIONS, scope, payloadSummary);
        updateTaskStatus(task.getId(), SyncStatus.RUNNING, 10, "开始下发权限信息");
        // TODO 下发门禁/考勤权限到设备或平台
        updateTaskStatus(task.getId(), SyncStatus.SUCCESS, 100, "权限信息下发完成");
        return getTask(task.getId());
    }

    public SyncTask dispatchTimeGroups(String scope, String payloadSummary) {
        SyncTask task = createTask(SyncType.TIME_GROUPS, scope, payloadSummary);
        updateTaskStatus(task.getId(), SyncStatus.RUNNING, 10, "开始下发时间组");
        // TODO 时间段/班次映射到设备的时间组
        updateTaskStatus(task.getId(), SyncStatus.SUCCESS, 100, "时间组下发完成");
        return getTask(task.getId());
    }

    public SyncTask dispatchAttendanceRules(String scope, String payloadSummary) {
        SyncTask task = createTask(SyncType.ATTENDANCE_RULES, scope, payloadSummary);
        updateTaskStatus(task.getId(), SyncStatus.RUNNING, 10, "开始下发考勤规则");
        // TODO 将规则映射到设备/平台策略
        updateTaskStatus(task.getId(), SyncStatus.SUCCESS, 100, "考勤规则下发完成");
        return getTask(task.getId());
    }
}