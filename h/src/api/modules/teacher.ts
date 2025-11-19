import request from '../request'

export interface PageParams { page: number; size: number }
export interface PageResult<T> { records: T[]; total: number }

export interface TeacherInfo {
  id: number
  name: string
  teacherNo: string
  department?: string
  phone?: string
  status?: number
}

export interface TeacherQuery extends PageParams {
  name?: string
  department?: string
}

// 兼容不同后端分页返回结构，统一为 { records, total }
export const getTeacherList = async (params: TeacherQuery): Promise<PageResult<TeacherInfo>> => {
  const raw = await request.get<any>('/api/v1/teachers', { params })
  // 如果返回为空，兜底为空分页
  if (!raw) return { records: [], total: 0 }
  // 可能存在 { data: {...} } 包装
  const data = (raw && typeof raw === 'object' && 'data' in raw) ? (raw as any).data : raw
  // 直接数组（不分页）
  if (Array.isArray(data)) {
    return { records: data as TeacherInfo[], total: (data as TeacherInfo[]).length }
  }
  // 常见分页结构
  if (data && typeof data === 'object') {
    if ('records' in data && 'total' in data) return data as PageResult<TeacherInfo>
    if ('list' in data && 'total' in data) return { records: (data as any).list || [], total: (data as any).total || 0 }
    if ('items' in data && 'count' in data) return { records: (data as any).items || [], total: (data as any).count || 0 }
  }
  // 兜底
  return { records: [], total: 0 }
}

export const getTeacherDetail = (id: number) => {
  return request.get<TeacherInfo>(`/api/v1/teachers/${id}`)
}

export const createTeacher = (data: Omit<TeacherInfo, 'id'>) => {
  return request.post<TeacherInfo>('/api/v1/teachers', data)
}

export const updateTeacher = (id: number, data: Partial<TeacherInfo>) => {
  return request.put<TeacherInfo>(`/api/v1/teachers/${id}`, data)
}

export const deleteTeacher = (id: number) => {
  return request.delete(`/api/v1/teachers/${id}`)
}

export const enableTeacher = (id: number) => request.post(`/api/v1/teachers/${id}/enable`)
export const disableTeacher = (id: number) => request.post(`/api/v1/teachers/${id}/disable`)

export const importTeachers = (formData: FormData) => {
  return request.post('/api/v1/teachers/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 照片管理
export const getTeacherPhotoUrl = (id: number) => {
  const base = (import.meta as any).env?.VITE_API_BASE || ''
  return `${base}/api/v1/teachers/${id}/photo`
}
export const uploadTeacherPhoto = (id: number, file: File) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post<import('./teacher').TeacherInfo>(`/api/v1/teachers/${id}/photo`, fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export const deleteTeacherPhoto = (id: number) => {
  return request.delete(`/api/v1/teachers/${id}/photo`)
}

// 档案管理
export const getTeacherArchive = (id: number) => {
  return request.get<string>(`/api/v1/teachers/${id}/archive`)
}
export const updateTeacherArchive = (id: number, archive: string) => {
  return request.put(`/api/v1/teachers/${id}/archive`, archive, {
    headers: { 'Content-Type': 'text/plain' }
  })
}

// 教师专属：卡片、人脸、关联便捷接口
export interface TeacherCardIssueReq { typeId: number; initialBalance?: number; note?: string }
export const listTeacherCards = (id: number) => request.get(`/api/v1/teachers/${id}/cards`)
export const issueTeacherCard = (id: number, data: TeacherCardIssueReq) => request.post(`/api/v1/teachers/${id}/cards/issue`, data)
export const getTeacherCardBalance = (id: number, cardNo: string) => request.get(`/api/v1/teachers/${id}/cards/${cardNo}/balance`)

export const listTeacherFaces = (id: number) => request.get(`/api/v1/teachers/${id}/faces`)

export const listHeadClassesByTeacher = (id: number) => request.get(`/api/v1/teachers/${id}/head-classes`)
export const listSubjectClassesByTeacher = (id: number) => request.get(`/api/v1/teachers/${id}/subject-classes`)