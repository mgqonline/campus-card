package com.campus.card.admin.queue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.campus.card.admin.service.AttendanceBatchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@ConditionalOnProperty(name = "attendance.queue.enabled", havingValue = "true", matchIfMissing = true)
@Service
public class AttendanceQueueConsumer {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final AttendanceBatchService batchService;

    @Value("${attendance.queue.batch-size:100}")
    private int batchSize;

    @Value("${attendance.queue.pop-timeout-ms:200}")
    private long popTimeoutMs;

    public AttendanceQueueConsumer(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper, AttendanceBatchService batchService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.batchService = batchService;
    }

    // 定时消费队列，尽量批量处理
    @Scheduled(fixedDelayString = "${attendance.queue.poll-interval-ms:500}")
    public void drainAndProcess() {
        try {
            List<String> items = popBatch(batchSize);
            if (items.isEmpty()) {
                return;
            }
            List<com.campus.card.admin.controller.AttendanceController.FaceIngestReq> faceReqs = new ArrayList<>();
            List<com.campus.card.admin.controller.AttendanceController.CardIngestReq> cardReqs = new ArrayList<>();
            for (String json : items) {
                try {
                    // 队列层去重：按原始消息内容hash做当日去重，避免重复处理
                    String dedupKey = buildDedupKey(json);
                    if (existsDedupKey(dedupKey)) {
                        continue;
                    }
                    JsonNode node = objectMapper.readTree(json);
                    String type = node.path("type").asText();
                    JsonNode payload = node.path("payload");
                    if ("face".equalsIgnoreCase(type)) {
                        com.campus.card.admin.controller.AttendanceController.FaceIngestReq req = objectMapper.convertValue(payload, com.campus.card.admin.controller.AttendanceController.FaceIngestReq.class);
                        faceReqs.add(req);
                        addDedupKey(dedupKey);
                    } else if ("card".equalsIgnoreCase(type)) {
                        com.campus.card.admin.controller.AttendanceController.CardIngestReq req = objectMapper.convertValue(payload, com.campus.card.admin.controller.AttendanceController.CardIngestReq.class);
                        cardReqs.add(req);
                        addDedupKey(dedupKey);
                    }
                } catch (Exception e) {
                    org.slf4j.LoggerFactory.getLogger(AttendanceQueueConsumer.class).error("Queue item processing failed", e);
                }
            }
            if (!faceReqs.isEmpty()) {
                batchService.processFaceBatch(faceReqs);
            }
            if (!cardReqs.isEmpty()) {
                batchService.processCardBatch(cardReqs);
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AttendanceQueueConsumer.class).warn("Queue drain failed (Redis likely unavailable)", e);
        }
    }

    private List<String> popBatch(int limit) {
        List<String> list = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            String val = stringRedisTemplate.opsForList().rightPop(AttendanceQueueKeys.QUEUE_KEY, Duration.ofMillis(popTimeoutMs));
            if (val == null) break;
            list.add(val);
        }
        return list;
    }

    private String buildDedupKey(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] d = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            String b64 = Base64.getUrlEncoder().withoutPadding().encodeToString(d);
            return AttendanceQueueKeys.DEDUP_SET_KEY_PREFIX + LocalDate.now() + ":" + b64;
        } catch (Exception e) {
            return AttendanceQueueKeys.DEDUP_SET_KEY_PREFIX + LocalDate.now() + ":" + raw.hashCode();
        }
    }

    private boolean existsDedupKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    private void addDedupKey(String key) {
        stringRedisTemplate.opsForValue().set(key, "1");
        stringRedisTemplate.expire(key, java.time.Duration.ofDays(7));
    }
}