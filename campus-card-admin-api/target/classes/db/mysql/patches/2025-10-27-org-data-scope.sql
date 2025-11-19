-- Patch: create org_data_scope table for organizational data range
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS org_data_scope (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  subject_type VARCHAR(16) NOT NULL,
  subject_id BIGINT NOT NULL,
  scope_type VARCHAR(16) NOT NULL,
  school_ids VARCHAR(2000) NULL,
  grade_ids VARCHAR(2000) NULL,
  class_ids VARCHAR(2000) NULL,
  student_ids VARCHAR(2000) NULL,
  status INT NOT NULL DEFAULT 1,
  INDEX idx_scope_subject (subject_type, subject_id),
  INDEX idx_scope_type (scope_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;