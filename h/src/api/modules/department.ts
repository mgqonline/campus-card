import request from '../request'

export interface PageParams { page?: number; size?: number }
export interface PageResult<T> { records: T[]; total: number }

export interface Department {
  id: number
  name: string
  status?: number // 1: 启用, 0: 禁用
}

export interface DepartmentQuery extends PageParams {
  name?: string
  status?: number
}

export function listDepartments(params: DepartmentQuery = { page: 1, size: 100 }) {
  return request.get<PageResult<Department>>('/api/v1/departments', { params })
}

export function getDepartment(id: number) {
  return request.get<Department>(`/api/v1/departments/${id}`)
}

export function createDepartment(data: Omit<Department, 'id'>) {
  return request.post<Department>('/api/v1/departments', data)
}

export function updateDepartment(id: number, data: Partial<Department>) {
  return request.put<Department>(`/api/v1/departments/${id}`, data)
}

export function deleteDepartment(id: number) {
  return request.delete(`/api/v1/departments/${id}`)
}

export const enableDepartment = (id: number) => request.post(`/api/v1/departments/${id}/enable`)
export const disableDepartment = (id: number) => request.post(`/api/v1/departments/${id}/disable`)