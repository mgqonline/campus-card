-- Wechat API 数据库表结构（DDL）
-- 目标库：campus_card

-- 通知中心
CREATE TABLE IF NOT EXISTS `wx_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `child_id` BIGINT NOT NULL,
  `category` VARCHAR(32) NOT NULL,
  `title` VARCHAR(128) NOT NULL,
  `content` VARCHAR(1024) NOT NULL,
  `extra` VARCHAR(2048) NULL,
  `read_flag` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_wx_notification_child` (`child_id`),
  KEY `idx_wx_notification_category` (`category`),
  KEY `idx_wx_notification_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 学生信息
CREATE TABLE IF NOT EXISTS `student` (
  `id` BIGINT NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `class_id` BIGINT NULL,
  `grade` VARCHAR(32) NULL,
  `card_no` VARCHAR(64) NULL,
  `face_status` VARCHAR(32) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 考勤事件
CREATE TABLE IF NOT EXISTS `attendance_event` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `child_id` BIGINT NOT NULL,
  `type` VARCHAR(32) NOT NULL,
  `gate` VARCHAR(64) NULL,
  `time` DATETIME NOT NULL,
  `photo_url` VARCHAR(255) NULL,
  `late` TINYINT(1) NULL,
  `early_leave` TINYINT(1) NULL,
  PRIMARY KEY (`id`),
  KEY `idx_attendance_event_child` (`child_id`),
  KEY `idx_attendance_event_time` (`time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 考勤提醒
CREATE TABLE IF NOT EXISTS `attendance_alert` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `child_id` BIGINT NOT NULL,
  `title` VARCHAR(128) NOT NULL,
  `desc` VARCHAR(512) NULL,
  `time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_attendance_alert_child` (`child_id`),
  KEY `idx_attendance_alert_time` (`time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 消费记录
CREATE TABLE IF NOT EXISTS `consume_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `child_id` BIGINT NOT NULL,
  `date` DATE NOT NULL,
  `merchant` VARCHAR(128) NOT NULL,
  `amount` DECIMAL(12,2) NOT NULL,
  `detail` VARCHAR(255) NULL,
  `channel` VARCHAR(32) NULL,
  `tx_id` VARCHAR(64) NULL,
  PRIMARY KEY (`id`),
  KEY `idx_consume_record_child` (`child_id`),
  KEY `idx_consume_record_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 人像采集
CREATE TABLE IF NOT EXISTS `face_collection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `child_id` BIGINT NOT NULL,
  `photo_url` VARCHAR(500) NULL,
  `collection_type` VARCHAR(20) NULL,
  `status` VARCHAR(20) NULL,
  `quality_score` DOUBLE NULL,
  `quality_issues` VARCHAR(500) NULL,
  `audit_comment` VARCHAR(500) NULL,
  `auditor_id` BIGINT NULL,
  `created_time` DATETIME NULL,
  `updated_time` DATETIME NULL,
  `audit_time` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_face_collection_child` (`child_id`),
  KEY `idx_face_collection_status` (`status`),
  KEY `idx_face_collection_created` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;