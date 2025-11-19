import request from '../request'

export interface PageParams { page: number; size: number }
export interface PageResult<T> { records: T[]; total: number }

export interface PermissionInfo {
  id: number
  name: string
  code: string
  description?: string
  status?: number
}

export interface PermissionQuery extends PageParams {
  name?: string
  code?: string
}

export const getPermissionList = (params: PermissionQuery) => {
  return request.get<PageResult<PermissionInfo>>('/api/v1/permissions', { params })
}

export const getPermissionDetail = (id: number) => {
  return request.get<PermissionInfo>(`/api/v1/permissions/${id}`)
}

export const createPermission = (data: Omit<PermissionInfo, 'id'>) => {
  return request.post<PermissionInfo>('/api/v1/permissions', data)
}

export const updatePermission = (id: number, data: Partial<PermissionInfo>) => {
  return request.put<PermissionInfo>(`/api/v1/permissions/${id}`, data)
}

export const deletePermission = (id: number) => {
  return request.delete(`/api/v1/permissions/${id}`)
}