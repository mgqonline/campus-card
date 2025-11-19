-- Create system_config table and seed defaults
CREATE TABLE IF NOT EXISTS system_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  cfg_key VARCHAR(64) NOT NULL UNIQUE,
  cfg_value VARCHAR(512) NULL,
  category VARCHAR(32) NOT NULL,
  updated_at DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed default keys for five categories
INSERT INTO system_config (cfg_key, cfg_value, category, updated_at) VALUES
 ('system.name', '校园卡管理系统', 'PARAM', CURRENT_TIMESTAMP),
 ('system.locale', 'zh-CN', 'PARAM', CURRENT_TIMESTAMP),
 ('api.baseUrl', 'http://localhost:8080', 'INTERFACE', CURRENT_TIMESTAMP),
 ('api.timeoutMs', '5000', 'INTERFACE', CURRENT_TIMESTAMP),
 ('push.enabled', 'false', 'PUSH', CURRENT_TIMESTAMP),
 ('push.webhookUrl', '', 'PUSH', CURRENT_TIMESTAMP),
 ('storage.type', 'LOCAL', 'STORAGE', CURRENT_TIMESTAMP),
 ('storage.basePath', '/var/data/campus-card', 'STORAGE', CURRENT_TIMESTAMP),
 ('backup.enabled', 'false', 'BACKUP', CURRENT_TIMESTAMP),
 ('backup.cron', '0 0 2 * * *', 'BACKUP', CURRENT_TIMESTAMP),
 ('backup.retentionDays', '7', 'BACKUP', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE cfg_value=VALUES(cfg_value), category=VALUES(category), updated_at=VALUES(updated_at);