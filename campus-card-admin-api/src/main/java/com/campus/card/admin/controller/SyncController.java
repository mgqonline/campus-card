package com.campus.card.admin.controller;

import com.campus.card.admin.domain.SyncTask;
import com.campus.card.admin.service.SyncService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @Data
    public static class DispatchRequest {
        private String scope;           // 下发范围，如设备ID/学校ID
        private String payloadSummary;  // 载荷概要，如人数/条数等
    }

    @PostMapping("/persons")
    public SyncTask dispatchPersons(@RequestBody DispatchRequest req) {
        return syncService.dispatchPersons(req.getScope(), req.getPayloadSummary());
    }

    @PostMapping("/face-photos")
    public SyncTask dispatchFacePhotos(@RequestBody DispatchRequest req) {
        return syncService.dispatchFacePhotos(req.getScope(), req.getPayloadSummary());
    }

    @PostMapping("/face-features")
    public SyncTask dispatchFaceFeatures(@RequestBody DispatchRequest req) {
        return syncService.dispatchFaceFeatures(req.getScope(), req.getPayloadSummary());
    }

    @PostMapping("/cards")
    public SyncTask dispatchCards(@RequestBody DispatchRequest req) {
        return syncService.dispatchCards(req.getScope(), req.getPayloadSummary());
    }

    @PostMapping("/permissions")
    public SyncTask dispatchPermissions(@RequestBody DispatchRequest req) {
        return syncService.dispatchPermissions(req.getScope(), req.getPayloadSummary());
    }

    @PostMapping("/time-groups")
    public SyncTask dispatchTimeGroups(@RequestBody DispatchRequest req) {
        return syncService.dispatchTimeGroups(req.getScope(), req.getPayloadSummary());
    }

    @PostMapping("/attendance-rules")
    public SyncTask dispatchAttendanceRules(@RequestBody DispatchRequest req) {
        return syncService.dispatchAttendanceRules(req.getScope(), req.getPayloadSummary());
    }

    @GetMapping("/tasks")
    public List<SyncTask> listTasks(@RequestParam(defaultValue = "100") int limit) {
        return syncService.listTasks(limit);
    }

    @GetMapping("/tasks/{id}")
    public SyncTask getTask(@PathVariable String id) {
        return syncService.getTask(id);
    }
}