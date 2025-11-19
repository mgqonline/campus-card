import request from '../request'

export interface PageParams { page: number; size: number }
export interface PageResult<T> { records: T[]; total: number }

import type { ParentInfo } from './parent'

export interface StudentInfo {
  id: number
  name: string
  studentNo: string
  gradeId?: number
  classId: number
  status?: number
  parents?: ParentInfo[]
  schoolId?: number
  schoolName?: string
  gradeName?: string
  className?: string
  photoPath?: string
}

export interface StudentQuery extends PageParams {
  name?: string
  gradeId?: number
  classId?: number
  schoolId?: number
}

export const getStudentList = (params: StudentQuery) => {
  return request.get<PageResult<StudentInfo>>('/api/v1/students', { params })
}

export const getStudentDetail = (id: number) => {
  return request.get<StudentInfo>(`/api/v1/students/${id}`)
}

export const getStudentByNo = (studentNo: string) => {
  return request.get<StudentInfo>(`/api/v1/students/by-no/${encodeURIComponent(studentNo)}`)
}

export const createStudent = (data: Omit<StudentInfo, 'id'>) => {
  return request.post<StudentInfo>('/api/v1/students', data)
}

export const updateStudent = (id: number, data: Partial<StudentInfo>) => {
  return request.put<StudentInfo>(`/api/v1/students/${id}`, data)
}

export const deleteStudent = (id: number) => {
  return request.delete(`/api/v1/students/${id}`)
}

export const enableStudent = (id: number) => request.post(`/api/v1/students/${id}/enable`)
export const disableStudent = (id: number) => request.post(`/api/v1/students/${id}/disable`)
export const graduateStudent = (id: number) => request.post(`/api/v1/students/${id}/graduate`)

export const importStudents = (formData: FormData) => {
  return request.post('/api/v1/students/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 批量/单个转班（前端复用批量接口，或调用单个接口）
export const transferStudents = (studentIds: number[], classId: number) => {
  return request.post('/api/v1/students/transfer', { studentIds, classId })
}
export const transferStudent = (id: number, classId: number) => {
  return request.post(`/api/v1/students/${id}/transfer`, { classId })
}

// 人脸信息
export const getStudentFace = (id: number) => {
  return request.get<string>(`/api/v1/students/${id}/face`)
}
export const setStudentFace = (id: number, faceToken: string) => {
  return request.put(`/api/v1/students/${id}/face`, { faceToken })
}
export const deleteStudentFace = (id: number) => {
  return request.delete(`/api/v1/students/${id}/face`)
}

// 照片管理
export const getStudentPhotoUrl = (id: number) => {
  const base = (import.meta as any).env?.VITE_API_BASE || ''
  return `${base}/api/v1/students/${id}/photo`
}
export const uploadStudentPhoto = (id: number, file: File) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post<StudentInfo>(`/api/v1/students/${id}/photo`, fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export const deleteStudentPhoto = (id: number) => {
  return request.delete(`/api/v1/students/${id}/photo`)
}

// 档案管理
export const getStudentArchive = (id: number) => {
  return request.get<string>(`/api/v1/students/${id}/archive`)
}
export const updateStudentArchive = (id: number, archive: string) => {
  return request.put(`/api/v1/students/${id}/archive`, archive, {
    headers: { 'Content-Type': 'text/plain' }
  })
}