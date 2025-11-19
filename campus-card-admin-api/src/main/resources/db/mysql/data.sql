-- Initial data for campus-card-admin-api (MySQL)
SET NAMES utf8mb4;

-- Default face recognition config
INSERT INTO face_config (id, recognition_threshold, recognition_mode, liveness_enabled, library_capacity, updated_at)
VALUES (1, 75, 'ONE_TO_ONE', 0, 10000, CURRENT_TIMESTAMP);

-- Roles
INSERT INTO sys_role (name) VALUES ('ADMIN'), ('TEACHER'),
  ('超级管理员'), ('学校管理员'), ('财务管理员'), ('班主任'), ('教师'), ('家长');

-- Menus
INSERT INTO sys_menu (name, path, sort_order, status) VALUES
  ('学生管理', '/student/list', 1, 1),
  ('用户管理', '/system/user', 2, 1);

-- Admin user (phone set NULL to avoid encryption mismatch)
INSERT INTO sys_user (username, password, phone, status) VALUES ('admin', '123456', NULL, 1);

-- Bind admin role to admin user
INSERT INTO sys_user_role (user_id, role_id)
VALUES ((SELECT id FROM sys_user WHERE username='admin'), (SELECT id FROM sys_role WHERE name='ADMIN'));

-- Departments sample data
INSERT INTO department (name, status, create_time, update_time) VALUES ('语文组', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO department (name, status, create_time, update_time) VALUES ('数学组', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO department (name, status, create_time, update_time) VALUES ('英语组', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Students sample data
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三1','S2024001',1,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三2','S2024002',2,0);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三3','S2024003',3,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三4','S2024004',4,0);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三5','S2024005',5,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三6','S2024006',1,0);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三7','S2024007',2,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三8','S2024008',3,0);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三9','S2024009',4,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三10','S2024010',5,0);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三11','S2024011',1,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三12','S2024012',2,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三13','S2024013',3,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三14','S2024014',4,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三15','S2024015',5,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三16','S2024016',1,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三17','S2024017',2,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三18','S2024018',3,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三19','S2024019',4,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三20','S2024020',5,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三21','S2024021',1,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三22','S2024022',2,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三23','S2024023',3,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三24','S2024024',4,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三25','S2024025',5,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三26','S2024026',1,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三27','S2024027',2,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三28','S2024028',3,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三29','S2024029',4,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三30','S2024030',5,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三31','S2024031',1,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三32','S2024032',2,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三33','S2024033',3,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三34','S2024034',4,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三35','S2024035',5,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三36','S2024036',1,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三37','S2024037',2,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三38','S2024038',3,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三39','S2024039',4,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三40','S2024040',5,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三41','S2024041',1,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三42','S2024042',2,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三43','S2024043',3,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三44','S2024044',4,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三45','S2024045',5,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三46','S2024046',1,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三47','S2024047',2,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三48','S2024048',3,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三49','S2024049',4,1);
INSERT INTO student (name, student_no, class_id, status) VALUES ('张三50','S2024050',5,1);

-- 考勤规则数据
INSERT INTO attendance_rule (scenario, work_days, work_start, work_end, late_grace_min, early_grace_min, enabled) VALUES 
('SCHOOL', 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', '08:00:00', '17:00:00', 10, 5, 1);

-- 考勤记录数据 (200条)
INSERT INTO attendance_record (student_id, student_name, student_no, class_id, class_name, device_id, device_name, attendance_time, attendance_type, check_type, photo_url, status, remark) VALUES
-- 第1-20条：正常考勤记录
(1, '张三1', 'S2024001', 101, '高一(1)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 DAY) + INTERVAL 8 HOUR, 'in', 'card', '/photos/001_in.jpg', 'normal', '正常签到'),
(1, '张三1', 'S2024001', 101, '高一(1)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 DAY) + INTERVAL 17 HOUR, 'out', 'card', '/photos/001_out.jpg', 'normal', '正常签退'),
(2, '张三2', 'S2024002', 102, '高一(2)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 29 DAY) + INTERVAL 8 HOUR + INTERVAL 5 MINUTE, 'in', 'face', '/photos/002_in.jpg', 'normal', '正常签到'),
(2, '张三2', 'S2024002', 102, '高一(2)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 29 DAY) + INTERVAL 17 HOUR + INTERVAL 10 MINUTE, 'out', 'face', '/photos/002_out.jpg', 'normal', '正常签退'),
(3, '张三3', 'S2024003', 103, '高一(3)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 28 DAY) + INTERVAL 8 HOUR + INTERVAL 15 MINUTE, 'in', 'card', '/photos/003_in.jpg', 'late', '迟到15分钟'),
(3, '张三3', 'S2024003', 103, '高一(3)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 28 DAY) + INTERVAL 16 HOUR + INTERVAL 45 MINUTE, 'out', 'card', '/photos/003_out.jpg', 'early', '早退15分钟'),
(4, '张三4', 'S2024004', 104, '高一(4)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 27 DAY) + INTERVAL 7 HOUR + INTERVAL 55 MINUTE, 'in', 'face', '/photos/004_in.jpg', 'normal', '正常签到'),
(4, '张三4', 'S2024004', 104, '高一(4)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 27 DAY) + INTERVAL 17 HOUR + INTERVAL 5 MINUTE, 'out', 'face', '/photos/004_out.jpg', 'normal', '正常签退'),
(5, '张三5', 'S2024005', 105, '高一(5)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 26 DAY) + INTERVAL 8 HOUR + INTERVAL 20 MINUTE, 'in', 'card', '/photos/005_in.jpg', 'late', '迟到20分钟'),
(5, '张三5', 'S2024005', 105, '高一(5)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 26 DAY) + INTERVAL 17 HOUR, 'out', 'card', '/photos/005_out.jpg', 'normal', '正常签退'),
(6, '张三6', 'S2024006', 101, '高一(1)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 25 DAY) + INTERVAL 8 HOUR + INTERVAL 2 MINUTE, 'in', 'face', '/photos/006_in.jpg', 'normal', '正常签到'),
(6, '张三6', 'S2024006', 101, '高一(1)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 25 DAY) + INTERVAL 16 HOUR + INTERVAL 50 MINUTE, 'out', 'face', '/photos/006_out.jpg', 'early', '早退10分钟'),
(7, '张三7', 'S2024007', 102, '高一(2)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 DAY) + INTERVAL 8 HOUR + INTERVAL 8 MINUTE, 'in', 'card', '/photos/007_in.jpg', 'normal', '正常签到'),
(7, '张三7', 'S2024007', 102, '高一(2)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 DAY) + INTERVAL 17 HOUR + INTERVAL 3 MINUTE, 'out', 'card', '/photos/007_out.jpg', 'normal', '正常签退'),
(8, '张三8', 'S2024008', 103, '高一(3)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 23 DAY) + INTERVAL 8 HOUR + INTERVAL 25 MINUTE, 'in', 'face', '/photos/008_in.jpg', 'late', '迟到25分钟'),
(8, '张三8', 'S2024008', 103, '高一(3)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 23 DAY) + INTERVAL 17 HOUR + INTERVAL 8 MINUTE, 'out', 'face', '/photos/008_out.jpg', 'normal', '正常签退'),
(9, '张三9', 'S2024009', 104, '高一(4)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 22 DAY) + INTERVAL 7 HOUR + INTERVAL 58 MINUTE, 'in', 'card', '/photos/009_in.jpg', 'normal', '正常签到'),
(9, '张三9', 'S2024009', 104, '高一(4)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 22 DAY) + INTERVAL 16 HOUR + INTERVAL 55 MINUTE, 'out', 'card', '/photos/009_out.jpg', 'early', '早退5分钟'),
(10, '张三10', 'S2024010', 105, '高一(5)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 21 DAY) + INTERVAL 8 HOUR + INTERVAL 12 MINUTE, 'in', 'face', '/photos/010_in.jpg', 'late', '迟到12分钟'),
(10, '张三10', 'S2024010', 105, '高一(5)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 21 DAY) + INTERVAL 17 HOUR + INTERVAL 2 MINUTE, 'out', 'face', '/photos/010_out.jpg', 'normal', '正常签退'),

-- 第21-50条：更多考勤记录
(11, '张三11', 'S2024011', 101, '高一(1)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY) + INTERVAL 8 HOUR + INTERVAL 3 MINUTE, 'in', 'card', '/photos/011_in.jpg', 'normal', '正常签到'),
(11, '张三11', 'S2024011', 101, '高一(1)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 DAY) + INTERVAL 17 HOUR + INTERVAL 5 MINUTE, 'out', 'card', '/photos/011_out.jpg', 'normal', '正常签退'),
(12, '张三12', 'S2024012', 102, '高一(2)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 19 DAY) + INTERVAL 8 HOUR + INTERVAL 18 MINUTE, 'in', 'face', '/photos/012_in.jpg', 'late', '迟到18分钟'),
(12, '张三12', 'S2024012', 102, '高一(2)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 19 DAY) + INTERVAL 16 HOUR + INTERVAL 48 MINUTE, 'out', 'face', '/photos/012_out.jpg', 'early', '早退12分钟'),
(13, '张三13', 'S2024013', 103, '高一(3)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 18 DAY) + INTERVAL 7 HOUR + INTERVAL 56 MINUTE, 'in', 'card', '/photos/013_in.jpg', 'normal', '正常签到'),
(13, '张三13', 'S2024013', 103, '高一(3)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 18 DAY) + INTERVAL 17 HOUR + INTERVAL 7 MINUTE, 'out', 'card', '/photos/013_out.jpg', 'normal', '正常签退'),
(14, '张三14', 'S2024014', 104, '高一(4)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY) + INTERVAL 8 HOUR + INTERVAL 22 MINUTE, 'in', 'face', '/photos/014_in.jpg', 'late', '迟到22分钟'),
(14, '张三14', 'S2024014', 104, '高一(4)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 17 DAY) + INTERVAL 17 HOUR + INTERVAL 1 MINUTE, 'out', 'face', '/photos/014_out.jpg', 'normal', '正常签退'),
(15, '张三15', 'S2024015', 105, '高一(5)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 16 DAY) + INTERVAL 8 HOUR + INTERVAL 6 MINUTE, 'in', 'card', '/photos/015_in.jpg', 'normal', '正常签到'),
(15, '张三15', 'S2024015', 105, '高一(5)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 16 DAY) + INTERVAL 16 HOUR + INTERVAL 52 MINUTE, 'out', 'card', '/photos/015_out.jpg', 'early', '早退8分钟'),
(16, '张三16', 'S2024016', 101, '高一(1)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 15 DAY) + INTERVAL 8 HOUR + INTERVAL 1 MINUTE, 'in', 'face', '/photos/016_in.jpg', 'normal', '正常签到'),
(16, '张三16', 'S2024016', 101, '高一(1)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 15 DAY) + INTERVAL 17 HOUR + INTERVAL 4 MINUTE, 'out', 'face', '/photos/016_out.jpg', 'normal', '正常签退'),
(17, '张三17', 'S2024017', 102, '高一(2)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 14 DAY) + INTERVAL 8 HOUR + INTERVAL 28 MINUTE, 'in', 'card', '/photos/017_in.jpg', 'late', '迟到28分钟'),
(17, '张三17', 'S2024017', 102, '高一(2)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 14 DAY) + INTERVAL 17 HOUR + INTERVAL 6 MINUTE, 'out', 'card', '/photos/017_out.jpg', 'normal', '正常签退'),
(18, '张三18', 'S2024018', 103, '高一(3)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 13 DAY) + INTERVAL 7 HOUR + INTERVAL 59 MINUTE, 'in', 'face', '/photos/018_in.jpg', 'normal', '正常签到'),
(18, '张三18', 'S2024018', 103, '高一(3)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 13 DAY) + INTERVAL 16 HOUR + INTERVAL 47 MINUTE, 'out', 'face', '/photos/018_out.jpg', 'early', '早退13分钟'),
(19, '张三19', 'S2024019', 104, '高一(4)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 12 DAY) + INTERVAL 8 HOUR + INTERVAL 14 MINUTE, 'in', 'card', '/photos/019_in.jpg', 'late', '迟到14分钟'),
(19, '张三19', 'S2024019', 104, '高一(4)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 12 DAY) + INTERVAL 17 HOUR + INTERVAL 3 MINUTE, 'out', 'card', '/photos/019_out.jpg', 'normal', '正常签退'),
(20, '张三20', 'S2024020', 105, '高一(5)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 11 DAY) + INTERVAL 8 HOUR + INTERVAL 4 MINUTE, 'in', 'face', '/photos/020_in.jpg', 'normal', '正常签到'),
(20, '张三20', 'S2024020', 105, '高一(5)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 11 DAY) + INTERVAL 17 HOUR + INTERVAL 8 MINUTE, 'out', 'face', '/photos/020_out.jpg', 'normal', '正常签退'),

-- 第51-100条：继续添加考勤记录
(21, '张三21', 'S2024021', 101, '高一(1)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY) + INTERVAL 8 HOUR + INTERVAL 16 MINUTE, 'in', 'card', '/photos/021_in.jpg', 'late', '迟到16分钟'),
(21, '张三21', 'S2024021', 101, '高一(1)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY) + INTERVAL 16 HOUR + INTERVAL 54 MINUTE, 'out', 'card', '/photos/021_out.jpg', 'early', '早退6分钟'),
(22, '张三22', 'S2024022', 102, '高一(2)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 9 DAY) + INTERVAL 7 HOUR + INTERVAL 57 MINUTE, 'in', 'face', '/photos/022_in.jpg', 'normal', '正常签到'),
(22, '张三22', 'S2024022', 102, '高一(2)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 9 DAY) + INTERVAL 17 HOUR + INTERVAL 2 MINUTE, 'out', 'face', '/photos/022_out.jpg', 'normal', '正常签退'),
(23, '张三23', 'S2024023', 103, '高一(3)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 8 DAY) + INTERVAL 8 HOUR + INTERVAL 24 MINUTE, 'in', 'card', '/photos/023_in.jpg', 'late', '迟到24分钟'),
(23, '张三23', 'S2024023', 103, '高一(3)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 8 DAY) + INTERVAL 17 HOUR + INTERVAL 5 MINUTE, 'out', 'card', '/photos/023_out.jpg', 'normal', '正常签退'),
(24, '张三24', 'S2024024', 104, '高一(4)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) + INTERVAL 8 HOUR + INTERVAL 7 MINUTE, 'in', 'face', '/photos/024_in.jpg', 'normal', '正常签到'),
(24, '张三24', 'S2024024', 104, '高一(4)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) + INTERVAL 16 HOUR + INTERVAL 49 MINUTE, 'out', 'face', '/photos/024_out.jpg', 'early', '早退11分钟'),
(25, '张三25', 'S2024025', 105, '高一(5)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 DAY) + INTERVAL 8 HOUR + INTERVAL 19 MINUTE, 'in', 'card', '/photos/025_in.jpg', 'late', '迟到19分钟'),
(25, '张三25', 'S2024025', 105, '高一(5)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 DAY) + INTERVAL 17 HOUR + INTERVAL 1 MINUTE, 'out', 'card', '/photos/025_out.jpg', 'normal', '正常签退'),
(26, '张三26', 'S2024026', 101, '高一(1)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY) + INTERVAL 8 HOUR + INTERVAL 2 MINUTE, 'in', 'face', '/photos/026_in.jpg', 'normal', '正常签到'),
(26, '张三26', 'S2024026', 101, '高一(1)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY) + INTERVAL 17 HOUR + INTERVAL 7 MINUTE, 'out', 'face', '/photos/026_out.jpg', 'normal', '正常签退'),
(27, '张三27', 'S2024027', 102, '高一(2)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY) + INTERVAL 8 HOUR + INTERVAL 26 MINUTE, 'in', 'card', '/photos/027_in.jpg', 'late', '迟到26分钟'),
(27, '张三27', 'S2024027', 102, '高一(2)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 4 DAY) + INTERVAL 17 HOUR + INTERVAL 4 MINUTE, 'out', 'card', '/photos/027_out.jpg', 'normal', '正常签退'),
(28, '张三28', 'S2024028', 103, '高一(3)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) + INTERVAL 7 HOUR + INTERVAL 58 MINUTE, 'in', 'face', '/photos/028_in.jpg', 'normal', '正常签到'),
(28, '张三28', 'S2024028', 103, '高一(3)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY) + INTERVAL 16 HOUR + INTERVAL 51 MINUTE, 'out', 'face', '/photos/028_out.jpg', 'early', '早退9分钟'),
(29, '张三29', 'S2024029', 104, '高一(4)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) + INTERVAL 8 HOUR + INTERVAL 11 MINUTE, 'in', 'card', '/photos/029_in.jpg', 'late', '迟到11分钟'),
(29, '张三29', 'S2024029', 104, '高一(4)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 DAY) + INTERVAL 17 HOUR + INTERVAL 6 MINUTE, 'out', 'card', '/photos/029_out.jpg', 'normal', '正常签退'),
(30, '张三30', 'S2024030', 105, '高一(5)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) + INTERVAL 8 HOUR + INTERVAL 5 MINUTE, 'in', 'face', '/photos/030_in.jpg', 'normal', '正常签到'),
(30, '张三30', 'S2024030', 105, '高一(5)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) + INTERVAL 17 HOUR + INTERVAL 3 MINUTE, 'out', 'face', '/photos/030_out.jpg', 'normal', '正常签退'),

-- 第101-150条：当天和近期考勤记录
(1, '张三1', 'S2024001', 101, '高一(1)班', 1001, '教学楼A入口', CURRENT_TIMESTAMP - INTERVAL 9 HOUR, 'in', 'card', '/photos/101_in.jpg', 'normal', '正常签到'),
(1, '张三1', 'S2024001', 101, '高一(1)班', 1002, '教学楼A出口', CURRENT_TIMESTAMP - INTERVAL 1 MINUTE, 'out', 'card', '/photos/101_out.jpg', 'normal', '正常签退'),
(2, '张三2', 'S2024002', 102, '高一(2)班', 1001, '教学楼A入口', CURRENT_TIMESTAMP - INTERVAL 8 HOUR - INTERVAL 55 MINUTE, 'in', 'face', '/photos/102_in.jpg', 'normal', '正常签到'),
(2, '张三2', 'S2024002', 102, '高一(2)班', 1002, '教学楼A出口', CURRENT_TIMESTAMP - INTERVAL 5 MINUTE, 'out', 'face', '/photos/102_out.jpg', 'normal', '正常签退'),
(3, '张三3', 'S2024003', 103, '高一(3)班', 1001, '教学楼A入口', CURRENT_TIMESTAMP - INTERVAL 8 HOUR - INTERVAL 45 MINUTE, 'in', 'card', '/photos/103_in.jpg', 'late', '迟到15分钟'),
(3, '张三3', 'S2024003', 103, '高一(3)班', 1002, '教学楼A出口', CURRENT_TIMESTAMP - INTERVAL 15 MINUTE, 'out', 'card', '/photos/103_out.jpg', 'early', '早退15分钟'),
(4, '张三4', 'S2024004', 104, '高一(4)班', 1001, '教学楼A入口', CURRENT_TIMESTAMP - INTERVAL 9 HOUR - INTERVAL 5 MINUTE, 'in', 'face', '/photos/104_in.jpg', 'normal', '正常签到'),
(4, '张三4', 'S2024004', 104, '高一(4)班', 1002, '教学楼A出口', CURRENT_TIMESTAMP - INTERVAL 3 MINUTE, 'out', 'face', '/photos/104_out.jpg', 'normal', '正常签退'),
(5, '张三5', 'S2024005', 105, '高一(5)班', 1001, '教学楼A入口', CURRENT_TIMESTAMP - INTERVAL 8 HOUR - INTERVAL 40 MINUTE, 'in', 'card', '/photos/105_in.jpg', 'late', '迟到20分钟'),
(5, '张三5', 'S2024005', 105, '高一(5)班', 1002, '教学楼A出口', CURRENT_TIMESTAMP - INTERVAL 2 MINUTE, 'out', 'card', '/photos/105_out.jpg', 'normal', '正常签退'),
(6, '张三6', 'S2024006', 101, '高一(1)班', 1001, '教学楼A入口', CURRENT_TIMESTAMP - INTERVAL 8 HOUR - INTERVAL 58 MINUTE, 'in', 'face', '/photos/106_in.jpg', 'normal', '正常签到'),
(6, '张三6', 'S2024006', 101, '高一(1)班', 1002, '教学楼A出口', CURRENT_TIMESTAMP - INTERVAL 10 MINUTE, 'out', 'face', '/photos/106_out.jpg', 'early', '早退10分钟'),
(7, '张三7', 'S2024007', 102, '高一(2)班', 1001, '教学楼A入口', CURRENT_TIMESTAMP - INTERVAL 8 HOUR - INTERVAL 52 MINUTE, 'in', 'card', '/photos/107_in.jpg', 'normal', '正常签到'),
(7, '张三7', 'S2024007', 102, '高一(2)班', 1002, '教学楼A出口', CURRENT_TIMESTAMP - INTERVAL 7 MINUTE, 'out', 'card', '/photos/107_out.jpg', 'normal', '正常签退'),
(8, '张三8', 'S2024008', 103, '高一(3)班', 1001, '教学楼A入口', CURRENT_TIMESTAMP - INTERVAL 8 HOUR - INTERVAL 35 MINUTE, 'in', 'face', '/photos/108_in.jpg', 'late', '迟到25分钟'),
(8, '张三8', 'S2024008', 103, '高一(3)班', 1002, '教学楼A出口', CURRENT_TIMESTAMP - INTERVAL 8 MINUTE, 'out', 'face', '/photos/108_out.jpg', 'normal', '正常签退'),
(9, '张三9', 'S2024009', 104, '高一(4)班', 1001, '教学楼A入口', CURRENT_TIMESTAMP - INTERVAL 9 HOUR - INTERVAL 2 MINUTE, 'in', 'card', '/photos/109_in.jpg', 'normal', '正常签到'),
(9, '张三9', 'S2024009', 104, '高一(4)班', 1002, '教学楼A出口', CURRENT_TIMESTAMP - INTERVAL 5 MINUTE, 'out', 'card', '/photos/109_out.jpg', 'early', '早退5分钟'),
(10, '张三10', 'S2024010', 105, '高一(5)班', 1001, '教学楼A入口', CURRENT_TIMESTAMP - INTERVAL 8 HOUR - INTERVAL 48 MINUTE, 'in', 'face', '/photos/110_in.jpg', 'late', '迟到12分钟'),
(10, '张三10', 'S2024010', 105, '高一(5)班', 1002, '教学楼A出口', CURRENT_TIMESTAMP - INTERVAL 4 MINUTE, 'out', 'face', '/photos/110_out.jpg', 'normal', '正常签退'),

-- 第151-200条：更多多样化考勤记录
(31, '张三31', 'S2024031', 101, '高一(1)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 35 DAY) + INTERVAL 8 HOUR + INTERVAL 9 MINUTE, 'in', 'card', '/photos/131_in.jpg', 'normal', '正常签到'),
(31, '张三31', 'S2024031', 101, '高一(1)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 35 DAY) + INTERVAL 17 HOUR + INTERVAL 2 MINUTE, 'out', 'card', '/photos/131_out.jpg', 'normal', '正常签退'),
(32, '张三32', 'S2024032', 102, '高一(2)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 34 DAY) + INTERVAL 8 HOUR + INTERVAL 17 MINUTE, 'in', 'face', '/photos/132_in.jpg', 'late', '迟到17分钟'),
(32, '张三32', 'S2024032', 102, '高一(2)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 34 DAY) + INTERVAL 16 HOUR + INTERVAL 53 MINUTE, 'out', 'face', '/photos/132_out.jpg', 'early', '早退7分钟'),
(33, '张三33', 'S2024033', 103, '高一(3)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 33 DAY) + INTERVAL 7 HOUR + INTERVAL 54 MINUTE, 'in', 'card', '/photos/133_in.jpg', 'normal', '正常签到'),
(33, '张三33', 'S2024033', 103, '高一(3)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 33 DAY) + INTERVAL 17 HOUR + INTERVAL 9 MINUTE, 'out', 'card', '/photos/133_out.jpg', 'normal', '正常签退'),
(34, '张三34', 'S2024034', 104, '高一(4)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 32 DAY) + INTERVAL 8 HOUR + INTERVAL 21 MINUTE, 'in', 'face', '/photos/134_in.jpg', 'late', '迟到21分钟'),
(34, '张三34', 'S2024034', 104, '高一(4)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 32 DAY) + INTERVAL 17 HOUR + INTERVAL 4 MINUTE, 'out', 'face', '/photos/134_out.jpg', 'normal', '正常签退'),
(35, '张三35', 'S2024035', 105, '高一(5)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 31 DAY) + INTERVAL 8 HOUR + INTERVAL 8 MINUTE, 'in', 'card', '/photos/135_in.jpg', 'normal', '正常签到'),
(35, '张三35', 'S2024035', 105, '高一(5)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 31 DAY) + INTERVAL 16 HOUR + INTERVAL 56 MINUTE, 'out', 'card', '/photos/135_out.jpg', 'early', '早退4分钟'),
(36, '张三36', 'S2024036', 101, '高一(1)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 40 DAY) + INTERVAL 8 HOUR + INTERVAL 13 MINUTE, 'in', 'face', '/photos/136_in.jpg', 'late', '迟到13分钟'),
(36, '张三36', 'S2024036', 101, '高一(1)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 40 DAY) + INTERVAL 17 HOUR + INTERVAL 6 MINUTE, 'out', 'face', '/photos/136_out.jpg', 'normal', '正常签退'),
(37, '张三37', 'S2024037', 102, '高一(2)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 39 DAY) + INTERVAL 7 HOUR + INTERVAL 59 MINUTE, 'in', 'card', '/photos/137_in.jpg', 'normal', '正常签到'),
(37, '张三37', 'S2024037', 102, '高一(2)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 39 DAY) + INTERVAL 17 HOUR + INTERVAL 1 MINUTE, 'out', 'card', '/photos/137_out.jpg', 'normal', '正常签退'),
(38, '张三38', 'S2024038', 103, '高一(3)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 38 DAY) + INTERVAL 8 HOUR + INTERVAL 23 MINUTE, 'in', 'face', '/photos/138_in.jpg', 'late', '迟到23分钟'),
(38, '张三38', 'S2024038', 103, '高一(3)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 38 DAY) + INTERVAL 16 HOUR + INTERVAL 50 MINUTE, 'out', 'face', '/photos/138_out.jpg', 'early', '早退10分钟'),
(39, '张三39', 'S2024039', 104, '高一(4)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 37 DAY) + INTERVAL 8 HOUR + INTERVAL 4 MINUTE, 'in', 'card', '/photos/139_in.jpg', 'normal', '正常签到'),
(39, '张三39', 'S2024039', 104, '高一(4)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 37 DAY) + INTERVAL 17 HOUR + INTERVAL 8 MINUTE, 'out', 'card', '/photos/139_out.jpg', 'normal', '正常签退'),
(40, '张三40', 'S2024040', 105, '高一(5)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 36 DAY) + INTERVAL 8 HOUR + INTERVAL 27 MINUTE, 'in', 'face', '/photos/140_in.jpg', 'late', '迟到27分钟'),
(40, '张三40', 'S2024040', 105, '高一(5)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 36 DAY) + INTERVAL 17 HOUR + INTERVAL 3 MINUTE, 'out', 'face', '/photos/140_out.jpg', 'normal', '正常签退'),
(41, '张三41', 'S2024041', 101, '高一(1)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 45 DAY) + INTERVAL 8 HOUR + INTERVAL 10 MINUTE, 'in', 'card', '/photos/141_in.jpg', 'late', '迟到10分钟'),
(41, '张三41', 'S2024041', 101, '高一(1)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 45 DAY) + INTERVAL 16 HOUR + INTERVAL 57 MINUTE, 'out', 'card', '/photos/141_out.jpg', 'early', '早退3分钟'),
(42, '张三42', 'S2024042', 102, '高一(2)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 44 DAY) + INTERVAL 7 HOUR + INTERVAL 56 MINUTE, 'in', 'face', '/photos/142_in.jpg', 'normal', '正常签到'),
(42, '张三42', 'S2024042', 102, '高一(2)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 44 DAY) + INTERVAL 17 HOUR + INTERVAL 5 MINUTE, 'out', 'face', '/photos/142_out.jpg', 'normal', '正常签退'),
(43, '张三43', 'S2024043', 103, '高一(3)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 43 DAY) + INTERVAL 8 HOUR + INTERVAL 29 MINUTE, 'in', 'card', '/photos/143_in.jpg', 'late', '迟到29分钟'),
(43, '张三43', 'S2024043', 103, '高一(3)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 43 DAY) + INTERVAL 17 HOUR + INTERVAL 7 MINUTE, 'out', 'card', '/photos/143_out.jpg', 'normal', '正常签退'),
(44, '张三44', 'S2024044', 104, '高一(4)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 42 DAY) + INTERVAL 8 HOUR + INTERVAL 6 MINUTE, 'in', 'face', '/photos/144_in.jpg', 'normal', '正常签到'),
(44, '张三44', 'S2024044', 104, '高一(4)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 42 DAY) + INTERVAL 16 HOUR + INTERVAL 52 MINUTE, 'out', 'face', '/photos/144_out.jpg', 'early', '早退8分钟'),
(45, '张三45', 'S2024045', 105, '高一(5)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 41 DAY) + INTERVAL 8 HOUR + INTERVAL 18 MINUTE, 'in', 'card', '/photos/145_in.jpg', 'late', '迟到18分钟'),
(45, '张三45', 'S2024045', 105, '高一(5)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 41 DAY) + INTERVAL 17 HOUR + INTERVAL 2 MINUTE, 'out', 'card', '/photos/145_out.jpg', 'normal', '正常签退'),
(46, '张三46', 'S2024046', 101, '高一(1)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 50 DAY) + INTERVAL 8 HOUR + INTERVAL 1 MINUTE, 'in', 'face', '/photos/146_in.jpg', 'normal', '正常签到'),
(46, '张三46', 'S2024046', 101, '高一(1)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 50 DAY) + INTERVAL 17 HOUR + INTERVAL 9 MINUTE, 'out', 'face', '/photos/146_out.jpg', 'normal', '正常签退'),
(47, '张三47', 'S2024047', 102, '高一(2)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 49 DAY) + INTERVAL 8 HOUR + INTERVAL 25 MINUTE, 'in', 'card', '/photos/147_in.jpg', 'late', '迟到25分钟'),
(47, '张三47', 'S2024047', 102, '高一(2)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 49 DAY) + INTERVAL 16 HOUR + INTERVAL 48 MINUTE, 'out', 'card', '/photos/147_out.jpg', 'early', '早退12分钟'),
(48, '张三48', 'S2024048', 103, '高一(3)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 48 DAY) + INTERVAL 7 HOUR + INTERVAL 58 MINUTE, 'in', 'face', '/photos/148_in.jpg', 'normal', '正常签到'),
(48, '张三48', 'S2024048', 103, '高一(3)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 48 DAY) + INTERVAL 17 HOUR + INTERVAL 4 MINUTE, 'out', 'face', '/photos/148_out.jpg', 'normal', '正常签退'),
(49, '张三49', 'S2024049', 104, '高一(4)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 47 DAY) + INTERVAL 8 HOUR + INTERVAL 15 MINUTE, 'in', 'card', '/photos/149_in.jpg', 'late', '迟到15分钟'),
(49, '张三49', 'S2024049', 104, '高一(4)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 47 DAY) + INTERVAL 17 HOUR + INTERVAL 6 MINUTE, 'out', 'card', '/photos/149_out.jpg', 'normal', '正常签退'),
(50, '张三50', 'S2024050', 105, '高一(5)班', 1001, '教学楼A入口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 46 DAY) + INTERVAL 8 HOUR + INTERVAL 3 MINUTE, 'in', 'face', '/photos/150_in.jpg', 'normal', '正常签到'),
(50, '张三50', 'S2024050', 105, '高一(5)班', 1002, '教学楼A出口', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 46 DAY) + INTERVAL 16 HOUR + INTERVAL 55 MINUTE, 'out', 'face', '/photos/150_out.jpg', 'early', '早退5分钟');


-- 校园卡业务域示例数据（统一到 MySQL data.sql）
-- 学校
INSERT INTO school (name, code, address, phone, email, principal, description, status, create_time, update_time) VALUES
('北京第一中学', 'BJ001', '北京市朝阳区学院路1号', '010-12345678', 'admin@bj001.edu.cn', '张校长', '北京市重点中学', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('上海实验中学', 'SH002', '上海市浦东新区科技路88号', '021-87654321', 'info@sh002.edu.cn', '李校长', '上海市示范性高中', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 年级
INSERT INTO grade (name, year, status, school_id, create_time, update_time) VALUES
('高一', 2024, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('高二', 2023, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('高三', 2022, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('高一', 2024, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('高二', 2023, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 班级
INSERT INTO school_class (name, grade_id, school_id, status, create_time, update_time) VALUES
('高一(1)班', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('高一(2)班', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('高一(3)班', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('高二(1)班', 2, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('高二(2)班', 2, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('高三(1)班', 3, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('高一(1)班', 4, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('高一(2)班', 4, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 教师
INSERT INTO teacher (name, teacher_no, department, phone, status, school_id, create_time, update_time) VALUES
('王老师', 'T001', '数学组', '13800138001', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('李老师', 'T002', '语文组', '13800138002', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('张老师', 'T003', '英语组', '13800138003', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('刘老师', 'T004', '物理组', '13800138004', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('陈老师', 'T005', '化学组', '13800138005', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('赵老师', 'T006', '数学组', '13800138006', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('孙老师', 'T007', '语文组', '13800138007', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 卡种
INSERT INTO card_type (name, description) VALUES
('学生卡', '学生专用校园卡'),
('教师卡', '教师专用校园卡'),
('访客卡', '临时访客卡'),
('管理卡', '管理员专用卡');

-- 卡片
INSERT INTO card (card_no, type_id, holder_type, holder_id, status, balance, created_at) VALUES
('C20240101001', 1, 'STUDENT', '1', 'ACTIVE', 100.00, CURRENT_TIMESTAMP),
('C20240101002', 1, 'STUDENT', '2', 'ACTIVE', 150.50, CURRENT_TIMESTAMP),
('C20240101003', 1, 'STUDENT', '3', 'ACTIVE', 80.25, CURRENT_TIMESTAMP),
('C20240101004', 2, 'TEACHER', '1', 'ACTIVE', 200.00, CURRENT_TIMESTAMP),
('C20240101005', 2, 'TEACHER', '2', 'ACTIVE', 180.75, CURRENT_TIMESTAMP),
('C20240101006', 3, 'STAFF', '1', 'ACTIVE', 50.00, CURRENT_TIMESTAMP);

-- 卡交易（MySQL 时间函数）
INSERT INTO card_tx (card_no, type, amount, balance_after, merchant, occurred_at, note) VALUES
('C20240101001', 'RECHARGE', 100.00, 100.00, '充值机001', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY), '开卡充值'),
('C20240101001', 'CONSUME', -15.50, 84.50, '食堂A', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 DAY), '午餐消费'),
('C20240101001', 'RECHARGE', 30.00, 114.50, '充值机002', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 3 DAY), '自助充值'),
('C20240101001', 'CONSUME', -14.50, 100.00, '食堂B', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY), '晚餐消费'),
('C20240101002', 'RECHARGE', 150.50, 150.50, '充值机001', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 6 DAY), '开卡充值'),
('C20240101004', 'RECHARGE', 200.00, 200.00, '财务处', DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY), '教师卡充值');

-- 人脸信息示例数据（用于前端联调展示）
INSERT INTO face_info (person_type, person_id, photo_base64, features, quality_score, created_at, updated_at) VALUES
('STUDENT', 'S2024001', 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAAB', '', 95, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO face_info (person_type, person_id, photo_base64, features, quality_score, created_at, updated_at) VALUES
('TEACHER', 'T2024001', 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAAB', '', 90, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO face_info (person_type, person_id, photo_base64, features, quality_score, created_at, updated_at) VALUES
('STAFF', 'E2024001', 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAAB', '', 85, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- 学科样例数据（学校1）
INSERT INTO subject (name, status, school_id, create_time, update_time)
VALUES ('语文', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('数学', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('英语', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('物理', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('化学', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 任课教师分配示例（学校1，高一各班）
INSERT INTO class_subject_teacher (class_id, subject_id, teacher_id, create_time, update_time) VALUES
(1, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- 高一(1)班 语文 -> 李老师
(1, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- 高一(1)班 数学 -> 王老师
(1, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- 高一(1)班 英语 -> 张老师
(2, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- 高一(2)班 语文 -> 李老师
(2, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP), -- 高一(2)班 数学 -> 王老师
(2, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); -- 高一(2)班 英语 -> 张老师

-- 家长样例数据（用于前端联调展示）
INSERT INTO parent (name, phone, relation, status, create_time) VALUES
('张一','13800000001','FATHER',1,CURRENT_TIMESTAMP),
('李二','13800000002','MOTHER',1,CURRENT_TIMESTAMP),
('王三','13800000003','GUARDIAN',1,CURRENT_TIMESTAMP),
('赵四','13800000004','OTHER',1,CURRENT_TIMESTAMP),
('钱五','13800000005','FATHER',1,CURRENT_TIMESTAMP),
('孙六','13800000006','MOTHER',1,CURRENT_TIMESTAMP),
('周七','13800000007','GUARDIAN',1,CURRENT_TIMESTAMP),
('吴八','13800000008','OTHER',1,CURRENT_TIMESTAMP),
('郑九','13800000009','FATHER',1,CURRENT_TIMESTAMP),
('冯十','13800000010','MOTHER',1,CURRENT_TIMESTAMP);