-- 与 /api/v1/notify/mock/generate 一致的示例数据
-- 可按需调整 child_id（默认 2001）
SET @child_id := 2001;

INSERT INTO `wx_notification` (`child_id`, `category`, `title`, `content`, `extra`, `read_flag`, `created_at`) VALUES
  (@child_id, 'ATTENDANCE', '考勤提醒', '今日早读已签到', NULL, 0, NOW()),
  (@child_id, 'CONSUME', '消费提醒', '食堂消费 12.00 元', '{"amount":12.00,"place":"一食堂"}', 0, NOW()),
  (@child_id, 'LOW_BALANCE', '余额不足', '卡片余额低于 10 元，请尽快充值', '{"threshold":10}', 0, NOW()),
  (@child_id, 'LEAVE_APPROVAL', '请假审批通知', '学生请假已审批通过', NULL, 0, NOW()),
  (@child_id, 'ANNOUNCEMENT', '学校通知公告', '本周五举行运动会，请合理安排', NULL, 0, NOW());