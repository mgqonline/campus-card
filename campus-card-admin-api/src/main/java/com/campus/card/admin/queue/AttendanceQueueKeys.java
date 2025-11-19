package com.campus.card.admin.queue;

public final class AttendanceQueueKeys {
    private AttendanceQueueKeys() {}
    public static final String QUEUE_KEY = "attendance:ingest:queue";
    public static final String DEDUP_SET_KEY_PREFIX = "attendance:dedup:"; // e.g., attendance:dedup:2025-10-25
}