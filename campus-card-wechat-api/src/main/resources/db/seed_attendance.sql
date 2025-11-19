-- 初始化示例学生（班级 301）
INSERT INTO student (id, name, class_id, grade, card_no, face_status, student_no) VALUES
  (2001, '张三', 301, '三年级', '62220001', '已采集', 'S2001'),
  (2002, '李四', 301, '三年级', '62220002', '已采集', 'S2002'),
  (2003, '王五', 301, '三年级', '62220003', '未采集', 'S2003'),
  (2004, '赵六', 301, '三年级', '62220004', '已采集', 'S2004'),
  (2005, '钱七', 301, '三年级', '62220005', '已采集', 'S2005');

-- 为上述学生生成当天的进校/离校事件
-- 注意：attendance_event 的时间字段为 DATETIME
INSERT INTO attendance_event (child_id, type, gate, time, photo_url, late, early_leave) VALUES
  (2001, '进校', '南门', CONCAT(CURDATE(), ' 08:05:00'), NULL, TRUE, FALSE),
  (2001, '离校', '南门', CONCAT(CURDATE(), ' 16:55:00'), NULL, FALSE, FALSE),
  (2002, '进校', '南门', CONCAT(CURDATE(), ' 08:00:00'), NULL, FALSE, FALSE),
  (2002, '离校', '南门', CONCAT(CURDATE(), ' 16:40:00'), NULL, FALSE, TRUE),
  (2003, '进校', '南门', CONCAT(CURDATE(), ' 08:12:00'), NULL, TRUE, FALSE),
  (2003, '离校', '南门', CONCAT(CURDATE(), ' 17:05:00'), NULL, FALSE, FALSE);

-- 可按需追加更多日期的样本：
-- INSERT INTO attendance_event (...) VALUES (2001, '进校', '南门', DATE_SUB(CONCAT(CURDATE(),' 08:05:00'), INTERVAL 1 DAY), NULL, TRUE, FALSE);