-- Lite initial data for E2E split & paging
SET NAMES utf8mb4;

-- 基础学校与年级
INSERT INTO school (name, code, address, phone, status, create_time, update_time)
VALUES ('测试学校', 'TS001', '北京市海淀区知春路1号', '010-00000000', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO grade (name, year, status, school_id, create_time, update_time)
VALUES ('高一', 2024, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 班级
INSERT INTO school_class (name, grade_id, school_id, status, create_time, update_time)
VALUES ('高一(1)班', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('高一(2)班', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 学生样例（用于班级拆分与分页）
INSERT INTO student (name, student_no, class_id, status)
VALUES ('张一','S0001',1,1),
       ('张二','S0002',1,1),
       ('张三','S0003',1,1),
       ('李四','S0004',2,1);

-- 学科（绑定到学校1）
INSERT INTO subject (name, status, school_id, create_time, update_time)
VALUES ('语文', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('数学', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('英语', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 教师（Lite）
INSERT INTO teacher (name, teacher_no, department, phone, status, school_id, create_time, update_time)
VALUES ('王老师', 'LT001', '数学组', '13800000001', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('李老师', 'LT002', '语文组', '13800000002', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('张老师', 'LT003', '英语组', '13800000003', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 任课教师分配（Lite）
INSERT INTO class_subject_teacher (class_id, subject_id, teacher_id, create_time, update_time)
VALUES (1, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);