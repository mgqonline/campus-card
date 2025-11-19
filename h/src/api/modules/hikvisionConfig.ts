import request from '../request'

export interface HikvisionConfig {
  id?: number
  schoolId: number
  deviceIp: string
  devicePort: number
  sdkVersion: string
  deviceCodePrefix: string
  protocolType: string
  username: string
  password: string
  heartbeatEnabled: boolean
  heartbeatInterval: number
  sdkTimeout: number
  maxRetryCount: number
  faceRecognitionEnabled: boolean
  cardRecognitionEnabled: boolean
  temperatureDetectionEnabled: boolean
  syncInterval: number
  syncEnabled: boolean
  status: number
  createdAt?: string
  updatedAt?: string
}

export interface School {
  id: number
  name: string
  code: string
  address?: string
  phone?: string
  email?: string
  principal?: string
  description?: string
  status: number
  createTime?: string
  updateTime?: string
}

// 新增：通用分页返回结构
export interface PageResult<T> {
  total: number
  records: T[]
}

export interface BatchConfigRequest {
  schoolIds: number[]
  template: Partial<HikvisionConfig>
}

export interface CopyConfigRequest {
  sourceSchoolId: number
  targetSchoolId: number
}

export interface ConfigStats {
  totalConfigs: number
  activeConfigs: number
  inactiveConfigs: number
}

export interface TestConnectionResult {
  success: boolean
  message: string
  responseTime?: number
}

// 获取所有配置列表
export const getConfigList = (params?: { page?: number; size?: number; schoolId?: number; status?: number }) => {
  return request.get<HikvisionConfig[]>('/api/v1/hikvision-config', { params })
}

// 根据学校ID获取配置
export const getConfigBySchoolId = (schoolId: number) => {
  return request.get<HikvisionConfig>(`/api/v1/hikvision-config/school/${schoolId}`)
}

// 获取配置详情
export const getConfigDetail = (id: number) => {
  return request.get<HikvisionConfig>(`/api/v1/hikvision-config/${id}`)
}

// 创建配置
export const createConfig = (data: Omit<HikvisionConfig, 'id' | 'createdAt' | 'updatedAt'>) => {
  return request.post<HikvisionConfig>('/api/v1/hikvision-config', data)
}

// 更新配置
export const updateConfig = (id: number, data: Partial<HikvisionConfig>) => {
  return request.put<HikvisionConfig>(`/api/v1/hikvision-config/${id}`, data)
}

// 删除配置
export const deleteConfig = (id: number) => {
  return request.delete<void>(`/api/v1/hikvision-config/${id}`)
}

// 测试连接
export const testConnection = (id: number) => {
  return request.post<TestConnectionResult>(`/api/v1/hikvision-config/${id}/test`)
}

// 更新状态
export const updateConfigStatus = (id: number, status: number) => {
  return request.put<HikvisionConfig>(`/api/v1/hikvision-config/${id}/status`, { status })
}

// 获取默认配置模板
export const getConfigTemplate = () => {
  return request.get<HikvisionConfig>('/api/v1/hikvision-config/template')
}

// 批量配置学校设备参数
export const batchConfigBySchools = (data: BatchConfigRequest) => {
  return request.post<HikvisionConfig[]>('/api/v1/hikvision-config/batch-config', data)
}

// 按学校复制配置
export const copyConfigToSchool = (data: CopyConfigRequest) => {
  return request.post<HikvisionConfig>('/api/v1/hikvision-config/copy-config', data)
}

// 获取学校配置统计
export const getConfigStats = () => {
  return request.get<ConfigStats>('/api/v1/hikvision-config/stats')
}

// 获取学校列表（用于选择器），兼容分页返回或直接数组
export const getSchoolList = (params?: { page?: number; size?: number; name?: string; code?: string }) => {
  return request.get<PageResult<School> | School[]>('/api/v1/schools', { params })
}