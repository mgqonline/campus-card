-- Patch: create missing tables for face dispatch feature
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

-- Create table: face_dispatch_task
CREATE TABLE IF NOT EXISTS face_dispatch_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_type VARCHAR(32) NOT NULL,
  device_ids VARCHAR(512) NULL,
  description LONGTEXT NULL,
  total_items INT NOT NULL DEFAULT 0,
  success_items INT NOT NULL DEFAULT 0,
  failed_items INT NOT NULL DEFAULT 0,
  failed_person_ids VARCHAR(2000) NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NULL,
  updated_at DATETIME NULL,
  INDEX idx_face_dispatch_status (status),
  INDEX idx_face_dispatch_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create table: device_face
CREATE TABLE IF NOT EXISTS device_face (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  device_id BIGINT NOT NULL,
  person_type VARCHAR(32) NOT NULL,
  person_id VARCHAR(64) NOT NULL,
  dispatched_at DATETIME NULL,
  status INT NOT NULL DEFAULT 1,
  UNIQUE KEY uk_device_person (device_id, person_id),
  INDEX idx_device_id (device_id),
  INDEX idx_person_id (person_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS=1;