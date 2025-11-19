package com.campus.card.wechat.controller;

import com.campus.card.common.result.Result;
import com.campus.card.wechat.model.FaceCollection;
import com.campus.card.wechat.repository.FaceCollectionRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/face")
public class FaceCollectionController {

    @Autowired
    private FaceCollectionRepository faceCollectionRepository;

    /**
     * 上传人脸照片
     */
    @PostMapping("/upload")
    public Result<UploadResp> upload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("childId") Long childId) {
        try {
            // 模拟文件上传处理
            String fileName = file.getOriginalFilename();
            String fileUrl = "/uploads/faces/" + System.currentTimeMillis() + "_" + fileName;
            
            // 创建人像采集记录
            FaceCollection faceCollection = new FaceCollection();
            faceCollection.setChildId(childId);
            faceCollection.setPhotoUrl(fileUrl);
            faceCollection.setCollectionType("UPLOAD");
            faceCollection.setStatus("PROCESSING");
            
            // 模拟质量检测
            QualityCheckResult qualityResult = performQualityCheck(file);
            faceCollection.setQualityScore(qualityResult.getScore());
            faceCollection.setQualityIssues(qualityResult.getIssues());
            
            // 根据质量评分决定状态
            if (qualityResult.getScore() >= 70) {
                faceCollection.setStatus("PENDING"); // 待审核
            } else {
                faceCollection.setStatus("REJECTED"); // 质量不合格直接拒绝
                faceCollection.setAuditComment("照片质量不符合要求：" + qualityResult.getIssues());
            }
            
            faceCollection = faceCollectionRepository.save(faceCollection);
            
            UploadResp resp = new UploadResp();
            resp.setId(faceCollection.getId());
            resp.setUrl(fileUrl);
            resp.setStatus(faceCollection.getStatus());
            resp.setQualityScore(faceCollection.getQualityScore());
            resp.setQualityIssues(faceCollection.getQualityIssues());
            
            return Result.ok(resp);
        } catch (Exception e) {
            return Result.error("上传失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 拍摄人脸照片（接收base64数据）
     */
    @PostMapping("/capture")
    public Result<CaptureResp> capture(@RequestBody CaptureReq req) {
        try {
            // 模拟保存base64图片
            String fileUrl = "/uploads/faces/capture_" + System.currentTimeMillis() + ".jpg";
            
            FaceCollection faceCollection = new FaceCollection();
            faceCollection.setChildId(req.getChildId());
            faceCollection.setPhotoUrl(fileUrl);
            faceCollection.setCollectionType("CAMERA");
            faceCollection.setStatus("PROCESSING");
            
            // 模拟质量检测
            QualityCheckResult qualityResult = performQualityCheckBase64(req.getImageData());
            faceCollection.setQualityScore(qualityResult.getScore());
            faceCollection.setQualityIssues(qualityResult.getIssues());
            
            if (qualityResult.getScore() >= 70) {
                faceCollection.setStatus("PENDING");
            } else {
                faceCollection.setStatus("REJECTED");
                faceCollection.setAuditComment("照片质量不符合要求：" + qualityResult.getIssues());
            }
            
            faceCollection = faceCollectionRepository.save(faceCollection);
            
            CaptureResp resp = new CaptureResp();
            resp.setId(faceCollection.getId());
            resp.setUrl(fileUrl);
            resp.setStatus(faceCollection.getStatus());
            resp.setQualityScore(faceCollection.getQualityScore());
            resp.setQualityIssues(faceCollection.getQualityIssues());
            
            return Result.ok(resp);
        } catch (Exception e) {
            return Result.error("拍摄保存失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 查询人像采集进度
     */
    @GetMapping("/progress")
    public Result<ProgressResp> progress(@RequestParam Long childId) {
        try {
            Optional<FaceCollection> latest = faceCollectionRepository.findTopByChildIdOrderByCreatedTimeDesc(childId);
            
            ProgressResp resp = new ProgressResp();
            resp.setChildId(childId);
            
            if (latest.isPresent()) {
                FaceCollection fc = latest.get();
                resp.setHasRecord(true);
                resp.setStatus(fc.getStatus());
                resp.setQualityScore(fc.getQualityScore());
                resp.setCreatedTime(fc.getCreatedTime().toString());
                resp.setAuditComment(fc.getAuditComment());
                resp.setPhotoUrl(fc.getPhotoUrl());
            } else {
                resp.setHasRecord(false);
                resp.setStatus("NOT_COLLECTED");
            }
            
            return Result.ok(resp);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 查询人像采集历史记录
     */
    @GetMapping("/history")
    public Result<List<HistoryItem>> history(@RequestParam Long childId) {
        try {
            List<FaceCollection> records = faceCollectionRepository.findByChildIdOrderByCreatedTimeDesc(childId);
            
            List<HistoryItem> items = records.stream().map(fc -> {
                HistoryItem item = new HistoryItem();
                item.setId(fc.getId());
                item.setPhotoUrl(fc.getPhotoUrl());
                item.setCollectionType(fc.getCollectionType());
                item.setStatus(fc.getStatus());
                item.setQualityScore(fc.getQualityScore());
                item.setCreatedTime(fc.getCreatedTime().toString());
                item.setAuditComment(fc.getAuditComment());
                return item;
            }).collect(Collectors.toList());
            
            return Result.ok(items);
        } catch (Exception e) {
            return Result.error("查询历史记录失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 重新提交人像采集
     */
    @PostMapping("/resubmit")
    public Result<String> resubmit(@RequestParam Long childId) {
        try {
            // 将之前的记录状态更新为已取消
            List<FaceCollection> oldRecords = faceCollectionRepository.findByChildIdAndStatus(childId, "REJECTED");
            for (FaceCollection fc : oldRecords) {
                fc.setStatus("CANCELLED");
                faceCollectionRepository.save(fc);
            }
            
            return Result.ok("可以重新提交人像采集");
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 模拟人脸质量检测（文件上传）
     */
    private QualityCheckResult performQualityCheck(MultipartFile file) {
        QualityCheckResult result = new QualityCheckResult();
        List<String> issues = new ArrayList<>();
        
        // 模拟检测逻辑
        double score = 75 + Math.random() * 20; // 75-95分随机
        
        if (file.getSize() < 10000) {
            issues.add("图片文件过小");
            score -= 20;
        }
        
        if (file.getSize() > 5000000) {
            issues.add("图片文件过大");
            score -= 10;
        }
        
        // 随机添加一些质量问题
        if (Math.random() < 0.3) {
            issues.add("光线不足");
            score -= 15;
        }
        
        if (Math.random() < 0.2) {
            issues.add("人脸角度偏斜");
            score -= 10;
        }
        
        result.setScore(Math.max(0, score));
        result.setIssues(String.join(", ", issues));
        
        return result;
    }

    /**
     * 模拟人脸质量检测（base64数据）
     */
    private QualityCheckResult performQualityCheckBase64(String base64Data) {
        QualityCheckResult result = new QualityCheckResult();
        List<String> issues = new ArrayList<>();
        
        double score = 80 + Math.random() * 15; // 80-95分随机
        
        if (base64Data.length() < 1000) {
            issues.add("图片数据过小");
            score -= 25;
        }
        
        // 随机质量检测
        if (Math.random() < 0.25) {
            issues.add("图像模糊");
            score -= 20;
        }
        
        if (Math.random() < 0.15) {
            issues.add("背景复杂");
            score -= 10;
        }
        
        result.setScore(Math.max(0, score));
        result.setIssues(String.join(", ", issues));
        
        return result;
    }

    // 内部类定义
    @Data
    public static class CaptureReq {
        private Long childId;
        private String imageData; // base64编码的图片数据
    }

    @Data
    public static class UploadResp {
        private Long id;
        private String url;
        private String status;
        private Double qualityScore;
        private String qualityIssues;
    }

    @Data
    public static class CaptureResp {
        private Long id;
        private String url;
        private String status;
        private Double qualityScore;
        private String qualityIssues;
    }

    @Data
    public static class ProgressResp {
        private Long childId;
        private Boolean hasRecord;
        private String status;
        private Double qualityScore;
        private String createdTime;
        private String auditComment;
        private String photoUrl;
    }

    @Data
    public static class HistoryItem {
        private Long id;
        private String photoUrl;
        private String collectionType;
        private String status;
        private Double qualityScore;
        private String createdTime;
        private String auditComment;
    }

    @Data
    private static class QualityCheckResult {
        private Double score;
        private String issues;
    }
}