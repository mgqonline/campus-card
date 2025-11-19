-- 请假申请表结构（与 JPA 实体 LeaveApplication 对齐）
CREATE TABLE IF NOT EXISTS `leave_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `child_id` BIGINT NOT NULL,
  `class_id` BIGINT NULL,
  `type` VARCHAR(32) NULL,
  `start_time` DATETIME NULL,
  `end_time` DATETIME NULL,
  `reason` VARCHAR(1024) NULL,
  `attachments_json` LONGTEXT NULL,
  `status` VARCHAR(20) NOT NULL,
  `apply_time` DATETIME NOT NULL,
  `approve_time` DATETIME NULL,
  `approver_teacher_id` BIGINT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_leave_child` (`child_id`),
  KEY `idx_leave_class` (`class_id`),
  KEY `idx_leave_status` (`status`),
  KEY `idx_leave_apply` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;