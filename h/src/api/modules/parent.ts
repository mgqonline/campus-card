import request from '../request'

export interface ParentInfo {
  id: number
  name: string
  phone?: string
  email?: string
  status?: number
  // 兼容学生接口中的家长列表项可能包含关系
  relation?: 'FATHER' | 'MOTHER' | 'GUARDIAN' | 'OTHER'
}

export interface PageParams { page: number; size: number }
export interface PageResult<T> { records: T[]; total: number }

export interface ParentQuery extends PageParams { name?: string }

export interface ParentStudentItem {
  id: number // 关联ID
  studentId: number
  studentName: string
  relation: 'FATHER' | 'MOTHER' | 'GUARDIAN' | 'OTHER'
  classId?: number
  status?: number
}

export const getParentList = (params: ParentQuery) => {
  return request.get<PageResult<ParentInfo>>('/api/v1/parents', { params })
}

export const getParentDetail = (id: number) => {
  return request.get<ParentInfo>(`/api/v1/parents/${id}`)
}

export const createParent = (data: Omit<ParentInfo, 'id'>) => {
  return request.post<ParentInfo>('/api/v1/parents', data)
}

export const updateParent = (id: number, data: Partial<ParentInfo>) => {
  return request.put<ParentInfo>(`/api/v1/parents/${id}`, data)
}

export const deleteParent = (id: number) => {
  return request.delete(`/api/v1/parents/${id}`)
}

export const getStudentsByParentId = (id: number) => {
  return request.get<ParentStudentItem[]>(`/api/v1/parents/${id}/students`)
}

// 新增：家长账号状态
export const enableParent = (id: number) => request.put(`/api/v1/parents/${id}/enable`)
export const disableParent = (id: number) => request.put(`/api/v1/parents/${id}/disable`)

// 新增：家长微信绑定
export interface ParentWechat {
  parentId: number
  openId: string
  unionId?: string
  nickname?: string
  avatarUrl?: string
  status?: number
  bindTime?: string
}
export const getParentWechat = (id: number) => request.get<ParentWechat>(`/api/v1/parents/${id}/wechat`)
export const bindParentWechat = (id: number, body: Partial<ParentWechat>) => request.put<ParentWechat>(`/api/v1/parents/${id}/wechat`, body)
export const unbindParentWechat = (id: number) => request.delete(`/api/v1/parents/${id}/wechat`)

// 新增：家长权限设置
export interface ParentPermission {
  parentId: number
  viewAttendance: boolean
  viewConsumption: boolean
  viewGrades: boolean
  messageTeacher: boolean
}
export const getParentPermissions = (id: number) => request.get<ParentPermission>(`/api/v1/parents/${id}/permissions`)
export const updateParentPermissions = (id: number, body: Partial<ParentPermission>) => request.put<ParentPermission>(`/api/v1/parents/${id}/permissions`, body)

// 学生-家长关联（学生视角）
export interface ParentLinkRequest {
  parentId?: number
  name?: string
  phone?: string
  email?: string
  relation: 'FATHER' | 'MOTHER' | 'GUARDIAN' | 'OTHER'
}

export const getParentsByStudentId = (studentId: number) => {
  return request.get<ParentInfo[]>(`/api/v1/students/${studentId}/parents`)
}
export const linkParentToStudent = (studentId: number, body: ParentLinkRequest) => {
  return request.post<ParentStudentItem>(`/api/v1/students/${studentId}/parents`, body)
}
export const updateParentRelation = (studentId: number, parentId: number, relation: ParentLinkRequest['relation']) => {
  return request.put(`/api/v1/students/${studentId}/parents/${parentId}`, { relation })
}
export const unlinkParentFromStudent = (studentId: number, parentId: number) => {
  return request.delete(`/api/v1/students/${studentId}/parents/${parentId}`)
}