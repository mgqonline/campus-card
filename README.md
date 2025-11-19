# 校园卡系统技术架构与部署指南

## 项目概述
校园卡系统采用前后端分离与多模块架构，后端以 Spring Boot 为核心，前端以 Vue 3 + Vite 为主。后端包含后台管理、微信端 API、外部网关三大服务，共享一个公共模块；前端包含管理后台与微信 Web/H5 两套应用。

```
campus-card
├── 后端
│   ├── campus-card-common           # 公共模块（工具/模型）
│   ├── campus-card-admin-api        # 管理后台 API
│   ├── campus-card-wechat-api       # 微信端 API
│   └── campus-card-gateway-api      # 外部访问网关 API
└── 前端
    ├── h                            # 管理后台前端（Vue 3）
    └── campus-card-wechat-web       # 微信 Web/H5 前端（Vue 3 + Vant）
```

## 技术栈
- 后端：Spring Boot 2.7.x、Spring Data JPA、MySQL 8、Redis（部分模块）、Lombok、Maven
- 前端：Vue 3、TypeScript、Vite、Ant Design Vue（管理后台）、Vant（微信端）、Axios、Pinia、Vue Router
- JDK：Java 8 兼容（各模块 `maven-compiler-plugin` 配置为 1.8）

## 模块与端口
- 管理后台 API（`campus-card-admin-api`）
  - 端口：`8080`
  - 数据库：`MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_DB`、`MYSQL_USER`、`MYSQL_PASSWORD`
  - Redis：`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`
  - JPA：`ddl-auto=none`；通过 SQL 脚本初始化与补丁
  - 配置参考：`campus-card-admin-api/src/main/resources/application-mysql.yml:1-9,34-37`
- 微信端 API（`campus-card-wechat-api`）
  - 端口：`8082`
  - 数据库：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`
  - JPA：`ddl-auto=update`
  - 配置参考：`campus-card-wechat-api/target/classes/application.yml:1-14`
- 外部网关 API（`campus-card-gateway-api`）
  - 端口：`8083`
  - 文档：`/swagger-ui.html`、`/v3/api-docs`
  - 认证：JWT（HS256），关键属性在 `gateway.auth.*`
  - CORS：默认允许 `http://localhost:3000`，按需改为前端实际域名
  - 配置参考：`campus-card-gateway-api/target/classes/application.yml:1-35,19-31`
- 前端开发服务器
  - 管理后台（`h`）：`5175`，代理 `/api` 到 `http://localhost:8080`（`h/vite.config.ts:12-20`）
  - 微信 Web（`campus-card-wechat-web`）：默认 `5173`，代理 `/api` 到 `http://localhost:8082`（`campus-card-wechat-web/vite.config.ts:12-21`）

## 环境准备
- MySQL 8：创建数据库 `campus_card`
- Redis：开发可选（管理后台用于队列/缓存）
- JDK 8+、Maven 3.8+、Node.js 18+

## 构建与运行
- 后端（根目录）
  - 构建全部模块：`mvn clean package -DskipTests`
  - 单模块构建：`mvn clean package -pl <module> -DskipTests`
  - 运行示例：
    - 管理后台：`MYSQL_HOST=localhost MYSQL_DB=campus_card MYSQL_USER=root MYSQL_PASSWORD=*** java -jar campus-card-admin-api/target/campus-card-admin-api-0.0.1-SNAPSHOT.jar`
    - 微信端：`DB_URL=jdbc:mysql://localhost:3306/campus_card DB_USERNAME=root DB_PASSWORD=*** java -jar campus-card-wechat-api/target/campus-card-wechat-api-0.0.1-SNAPSHOT.jar`
    - 网关：`java -jar campus-card-gateway-api/target/campus-card-gateway-api-0.0.1-SNAPSHOT.jar`
- 前端
  - 管理后台：`cd h && npm install && npm run dev`；生产构建：`npm run build`
  - 微信 Web：`cd campus-card-wechat-web && npm install && npm run dev`；生产构建：`npm run build`

## 数据库初始化
- 管理后台：启用 `spring.sql.init.mode=always`，自动执行补丁脚本（`application-mysql.yml:34-37`）；完整结构参考 `campus-card-admin-api/src/main/resources/db/mysql/schema.sql`
- 微信端：JPA 自动维护结构（`ddl-auto=update`）；也可用 `src/main/resources/db/schema.sql` 初始化

## 生产部署建议
- 前端部署：将 `dist` 静态文件部署至 Nginx/Apache，并配置反向代理到各后端服务
- 后端部署：按模块分别打包为可执行 JAR，使用进程守护或容器编排
- 安全与文档：
  - 确保 `gateway.auth.secret` 长度≥32 字节；文档访问可通过 `docAllowLocalOnly` 与 `docAccessSecret` 控制
  - Swagger 入口：`http://{host}:8083/swagger-ui.html`
- 可选：Docker Compose/K8s 进行编排、负载均衡与监控（Prometheus/Grafana），日志采用 ELK

## 常见问题
- 端口冲突：前后端默认端口分别为 `8080/8082/8083` 与 `5175/5173`
- CORS：前端跨域来源通过网关 `gateway.auth.cors-allowed-origins` 控制
- 认证失败：检查 JWT `issuer`、签名密钥与过期时间；参考 `campus-card-gateway-api/target/classes/application.yml:19-31`
- 数据库连接失败：确认 MySQL/Redis 的主机、端口与凭据；管理后台与微信端使用不同的环境变量前缀

## 目录速览
- 根 POM：`/pom.xml`（聚合模块）
- 模块 POM：`campus-card-*/pom.xml`（依赖与编译目标 `1.8`）
- 配置文件：各模块 `src/main/resources`（或构建产物 `target/classes`）
- 前端配置：`vite.config.ts`、`package.json`（脚本与代理）
