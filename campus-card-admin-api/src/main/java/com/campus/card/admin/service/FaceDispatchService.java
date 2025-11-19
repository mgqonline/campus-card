package com.campus.card.admin.service;

import com.campus.card.admin.domain.DeviceFace;
import com.campus.card.admin.domain.FaceDispatchTask;
import com.campus.card.admin.domain.FaceInfo;
import com.campus.card.admin.repository.DeviceFaceRepository;
import com.campus.card.admin.repository.FaceDispatchTaskRepository;
import com.campus.card.admin.repository.FaceInfoRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FaceDispatchService {
    private final FaceInfoRepository faceRepo;
    private final DeviceFaceRepository deviceFaceRepo;
    private final FaceDispatchTaskRepository taskRepo;

    public FaceDispatchService(FaceInfoRepository faceRepo, DeviceFaceRepository deviceFaceRepo, FaceDispatchTaskRepository taskRepo) {
        this.faceRepo = faceRepo;
        this.deviceFaceRepo = deviceFaceRepo;
        this.taskRepo = taskRepo;
    }

    @Data
    public static class BatchReq {
        private List<Long> deviceIds;
        private String personType; // 可选
        private String personId;   // 可选
        private List<Long> faceIds; // 可选：指定人脸记录集合
    }

    @Data
    public static class IncrementalReq {
        private List<Long> deviceIds;
        private String since; // ISO时间字符串，例如 2025-01-01T00:00:00
    }

    @Data
    public static class DeleteReq {
        private List<Long> deviceIds;
        private List<String> personIds; // 删除指定人员的人脸
    }

    public FaceDispatchTask batch(BatchReq req) {
        List<FaceInfo> faces;
        if (req.getFaceIds() != null && !req.getFaceIds().isEmpty()) {
            faces = faceRepo.findAllById(req.getFaceIds());
        } else {
            faces = faceRepo.findAll();
            if (req.getPersonType() != null && !req.getPersonType().isEmpty()) {
                faces = faces.stream().filter(f -> req.getPersonType().equalsIgnoreCase(f.getPersonType())).collect(Collectors.toList());
            }
            if (req.getPersonId() != null && !req.getPersonId().isEmpty()) {
                faces = faces.stream().filter(f -> req.getPersonId().equals(f.getPersonId())).collect(Collectors.toList());
            }
        }

        FaceDispatchTask task = new FaceDispatchTask();
        task.setTaskType("BATCH");
        task.setDeviceIds(joinIds(req.getDeviceIds()));
        task.setDescription("batch dispatch: devices=" + joinIds(req.getDeviceIds()) + ", count=" + faces.size());
        task.setStatus("RUNNING");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task = taskRepo.save(task);

        int success = 0; int failed = 0; List<String> failedPersons = new ArrayList<>();
        for (FaceInfo f : faces) {
            for (Long did : req.getDeviceIds()) {
                try {
                    // 写入设备人脸库（模拟下发成功）
                    DeviceFace df = deviceFaceRepo.findByDeviceIdAndPersonId(did, f.getPersonId()).orElse(new DeviceFace());
                    df.setDeviceId(did);
                    df.setPersonType(f.getPersonType());
                    df.setPersonId(f.getPersonId());
                    df.setDispatchedAt(LocalDateTime.now());
                    df.setStatus(1);
                    deviceFaceRepo.save(df);
                    success++;
                } catch (Exception e) {
                    failed++;
                    failedPersons.add(f.getPersonId());
                }
            }
        }
        task.setTotalItems(success + failed);
        task.setSuccessItems(success);
        task.setFailedItems(failed);
        task.setFailedPersonIds(String.join(",", failedPersons));
        task.setStatus(failed == 0 ? "SUCCESS" : (success > 0 ? "PARTIAL_FAILED" : "FAILED"));
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepo.save(task);
    }

    public FaceDispatchTask incremental(IncrementalReq req) {
        LocalDateTime sinceTime = parseTime(req.getSince());
        List<FaceInfo> faces = faceRepo.findAll().stream()
                .filter(f -> f.getUpdatedAt() != null && (sinceTime == null || !f.getUpdatedAt().isBefore(sinceTime)))
                .collect(Collectors.toList());
        FaceDispatchTask task = new FaceDispatchTask();
        task.setTaskType("INCREMENTAL");
        task.setDeviceIds(joinIds(req.getDeviceIds()));
        task.setDescription("incremental dispatch since=" + req.getSince() + ", count=" + faces.size());
        task.setStatus("RUNNING");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task = taskRepo.save(task);

        int success = 0; int failed = 0; List<String> failedPersons = new ArrayList<>();
        for (FaceInfo f : faces) {
            for (Long did : req.getDeviceIds()) {
                try {
                    DeviceFace df = deviceFaceRepo.findByDeviceIdAndPersonId(did, f.getPersonId()).orElse(new DeviceFace());
                    df.setDeviceId(did);
                    df.setPersonType(f.getPersonType());
                    df.setPersonId(f.getPersonId());
                    df.setDispatchedAt(LocalDateTime.now());
                    df.setStatus(1);
                    deviceFaceRepo.save(df);
                    success++;
                } catch (Exception e) {
                    failed++;
                    failedPersons.add(f.getPersonId());
                }
            }
        }
        task.setTotalItems(success + failed);
        task.setSuccessItems(success);
        task.setFailedItems(failed);
        task.setFailedPersonIds(String.join(",", failedPersons));
        task.setStatus(failed == 0 ? "SUCCESS" : (success > 0 ? "PARTIAL_FAILED" : "FAILED"));
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepo.save(task);
    }

    public FaceDispatchTask delete(DeleteReq req) {
        FaceDispatchTask task = new FaceDispatchTask();
        task.setTaskType("DELETE");
        task.setDeviceIds(joinIds(req.getDeviceIds()));
        task.setDescription("delete faces on devices, persons=" + (req.getPersonIds() != null ? req.getPersonIds().size() : 0));
        task.setStatus("RUNNING");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task = taskRepo.save(task);

        int success = 0; int failed = 0; List<String> failedPersons = new ArrayList<>();
        List<String> personIds = Optional.ofNullable(req.getPersonIds()).orElse(Collections.emptyList());
        for (String pid : personIds) {
            for (Long did : req.getDeviceIds()) {
                try {
                    int del = deviceFaceRepo.deleteByDeviceIdAndPersonId(did, pid);
                    if (del > 0) success++; else failed++;
                } catch (Exception e) {
                    failed++;
                    failedPersons.add(pid);
                }
            }
        }
        task.setTotalItems(success + failed);
        task.setSuccessItems(success);
        task.setFailedItems(failed);
        task.setFailedPersonIds(String.join(",", failedPersons));
        task.setStatus(failed == 0 ? "SUCCESS" : (success > 0 ? "PARTIAL_FAILED" : "FAILED"));
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepo.save(task);
    }

    public List<FaceDispatchTask> listTasks() {
        return taskRepo.findAll();
    }

    public Optional<FaceDispatchTask> getTask(Long id) {
        return taskRepo.findById(id);
    }

    public FaceDispatchTask retry(Long id) {
        FaceDispatchTask task = taskRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        if (task.getFailedItems() == null || task.getFailedItems() == 0) return task;
        List<Long> deviceIds = parseIds(task.getDeviceIds());
        List<String> failedPersons = Arrays.stream(Optional.ofNullable(task.getFailedPersonIds()).orElse("").split(",")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        int success = 0; int failed = 0; List<String> stillFailed = new ArrayList<>();
        for (String pid : failedPersons) {
            for (Long did : deviceIds) {
                try {
                    DeviceFace df = deviceFaceRepo.findByDeviceIdAndPersonId(did, pid).orElse(new DeviceFace());
                    // 由于删除任务也可能失败，这里统一按下发覆盖处理
                    df.setDeviceId(did);
                    // 简化：personType未知情况下，按STUDENT填充
                    df.setPersonType("STUDENT");
                    df.setPersonId(pid);
                    df.setDispatchedAt(LocalDateTime.now());
                    df.setStatus(1);
                    deviceFaceRepo.save(df);
                    success++;
                } catch (Exception e) {
                    failed++;
                    stillFailed.add(pid);
                }
            }
        }
        task.setSuccessItems((task.getSuccessItems() == null ? 0 : task.getSuccessItems()) + success);
        task.setFailedItems(stillFailed.size());
        task.setFailedPersonIds(String.join(",", stillFailed));
        task.setStatus(task.getFailedItems() != null && task.getFailedItems() > 0 ? "PARTIAL_FAILED" : "SUCCESS");
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepo.save(task);
    }

    private static String joinIds(List<Long> ids) {
        return ids == null ? "" : ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
    private static List<Long> parseIds(String s) {
        if (s == null || s.isEmpty()) return Collections.emptyList();
        return Arrays.stream(s.split(",")).filter(t -> !t.isEmpty()).map(Long::valueOf).collect(Collectors.toList());
    }
    private static LocalDateTime parseTime(String s) {
        try { return s == null || s.isEmpty() ? null : LocalDateTime.parse(s); } catch (Exception e) { return null; }
    }
}