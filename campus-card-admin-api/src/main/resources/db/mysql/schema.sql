-- MySQL schema for campus-card-admin-api
-- Charset & Engine
SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS student;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  phone VARCHAR(255) NULL,
  status INT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Face recognition global configuration
DROP TABLE IF EXISTS face_config;
CREATE TABLE face_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  recognition_threshold INT NOT NULL DEFAULT 75,
  recognition_mode ENUM('ONE_TO_ONE','ONE_TO_MANY') NOT NULL DEFAULT 'ONE_TO_ONE',
  liveness_enabled TINYINT NOT NULL DEFAULT 0,
  library_capacity INT NOT NULL DEFAULT 10000,
  updated_at DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(128) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(256) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  path VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status INT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role_permission (
  role_id BIGINT NOT NULL,
  perm_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role_menu (
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE student (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  student_no VARCHAR(64) NOT NULL UNIQUE,
  class_id BIGINT NULL,
  status INT NOT NULL DEFAULT 1,
  photo_path VARCHAR(255) NULL,
  archive LONGTEXT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Additional domain tables: school, grade, class, teacher, attendance, card

DROP TABLE IF EXISTS student_parent;
DROP TABLE IF EXISTS parent;
DROP TABLE IF EXISTS card_tx;
DROP TABLE IF EXISTS card;
DROP TABLE IF EXISTS card_type;
DROP TABLE IF EXISTS attendance_record;
DROP TABLE IF EXISTS school_class;
DROP TABLE IF EXISTS grade;
DROP TABLE IF EXISTS school;

CREATE TABLE school (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  code VARCHAR(32) NOT NULL UNIQUE,
  address VARCHAR(128) NULL,
  phone VARCHAR(64) NULL,
  email VARCHAR(128) NULL,
  principal VARCHAR(64) NULL,
  description VARCHAR(512) NULL,
  status INT NOT NULL DEFAULT 1,
  create_time DATETIME NULL,
  update_time DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE grade (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  year INT NULL,
  status INT NOT NULL DEFAULT 1,
  school_id BIGINT NULL,
  create_time DATETIME NULL,
  update_time DATETIME NULL,
  INDEX idx_grade_school (school_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE school_class (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  grade_id BIGINT NULL,
  school_id BIGINT NULL,
  head_teacher_id BIGINT NULL,
  status INT NOT NULL DEFAULT 1,
  create_time DATETIME NULL,
  update_time DATETIME NULL,
  INDEX idx_class_grade (grade_id),
  INDEX idx_class_school (school_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE teacher (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  teacher_no VARCHAR(64) NOT NULL UNIQUE,
  department VARCHAR(128) NULL,
  phone VARCHAR(64) NULL,
  status INT NOT NULL DEFAULT 1,
  school_id BIGINT NULL,
  create_time DATETIME NULL,
  update_time DATETIME NULL,
  INDEX idx_teacher_school (school_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE attendance_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  student_name VARCHAR(64) NOT NULL,
  student_no VARCHAR(64) NOT NULL,
  class_id BIGINT NULL,
  class_name VARCHAR(64) NULL,
  device_id BIGINT NULL,
  device_name VARCHAR(128) NULL,
  attendance_time DATETIME NOT NULL,
  attendance_type ENUM('in','out') NOT NULL,
  check_type ENUM('card','face') NOT NULL,
  photo_url VARCHAR(255) NULL,
  status ENUM('normal','late','early','absence') NOT NULL,
  remark VARCHAR(255) NULL,
  INDEX idx_attendance_student (student_id),
  INDEX idx_attendance_class (class_id),
  INDEX idx_attendance_time (attendance_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE card_type (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(256) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE card (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  card_no VARCHAR(64) NOT NULL UNIQUE,
  type_id BIGINT NOT NULL,
  holder_type ENUM('STUDENT','TEACHER','STAFF','VISITOR') NOT NULL,
  holder_id VARCHAR(64) NOT NULL,
  status ENUM('ACTIVE','LOST','FROZEN','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
  balance DECIMAL(10,2) NOT NULL DEFAULT 0,
  created_at DATETIME NULL,
  expire_at DATETIME NULL,
  INDEX idx_card_type (type_id),
  INDEX idx_card_holder (holder_type, holder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE card_tx (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  card_no VARCHAR(64) NOT NULL,
  type ENUM('CONSUME','RECHARGE','REFUND') NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  balance_after DECIMAL(10,2) NOT NULL,
  merchant VARCHAR(128) NULL,
  occurred_at DATETIME NOT NULL,
  note VARCHAR(255) NULL,
  INDEX idx_tx_card (card_no),
  INDEX idx_tx_type (type),
  INDEX idx_tx_time (occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE parent (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  phone VARCHAR(64) NULL,
  relation ENUM('FATHER','MOTHER','GUARDIAN','OTHER') NULL,
  status INT NOT NULL DEFAULT 1,
  create_time DATETIME Default CURRENT_TIMESTAMP,
  update_time DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE student_parent (
  student_id BIGINT NOT NULL,
  parent_id BIGINT NOT NULL,
  relation VARCHAR(32) NULL,
  PRIMARY KEY (student_id, parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS attendance_rule;
CREATE TABLE attendance_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  scenario ENUM('SCHOOL','ENTERPRISE') NOT NULL DEFAULT 'SCHOOL',
  work_days VARCHAR(64) NOT NULL,
  work_start TIME NOT NULL,
  work_end TIME NOT NULL,
  late_grace_min INT NOT NULL DEFAULT 5,
  early_grace_min INT NOT NULL DEFAULT 5,
  enabled TINYINT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Extensions for school management
CREATE TABLE campus (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  school_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(32) NOT NULL,
  address VARCHAR(128) NULL,
  status INT NOT NULL DEFAULT 1,
  create_time DATETIME NULL,
  update_time DATETIME NULL,
  INDEX idx_campus_school (school_id),
  UNIQUE KEY uk_campus_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE school_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  school_id BIGINT NOT NULL,
  cfg_key VARCHAR(64) NOT NULL,
  cfg_value VARCHAR(256) NULL,
  UNIQUE KEY uk_school_key (school_id, cfg_key),
  INDEX idx_cfg_school (school_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE semester (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  school_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  code VARCHAR(32) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  current TINYINT NOT NULL DEFAULT 0,
  status INT NOT NULL DEFAULT 1,
  create_time DATETIME NULL,
  update_time DATETIME NULL,
  INDEX idx_semester_school (school_id),
  UNIQUE KEY uk_semester_code (school_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE class_teacher (
  class_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  role ENUM('HEAD','SUBJECT') NOT NULL,
  PRIMARY KEY (class_id, teacher_id, role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE student_photo (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  photo_url VARCHAR(255) NOT NULL,
  created_at DATETIME NULL,
  INDEX idx_student_photo (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS student_face;
CREATE TABLE student_face (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  face_token VARCHAR(128) NULL,
  status INT NOT NULL DEFAULT 1,
  created_at DATETIME NULL,
  INDEX idx_student_face (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE school_logo (
  school_id BIGINT PRIMARY KEY,
  logo_url VARCHAR(255) NOT NULL,
  updated_at DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
DROP TABLE IF EXISTS face_info;
CREATE TABLE face_info (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  person_type VARCHAR(32) NOT NULL,
  person_id VARCHAR(64) NOT NULL,
  photo_base64 LONGTEXT NULL,
  features LONGTEXT NULL,
  quality_score INT NULL,
  created_at DATETIME NULL,
  updated_at DATETIME NULL,
  INDEX idx_face_person_type (person_type),
  INDEX idx_face_person_id (person_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
DROP TABLE IF EXISTS subject;
CREATE TABLE subject (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  status INT NOT NULL DEFAULT 1,
  school_id BIGINT NULL,
  create_time DATETIME NULL,
  update_time DATETIME NULL,
  INDEX idx_subject_school (school_id),
  UNIQUE KEY uk_subject_school_name (school_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 部门管理
DROP TABLE IF EXISTS department;
CREATE TABLE department (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  status INT NOT NULL DEFAULT 1,
  school_id BIGINT NULL,
  create_time DATETIME NULL,
  update_time DATETIME NULL,
  UNIQUE KEY uk_department_name (name),
  INDEX idx_department_school (school_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 任课教师分配（按班级-学科维度）
DROP TABLE IF EXISTS class_subject_teacher;
CREATE TABLE class_subject_teacher (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  create_time DATETIME NULL,
  update_time DATETIME NULL,
  UNIQUE KEY uk_class_subject (class_id, subject_id),
  INDEX idx_class_id (class_id),
  INDEX idx_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 新增：家长微信绑定表
DROP TABLE IF EXISTS parent_wechat;
CREATE TABLE parent_wechat (
  parent_id BIGINT PRIMARY KEY,
  openId VARCHAR(128) NOT NULL,
  unionId VARCHAR(128) NULL,
  nickname VARCHAR(64) NULL,
  avatarUrl VARCHAR(255) NULL,
  status INT NOT NULL DEFAULT 1,
  bindTime DATETIME NULL,
  UNIQUE KEY uk_wechat_open (openId),
  UNIQUE KEY uk_wechat_union (unionId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 新增：家长权限设置表
DROP TABLE IF EXISTS parent_permission;
CREATE TABLE parent_permission (
  parent_id BIGINT PRIMARY KEY,
  view_attendance TINYINT NOT NULL DEFAULT 1,
  view_consumption TINYINT NOT NULL DEFAULT 1,
  view_grades TINYINT NOT NULL DEFAULT 1,
  message_teacher TINYINT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 新增：海康设备配置表
DROP TABLE IF EXISTS hikvision_config;
CREATE TABLE hikvision_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  school_id BIGINT NOT NULL,
  school_name VARCHAR(128) NULL,
  
  -- 设备IP配置
  device_ip VARCHAR(64) NULL,
  device_port INT DEFAULT 8000,
  
  -- SDK对接配置
  sdk_version VARCHAR(32) DEFAULT '6.1.9.47',
  sdk_timeout INT DEFAULT 5000,
  max_connections INT DEFAULT 10,
  
  -- 设备编号配置
  device_code_prefix VARCHAR(16) DEFAULT 'HK',
  device_code_length INT DEFAULT 8,
  
  -- 通讯协议配置
  protocol_type VARCHAR(16) DEFAULT 'TCP',
  encoding VARCHAR(16) DEFAULT 'UTF-8',
  data_format VARCHAR(16) DEFAULT 'JSON',
  
  -- 认证配置
  username VARCHAR(64) DEFAULT 'admin',
  password VARCHAR(128) NULL,
  auth_mode VARCHAR(16) DEFAULT 'DIGEST',
  
  -- 心跳检测配置
  heartbeat_enabled TINYINT DEFAULT 1,
  heartbeat_interval INT DEFAULT 30,
  heartbeat_timeout INT DEFAULT 10,
  max_retry_count INT DEFAULT 3,
  
  -- 功能开关
  face_recognition_enabled TINYINT DEFAULT 1,
  card_recognition_enabled TINYINT DEFAULT 1,
  temperature_detection_enabled TINYINT DEFAULT 0,
  mask_detection_enabled TINYINT DEFAULT 0,
  
  -- 数据同步配置
  sync_enabled TINYINT DEFAULT 1,
  sync_interval INT DEFAULT 300,
  batch_size INT DEFAULT 100,
  
  -- 状态和时间
  status INT DEFAULT 1,
  created_at DATETIME NULL,
  updated_at DATETIME NULL,
  last_test_at DATETIME NULL,
  test_result VARCHAR(512) NULL,
  
  UNIQUE KEY uk_hikvision_school (school_id),
  INDEX idx_hikvision_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;