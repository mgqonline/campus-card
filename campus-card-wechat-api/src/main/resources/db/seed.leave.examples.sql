-- 示例请假数据（用于本地演示与验证教师端页面）
-- 依赖 seed.students.sql 中的示例学生 (2001 -> class 301, 2002 -> class 302)

INSERT INTO `leave_application`
  (`child_id`,`class_id`,`type`,`start_time`,`end_time`,`reason`,`attachments_json`,`status`,`apply_time`)
VALUES
  (2001, 301, 'SICK', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY + INTERVAL 2 HOUR,
   '发烧请假', '["https://example.com/att-1.jpg"]', 'PENDING', NOW() - INTERVAL 2 DAY),
  (2002, 302, 'PERSONAL', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY + INTERVAL 3 HOUR,
   '家庭事务', '["https://example.com/att-2.jpg"]', 'PENDING', NOW() - INTERVAL 1 DAY);

-- 可选：示例审批，将第一条设为已通过
UPDATE `leave_application`
SET `status`='APPROVED', `approve_time`=NOW(), `approver_teacher_id`=9001
WHERE `child_id`=2001
  AND `class_id`=301
LIMIT 1;