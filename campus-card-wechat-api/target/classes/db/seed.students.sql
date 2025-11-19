-- 初始化示例学生（与前端默认绑定一致）
INSERT INTO `student` (`id`, `name`, `class_id`, `grade`, `card_no`, `face_status`) VALUES
  (2001, '张小明', 301, '三年级', 'CARD-2001', '未采集'),
  (2002, '张小红', 302, '三年级', 'CARD-2002', '未采集')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`), `class_id`=VALUES(`class_id`), `grade`=VALUES(`grade`), `card_no`=VALUES(`card_no`), `face_status`=VALUES(`face_status`);