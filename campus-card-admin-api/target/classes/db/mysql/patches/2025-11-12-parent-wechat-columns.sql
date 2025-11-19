-- Patch: normalize parent_wechat columns to snake_case to match SpringPhysicalNamingStrategy
SET NAMES utf8mb4;

-- Drop old unique indexes if they exist (referencing camelCase columns)
ALTER TABLE parent_wechat
  DROP INDEX uk_wechat_open,
  DROP INDEX uk_wechat_union;

-- Rename camelCase columns to snake_case
ALTER TABLE parent_wechat
  CHANGE COLUMN `openId` `open_id` VARCHAR(128) NOT NULL,
  CHANGE COLUMN `unionId` `union_id` VARCHAR(128) NULL,
  CHANGE COLUMN `nickname` `nickname` VARCHAR(64) NULL,
  CHANGE COLUMN `avatarUrl` `avatar_url` VARCHAR(255) NULL,
  CHANGE COLUMN `bindTime` `bind_time` DATETIME NULL;

-- Recreate unique indexes on new column names
ALTER TABLE parent_wechat
  ADD UNIQUE KEY uk_wechat_open (open_id),
  ADD UNIQUE KEY uk_wechat_union (union_id);