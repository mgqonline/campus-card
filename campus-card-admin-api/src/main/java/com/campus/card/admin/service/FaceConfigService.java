package com.campus.card.admin.service;

import com.campus.card.admin.domain.FaceConfig;
import com.campus.card.admin.repository.FaceConfigRepository;
import com.campus.card.admin.repository.FaceInfoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FaceConfigService {
    private final FaceConfigRepository repo;
    private final FaceInfoRepository faceRepo;

    public FaceConfigService(FaceConfigRepository repo, FaceInfoRepository faceRepo) {
        this.repo = repo;
        this.faceRepo = faceRepo;
    }

    public FaceConfig get() {
        return repo.findTopByOrderByIdAsc().orElseGet(() -> {
            FaceConfig c = new FaceConfig();
            c.setUpdatedAt(LocalDateTime.now());
            return repo.save(c);
        });
    }

    public FaceConfig update(Integer threshold, String mode, Boolean liveness, Integer capacity) {
        FaceConfig c = get();
        if (threshold != null) {
            if (threshold < 0 || threshold > 100) throw new IllegalArgumentException("识别阈值必须在0-100之间");
            c.setRecognitionThreshold(threshold);
        }
        if (mode != null) {
            try {
                c.setRecognitionMode(FaceConfig.RecognitionMode.valueOf(mode));
            } catch (Exception e) {
                throw new IllegalArgumentException("识别模式只支持 ONE_TO_ONE 或 ONE_TO_MANY");
            }
        }
        if (liveness != null) c.setLivenessEnabled(liveness);
        if (capacity != null) {
            if (capacity <= 0) throw new IllegalArgumentException("人脸库容量必须为正数");
            long used = faceRepo.count();
            if (capacity < used) throw new IllegalArgumentException("容量不能低于当前已存数量(" + used + ")");
            c.setLibraryCapacity(capacity);
        }
        c.setUpdatedAt(java.time.LocalDateTime.now());
        return repo.save(c);
    }
}