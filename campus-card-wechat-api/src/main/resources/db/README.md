# Wechat API 数据库脚本说明

本目录用于记录 wechat-api 相关的数据库表结构（DDL）与示例数据（Seed），便于后续开发追踪数据来源与复现。

## 数据源配置
- 配置文件：`src/main/resources/application.yml`
- 关键项：
  - `spring.datasource.url` 默认指向 `jdbc:mysql://localhost:3306/campus_card`
  - `spring.jpa.hibernate.ddl-auto: update`（运行时由 Hibernate 自动维护表结构）

生产环境或联调时，建议使用本目录下的脚本来初始化数据库结构与示例数据，并在需要时导出或审计数据变更。

## 表结构（DDL）
- `schema.sql`：记录实体对应的表结构定义，包含以下表：
  - `wx_notification`（通知中心）
  - `student`（学生信息）
  - `attendance_event`（考勤事件）
  - `attendance_alert`（考勤提醒）
  - `consume_record`（消费记录）
  - `face_collection`（人像采集记录）

## 示例数据（Seed）
- `seed.students.sql`：初始化示例学生（与前端默认绑定的 2001/2002 ID 对应）。
- `seed.notifications.examples.sql`：与接口 `/api/v1/notify/mock/generate` 一致的通知示例数据。

## 数据来源说明
- 通知中心：
  - 列表/分页：`GET /api/v1/notify/list`
  - 未读数量：`GET /api/v1/notify/unread_count`
  - 标记已读：`POST /api/v1/notify/mark_read`
  - 实时拉取：`GET /api/v1/notify/realtime`
  - 单条详情：`GET /api/v1/notify/item`
  - 生成示例：`POST /api/v1/notify/mock/generate`（对应 seed 脚本内容）

## 使用建议
- 初始化结构：在目标数据库执行 `schema.sql`。
- 初始化示例数据：按需执行 `seed.students.sql`、`seed.notifications.examples.sql`。
- 若需自动执行，可在应用配置中启用 `spring.sql.init.mode=always` 并放置脚本到 `classpath:`，但当前项目默认由 JPA 管理 DDL，脚本主要用于记录与审计。