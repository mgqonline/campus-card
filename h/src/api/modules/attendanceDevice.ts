import request from '../request'

export interface Device {
  id?: number
  name: string
  code: string
  vendor?: string
  ip?: string
  port?: number
  username?: string
  password?: string
  location?: string
  status?: number
  groupType?: 'TEACHER' | 'STUDENT' | 'ACCESS'
  paramJson?: string
  lastHeartbeatAt?: string
  createdAt?: string
  updatedAt?: string
}

export const registerDevice = (data: Device) => {
  return request.post<Device>('/api/v1/attendance/devices/register', data)
}

export const getDeviceList = async (params?: { vendor?: string; groupType?: string; page?: number; size?: number }) => {
  // 切回考勤设备列表接口 `/api/v1/attendance/devices`
  // 后端返回结构为 Result<List<Device>>，拦截器会自动解包为数组
  const res = await request.get<Device[] | { records: Device[]; total: number }>(
    '/api/v1/attendance/devices',
    { params }
  )
  // 兼容偶发分页结构（若后端改为分页返回）
  return Array.isArray(res) ? res : (res && (res as any).records ? (res as any).records : [])
}

export const getDeviceDetail = (id: number) => {
  return request.get<Device>(`/api/v1/attendance/devices/${id}`)
}

export const updateDevice = (id: number, data: Partial<Device>) => {
  return request.put<Device>(`/api/v1/attendance/devices/${id}`, data)
}

export const deleteDevice = (id: number) => {
  return request.delete<void>(`/api/v1/attendance/devices/${id}`)
}

export const getDeviceStatus = (id: number) => {
  return request.get<{ status: number; lastHeartbeatAt?: string }>(`/api/v1/attendance/devices/${id}/status`)
}

export const setDeviceOnline = (id: number) => {
  return request.put<void>(`/api/v1/attendance/devices/${id}/online`)
}

export const setDeviceOffline = (id: number) => {
  return request.put<void>(`/api/v1/attendance/devices/${id}/offline`)
}

export const updateDeviceParams = (id: number, paramJson: string) => {
  return request.put<void>(`/api/v1/attendance/devices/${id}/params`, { paramJson })
}

export const updateDeviceGroup = (id: number, groupType: 'TEACHER' | 'STUDENT' | 'ACCESS') => {
  return request.put<void>(`/api/v1/attendance/devices/${id}/group`, { groupType })
}

export const getDeviceGroups = () => {
  return request.get<string[]>('/api/v1/attendance/devices/groups')
}