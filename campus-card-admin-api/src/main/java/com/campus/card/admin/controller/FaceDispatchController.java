package com.campus.card.admin.controller;

import com.campus.card.admin.domain.FaceDispatchTask;
import com.campus.card.admin.service.FaceDispatchService;
import com.campus.card.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/face-dispatch")
public class FaceDispatchController {
    private final FaceDispatchService dispatchService;

    public FaceDispatchController(FaceDispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping("/batch")
    public Result<FaceDispatchTask> batch(@RequestBody FaceDispatchService.BatchReq req) {
        try {
            return Result.ok(dispatchService.batch(req));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/incremental")
    public Result<FaceDispatchTask> incremental(@RequestBody FaceDispatchService.IncrementalReq req) {
        try {
            return Result.ok(dispatchService.incremental(req));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @PostMapping("/delete")
    public Result<FaceDispatchTask> delete(@RequestBody FaceDispatchService.DeleteReq req) {
        try {
            return Result.ok(dispatchService.delete(req));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }

    @GetMapping("/tasks")
    public Result<List<FaceDispatchTask>> listTasks() {
        return Result.ok(dispatchService.listTasks());
    }

    @GetMapping("/tasks/{id}")
    public Result<FaceDispatchTask> taskDetail(@PathVariable Long id) {
        Optional<FaceDispatchTask> of = dispatchService.getTask(id);
        return of.map(Result::ok).orElse(Result.error("任务不存在", 404));
    }

    @PostMapping("/tasks/{id}/retry")
    public Result<FaceDispatchTask> retry(@PathVariable Long id) {
        try {
            return Result.ok(dispatchService.retry(id));
        } catch (Exception e) {
            return Result.error(e.getMessage(), 400);
        }
    }
}