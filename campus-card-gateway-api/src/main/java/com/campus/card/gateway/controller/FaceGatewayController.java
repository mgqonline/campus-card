package com.campus.card.gateway.controller;

import com.campus.card.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Tag(name = "人脸数据", description = "人脸信息、上传、下发与识别结果接口")
@RestController
@RequestMapping("/api/v1/gw/faces")
public class FaceGatewayController {

    @Operation(summary = "人脸信息接口")
    @GetMapping("/info/{personType}/{personId}")
    public Result<List<FaceInfo>> faceInfo(@PathVariable String personType, @PathVariable String personId) {
        FaceInfo f1 = new FaceInfo("face-001", personType, personId, "https://example.com/face-001.jpg", "APPROVED");
        FaceInfo f2 = new FaceInfo("face-002", personType, personId, "https://example.com/face-002.jpg", "PENDING");
        return Result.ok(Arrays.asList(f1, f2));
    }

    @Operation(summary = "人脸上传接口")
    @PostMapping("/upload")
    public Result<UploadResult> upload(@RequestBody UploadRequest body) {
        UploadResult r = new UploadResult();
        r.setFaceId("face-" + System.currentTimeMillis());
        r.setStatus("RECEIVED");
        return Result.ok(r);
    }

    @Operation(summary = "人脸下发接口")
    @PostMapping("/issue")
    public Result<IssueResult> issue(@RequestBody IssueRequest body) {
        IssueResult r = new IssueResult();
        r.setDeviceCount(body.getDeviceIds() == null ? 0 : body.getDeviceIds().size());
        r.setStatus("DISPATCHED");
        return Result.ok(r);
    }

    @Operation(summary = "人脸识别结果接口")
    @GetMapping("/recognitions")
    public Result<List<RecognitionRecord>> recognitions(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String personType,
            @RequestParam(required = false) String personId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        RecognitionRecord r1 = new RecognitionRecord("dev-001", "STUDENT", "S20250001", true, 0.93, LocalDateTime.now().minusMinutes(10));
        RecognitionRecord r2 = new RecognitionRecord("dev-002", "TEACHER", "T10086", false, 0.41, LocalDateTime.now().minusMinutes(5));
        return Result.ok(Arrays.asList(r1, r2));
    }

    @Data
    public static class FaceInfo {
        private String faceId;
        private String personType;
        private String personId;
        private String imageUrl;
        private String status;
        public FaceInfo(String faceId, String personType, String personId, String imageUrl, String status) {
            this.faceId = faceId; this.personType = personType; this.personId = personId; this.imageUrl = imageUrl; this.status = status;
        }
    }

    @Data
    public static class UploadRequest {
        private String personType; // STUDENT/TEACHER
        private String personId;
        private String imageBase64; // 或 imageUrl
        private String imageUrl;
    }

    @Data
    public static class UploadResult { private String faceId; private String status; }

    @Data
    public static class IssueRequest {
        private String personType;
        private String personId;
        private List<String> deviceIds;
    }

    @Data
    public static class IssueResult { private int deviceCount; private String status; }

    @Data
    public static class RecognitionRecord {
        private String deviceId;
        private String personType;
        private String personId;
        private boolean success;
        private double score;
        private LocalDateTime time;
        public RecognitionRecord(String deviceId, String personType, String personId, boolean success, double score, LocalDateTime time) {
            this.deviceId = deviceId; this.personType = personType; this.personId = personId; this.success = success; this.score = score; this.time = time;
        }
    }
}