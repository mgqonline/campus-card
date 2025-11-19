package com.campus.card.wechat.model;

import javax.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "wx_notification")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联学生/子女ID（家长收到的都是面向childId的通知） */
    private Long childId;

    /** 通知分类：ATTENDANCE/CONSUME/LOW_BALANCE/LEAVE_APPROVAL/ANNOUNCEMENT */
    @Column(length = 32)
    private String category;

    @Column(length = 128)
    private String title;

    @Column(length = 1024)
    private String content;

    /** 扩展字段（JSON/String），非必填 */
    @Column(length = 2048)
    private String extra;

    private boolean readFlag = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}