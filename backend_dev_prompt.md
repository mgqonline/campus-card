# 校园一卡通系统 - 后端开发规范与Claude提示词模板

## 一、技术栈规范

### 1.1 核心技术
- **框架**: Spring Boot 3.2.x
- **JDK版本**: JDK 17 (LTS)
- **构建工具**: Maven 3.9.x
- **ORM框架**: MyBatis-Plus 3.5.x
- **数据库**: MySQL 8.0+ / PostgreSQL 14+
- **缓存**: Redis 7.x
- **消息队列**: RabbitMQ 3.12.x / Kafka
- **API文档**: Knife4j (Swagger 3.x)

### 1.2 核心依赖
```xml
<properties>
    <java.version>17</java.version>
    <spring-boot.version>3.2.0</spring-boot.version>
    <mybatis-plus.version>3.5.5</mybatis-plus.version>
    <hutool.version>5.8.25</hutool.version>
    <knife4j.version>4.4.0</knife4j.version>
    <sa-token.version>1.37.0</sa-token.version>
</properties>

<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>${mybatis-plus.version}</version>
    </dependency>
    
    <!-- MySQL驱动 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Sa-Token权限认证 -->
    <dependency>
        <groupId>cn.dev33</groupId>
        <artifactId>sa-token-spring-boot3-starter</artifactId>
        <version>${sa-token.version}</version>
    </dependency>
    
    <!-- Hutool工具类 -->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>${hutool.version}</version>
    </dependency>
    
    <!-- Knife4j接口文档 -->
    <dependency>
        <groupId>com.github.xiaoymin</groupId>
        <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
        <version>${knife4j.version}</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 1.3 辅助工具
- **工具库**: Hutool 5.8.x
- **JSON处理**: Jackson (Spring Boot自带)
- **权限认证**: Sa-Token 1.37.x
- **日志框架**: Logback (Spring Boot自带)
- **数据校验**: Jakarta Validation
- **Excel处理**: EasyExcel 3.3.x
- **HTTP客户端**: OkHttp 4.x / RestTemplate
- **任务调度**: XXL-Job 2.4.x

---

## 二、项目结构规范

```
campus-card-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── campus/
│   │   │           └── card/
│   │   │               ├── CampusCardApplication.java
│   │   │               ├── common/              # 公共模块
│   │   │               │   ├── annotation/      # 自定义注解
│   │   │               │   │   ├── Log.java     # 日志注解
│   │   │               │   │   ├── Permission.java # 权限注解
│   │   │               │   │   └── DataScope.java  # 数据权限注解
│   │   │               │   ├── aspect/          # 切面
│   │   │               │   │   ├── LogAspect.java
│   │   │               │   │   ├── PermissionAspect.java
│   │   │               │   │   └── DataScopeAspect.java
│   │   │               │   ├── constant/        # 常量
│   │   │               │   │   ├── CacheConstants.java
│   │   │               │   │   ├── CommonConstants.java
│   │   │               │   │   └── RedisKeyConstants.java
│   │   │               │   ├── enums/           # 枚举
│   │   │               │   │   ├── ResultCode.java
│   │   │               │   │   ├── UserType.java
│   │   │               │   │   ├── CardStatus.java
│   │   │               │   │   ├── AttendanceStatus.java
│   │   │               │   │   └── DeviceType.java
│   │   │               │   ├── exception/       # 异常
│   │   │               │   │   ├── BusinessException.java
│   │   │               │   │   ├── GlobalExceptionHandler.java
│   │   │               │   │   └── ServiceException.java
│   │   │               │   └── result/          # 统一响应
│   │   │               │       ├── Result.java
│   │   │               │       └── PageResult.java
│   │   │               ├── config/              # 配置类
│   │   │               │   ├── MybatisPlusConfig.java
│   │   │               │   ├── RedisConfig.java
│   │   │               │   ├── SaTokenConfig.java
│   │   │               │   ├── Knife4jConfig.java
│   │   │               │   ├── WebMvcConfig.java
│   │   │               │   └── ThreadPoolConfig.java
│   │   │               ├── controller/          # 控制器
│   │   │               │   ├── AuthController.java        # 认证
│   │   │               │   ├── CardController.java        # 卡务管理
│   │   │               │   ├── StudentController.java     # 学生管理
│   │   │               │   ├── TeacherController.java     # 教师管理
│   │   │               │   ├── ParentController.java      # 家长管理
│   │   │               │   ├── ClassController.java       # 班级管理
│   │   │               │   ├── SchoolController.java      # 学校管理
│   │   │               │   ├── FaceController.java        # 人脸管理
│   │   │               │   ├── DeviceController.java      # 设备管理
│   │   │               │   │   ├── ConsumptionDeviceController.java
│   │   │               │   │   └── AttendanceDeviceController.java
│   │   │               │   ├── AttendanceController.java  # 考勤管理
│   │   │               │   ├── ConsumptionController.java # 消费管理
│   │   │               │   ├── ReportController.java      # 报表统计
│   │   │               │   ├── SystemController.java      # 系统管理
│   │   │               │   │   ├── UserController.java
│   │   │               │   │   ├── RoleController.java
│   │   │               │   │   └── PermissionController.java
│   │   │               │   └── wechat/          # 微信端接口
│   │   │               │       ├── WxAuthController.java
│   │   │               │       ├── WxConsumptionController.java
│   │   │               │       ├── WxAttendanceController.java
│   │   │               │       └── WxLeaveController.java
│   │   │               ├── domain/              # 领域对象
│   │   │               │   ├── entity/          # 实体类
│   │   │               │   │   ├── Card.java
│   │   │               │   │   ├── Student.java
│   │   │               │   │   ├── Teacher.java
│   │   │               │   │   ├── Parent.java
│   │   │               │   │   ├── Class.java
│   │   │               │   │   ├── School.java
│   │   │               │   │   ├── FaceInfo.java
│   │   │               │   │   ├── Device.java
│   │   │               │   │   ├── AttendanceRecord.java
│   │   │               │   │   ├── ConsumptionRecord.java
│   │   │               │   │   ├── User.java
│   │   │               │   │   ├── Role.java
│   │   │               │   │   └── Permission.java
│   │   │               │   ├── dto/             # 数据传输对象
│   │   │               │   │   ├── request/     # 请求DTO
│   │   │               │   │   │   ├── LoginRequest.java
│   │   │               │   │   │   ├── StudentQueryRequest.java
│   │   │               │   │   │   ├── StudentSaveRequest.java
│   │   │               │   │   │   ├── AttendanceQueryRequest.java
│   │   │               │   │   │   └── ...
│   │   │               │   │   └── response/    # 响应DTO
│   │   │               │   │       ├── LoginResponse.java
│   │   │               │   │       ├── StudentResponse.java
│   │   │               │   │       ├── AttendanceStatisticsResponse.java
│   │   │               │   │       └── ...
│   │   │               │   └── vo/              # 视图对象
│   │   │               │       ├── StudentVO.java
│   │   │               │       ├── AttendanceVO.java
│   │   │               │       └── ...
│   │   │               ├── mapper/              # MyBatis Mapper
│   │   │               │   ├── CardMapper.java
│   │   │               │   ├── StudentMapper.java
│   │   │               │   ├── TeacherMapper.java
│   │   │               │   ├── AttendanceRecordMapper.java
│   │   │               │   └── ...
│   │   │               ├── service/             # 服务接口
│   │   │               │   ├── IAuthService.java
│   │   │               │   ├── ICardService.java
│   │   │               │   ├── IStudentService.java
│   │   │               │   ├── ITeacherService.java
│   │   │               │   ├── IAttendanceService.java
│   │   │               │   ├── IConsumptionService.java
│   │   │               │   ├── IDeviceService.java
│   │   │               │   ├── IFaceService.java
│   │   │               │   ├── device/          # 设备对接服务
│   │   │               │   │   ├── IYikeshiService.java
│   │   │               │   │   └── IHikVisionService.java
│   │   │               │   └── impl/            # 服务实现
│   │   │               │       ├── AuthServiceImpl.java
│   │   │               │       ├── CardServiceImpl.java
│   │   │               │       ├── StudentServiceImpl.java
│   │   │               │       ├── device/
│   │   │               │       │   ├── YikeshiServiceImpl.java
│   │   │               │       │   └── HikVisionServiceImpl.java
│   │   │               │       └── ...
│   │   │               ├── integration/         # 第三方集成
│   │   │               │   ├── device/          # 设备SDK封装
│   │   │               │   │   ├── yikeshi/     # 易科士设备
│   │   │               │   │   │   ├── YikeshiClient.java
│   │   │               │   │   │   ├── YikeshiCommandBuilder.java
│   │   │               │   │   │   └── YikeshiDataParser.java
│   │   │               │   │   └── hikvision/   # 海康设备
│   │   │               │   │       ├── HikVisionSdkClient.java
│   │   │               │   │       ├── HikVisionFaceManager.java
│   │   │               │   │       └── HikVisionDataCollector.java
│   │   │               │   └── wechat/          # 微信集成
│   │   │               │       ├── WxPayService.java
│   │   │               │       ├── WxMessageService.java
│   │   │               │       └── WxAuthService.java
│   │   │               ├── task/                # 定时任务
│   │   │               │   ├── DeviceHeartbeatTask.java
│   │   │               │   ├── DataSyncTask.java
│   │   │               │   └── AttendanceStatisticsTask.java
│   │   │               └── utils/               # 工具类
│   │   │                   ├── JwtUtil.java
│   │   │                   ├── RedisUtil.java
│   │   │                   ├── FileUtil.java
│   │   │                   ├── ExcelUtil.java
│   │   │                   ├── DateUtil.java
│   │   │                   └── SecurityUtil.java
│   │   └── resources/
│   │       ├── application.yml                  # 主配置
│   │       ├── application-dev.yml              # 开发环境
│   │       ├── application-prod.yml             # 生产环境
│   │       ├── logback-spring.xml               # 日志配置
│   │       └── mapper/                          # MyBatis XML
│   │           ├── CardMapper.xml
│   │           ├── StudentMapper.xml
│   │           └── ...
│   └── test/
│       └── java/
│           └── com/
│               └── campus/
│                   └── card/
│                       ├── service/
│                       └── integration/
├── pom.xml
└── README.md
```

---

## 三、编码规范

### 3.1 实体类规范 (Entity)

```java
package com.campus.card.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学生实体类
 *
 * @author system
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_student")
@Schema(description = "学生实体")
public class Student implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 学号
     */
    @TableField("student_no")
    @Schema(description = "学号")
    private String studentNo;

    /**
     * 姓名
     */
    @TableField("name")
    @Schema(description = "姓名")
    private String name;

    /**
     * 性别 (0-女, 1-男)
     */
    @TableField("gender")
    @Schema(description = "性别")
    private Integer gender;

    /**
     * 身份证号
     */
    @TableField("id_card")
    @Schema(description = "身份证号")
    private String idCard;

    /**
     * 手机号
     */
    @TableField("phone")
    @Schema(description = "手机号")
    private String phone;

    /**
     * 班级ID
     */
    @TableField("class_id")
    @Schema(description = "班级ID")
    private Long classId;

    /**
     * 学校ID
     */
    @TableField("school_id")
    @Schema(description = "学校ID")
    private Long schoolId;

    /**
     * 照片URL
     */
    @TableField("photo_url")
    @Schema(description = "照片URL")
    private String photoUrl;

    /**
     * 状态 (0-禁用, 1-启用)
     */
    @TableField("status")
    @Schema(description = "状态")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private Long createBy;

    /**
     * 更新人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人")
    private Long updateBy;

    /**
     * 是否删除 (0-未删除, 1-已删除)
     */
    @TableField("deleted")
    @TableLogic
    @Schema(description = "删除标识")
    private Integer deleted;
}
```

### 3.2 DTO规范 (Data Transfer Object)

```java
package com.campus.card.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 学生保存请求DTO
 *
 * @author system
 * @since 2024-01-01
 */
@Data
@Schema(description = "学生保存请求")
public class StudentSaveRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "学生ID(更新时必填)")
    private Long id;

    @NotBlank(message = "学号不能为空")
    @Size(min = 6, max = 12, message = "学号长度为6-12位")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "学号只能包含字母和数字")
    @Schema(description = "学号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String studentNo;

    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度为2-20个字符")
    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别值错误")
    @Max(value = 1, message = "性别值错误")
    @Schema(description = "性别(0-女,1-男)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer gender;

    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$", 
             message = "身份证号格式错误")
    @Schema(description = "身份证号")
    private String idCard;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @NotNull(message = "班级不能为空")
    @Schema(description = "班级ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classId;

    @Schema(description = "照片URL")
    private String photoUrl;
}
```

```java
package com.campus.card.domain.dto.request;

import com.campus.card.common.base.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生查询请求DTO
 *
 * @author system
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学生查询请求")
public class StudentQueryRequest extends BasePageRequest {

    @Schema(description = "关键字(姓名/学号)")
    private String keyword;

    @Schema(description = "班级ID")
    private Long classId;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "状态")
    private Integer status;
}
```

### 3.3 Service规范

```java
package com.campus.card.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.card.common.result.PageResult;
import com.campus.card.domain.entity.Student;
import com.campus.card.domain.dto.request.StudentQueryRequest;
import com.campus.card.domain.dto.request.StudentSaveRequest;
import com.campus.card.domain.vo.StudentVO;

import java.util.List;

/**
 * 学生服务接口
 *
 * @author system
 * @since 2024-01-01
 */
public interface IStudentService extends IService<Student> {

    /**
     * 分页查询学生列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<StudentVO> pageList(StudentQueryRequest request);

    /**
     * 根据ID获取学生详情
     *
     * @param id 学生ID
     * @return 学生详情
     */
    StudentVO getDetailById(Long id);

    /**
     * 保存学生(新增或更新)
     *
     * @param request 学生信息
     * @return 是否成功
     */
    boolean saveStudent(StudentSaveRequest request);

    /**
     * 批量删除学生
     *
     * @param ids 学生ID列表
     * @return 是否成功
     */
    boolean batchDelete(List<Long> ids);

    /**
     * 根据学号查询学生
     *
     * @param studentNo 学号
     * @return 学生信息
     */
    Student getByStudentNo(String studentNo);

    /**
     * 批量导入学生
     *
     * @param students 学生列表
     * @return 导入结果
     */
    ImportResult batchImport(List<StudentSaveRequest> students);

    /**
     * 导出学生列表
     *
     * @param request 查询条件
     * @return Excel文件字节数组
     */
    byte[] exportList(StudentQueryRequest request);
}
```

```java
package com.campus.card.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.card.common.exception.BusinessException;
import com.campus.card.common.result.PageResult;
import com.campus.card.domain.entity.Student;
import com.campus.card.domain.dto.request.StudentQueryRequest;
import com.campus.card.domain.dto.request.StudentSaveRequest;
import com.campus.card.domain.vo.StudentVO;
import com.campus.card.mapper.StudentMapper;
import com.campus.card.service.IStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学生服务实现类
 *
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> 
        implements IStudentService {

    @Override
    public PageResult<StudentVO> pageList(StudentQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StrUtil.isNotBlank(request.getKeyword()), w -> w
                .like(Student::getName, request.getKeyword())
                .or()
                .like(Student::getStudentNo, request.getKeyword())
        );
        wrapper.eq(request.getClassId() != null, Student::getClassId, request.getClassId());
        wrapper.eq(request.getGender() != null, Student::getGender, request.getGender());
        wrapper.eq(request.getStatus() != null, Student::getStatus, request.getStatus());
        wrapper.orderByDesc(Student::getCreateTime);

        // 分页查询
        IPage<Student> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<Student> result = this.page(page, wrapper);

        // 转换为VO
        List<StudentVO> voList = BeanUtil.copyToList(result.getRecords(), StudentVO.class);
        
        // 封装分页结果
        return PageResult.of(result.getTotal(), voList);
    }

    @Override
    public StudentVO getDetailById(Long id) {
        Student student = this.getById(id);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        return BeanUtil.copyProperties(student, StudentVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveStudent(StudentSaveRequest request) {
        // 检查学号唯一性
        if (checkStudentNoExists(request.getStudentNo(), request.getId())) {
            throw new BusinessException("学号已存在");
        }

        Student student = BeanUtil.copyProperties(request, Student.class);
        
        // 新增或更新
        return this.saveOrUpdate(student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new BusinessException("删除ID不能为空");
        }
        return this.removeByIds(ids);
    }

    @Override
    public Student getByStudentNo(String studentNo) {
        return this.lambdaQuery()
                .eq(Student::getStudentNo, studentNo)
                .one();
    }

    /**
     * 检查学号是否存在
     *
     * @param studentNo 学号
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    private boolean checkStudentNoExists(String studentNo, Long excludeId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStudentNo, studentNo);
        if (excludeId != null) {
            wrapper.ne(Student::getId, excludeId);
        }
        return this.count(wrapper) > 0;
    }
}
```

### 3.4 Controller规范

```java
package com.campus.card.controller;

import com.campus.card.common.annotation.Log;
import com.campus.card.common.annotation.Permission;
import com.campus.card.common.result.PageResult;
import com.campus.card.common.result.Result;
import com.campus.card.domain.dto.request.StudentQueryRequest;
import com.campus.card.domain.dto.request.StudentSaveRequest;
import com.campus.card.domain.vo.StudentVO;
import com.campus.card.service.IStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 学生管理控制器
 *
 * @author system
 * @since 2024-01-01
 */
@Tag(name = "学生管理", description = "学生信息的增删改查")
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final IStudentService studentService;

    @Operation(summary = "分页查询学生列表")
    @GetMapping
    @Permission("student:list")
    public Result<PageResult<StudentVO>> pageList(@Valid StudentQueryRequest request) {
        PageResult<StudentVO> result = studentService.pageList(request);
        return Result.success(result);
    }

    @Operation(summary = "获取学生详情")
    @Parameter(name = "id", description = "学生ID", required = true)
    @GetMapping("/{id}")
    @Permission("student:detail")
    public Result<StudentVO> getDetail(@PathVariable Long id) {
        StudentVO student = studentService.getDetailById(id);
        return Result.success(student);
    }

    @Operation(summary = "新增学生")
    @PostMapping
    @Permission("student:create")
    @Log(title = "新增学生", businessType = "INSERT")
    public Result<Void> create(@Valid @RequestBody StudentSaveRequest request) {
        studentService.saveStudent(request);
        return Result.success();
    }

    @Operation(summary = "更新学生")
    @PutMapping("/{id}")
    @Permission("student:update")
    @Log(title = "更新学生", businessType = "UPDATE")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody StudentSaveRequest request) {
        request.setId(id);
        studentService.saveStudent(request);
        return Result.success();
    }

    @Operation(summary = "删除学生")
    @DeleteMapping("/{id}")
    @Permission("student:delete")
    @Log(title = "删除学生", businessType = "DELETE")
    public Result<Void> delete(@PathVariable Long id) {
        studentService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "批量删除学生")
    @DeleteMapping("/batch")
    @Permission("student:delete")
    @Log(title = "批量删除学生", businessType = "DELETE")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        studentService.batchDelete(ids);
        return Result.success();
    }

    @Operation(summary = "导入学生")
    @PostMapping("/import")
    @Permission("student:import")
    @Log(title = "导入学生", businessType = "IMPORT")
    public Result<ImportResult> importData(@RequestParam("file") MultipartFile file) {
        ImportResult result = studentService.importExcel(file);
        return Result.success(result);
    }

    @Operation(summary = "导出学生")
    @PostMapping("/export")
    @Permission("student:export")
    @Log(title = "导出学生", businessType = "EXPORT")
    public void export(@RequestBody StudentQueryRequest request, HttpServletResponse response) {
        byte[] data = studentService.exportList(request);
        ExcelUtil.download(response, "学生列表.xlsx", data);
    }
}
```

### 3.5 统一响应格式

```java
package com.campus.card.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结果
 *
 * @author system
 * @since 2024-01-01
 */
@Data
@Schema(description = "统一响应结果")
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    @Schema(description = "响应码")
    private Integer code;

    /**
     * 响应消息
     */
    @Schema(description = "响应消息")
    private String message;

    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;

    /**
     * 时间戳
     */
    @Schema(description = "时间戳")
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> fail() {
        return new Result<>(500, "操作失败", null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
```

```java
package com.campus.card.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 *
 * @author system
 * @since 2024-01-01
 */
@Data
@Schema(description = "分页响应结果")
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数")
    private Long total;

    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
    private List<T> records;

    public PageResult() {
    }

    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    public static <T> PageResult<T> of(Long total, List<T> records) {
        return new PageResult<>(total, records);
    }
}
```

---

## 四、Claude提示词模板

### 4.1 实体类开发提示词

```
# 任务：开发校园一卡通系统的[实体名称]实体类

## 技术要求
- Spring Boot 3.2.x
- JDK 17
- MyBatis-Plus 3.5.x
- Lombok
- Swagger注解

## 表结构
表名: t_[table_name]

字段列表：
| 字段名 | 类型 | 长度 | 允许空 | 说明 | 默认值 |
|--------|------|------|--------|------|--------|
| id | BIGINT | - | NO | 主键ID | 自增 |
| [field_name] | VARCHAR | 50 | NO | [说明] | - |
| ... | ... | ... | ... | ... | ... |
| status | TINYINT | - | NO | 状态(0-禁用,1-启用) | 1 |
| create_time | DATETIME | - | NO | 创建时间 | CURRENT_TIMESTAMP |
| update_time | DATETIME | - | NO | 更新时间 | CURRENT_TIMESTAMP |
| create_by | BIGINT | - | YES | 创建人 | NULL |
| update_by | BIGINT | - | YES | 更新人 | NULL |
| deleted | TINYINT | - | NO | 删除标识(0-未删除,1-已删除) | 0 |

## 开发要求
1. 使用@TableName注解指定表名
2. 使用@TableId指定主键，类型为AUTO自增
3. 使用@TableField指定字段名（驼峰命名自动映射的可省略）
4. create_time和update_time使用@TableField的fill属性自动填充
5. deleted字段使用@TableLogic注解实现逻辑删除
6. 所有字段添加Swagger注解@Schema
7. 实现Serializable接口
8. 使用Lombok的@Data注解
9. 添加详细的字段注释

## 包路径
package com.campus.card.domain.entity;

请生成完整的实体类代码。
```

### 4.2 Service层开发提示词

```
# 任务：开发校园一卡通系统的[模块名称]Service层

## 技术要求
- Spring Boot 3.2.x
- JDK 17
- MyBatis-Plus 3.5.x
- 使用Lambda表达式构建查询条件
- 使用事务注解@Transactional

## 功能需求
[详细描述Service需要实现的功能，例如：]
1. 分页查询考勤记录（支持多条件筛选）
2. 获取考勤详情
3. 批量保存考勤记录
4. 考勤数据统计（按日期、班级、学生）
5. 导出考勤记录
6. 考勤异常处理

## 查询条件
- 学生ID（studentId）
- 班级ID（classId）
- 开始日期（startDate）
- 结束日期（endDate）
- 考勤状态（status: normal/late/early/absence）
- 打卡类型（checkType: card/face）

## 实体类
```java
public class AttendanceRecord {
    private Long id;
    private Long studentId;
    private Long classId;
    private Long deviceId;
    private LocalDateTime attendanceTime;
    private String attendanceType; // in/out
    private String checkType; // card/face
    private String photoUrl;
    private String status; // normal/late/early/absence
    // ... 其他字段
}
```

## 统计结果
```java
public class AttendanceStatistics {
    private Long totalCount;      // 总记录数
    private Long normalCount;     // 正常
    private Long lateCount;       // 迟到
    private Long earlyCount;      // 早退
    private Long absenceCount;    // 缺勤
    private Double attendanceRate; // 出勤率
}
```

## 开发要求
1. 接口继承IService<Entity>
2. 实现类继承ServiceImpl<Mapper, Entity>并实现接口
3. 分页查询使用MyBatis-Plus的Page
4. 使用LambdaQueryWrapper构建查询条件
5. 复杂查询需要在Mapper的XML中实现
6. 涉及数据修改的方法添加@Transactional注解
7. 异常情况抛出BusinessException
8. 添加详细的方法注释和参数说明
9. 使用@RequiredArgsConstructor注入依赖
10. 使用@Slf4j添加日志

## 包路径
- 接口: com.campus.card.service.IAttendanceService
- 实现: com.campus.card.service.impl.AttendanceServiceImpl

请生成完整的Service接口和实现类代码。
```

### 4.3 Controller层开发提示词

```
# 任务：开发校园一卡通系统的[模块名称]Controller

## 技术要求
- Spring Boot 3.2.x
- JDK 17
- RESTful API规范
- Swagger注解
- 参数校验（Jakarta Validation）

## API接口列表
[列出所有需要的接口]
1. GET /api/v1/attendance/records - 分页查询考勤记录
2. GET /api/v1/attendance/records/{id} - 获取考勤详情
3. GET /api/v1/attendance/statistics - 考勤统计
4. POST /api/v1/attendance/records/export - 导出考勤记录
5. PUT /api/v1/attendance/records/{id}/handle - 处理异常考勤

## 请求参数示例
```java
// 查询请求
public class AttendanceQueryRequest extends BasePageRequest {
    private Long studentId;
    private Long classId;
    private String startDate;
    private String endDate;
    private String status;
    private String checkType;
}

// 保存请求
public class AttendanceHandleRequest {
    @NotNull(message = "考勤记录ID不能为空")
    private Long recordId;
    
    @NotBlank(message = "处理结果不能为空")
    private String result;
    
    private String remark;
}
```

## 开发要求
1. 使用@RestController和@RequestMapping注解
2. 使用@Tag和@Operation添加Swagger文档
3. 所有请求参数使用@Valid进行校验
4. 使用统一的Result<T>包装响应结果
5. 使用@Permission注解进行权限控制
6. 使用@Log注解记录操作日志
7. 使用@RequiredArgsConstructor注入Service
8. 遵循RESTful规范：
   - GET: 查询
   - POST: 新增
   - PUT: 更新
   - DELETE: 删除
9. 路径参数使用@PathVariable
10. 查询参数使用@RequestParam或对象接收
11. 请求体使用@RequestBody

## 权限标识
格式: [模块]:[操作]
例如: attendance:list, attendance:export, attendance:handle

## 包路径
package com.campus.card.controller;

请生成完整的Controller代码。
```

### 4.4 设备对接开发提示词

```
# 任务：开发校园一卡通系统的[设备厂商]设备对接服务

## 设备信息
- 设备类型: [易科士消费机 / 海康考勤机]
- 通讯协议: [TCP/IP / SDK]
- 数据格式: [二进制 / JSON / XML]

## 功能需求

### 易科士消费机对接
1. 设备连接管理
   - TCP Socket连接
   - 心跳检测
   - 断线重连
   - 连接池管理

2. 消费数据采集
   - 实时数据接收
   - 数据解析（刷卡/人脸消费）
   - 数据存储
   - 数据去重

3. 参数下发
   - 黑白名单下发
   - 消费金额设置
   - 时段限制设置
   - 时间同步

4. 设备参数配置（按学校）
   - IP地址
   - 端口号
   - 设备编号
   - 通讯密钥
   - 心跳间隔

### 海康考勤设备对接
1. SDK集成
   - 海康SDK初始化
   - 设备登录
   - 设备登出
   - 资源释放

2. 人脸数据下发
   - 人脸照片上传
   - 人脸特征提取
   - 批量下发
   - 下发状态查询
   - 失败重试

3. 考勤数据采集
   - 实时数据订阅
   - 考勤照片获取
   - 数据解析存储
   - 历史数据补采

4. 基础数据下发
   - 人员信息下发
   - 卡片信息下发
   - 权限下发
   - 时间组下发

5. 设备参数配置（按学校）
   - 设备IP
   - 设备端口
   - 用户名密码
   - 识别阈值
   - 存储策略

## 技术要求
1. 使用@Service注解标记服务类
2. 设备连接使用线程池管理
3. 数据接收使用异步处理
4. 添加完善的异常处理和日志
5. 使用Redis缓存设备配置
6. 心跳检测使用定时任务
7. 数据解析使用策略模式
8. 支持多学校设备隔离

## 配置类示例
```java
public class DeviceConfig {
    private Long schoolId;      // 学校ID
    private String ip;          // 设备IP
    private Integer port;       // 端口
    private String deviceNo;    // 设备编号
    private String username;    // 用户名(海康)
    private String password;    // 密码(海康)
    private Map<String, Object> extParams; // 扩展参数
}
```

## 数据模型
```java
// 消费数据
public class ConsumptionData {
    private String deviceNo;    // 设备编号
    private String cardNo;      // 卡号
    private String studentNo;   // 学号(人脸识别)
    private BigDecimal amount;  // 消费金额
    private String consumeType; // 消费类型(card/face)
    private LocalDateTime consumeTime; // 消费时间
}

// 考勤数据
public class AttendanceData {
    private String deviceNo;
    private String cardNo;
    private String studentNo;
    private String checkType;   // card/face
    private String attendanceType; // in/out
    private String photoUrl;    // 抓拍照片
    private LocalDateTime checkTime;
}
```

## 开发要求
1. 创建设备客户端类封装SDK操作
2. 创建数据解析器解析设备数据
3. 创建命令构建器构建下发命令
4. 使用线程安全的数据结构
5. 添加详细的错误处理和重试机制
6. 记录设备操作日志
7. 提供设备状态监控接口

## 包路径
- com.campus.card.integration.device.yikeshi.YikeshiClient
- com.campus.card.integration.device.hikvision.HikVisionSdkClient
- com.campus.card.service.device.IYikeshiService
- com.campus.card.service.device.IHikVisionService

请生成完整的设备对接代码，包括客户端、服务接口和实现。
```

### 4.5 配置类开发提示词

```
# 任务：开发校园一卡通系统的配置类

## 配置类型
- [ ] MyBatis-Plus配置
- [ ] Redis配置
- [ ] Sa-Token配置
- [ ] Swagger配置
- [ ] 线程池配置
- [ ] CORS跨域配置

## MyBatis-Plus配置要求
1. 配置分页插件
2. 配置逻辑删除
3. 配置自动填充（create_time, update_time等）
4. 配置性能分析插件（仅开发环境）
5. 配置乐观锁插件

```java
@Configuration
@MapperScan("com.campus.card.mapper")
public class MybatisPlusConfig {
    
    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
    
    /**
     * 自动填充
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                // 从上下文获取当前用户ID
                Long userId = getCurrentUserId();
                if (userId != null) {
                    this.strictInsertFill(metaObject, "createBy", Long.class, userId);
                    this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                Long userId = getCurrentUserId();
                if (userId != null) {
                    this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
                }
            }
            
            private Long getCurrentUserId() {
                // 从SaToken或ThreadLocal获取当前用户ID
                return null;
            }
        };
    }
}
```

## Redis配置要求
1. 配置RedisTemplate
2. 配置序列化方式（推荐Jackson2JsonRedisSerializer）
3. 配置Key前缀
4. 配置缓存管理器

## Sa-Token配置要求
1. 配置Token有效期
2. 配置Token前缀
3. 配置踢人下线
4. 配置并发登录
5. 配置权限认证
6. 配置路由拦截器

## Swagger配置要求
1. 配置接口文档标题、描述、版本
2. 配置认证方式（Bearer Token）
3. 配置全局参数
4. 配置分组
5. 生产环境关闭

## 线程池配置要求
1. 核心线程数: CPU核数 * 2
2. 最大线程数: CPU核数 * 4
3. 队列容量: 1000
4. 拒绝策略: CallerRunsPolicy
5. 线程名称前缀

## 包路径
package com.campus.card.config;

请生成指定类型的完整配置类代码，包含详细注释。
```

### 4.6 工具类开发提示词

```
# 任务：开发校园一卡通系统的[工具类名称]

## 工具类类型
- [ ] Redis工具类
- [ ] Excel工具类
- [ ] 文件工具类
- [ ] 加密工具类
- [ ] 日期工具类

## 功能需求

### Redis工具类
```java
public class RedisUtil {
    // 字符串操作
    void set(String key, Object value);
    void set(String key, Object value, long timeout);
    Object get(String key);
    boolean delete(String key);
    boolean hasKey(String key);
    
    // 哈希操作
    void hPut(String key, String hashKey, Object value);
    Object hGet(String key, String hashKey);
    Map<Object, Object> hGetAll(String key);
    
    // 列表操作
    long lPush(String key, Object value);
    Object lPop(String key);
    List<Object> lRange(String key, long start, long end);
    
    // 集合操作
    long sAdd(String key, Object... values);
    Set<Object> sMembers(String key);
    
    // 有序集合操作
    boolean zAdd(String key, Object value, double score);
    Set<Object> zRange(String key, long start, long end);
    
    // 设置过期时间
    boolean expire(String key, long timeout);
    long getExpire(String key);
}
```

### Excel工具类（使用EasyExcel）
```java
public class ExcelUtil {
    // 导出Excel
    <T> void export(HttpServletResponse response, String fileName, 
                   Class<T> clazz, List<T> data);
    <T> byte[] exportToBytes(Class<T> clazz, List<T> data);
    
    // 导入Excel
    <T> List<T> importExcel(MultipartFile file, Class<T> clazz);
    <T> List<T> importExcel(InputStream inputStream, Class<T> clazz);
    
    // 下载模板
    <T> void downloadTemplate(HttpServletResponse response, 
                             String fileName, Class<T> clazz);
}
```

## 技术要求
1. 工具类使用private构造函数防止实例化
2. 方法使用static修饰
3. 添加详细的JavaDoc注释
4. 添加参数校验
5. 添加异常处理
6. 使用泛型增强通用性
7. Redis操作需要处理序列化
8. Excel操作需要处理流关闭

## 包路径
package com.campus.card.utils;

请生成完整的工具类代码。
```

---

## 五、应用配置规范

### 5.1 application.yml主配置

```yaml
server:
  port: 8080
  servlet:
    context-path: /
  tomcat:
    uri-encoding: UTF-8
    threads:
      max: 200
      min-spare: 10

spring:
  application:
    name: campus-card-system
  profiles:
    active: @profiles.active@
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 100MB
  jackson:
    time-zone: GMT+8
    date-format: yyyy-MM-dd HH:mm:ss
    serialization:
      write-dates-as-timestamps: false

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.campus.card.domain.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# Knife4j文档配置
knife4j:
  enable: true
  setting:
    language: zh_cn

# Sa-Token配置
sa-token:
  token-name: Authorization
  token-prefix: Bearer
  timeout: 2592000
  active-timeout: -1
  is-concurrent: true
  is-share: false
  token-style: uuid
  is-log: false

# 日志配置
logging:
  level:
    root: INFO
    com.campus.card: DEBUG
  file:
    name: logs/campus-card.log
```

### 5.2 application-dev.yml开发环境

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/campus_card?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
    username: root
    password: 123456
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-timeout: 30000
  
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms

# 设备配置
device:
  yikeshi:
    enabled: true
    heartbeat-interval: 30000
    reconnect-interval: 5000
  hikvision:
    enabled: true
    sdk-path: /path/to/sdk
```

---

## 六、数据库设计规范

### 6.1 表命名规范
- 表名前缀: t_
- 使用小写字母和下划线
- 见名知意，简洁明了
- 例如: t_student, t_teacher, t_attendance_record

### 6.2 字段命名规范
- 主键: id (BIGINT, AUTO_INCREMENT)
- 外键: [关联表]_id (例如: student_id, class_id)
- 状态字段: status (TINYINT)
- 时间字段: [操作]_time (例如: create_time, update_time)
- 删除标识: deleted (TINYINT, 0-未删除, 1-已删除)

### 6.3 必备字段
```sql
id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
status TINYINT DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
create_by BIGINT COMMENT '创建人',
update_by BIGINT COMMENT '更新人',
deleted TINYINT DEFAULT 0 COMMENT '删除标识(0-未删除,1-已删除)'
```

### 6.4 索引规范
- 主键自动创建主键索引
- 外键字段创建普通索引
- 频繁查询的字段创建索引
- 联合查询创建联合索引
- 索引命名: idx_[表名]_[字段名]
- 唯一索引命名: uk_[表名]_[字段名]

### 6.5 建表示例

```sql
CREATE TABLE t_student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_no VARCHAR(20) NOT NULL COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender TINYINT NOT NULL COMMENT '性别(0-女,1-男)',
    id_card VARCHAR(18) COMMENT '身份证号',
    phone VARCHAR(11) NOT NULL COMMENT '手机号',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    school_id BIGINT NOT NULL COMMENT '学校ID',
    photo_url VARCHAR(255) COMMENT '照片URL',
    status TINYINT DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标识',
    UNIQUE KEY uk_student_no (student_no, deleted),
    KEY idx_class_id (class_id),
    KEY idx_school_id (school_id),
    KEY idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';
```

---

## 七、异常处理规范

### 7.1 自定义异常

```java
package com.campus.card.common.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author system
 * @since 2024-01-01
 */
@Getter
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }
}
```

### 7.2 全局异常处理

```java
package com.campus.card.common.exception;

import com.campus.card.common.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author system
 * @since 2024-01-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常 - @RequestBody
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验失败: {}", message);
        return Result.fail(400, message);
    }

    /**
     * 参数校验异常 - @RequestParam
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验失败: {}", message);
        return Result.fail(400, message);
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数绑定失败: {}", message);
        return Result.fail(400, message);
    }

    /**
     * 运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return Result.fail("系统异常，请联系管理员");
    }

    /**
     * 其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail("系统异常，请联系管理员");
    }
}
```

---

## 八、日志规范

### 8.1 日志级别使用
- **ERROR**: 系统错误、异常信息
- **WARN**: 警告信息、潜在问题
- **INFO**: 重要业务流程、关键操作
- **DEBUG**: 调试信息、详细日志（仅开发环境）

### 8.2 日志记录规范

```java
@Slf4j
@Service
public class StudentServiceImpl implements IStudentService {

    @Override
    public boolean saveStudent(StudentSaveRequest request) {
        log.info("保存学生信息开始, studentNo: {}", request.getStudentNo());
        
        try {
            // 业务逻辑
            Student student = convertToEntity(request);
            boolean result = this.saveOrUpdate(student);
            
            log.info("保存学生信息成功, studentId: {}", student.getId());
            return result;
            
        } catch (Exception e) {
            log.error("保存学生信息失败, studentNo: {}, error: {}", 
                     request.getStudentNo(), e.getMessage(), e);
            throw new BusinessException("保存学生信息失败");
        }
    }
}
```

### 8.3 操作日志注解

```java
package com.campus.card.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * @author system
 * @since 2024-01-01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作标题
     */
    String title() default "";

    /**
     * 业务类型
     */
    String businessType() default "";

    /**
     * 是否保存请求参数
     */
    boolean saveRequestData() default true;

    /**
     * 是否保存响应结果
     */
    boolean saveResponseData() default false;
}
```

---

## 九、安全规范

### 9.1 密码加密

```java
package com.campus.card.utils;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码工具类
 *
 * @author system
 * @since 2024-01-01
 */
public class PasswordUtil {

    /**
     * 加密密码
     */
    public static String encrypt(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * 验证密码
     */
    public static boolean verify(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}
```

### 9.2 数据权限

```java
package com.campus.card.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 *
 * @author system
 * @since 2024-01-01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 数据权限字段（部门表的别名）
     */
    String deptAlias() default "";

    /**
     * 数据权限字段（用户表的别名）
     */
    String userAlias() default "";

    /**
     * 权限类型
     * ALL: 所有数据权限
     * SCHOOL: 本校数据权限
     * DEPT: 本部门数据权限
     * SELF: 仅本人数据权限
     */
    String permission() default "SCHOOL";
}
```

### 9.3 接口权限

```java
package com.campus.card.common.annotation;

import java.lang.annotation.*;

/**
 * 权限注解
 *
 * @author system
 * @since 2024-01-01
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permission {

    /**
     * 权限标识
     * 格式: 模块:操作
     * 例如: student:list, student:create
     */
    String value() default "";

    /**
     * 验证模式
     * AND: 必须拥有所有权限
     * OR: 拥有任意一个权限即可
     */
    String mode() default "AND";
}
```

---

## 十、性能优化规范

### 10.1 数据库查询优化

```java
@Service
public class StudentServiceImpl implements IStudentService {

    // 1. 分页查询，避免一次查询大量数据
    @Override
    public PageResult<StudentVO> pageList(StudentQueryRequest request) {
        IPage<Student> page = new Page<>(request.getCurrent(), request.getSize());
        // ...
    }

    // 2. 使用MyBatis-Plus的selectBatchIds批量查询
    @Override
    public List<Student> getByIds(List<Long> ids) {
        return this.listByIds(ids);
    }

    // 3. 只查询需要的字段
    @Override
    public List<StudentVO> listSimple() {
        return this.lambdaQuery()
                .select(Student::getId, Student::getName, Student::getStudentNo)
                .list()
                .stream()
                .map(s -> BeanUtil.copyProperties(s, StudentVO.class))
                .collect(Collectors.toList());
    }

    // 4. 避免N+1查询，使用关联查询或批量查询
    @Override
    public List<StudentVO> listWithClass() {
        List<Student> students = this.list();
        if (CollUtil.isEmpty(students)) {
            return Collections.emptyList();
        }
        
        // 批量查询班级信息
        Set<Long> classIds = students.stream()
                .map(Student::getClassId)
                .collect(Collectors.toSet());
        Map<Long, ClassInfo> classMap = classService.getByIds(classIds)
                .stream()
                .collect(Collectors.toMap(ClassInfo::getId, c -> c));
        
        // 组装数据
        return students.stream()
                .map(s -> {
                    StudentVO vo = BeanUtil.copyProperties(s, StudentVO.class);
                    vo.setClassName(classMap.get(s.getClassId()).getName());
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
```

### 10.2 缓存使用

```java
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements IStudentService {

    private final RedisUtil redisUtil;
    private static final String CACHE_KEY_PREFIX = "student:";
    private static final long CACHE_EXPIRE = 3600; // 1小时

    @Override
    public StudentVO getDetailById(Long id) {
        // 先从缓存获取
        String cacheKey = CACHE_KEY_PREFIX + id;
        StudentVO cached = (StudentVO) redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        Student student = this.getById(id);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        StudentVO vo = BeanUtil.copyProperties(student, StudentVO.class);
        
        // 写入缓存
        redisUtil.set(cacheKey, vo, CACHE_EXPIRE);
        
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveStudent(StudentSaveRequest request) {
        Student student = BeanUtil.copyProperties(request, Student.class);
        boolean result = this.saveOrUpdate(student);
        
        // 更新后删除缓存
        if (result && student.getId() != null) {
            String cacheKey = CACHE_KEY_PREFIX + student.getId();
            redisUtil.delete(cacheKey);
        }
        
        return result;
    }
}
```

### 10.3 异步处理

```java
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements IAttendanceService {

    @Async("taskExecutor")
    @Override
    public void batchSaveAttendance(List<AttendanceRecord> records) {
        log.info("开始批量保存考勤记录，数量: {}", records.size());
        
        // 分批保存，每次1000条
        int batchSize = 1000;
        for (int i = 0; i < records.size(); i += batchSize) {
            int end = Math.min(i + batchSize, records.size());
            List<AttendanceRecord> batch = records.subList(i, end);
            this.saveBatch(batch);
        }
        
        log.info("批量保存考勤记录完成");
    }
}
```

---

## 十一、测试规范

### 11.1 单元测试示例

```java
package com.campus.card.service;

import com.campus.card.domain.dto.request.StudentSaveRequest;
import com.campus.card.domain.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 学生服务测试类
 *
 * @author system
 * @since 2024-01-01
 */
@SpringBootTest
@Transactional // 测试完成后回滚
class StudentServiceTest {

    @Autowired
    private IStudentService studentService;

    @Test
    void testSaveStudent() {
        // 准备测试数据
        StudentSaveRequest request = new StudentSaveRequest();
        request.setStudentNo("202401001");
        request.setName("张三");
        request.setGender(1);
        request.setPhone("13800138000");
        request.setClassId(1L);

        // 执行测试
        boolean result = studentService.saveStudent(request);

        // 断言
        assertTrue(result);
        
        // 验证数据
        Student saved = studentService.getByStudentNo("202401001");
        assertNotNull(saved);
        assertEquals("张三", saved.getName());
    }

    @Test
    void testGetDetailById() {
        // 测试查询不存在的学生
        assertThrows(BusinessException.class, () -> {
            studentService.getDetailById(999999L);
        });
    }
}
```

---

## 十二、部署规范

### 12.1 Maven打包配置

```xml
<build>
    <finalName>${project.artifactId}</finalName>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 12.2 启动脚本

```bash
#!/bin/bash

APP_NAME=campus-card-backend
JAR_NAME=${APP_NAME}.jar
JVM_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC"
SPRING_PROFILE="prod"

# 停止旧进程
PID=$(ps -ef | grep ${JAR_NAME} | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "停止旧进程: $PID"
    kill -15 $PID
    sleep 5
fi

# 启动新进程
nohup java ${JVM_OPTS} -jar ${JAR_NAME} --spring.profiles.active=${SPRING_PROFILE} > app.log 2>&1 &

echo "应用启动成功"
```

---

## 十三、代码审查清单

### 13.1 代码质量
- [ ] 遵循阿里巴巴Java开发规范
- [ ] 使用JDK 17特性（record、sealed、pattern matching等）
- [ ] 代码格式统一（使用IDEA格式化）
- [ ] 无编译警告
- [ ] 无未使用的导入和变量
- [ ] 合理的命名（类名、方法名、变量名）

### 13.2 功能实现
- [ ] 功能完整，符合需求
- [ ] 参数校验完整
- [ ] 异常处理完善
- [ ] 事务控制正确
- [ ] 日志记录完整

### 13.3 性能与安全
- [ ] 数据库查询优化（索引、分页）
- [ ] 避免N+1查询
- [ ] 合理使用缓存
- [ ] SQL注入防护
- [ ] XSS攻击防护
- [ ] 敏感数据加密

### 13.4 文档
- [ ] 类和方法注释完整
- [ ] API文档（Swagger）完整
- [ ] 复杂逻辑有说明注释

---

## 十四、常见问题与解决方案

### 14.1 JDK 17迁移注意事项

```java
// 1. 使用record简化DTO
public record StudentDTO(
    Long id,
    String studentNo,
    String name,
    Integer gender
) {}

// 2. 使用sealed类限制继承
public sealed interface Result<T> 
    permits SuccessResult, FailResult {}

// 3. 使用pattern matching
if (obj instanceof String s) {
    System.out.println(s.toUpperCase());
}

// 4. 使用switch expressions
String result = switch (status) {
    case 0 -> "禁用";
    case 1 -> "启用";
    default -> "未知";
};
```

### 14.2 MyBatis-Plus常见问题

```java
// 1. 逻辑删除后查询包含已删除数据
// 解决：检查@TableLogic注解和配置

// 2. 自动填充不生效
// 解决：检查MetaObjectHandler配置和字段注解

// 3. 分页插件不生效
// 解决：检查MybatisPlusInterceptor配置

// 4. 批量操作性能问题
// 解决：使用saveBatch，设置合理的batchSize
this.saveBatch(list, 1000);
```

---

**此规范为校园一卡通系统后端开发的标准文档，所有开发人员必须严格遵守。使用Claude或其他AI工具时，请参考本文档中的提示词模板，确保生成的代码符合项目规范。**