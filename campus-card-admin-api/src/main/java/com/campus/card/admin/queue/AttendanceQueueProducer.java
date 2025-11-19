package com.campus.card.admin.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AttendanceQueueProducer {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final com.campus.card.admin.service.AttendanceBatchService batchService;
    @org.springframework.beans.factory.annotation.Value("${attendance.queue.enabled:true}")
    private boolean queueEnabled;

    public AttendanceQueueProducer(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper,
                                   com.campus.card.admin.service.AttendanceBatchService batchService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.batchService = batchService;
    }

    public void enqueueFace(com.campus.card.admin.controller.AttendanceController.FaceIngestReq req) {
        if (!queueEnabled) {
            java.util.List<com.campus.card.admin.controller.AttendanceController.FaceIngestReq> list =
                    java.util.Collections.singletonList(req);
            batchService.processFaceBatch(list);
            return;
        }
        try {
            enqueue("face", req);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AttendanceQueueProducer.class)
                    .warn("Redis enqueue failed for face, fallback to direct batch", e);
            java.util.List<com.campus.card.admin.controller.AttendanceController.FaceIngestReq> list =
                    java.util.Collections.singletonList(req);
            batchService.processFaceBatch(list);
        }
    }

    public void enqueueCard(com.campus.card.admin.controller.AttendanceController.CardIngestReq req) {
        if (!queueEnabled) {
            java.util.List<com.campus.card.admin.controller.AttendanceController.CardIngestReq> list =
                    java.util.Collections.singletonList(req);
            batchService.processCardBatch(list);
            return;
        }
        try {
            enqueue("card", req);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AttendanceQueueProducer.class)
                    .warn("Redis enqueue failed for card, fallback to direct batch", e);
            java.util.List<com.campus.card.admin.controller.AttendanceController.CardIngestReq> list =
                    java.util.Collections.singletonList(req);
            batchService.processCardBatch(list);
        }
    }

    private void enqueue(String type, Object payload) {
        try {
            java.util.Map<String, Object> wrapper = new java.util.HashMap<>();
            wrapper.put("type", type);
            wrapper.put("payload", payload);
            String json = objectMapper.writeValueAsString(wrapper);
            stringRedisTemplate.opsForList().leftPush(AttendanceQueueKeys.QUEUE_KEY, json);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize queue payload", e);
        }
    }
}