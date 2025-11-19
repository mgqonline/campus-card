# Campus Card Admin API — 使用 MySQL，取消外键约束

本服务默认使用 MySQL 运行（`application.yml` 中启用 `spring.profiles.active: mysql`），并已统一至 MySQL 脚本。为提高脚本执行的可移植性与顺序无关性，数据库表已移除外键约束，由应用层保证引用一致性。

## 1. 数据库与脚本
- 表结构：`src/main/resources/db/mysql/schema.sql`
- 初始化数据：`src/main/resources/db/mysql/data.sql`
- 约束策略：
  - 已移除所有外键约束（如 `grade.school_id`、`class.grade_id/school_id`、`teacher.school_id`、`card.type_id`、系统关联表及 `student_parent`）。
  - 保留必要索引（如 `idx_*`），保障查询性能。
  - 脚本执行顺序无依赖，适合独立导入或由 Spring 初始化执行。

> 注：`sys_user.phone` 在代码层使用 AES 加解密转换器，初始化脚本中置为 `NULL`，避免明文与转换器不匹配导致读取失败。

## 2. 运行配置（默认 MySQL）
- `application-mysql.yml` 关键项：
  - `spring.jpa.hibernate.ddl-auto: none`（不由 JPA 自动建表）
  - `spring.jpa.properties.hibernate.dialect: org.hibernate.dialect.MySQL8Dialect`
  - `spring.sql.init.mode: always`（始终执行 `schema.sql` 与 `data.sql`）
  - `spring.sql.init.schema-locations: classpath:db/mysql/schema.sql`
  - `spring.sql.init.data-locations: classpath:db/mysql/data.sql`
  - `spring.sql.init.continue-on-error: true`（忽略非关键错误，保证顺序无关）

- 数据源（可用环境变量覆盖）：
  - `MYSQL_HOST`（默认 `localhost`）
  - `MYSQL_PORT`（默认 `3306`）
  - `MYSQL_DB`（默认 `campus_card`）
  - `MYSQL_USER`（默认 `root`）
  - `MYSQL_PASSWORD`（默认 `mgq@2024`）

## 3. 启动与构建
```bash
mvn -pl campus-card-admin-api clean package -DskipTests
java -jar campus-card-admin-api/target/campus-card-admin-api-0.0.1-SNAPSHOT.jar
```
启动日志应显示：`Using dialect: org.hibernate.dialect.MySQL8Dialect`，`Tomcat started on port(s): 8080`。

## 4. 校验接口
```bash
# 卡种列表（验证 data.sql 已加载）
curl http://localhost:8080/api/v1/cards/types

# 登录示例（如启用鉴权模块）
curl -H "Content-Type: application/json" -X POST \
  -d '{"username":"admin","password":"123456"}' \
  http://localhost:8080/api/v1/auth/login
```

## 5. 一致性与业务约束
- 由于取消外键约束，引用完整性由应用层负责：
  - 删除上游记录前做依赖检查（如删除学校/年级/卡种前检查下游引用）。
  - 插入/更新时确保引用存在（如 `type_id` 对应卡种存在）。
- 已保留关键索引，查询性能不受影响；如新增引用列请同步创建索引。

## 6. 前端联调
如前端 `.env.development` 设置 `VITE_API_BASE=http://localhost:8080`，界面可直接调用后端，无需 Vite 代理。