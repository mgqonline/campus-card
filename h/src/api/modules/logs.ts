import request from '../request'

export interface PageResult<T> {
  total: number
  records: T[]
}

export interface OpLog {
  id: number
  occurredAt: string
  userId?: number
  subjectType?: string
  method?: string
  uri?: string
  action?: string
  params?: string
  clientIp?: string
  resultCode?: number
  durationMs?: number
}

export interface DeviceOpLog {
  id: number
  occurredAt: string
  deviceId?: number
  deviceCode?: string
  action?: string
  params?: string
  result?: string
  operatorId?: number
}

export interface DataChangeLog {
  id: number
  occurredAt: string
  entity: string
  entityId?: string
  changeType: string
  beforeJson?: string
  afterJson?: string
  changedFields?: string
  operatorId?: number
  remark?: string
}

export const getOpLogs = (params?: { page?: number; size?: number; keywords?: string }) => {
  return request.get<PageResult<OpLog>>('/api/v1/logs/op', { params })
}

export const getDeviceLogs = (params?: { page?: number; size?: number; keywords?: string }) => {
  return request.get<PageResult<DeviceOpLog>>('/api/v1/logs/device', { params })
}

export const getDataChangeLogs = (params?: { page?: number; size?: number; entity?: string; keywords?: string }) => {
  return request.get<PageResult<DataChangeLog>>('/api/v1/logs/data-change', { params })
}