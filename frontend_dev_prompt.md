# 校园一卡通系统 - 前端开发规范与Claude提示词模板

## 一、技术栈规范

### 1.1 核心技术
- **框架**: Vue.js 3.x (Composition API)
- **UI组件库**: Ant Design Vue 4.x
- **状态管理**: Pinia
- **路由管理**: Vue Router 4.x
- **HTTP客户端**: Axios
- **构建工具**: Vite 5.x
- **包管理器**: pnpm
- **TypeScript**: 5.x (强制使用)

### 1.2 辅助工具
- **代码规范**: ESLint + Prettier
- **CSS预处理器**: Less / Sass
- **工具库**: 
  - lodash-es (工具函数)
  - dayjs (日期处理)
  - crypto-js (加密)
- **图表库**: ECharts 5.x
- **表格增强**: vxe-table (复杂表格)
- **图标**: @ant-design/icons-vue

---

## 二、项目结构规范

```
campus-card-frontend/
├── public/                    # 静态资源
├── src/
│   ├── api/                   # API接口定义
│   │   ├── modules/          # 按模块划分
│   │   │   ├── card.ts       # 卡务管理
│   │   │   ├── student.ts    # 学生管理
│   │   │   ├── teacher.ts    # 教师管理
│   │   │   ├── attendance.ts # 考勤管理
│   │   │   ├── consumption.ts # 消费管理
│   │   │   └── ...
│   │   ├── request.ts        # Axios封装
│   │   └── types.ts          # API类型定义
│   ├── assets/               # 资源文件
│   │   ├── images/
│   │   ├── styles/           # 全局样式
│   │   └── icons/
│   ├── components/           # 公共组件
│   │   ├── common/           # 通用组件
│   │   │   ├── SearchForm/   # 搜索表单
│   │   │   ├── TablePage/    # 分页表格
│   │   │   ├── UploadImage/  # 图片上传
│   │   │   └── ...
│   │   └── business/         # 业务组件
│   │       ├── CardSelector/ # 卡片选择器
│   │       ├── StudentSelector/ # 学生选择器
│   │       └── ...
│   ├── composables/          # 组合式函数
│   │   ├── useTable.ts       # 表格逻辑
│   │   ├── useForm.ts        # 表单逻辑
│   │   ├── usePermission.ts  # 权限判断
│   │   └── ...
│   ├── directives/           # 自定义指令
│   │   ├── permission.ts     # 权限指令
│   │   └── loading.ts        # 加载指令
│   ├── hooks/                # 自定义Hooks
│   ├── layout/               # 布局组件
│   │   ├── BasicLayout.vue   # 基础布局
│   │   ├── BlankLayout.vue   # 空白布局
│   │   └── components/
│   ├── router/               # 路由配置
│   │   ├── index.ts
│   │   ├── routes.ts
│   │   └── guard.ts          # 路由守卫
│   ├── stores/               # Pinia状态管理
│   │   ├── modules/
│   │   │   ├── user.ts       # 用户状态
│   │   │   ├── app.ts        # 应用状态
│   │   │   ├── permission.ts # 权限状态
│   │   │   └── ...
│   │   └── index.ts
│   ├── utils/                # 工具函数
│   │   ├── auth.ts           # 认证工具
│   │   ├── storage.ts        # 存储工具
│   │   ├── validate.ts       # 验证工具
│   │   ├── format.ts         # 格式化工具
│   │   └── ...
│   ├── views/                # 页面组件
│   │   ├── login/            # 登录
│   │   ├── dashboard/        # 仪表盘
│   │   ├── card/             # 卡务管理
│   │   ├── student/          # 学生管理
│   │   ├── teacher/          # 教师管理
│   │   ├── parent/           # 家长管理
│   │   ├── class/            # 班级管理
│   │   ├── school/           # 学校管理
│   │   ├── face/             # 人脸管理
│   │   ├── device/           # 设备管理
│   │   │   ├── consumption/  # 消费机
│   │   │   └── attendance/   # 考勤机
│   │   ├── attendance/       # 考勤管理
│   │   ├── consumption/      # 消费管理
│   │   ├── system/           # 系统管理
│   │   │   ├── user/         # 用户管理
│   │   │   ├── role/         # 角色管理
│   │   │   └── permission/   # 权限管理
│   │   └── report/           # 报表统计
│   ├── App.vue
│   ├── main.ts
│   └── types/                # TypeScript类型定义
│       ├── global.d.ts
│       ├── api.d.ts
│       └── ...
├── .env.development          # 开发环境变量
├── .env.production           # 生产环境变量
├── .eslintrc.cjs             # ESLint配置
├── .prettierrc.json          # Prettier配置
├── tsconfig.json             # TypeScript配置
├── vite.config.ts            # Vite配置
└── package.json
```

---

## 三、编码规范

### 3.1 Vue组件规范
```typescript
// 组件命名：PascalCase
// 文件命名：PascalCase.vue

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import type { FormInstance } from 'ant-design-vue'

// 1. 定义Props
interface Props {
  visible: boolean
  data?: StudentInfo
}
const props = withDefaults(defineProps<Props>(), {
  data: undefined
})

// 2. 定义Emits
interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void
}
const emit = defineEmits<Emits>()

// 3. 响应式数据
const loading = ref(false)
const formRef = ref<FormInstance>()
const formState = reactive({
  name: '',
  age: 0
})

// 4. 计算属性
const isEdit = computed(() => !!props.data?.id)

// 5. 方法
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true
    // 提交逻辑
    emit('success')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 6. 生命周期
onMounted(() => {
  // 初始化逻辑
})
</script>

<template>
  <a-modal
    :open="visible"
    :title="isEdit ? '编辑' : '新增'"
    @cancel="emit('update:visible', false)"
  >
    <a-form ref="formRef" :model="formState">
      <!-- 表单内容 -->
    </a-form>
  </a-modal>
</template>

<style scoped lang="less">
// 样式
</style>
```

### 3.2 API接口规范
```typescript
// src/api/modules/student.ts
import request from '../request'
import type { PageParams, PageResult } from '../types'

export interface StudentInfo {
  id: number
  name: string
  studentNo: string
  classId: number
  // ...
}

export interface StudentQuery extends PageParams {
  name?: string
  classId?: number
}

// 获取学生列表
export const getStudentList = (params: StudentQuery) => {
  return request.get<PageResult<StudentInfo>>('/api/v1/students', { params })
}

// 获取学生详情
export const getStudentDetail = (id: number) => {
  return request.get<StudentInfo>(`/api/v1/students/${id}`)
}

// 创建学生
export const createStudent = (data: Omit<StudentInfo, 'id'>) => {
  return request.post<StudentInfo>('/api/v1/students', data)
}

// 更新学生
export const updateStudent = (id: number, data: Partial<StudentInfo>) => {
  return request.put<StudentInfo>(`/api/v1/students/${id}`, data)
}

// 删除学生
export const deleteStudent = (id: number) => {
  return request.delete(`/api/v1/students/${id}`)
}
```

### 3.3 Pinia Store规范
```typescript
// src/stores/modules/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types'
import { getUserInfo, login } from '@/api/modules/auth'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])

  // Getters
  const isLogin = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.name || '')

  // Actions
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
  }

  const loginAction = async (username: string, password: string) => {
    const res = await login({ username, password })
    setToken(res.token)
    await fetchUserInfo()
  }

  const fetchUserInfo = async () => {
    const res = await getUserInfo()
    setUserInfo(res)
    permissions.value = res.permissions
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    permissions,
    isLogin,
    userName,
    setToken,
    loginAction,
    logout
  }
})
```

### 3.4 Composable规范
```typescript
// src/composables/useTable.ts
import { ref, reactive } from 'vue'
import type { TableProps } from 'ant-design-vue'

export interface UseTableOptions<T = any> {
  fetchApi: (params: any) => Promise<PageResult<T>>
  immediate?: boolean
}

export const useTable = <T = any>(options: UseTableOptions<T>) => {
  const { fetchApi, immediate = true } = options

  const loading = ref(false)
  const dataSource = ref<T[]>([])
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0
  })

  const fetchData = async (params?: any) => {
    try {
      loading.value = true
      const res = await fetchApi({
        page: pagination.current,
        size: pagination.pageSize,
        ...params
      })
      dataSource.value = res.records
      pagination.total = res.total
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  }

  const handleTableChange: TableProps['onChange'] = (pag) => {
    pagination.current = pag.current || 1
    pagination.pageSize = pag.pageSize || 10
    fetchData()
  }

  const refresh = () => {
    pagination.current = 1
    fetchData()
  }

  if (immediate) {
    fetchData()
  }

  return {
    loading,
    dataSource,
    pagination,
    fetchData,
    handleTableChange,
    refresh
  }
}
```

---

## 四、Claude提示词模板

### 4.1 页面开发提示词模板

```
# 任务：开发校园一卡通系统的[模块名称]页面

## 技术栈要求
- Vue 3.x (Composition API + TypeScript)
- Ant Design Vue 4.x
- Pinia状态管理
- Axios HTTP请求

## 功能需求
[详细描述页面功能，例如：]
1. 显示学生列表，支持分页
2. 支持按姓名、学号、班级搜索
3. 支持新增、编辑、删除学生
4. 支持批量导入学生
5. 支持查看学生详情

## 页面结构要求
1. 顶部搜索区域（SearchForm组件）
2. 操作按钮区域（新增、批量导入、导出等）
3. 数据表格区域（支持分页）
4. 新增/编辑弹窗（Modal）

## 代码规范要求
1. 使用TypeScript，定义完整的类型
2. 使用Composition API的<script setup>语法
3. 使用useTable组合式函数处理表格逻辑
4. 表单验证使用Ant Design Vue的规则
5. API接口调用需要错误处理和loading状态
6. 所有用户操作需要成功/失败提示
7. 代码注释清晰，关键逻辑需要注释

## 表格字段
[列出表格需要显示的字段]
- 学号 (studentNo)
- 姓名 (name)
- 性别 (gender)
- 班级 (className)
- 卡号 (cardNo)
- 状态 (status)
- 操作列

## API接口
[列出需要调用的API接口]
- GET /api/v1/students - 获取学生列表
- GET /api/v1/students/:id - 获取学生详情
- POST /api/v1/students - 创建学生
- PUT /api/v1/students/:id - 更新学生
- DELETE /api/v1/students/:id - 删除学生

## 注意事项
1. 删除操作需要二次确认
2. 批量操作需要选中至少一条数据
3. 表单提交前需要验证
4. 权限控制：使用v-permission指令控制按钮显示
5. 响应式设计：适配不同屏幕尺寸

请生成完整的Vue组件代码，包括：
1. 主页面组件（List页面）
2. 新增/编辑弹窗组件
3. API接口定义文件
4. TypeScript类型定义
```

### 4.2 组件开发提示词模板

```
# 任务：开发校园一卡通系统的[组件名称]公共组件

## 组件类型
- [ ] 业务组件
- [ ] 通用组件
- [ ] 表单组件
- [ ] 展示组件

## 功能需求
[详细描述组件功能，例如：]
开发一个学生选择器组件，用于在多个页面选择学生。

功能要求：
1. 支持单选/多选模式
2. 支持搜索（按姓名、学号）
3. 支持按班级筛选
4. 支持分页加载
5. 支持默认值回显
6. 支持禁用状态

## Props定义
```typescript
interface Props {
  modelValue: number | number[] // v-model绑定值
  mode?: 'single' | 'multiple' // 选择模式
  classId?: number // 班级ID筛选
  disabled?: boolean // 禁用状态
  placeholder?: string
}
```

## Emits定义
```typescript
interface Emits {
  (e: 'update:modelValue', value: number | number[]): void
  (e: 'change', value: number | number[], records: StudentInfo[]): void
}
```

## 代码规范要求
1. 使用TypeScript定义Props和Emits
2. 使用<script setup>语法
3. 支持v-model双向绑定
4. 组件需要提供清空方法
5. 使用Ant Design Vue的Select或Modal组件
6. 添加详细的注释和使用示例

## API接口
- GET /api/v1/students - 获取学生列表（支持搜索和筛选）

## 使用示例
请在组件顶部注释中提供使用示例：
```vue
<template>
  <StudentSelector
    v-model="selectedIds"
    mode="multiple"
    :class-id="classId"
    @change="handleChange"
  />
</template>
```

请生成完整的组件代码。
```

### 4.3 API接口开发提示词模板

```
# 任务：开发校园一卡通系统的[模块名称]API接口定义

## 技术要求
- 使用TypeScript
- 使用封装好的request实例
- 定义完整的接口类型
- 使用RESTful规范

## 接口列表
[列出需要定义的接口]
1. 获取考勤记录列表（分页、搜索）
2. 获取考勤详情
3. 导出考勤记录
4. 考勤数据统计
5. 考勤异常处理

## 类型定义要求
```typescript
// 1. 定义实体类型
export interface AttendanceRecord {
  id: number
  studentId: number
  studentName: string
  studentNo: string
  classId: number
  className: string
  deviceId: number
  deviceName: string
  attendanceTime: string
  attendanceType: 'in' | 'out' // 进/出
  checkType: 'card' | 'face' // 刷卡/人脸
  photoUrl?: string
  status: 'normal' | 'late' | 'early' | 'absence'
  remark?: string
}

// 2. 定义查询参数类型
export interface AttendanceQuery extends PageParams {
  studentId?: number
  classId?: number
  startDate?: string
  endDate?: string
  status?: string
  checkType?: string
}

// 3. 定义统计类型
export interface AttendanceStatistics {
  totalCount: number
  normalCount: number
  lateCount: number
  earlyCount: number
  absenceCount: number
  attendanceRate: number
}
```

## 接口规范
1. 所有接口返回统一的响应格式
2. 列表接口返回PageResult<T>类型
3. 详情接口返回实体类型
4. 删除接口返回void
5. 统计接口返回统计类型

## 错误处理
接口调用错误由request拦截器统一处理，无需在接口定义中处理。

请生成完整的API接口定义文件，包括所有类型定义和接口方法。
```

### 4.4 状态管理提示词模板

```
# 任务：开发校园一卡通系统的[模块名称]Pinia Store

## 功能需求
[详细描述Store需要管理的状态和功能，例如：]
开发考勤管理的状态管理Store，用于：
1. 缓存考勤统计数据
2. 管理当前选择的日期范围
3. 管理考勤筛选条件
4. 提供考勤数据刷新方法

## State定义
```typescript
interface State {
  dateRange: [string, string] // 日期范围
  filterConditions: AttendanceQuery // 筛选条件
  statistics: AttendanceStatistics | null // 统计数据
  recentRecords: AttendanceRecord[] // 最近记录
  loading: boolean
}
```

## Actions需求
1. setDateRange - 设置日期范围
2. setFilterConditions - 设置筛选条件
3. fetchStatistics - 获取统计数据
4. fetchRecentRecords - 获取最近记录
5. refreshData - 刷新所有数据
6. clearCache - 清空缓存

## Getters需求
1. isToday - 判断是否查询今天
2. formattedDateRange - 格式化的日期范围
3. hasData - 是否有数据

## 代码规范
1. 使用Composition API风格（setup语法）
2. 使用TypeScript定义类型
3. 异步操作需要错误处理
4. 提供loading状态
5. 数据更新需要响应式

请生成完整的Pinia Store代码。
```

### 4.5 表单验证提示词模板

```
# 任务：为校园一卡通系统的[表单名称]定义表单验证规则

## 表单字段
[列出表单字段]
1. 学号 (studentNo) - 必填，6-12位字符
2. 姓名 (name) - 必填，2-20个字符
3. 性别 (gender) - 必填
4. 身份证号 (idCard) - 可选，18位身份证格式
5. 手机号 (phone) - 必填，11位手机号格式
6. 邮箱 (email) - 可选，邮箱格式
7. 班级 (classId) - 必填

## 验证规则要求
使用Ant Design Vue的表单验证规则格式：

```typescript
import type { Rule } from 'ant-design-vue/es/form'

export const studentFormRules: Record<string, Rule[]> = {
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 6, max: 12, message: '学号长度为6-12位', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]+$/, message: '学号只能包含字母和数字', trigger: 'blur' }
  ],
  // ... 其他字段规则
}
```

## 特殊验证需求
[描述特殊的验证逻辑]
1. 学号唯一性验证（调用API检查）
2. 手机号格式验证
3. 身份证号校验码验证
4. 邮箱格式验证

## 自定义验证器
对于复杂验证，需要定义自定义验证器：
```typescript
const validateStudentNo = async (rule: Rule, value: string) => {
  if (!value) return Promise.reject('请输入学号')
  // 调用API检查唯一性
  const exists = await checkStudentNoExists(value)
  if (exists) return Promise.reject('学号已存在')
  return Promise.resolve()
}
```

请生成完整的表单验证规则代码，包括所有字段的验证规则和自定义验证器。
```

---

## 五、开发流程规范

### 5.1 功能开发流程
1. **需求分析** - 明确页面功能和交互
2. **API接口** - 先定义接口类型和方法
3. **Store状态** - 如需要，创建Store
4. **组件开发** - 开发页面组件
5. **样式调整** - 调整UI样式
6. **功能测试** - 测试所有功能
7. **代码审查** - 提交前自查代码规范

### 5.2 Git提交规范
```
feat: 新增学生管理页面
fix: 修复考勤统计数据错误
style: 调整表格样式
refactor: 重构卡务管理组件
docs: 更新API文档
chore: 更新依赖版本
```

### 5.3 代码审查清单
- [ ] TypeScript类型定义完整
- [ ] 组件Props和Emits定义清晰
- [ ] 表单验证规则完整
- [ ] API接口错误处理
- [ ] 用户操作有反馈提示
- [ ] 删除操作有二次确认
- [ ] 权限控制正确
- [ ] 代码注释清晰
- [ ] 无console.log等调试代码
- [ ] 遵循ESLint规范

---

## 六、性能优化规范

### 6.1 组件优化
```typescript
// 1. 使用v-memo优化列表渲染
<div v-for="item in list" :key="item.id" v-memo="[item.id, item.status]">
  {{ item.name }}
</div>

// 2. 使用defineAsyncComponent异步加载组件
const StudentForm = defineAsyncComponent(() => 
  import('./components/StudentForm.vue')
)

// 3. 使用computed缓存计算结果
const filteredList = computed(() => {
  return list.value.filter(item => item.status === 'active')
})
```

### 6.2 请求优化
```typescript
// 1. 使用防抖处理搜索请求
import { useDebounceFn } from '@vueuse/core'

const handleSearch = useDebounceFn((value: string) => {
  fetchData({ keyword: value })
}, 300)

// 2. 取消重复请求
const controller = new AbortController()
const fetchData = () => {
  controller.abort() // 取消上一次请求
  return request.get('/api/data', { signal: controller.signal })
}
```

### 6.3 图片优化
```typescript
// 1. 使用懒加载
import { useLazyLoad } from '@/composables/useLazyLoad'

// 2. 压缩上传图片
const compressImage = (file: File) => {
  // 使用canvas压缩
}
```

---

## 七、常用工具函数

### 7.1 格式化工具
```typescript
// src/utils/format.ts

// 格式化日期
export const formatDate = (date: string | Date, format = 'YYYY-MM-DD') => {
  return dayjs(date).format(format)
}

// 格式化金额
export const formatMoney = (amount: number) => {
  return `¥${amount.toFixed(2)}`
}

// 格式化手机号
export const formatPhone = (phone: string) => {
  return phone.replace(/(\d{3})(\d{4})(\d{4})/, '$1 $2 $3')
}
```

### 7.2 验证工具
```typescript
// src/utils/validate.ts

// 验证手机号
export const isValidPhone = (phone: string) => {
  return /^1[3-9]\d{9}$/.test(phone)
}

// 验证身份证
export const isValidIdCard = (idCard: string) => {
  return /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/.test(idCard)
}
```

---

## 八、调试技巧

### 8.1 Vue DevTools使用
- 查看组件树和Props
- 查看Pinia Store状态
- 查看路由信息
- 性能分析

### 8.2 网络调试
- 使用Chrome DevTools查看API请求
- 使用Postman测试API接口
- 使用Mock数据开发

---

**此规范为校园一卡通系统前端开发的标准文档，所有开发人员必须严格遵守。**