-- 操作日志 / 设备操作日志 / 数据变更日志

CREATE TABLE IF NOT EXISTS op_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  occurred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  user_id BIGINT NULL,
  subject_type VARCHAR(32) DEFAULT 'USER',
  method VARCHAR(16),
  uri VARCHAR(255),
  action VARCHAR(64),
  params LONGTEXT,
  client_ip VARCHAR(64),
  result_code INT,
  duration_ms INT,
  INDEX idx_oplog_time (occurred_at),
  INDEX idx_oplog_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS device_op_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  occurred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  device_id BIGINT,
  device_code VARCHAR(64),
  action VARCHAR(64),
  params LONGTEXT,
  result VARCHAR(128),
  operator_id BIGINT,
  INDEX idx_devlog_time (occurred_at),
  INDEX idx_devlog_device (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS data_change_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  occurred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  entity VARCHAR(64),
  entity_id VARCHAR(64),
  change_type VARCHAR(16), -- INSERT/UPDATE/DELETE
  before_json LONGTEXT,
  after_json LONGTEXT,
  changed_fields VARCHAR(255),
  operator_id BIGINT,
  remark VARCHAR(255),
  INDEX idx_dcl_time (occurred_at),
  INDEX idx_dcl_entity (entity, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;