package com.campus.card.admin.service;

import com.campus.card.admin.domain.FaceInfo;
import com.campus.card.admin.repository.FaceInfoRepository;
import com.campus.card.admin.repository.FaceConfigRepository;
import com.campus.card.admin.domain.FaceConfig;
import com.campus.card.common.result.PageResult;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FaceService {
    private final FaceInfoRepository faceRepo;
    private final FaceConfigRepository configRepo;

    public FaceService(FaceInfoRepository faceRepo, FaceConfigRepository configRepo) {
        this.faceRepo = faceRepo;
        this.configRepo = configRepo;
    }

    public PageResult<FaceInfo> list(int page, int size, String personType, String personId) {
        List<FaceInfo> all = faceRepo.findAll();
        if (personType != null && !personType.isEmpty()) {
            all = all.stream().filter(f -> personType.equalsIgnoreCase(f.getPersonType())).collect(java.util.stream.Collectors.toList());
        }
        if (personId != null && !personId.isEmpty()) {
            all = all.stream().filter(f -> personId.equals(f.getPersonId())).collect(java.util.stream.Collectors.toList());
        }
        int total = all.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(total, from + size);
        List<FaceInfo> pageList = from >= total ? java.util.Collections.emptyList() : all.subList(from, to);
        return PageResult.of(total, pageList);
    }

    @Data
    public static class CreateReq {
        private String personType; // STUDENT/TEACHER/STAFF/VISITOR
        private String personId;   // 学号/工号/职工号/访客标识
        private String photoBase64;
        private String features;   // 预留
    }

    @Data
    public static class UpdateReq {
        private Long id;
        private String photoBase64;
        private String features;   // 预留
    }

    @Data
    public static class QualityReq {
        private String photoBase64;
    }

    @Data
    public static class QualityResult {
        private boolean success;
        private int score;
        private String message;
    }

    public FaceInfo create(CreateReq req) {
        if (req.getPersonType() == null || req.getPersonId() == null) {
            throw new IllegalArgumentException("personType/personId 必填");
        }
        FaceConfig cfg = configRepo.findTopByOrderByIdAsc().orElseGet(() -> {
            FaceConfig c = new FaceConfig();
            c.setUpdatedAt(java.time.LocalDateTime.now());
            return configRepo.save(c);
        });
        FaceInfo f = new FaceInfo();
        f.setPersonType(req.getPersonType());
        f.setPersonId(req.getPersonId());
        f.setPhotoBase64(req.getPhotoBase64());
        f.setFeatures(req.getFeatures());
        int q = estimateQuality(req.getPhotoBase64());
        // 阈值校验：质量分需不低于识别阈值
        if (cfg.getRecognitionThreshold() != null && q < cfg.getRecognitionThreshold()) {
            throw new IllegalArgumentException("质量分(" + q + ")低于识别阈值(" + cfg.getRecognitionThreshold() + ")");
        }
        // 容量校验：总条目不得超过库容量
        long total = faceRepo.count();
        if (cfg.getLibraryCapacity() != null && total >= cfg.getLibraryCapacity()) {
            throw new IllegalStateException("人脸库容量已满(" + cfg.getLibraryCapacity() + ")");
        }
        f.setQualityScore(q);
        f.setCreatedAt(java.time.LocalDateTime.now());
        f.setUpdatedAt(java.time.LocalDateTime.now());
        return faceRepo.save(f);
    }

    public FaceInfo update(UpdateReq req) {
        Optional<FaceInfo> of = faceRepo.findById(req.getId());
        if (!of.isPresent()) throw new IllegalArgumentException("人脸记录不存在");
        FaceInfo f = of.get();
        if (req.getPhotoBase64() != null) {
            f.setPhotoBase64(req.getPhotoBase64());
            f.setQualityScore(estimateQuality(req.getPhotoBase64()));
        }
        if (req.getFeatures() != null) f.setFeatures(req.getFeatures());
        f.setUpdatedAt(LocalDateTime.now());
        return faceRepo.save(f);
    }

    public void delete(Long id) {
        faceRepo.deleteById(id);
    }

    public Optional<FaceInfo> get(Long id) {
        return faceRepo.findById(id);
    }

    public QualityResult quality(QualityReq req) {
        QualityResult r = new QualityResult();
        int score = estimateQuality(req.getPhotoBase64());
        // 使用配置阈值来判断质量是否合格
        FaceConfig cfg = configRepo.findTopByOrderByIdAsc().orElseGet(() -> {
            FaceConfig c = new FaceConfig();
            c.setUpdatedAt(java.time.LocalDateTime.now());
            return configRepo.save(c);
        });
        int threshold = cfg.getRecognitionThreshold() != null ? cfg.getRecognitionThreshold() : 60;
        r.setScore(score);
        r.setSuccess(true);
        r.setMessage(score >= threshold ? "质量合格" : "质量偏低，请重新采集(阈值:" + threshold + ")");
        return r;
    }

    // 简易质量评估：校验Base64可解码、长度、是否含常见图片头
    private int estimateQuality(String base64) {
        if (base64 == null || base64.trim().isEmpty()) return 0;
        try {
            String payload = base64.contains(",") ? base64.substring(base64.indexOf(',') + 1) : base64;
            byte[] bytes = Base64.getDecoder().decode(payload);
            int len = bytes.length;
            boolean looksImage = len > 10 && (payload.startsWith("/9j/") /* jpeg */ || payload.startsWith("iVBORw0KGgo") /* png */);
            int sizeScore = Math.min(100, len / 1024); // 1KB -> 1分，最多100
            int headerScore = looksImage ? 20 : 0;
            int score = Math.min(100, sizeScore + headerScore);
            return score;
        } catch (Exception e) {
            return 0;
        }
    }
}