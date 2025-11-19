# 校园一卡通微信端角色功能梳理文档

## 目录
- [1. 项目概述](#1-项目概述)
- [2. 角色体系](#2-角色体系)
- [3. 登录认证流程](#3-登录认证流程)
- [4. 权限控制机制](#4-权限控制机制)
- [5. 家长端功能清单](#5-家长端功能清单)
- [6. 教师端功能清单](#6-教师端功能清单)
- [7. 页面路由结构](#7-页面路由结构)
- [8. 部署和运行步骤](#8-部署和运行步骤)
- [9. 开发调试指南](#9-开发调试指南)

---

## 1. 项目概述

### 1.1 项目结构
```
campus-card/
├── campus-card-wechat-web/     # 微信端前端（Vue3 + Vite + TypeScript）
├── campus-card-wechat-api/     # 微信端后端API（Spring Boot）
├── campus-card-admin-api/      # 管理端后端API（Spring Boot）
├── h/                          # 管理端前端（Vue3 + Ant Design Vue）
└── campus-card-common/         # 公共模块
```

### 1.2 技术栈
- **前端**: Vue 3 + TypeScript + Vite + Vant UI（微信端）/ Ant Design Vue（管理端）
- **后端**: Spring Boot 2.7.18 + JPA + MySQL
- **认证**: 基于Token的简单认证机制
- **状态管理**: Pinia
- **路由**: Vue Router 4

---

## 2. 角色体系

### 2.1 角色分类
项目支持两种主要角色：

| 角色 | 描述 | 登录入口 | 主要功能 |
|------|------|----------|----------|
| **家长** | 学生家长用户 | `/login` | 查看孩子考勤、消费记录，请假申请等 |
| **教师** | 学校教师用户 | `/t/login` | 班级管理、请假审批、学生信息管理等 |

### 2.2 角色权限特点
- **家长端**: 以孩子为中心，查看和管理与自己孩子相关的信息
- **教师端**: 以班级为中心，管理所负责班级的学生信息和审批事务
- **权限隔离**: 两个角色使用不同的API端点和Token体系，确保数据安全

---

## 3. 登录认证流程

### 3.1 家长端登录流程

#### 3.1.1 登录页面
- **路径**: `/login`
- **组件**: `src/views/Login.vue`
- **API**: `POST /api/v1/auth/login`

#### 3.1.2 登录步骤
1. 用户输入手机号
2. 点击"获取验证码"（测试环境固定为 `123456`）
3. 输入验证码并提交
4. 后端验证成功后返回Token
5. 前端存储Token到 `localStorage.wx_token`
6. 跳转到首页 `/`

#### 3.1.3 登录API接口
```typescript
// 请求参数
interface LoginReq {
  phone: string    // 手机号
  code: string     // 验证码
}

// 响应数据
interface LoginResp {
  token: string    // 访问令牌
  expireIn: number // 过期时间（秒）
}
```

### 3.2 教师端登录流程

#### 3.2.1 登录页面
- **路径**: `/t/login`
- **组件**: `src/views/teacher/TeacherLogin.vue`
- **API**: `POST /api/v1/t/auth/login`

#### 3.2.2 登录步骤
1. 用户输入手机号（默认: `13900001234`）
2. 输入验证码（测试环境固定为 `123456`）
3. 提交登录请求
4. 后端验证成功后返回Token
5. 前端存储Token到 `localStorage.t_token`
6. 跳转到教师端首页 `/t/home`

#### 3.2.3 登录API接口
```typescript
// 请求参数
interface TLoginReq {
  phone: string    // 手机号
  code: string     // 验证码
}

// 响应数据
interface TLoginResp {
  token: string    // 访问令牌
  expireIn: number // 过期时间（秒）
}
```

---

## 4. 权限控制机制

### 4.1 Token管理
- **家长端Token**: 存储在 `localStorage.wx_token`
- **教师端Token**: 存储在 `localStorage.t_token`
- **Token格式**: 
  - 家长端: `wechat-token-{timestamp}`
  - 教师端: `teacher-token-{timestamp}`

### 4.2 API请求拦截
```typescript
// 家长端请求拦截器
instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('wx_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 教师端请求拦截器
instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('t_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

### 4.3 路由守卫
- **家长端**: 检查 `wx_token` 存在性，未登录跳转到 `/login`
- **教师端**: 检查 `t_token` 存在性，未登录跳转到 `/t/login`
- **权限验证**: 基于Token的简单验证，无复杂的角色权限控制

---

## 5. 家长端功能清单

### 5.1 核心功能模块

#### 5.1.1 首页概览 (`/`)
- **组件**: `src/views/Home.vue`
- **功能**:
  - 显示家长基本信息
  - 孩子选择切换
  - 关键指标展示（余额、考勤统计、异常告警）
  - 快捷功能入口

#### 5.1.2 账户管理
- **余额查询** (`/balance`)
  - 组件: `src/views/Balance.vue`
  - 功能: 查看孩子一卡通余额、充值记录

- **消费记录** (`/consumption`)
  - 组件: `src/views/Consumption.vue`
  - 功能: 查看孩子消费明细、消费统计

#### 5.1.3 考勤管理
- **考勤记录** (`/attendance`)
  - 组件: `src/views/Attendance.vue`
  - 功能: 查看孩子进出校记录、考勤统计

#### 5.1.4 请假管理
- **请假申请** (`/leave/apply`)
  - 组件: `src/views/leave/LeaveApply.vue`
  - 功能: 为孩子提交请假申请

- **请假记录** (`/leave/records`)
  - 组件: `src/views/leave/LeaveRecords.vue`
  - 功能: 查看请假历史记录和状态

#### 5.1.5 消息通知
- **消息列表** (`/messages`)
  - 组件: `src/views/Messages.vue`
  - 功能: 查看学校通知、系统消息

#### 5.1.6 个人中心
- **个人信息** (`/profile`)
  - 组件: `src/views/Profile.vue`
  - 功能: 查看和修改家长个人信息、绑定微信

### 5.2 API接口清单

| 功能模块 | API端点 | 方法 | 描述 |
|----------|---------|------|------|
| 认证登录 | `/api/v1/auth/login` | POST | 家长登录 |
| 个人信息 | `/api/v1/auth/profile` | GET | 获取家长信息 |
| 个人信息 | `/api/v1/auth/profile` | PUT | 更新家长信息 |
| 微信绑定 | `/api/v1/auth/bind` | POST | 绑定微信账号 |
| 余额查询 | `/api/v1/account/balance` | GET | 查询余额 |
| 消费记录 | `/api/v1/consumption/records` | GET | 消费记录列表 |
| 考勤记录 | `/api/v1/attendance/records` | GET | 考勤记录列表 |
| 考勤统计 | `/api/v1/attendance/stats` | GET | 考勤统计数据 |
| 考勤告警 | `/api/v1/attendance/alerts` | GET | 考勤异常告警 |
| 请假申请 | `/api/v1/leave/apply` | POST | 提交请假申请 |
| 请假记录 | `/api/v1/leave/records` | GET | 请假记录列表 |
| 请假状态 | `/api/v1/leave/status` | GET | 请假状态查询 |
| 消息通知 | `/api/v1/notify/list` | GET | 消息列表 |
| 未读消息 | `/api/v1/notify/unread-count` | GET | 未读消息数量 |

---

## 6. 教师端功能清单

### 6.1 核心功能模块

#### 6.1.1 教师首页 (`/t/home`)
- **组件**: `src/views/teacher/TeacherHome.vue`
- **功能**:
  - 显示教师基本信息
  - 功能模块快捷入口
  - 班级概览信息

#### 6.1.2 班级管理
- **班级列表** (`/t/classes`)
  - 组件: `src/views/teacher/ClassList.vue`
  - 功能: 查看所负责的班级列表

- **学生名单** (`/t/class/:id/students`)
  - 组件: `src/views/teacher/ClassStudents.vue`
  - 功能: 查看班级学生名单

- **学生信息** (`/t/class/:id/student/:childId/info`)
  - 组件: `src/views/teacher/ClassStudentInfo.vue`
  - 功能: 查看学生详细信息

#### 6.1.3 考勤管理
- **学生考勤** (`/t/class/:id/student/:childId/attendance`)
  - 组件: `src/views/teacher/StudentAttendance.vue`
  - 功能: 查看单个学生考勤记录

- **班级考勤** (`/t/class/:id/attendance/records`)
  - 组件: `src/views/teacher/ClassAttendanceRecords.vue`
  - 功能: 查看班级整体考勤记录

#### 6.1.4 请假审批
- **审批列表** (`/t/leave`)
  - 组件: `src/views/teacher/TeacherLeaveApproval.vue`
  - 功能: 查看待审批请假单、审批操作

- **老师帮请** (`/t/leave/help`)
  - 组件: `src/views/teacher/TeacherHelpLeave.vue`
  - 功能: 教师代替学生提交请假申请

#### 6.1.5 消息通知
- **消息管理** (`/t/messages`)
  - 组件: `src/views/teacher/TeacherMessages.vue`
  - 功能: 查看和发送消息通知

#### 6.1.6 个人中心
- **个人信息** (`/t/account/profile`)
  - 组件: `src/views/teacher/TeacherProfile.vue`
  - 功能: 查看和修改教师个人信息

- **密码修改** (`/t/account/password`)
  - 组件: `src/views/teacher/ChangePassword.vue`
  - 功能: 修改登录密码

### 6.2 API接口清单

| 功能模块 | API端点 | 方法 | 描述 |
|----------|---------|------|------|
| 认证登录 | `/api/v1/t/auth/login` | POST | 教师登录 |
| 个人信息 | `/api/v1/t/auth/profile` | GET | 获取教师信息 |
| 密码修改 | `/api/v1/t/auth/password` | PUT | 修改密码 |
| 微信绑定 | `/api/v1/t/auth/bind` | POST | 绑定微信账号 |
| 退出登录 | `/api/v1/t/auth/logout` | POST | 教师登出 |
| 班级列表 | `/api/v1/t/class/list` | GET | 获取班级列表 |
| 学生名单 | `/api/v1/t/class/students` | GET | 获取班级学生 |
| 学生信息 | `/api/v1/t/class/student/info` | GET | 获取学生信息 |
| 考勤记录 | `/api/v1/t/attendance/records` | GET | 考勤记录查询 |
| 请假审批 | `/api/v1/t/leave/pending` | GET | 待审批列表 |
| 请假记录 | `/api/v1/t/leave/records` | GET | 请假记录列表 |
| 老师帮请 | `/api/v1/t/leave/help_apply` | POST | 教师代请假 |
| 审批操作 | `/api/v1/leave/approve` | POST | 审批请假 |
| 批量审批 | `/api/v1/leave/batch-approve` | POST | 批量审批 |

---

## 7. 页面路由结构

### 7.1 家长端路由 (`campus-card-wechat-web`)

```typescript
const routes = [
  // 认证相关
  { path: '/login', name: 'Login', component: Login },
  
  // 主要功能页面
  { path: '/', name: 'Home', component: Home },
  { path: '/balance', name: 'Balance', component: Balance },
  { path: '/consumption', name: 'Consumption', component: Consumption },
  { path: '/attendance', name: 'Attendance', component: Attendance },
  { path: '/messages', name: 'Messages', component: Messages },
  { path: '/profile', name: 'Profile', component: Profile },
  
  // 请假相关
  { path: '/leave/apply', name: 'LeaveApply', component: LeaveApply },
  { path: '/leave/records', name: 'LeaveRecords', component: LeaveRecords },
]
```

### 7.2 教师端路由 (`campus-card-wechat-web`)

```typescript
const routes = [
  // 教师认证
  { path: '/t/login', name: 'TeacherLogin', component: TeacherLogin },
  { path: '/t/home', name: 'TeacherHome', component: TeacherHome },
  
  // 个人账户
  { path: '/t/account/profile', name: 'TeacherProfile', component: TeacherProfile },
  { path: '/t/account/password', name: 'TeacherPassword', component: ChangePassword },
  
  // 班级管理
  { path: '/t/classes', name: 'TeacherClasses', component: ClassList },
  { path: '/t/class/:id/students', name: 'ClassStudents', component: ClassStudents },
  { path: '/t/class/:id/student/:childId/info', name: 'ClassStudentInfo', component: ClassStudentInfo },
  { path: '/t/class/:id/student/:childId/attendance', name: 'StudentAttendance', component: StudentAttendance },
  { path: '/t/class/:id/attendance/records', name: 'ClassAttendanceRecords', component: ClassAttendanceRecords },
  
  // 请假管理
  { path: '/t/leave', name: 'TeacherLeaveApproval', component: TeacherLeaveApproval },
  { path: '/t/leave/help', name: 'TeacherHelpLeave', component: TeacherHelpLeave },
  
  // 消息通知
  { path: '/t/messages', name: 'TeacherMessages', component: TeacherMessages },
]
```

---

## 8. 部署和运行步骤

### 8.1 环境要求

#### 8.1.1 开发环境
- **Node.js**: 18.x 或更高版本
- **Java**: JDK 8 或更高版本
- **Maven**: 3.6.x 或更高版本
- **MySQL**: 8.0 或更高版本

#### 8.1.2 数据库配置
```sql
-- 创建数据库
CREATE DATABASE campus_card CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER 'campus_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON campus_card.* TO 'campus_user'@'localhost';
FLUSH PRIVILEGES;
```

### 8.2 后端部署

#### 8.2.1 微信端API服务 (campus-card-wechat-api)

```bash
# 1. 进入项目目录
cd campus-card/campus-card-wechat-api

# 2. 配置数据库连接
# 编辑 src/main/resources/application.yml
# 修改数据库连接信息：
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_card?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root
    password: your_password

# 3. 编译打包
mvn clean package -DskipTests

# 4. 运行服务
java -jar target/campus-card-wechat-api-0.0.1-SNAPSHOT.jar

# 服务将在 http://localhost:8082 启动
```

#### 8.2.2 管理端API服务 (campus-card-admin-api)

```bash
# 1. 进入项目目录
cd campus-card/campus-card-admin-api

# 2. 配置数据库连接
# 编辑 src/main/resources/application-mysql.yml
# 修改数据库连接信息

# 3. 编译打包
mvn clean package -DskipTests

# 4. 运行服务
java -jar target/campus-card-admin-api-0.0.1-SNAPSHOT.jar

# 服务将在 http://localhost:8080 启动
```

### 8.3 前端部署

#### 8.3.1 微信端前端 (campus-card-wechat-web)

```bash
# 1. 进入项目目录
cd campus-card/campus-card-wechat-web

# 2. 安装依赖
npm install

# 3. 配置API地址
# 创建 .env.local 文件
echo "VITE_API_BASE=http://localhost:8082" > .env.local

# 4. 开发模式运行
npm run dev
# 访问 http://localhost:5173

# 5. 生产构建
npm run build
# 构建产物在 dist/ 目录
```

#### 8.3.2 管理端前端 (h)

```bash
# 1. 进入项目目录
cd campus-card/h

# 2. 安装依赖
npm install

# 3. 配置API地址
# 创建 .env.local 文件
echo "VITE_API_BASE=http://localhost:8080" > .env.local

# 4. 开发模式运行
npm run dev
# 访问 http://localhost:5174

# 5. 生产构建
npm run build
# 构建产物在 dist/ 目录
```

### 8.4 Docker部署（可选）

#### 8.4.1 创建 docker-compose.yml

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: mgq@2024
      MYSQL_DATABASE: campus_card
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  wechat-api:
    build: ./campus-card-wechat-api
    ports:
      - "8082:8082"
    depends_on:
      - mysql
    environment:
      DB_URL: jdbc:mysql://mysql:3306/campus_card?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
      DB_USERNAME: root
      DB_PASSWORD: mgq@2024

  admin-api:
    build: ./campus-card-admin-api
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    environment:
      MYSQL_HOST: mysql
      MYSQL_PORT: 3306
      MYSQL_DB: campus_card
      MYSQL_USER: root
      MYSQL_PASSWORD: mgq@2024

volumes:
  mysql_data:
```

#### 8.4.2 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f wechat-api
```

---

## 9. 开发调试指南

### 9.1 测试账号

#### 9.1.1 家长端测试账号
- **手机号**: 任意11位手机号（格式正确即可）
- **验证码**: `123456`（固定测试验证码）

#### 9.1.2 教师端测试账号
- **手机号**: `13900001234`（默认）
- **验证码**: `123456`（固定测试验证码）

### 9.2 API调试

#### 9.2.1 接口测试工具
推荐使用 Postman 或 curl 进行API测试：

```bash
# 家长登录
curl -X POST http://localhost:8082/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800001234","code":"123456"}'

# 教师登录
curl -X POST http://localhost:8082/api/v1/t/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13900001234","code":"123456"}'
```

#### 9.2.2 常见问题排查

1. **数据库连接失败**
   - 检查MySQL服务是否启动
   - 确认数据库连接配置正确
   - 验证数据库用户权限

2. **前端API请求失败**
   - 检查后端服务是否正常启动
   - 确认API地址配置正确
   - 查看浏览器控制台错误信息

3. **登录失败**
   - 确认验证码为 `123456`
   - 检查手机号格式是否正确
   - 查看后端日志确认请求是否到达

### 9.3 开发工具配置

#### 9.3.1 VS Code推荐插件
- Vue Language Features (Volar)
- TypeScript Vue Plugin (Volar)
- ESLint
- Prettier
- Auto Rename Tag

#### 9.3.2 IDEA推荐插件
- Vue.js
- Spring Boot
- Lombok
- MyBatis

---

## 总结

本文档详细梳理了校园一卡通微信端项目的角色体系、功能模块、认证流程和部署步骤。项目采用前后端分离架构，支持家长和教师两种角色，各自拥有独立的功能模块和权限体系。

### 关键特点
1. **角色分离**: 家长端和教师端功能完全独立，使用不同的路由和API
2. **简单认证**: 基于Token的轻量级认证机制，适合微信端场景
3. **模块化设计**: 功能模块清晰，便于维护和扩展
4. **响应式UI**: 基于Vant UI，适配移动端使用场景

### 后续优化建议
1. 增加更完善的权限控制机制
2. 添加数据缓存和离线支持
3. 完善错误处理和用户体验
4. 增加单元测试和集成测试
5. 优化性能和加载速度