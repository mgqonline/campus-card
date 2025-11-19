import request from '../request'

export interface PageParams { page?: number; size?: number }
export interface PageResult<T> { records: T[]; total: number }

export interface Semester {
  id: number
  schoolId: number
  name: string
  code: string
  startDate?: string
  endDate?: string
  current?: boolean
  status?: number
  createTime?: string
  updateTime?: string
}

export const getSemesterList = (params: PageParams & { name?: string; schoolId?: number } = { page: 1, size: 10 }) => {
  return request.get<PageResult<Semester>>('/api/v1/semesters', { params })
}

export const getSemesterDetail = (id: number) => {
  return request.get<Semester>(`/api/v1/semesters/${id}`)
}

export const createSemester = (data: Omit<Semester, 'id' | 'createTime' | 'updateTime'>) => {
  return request.post<Semester>('/api/v1/semesters', data)
}

export const updateSemester = (id: number, data: Partial<Semester>) => {
  return request.put<Semester>(`/api/v1/semesters/${id}`, data)
}

export const deleteSemester = (id: number) => {
  return request.delete(`/api/v1/semesters/${id}`)
}

export const enableSemester = (id: number) => request.post(`/api/v1/semesters/${id}/enable`)
export const disableSemester = (id: number) => request.post(`/api/v1/semesters/${id}/disable`)
export const setSemesterCurrent = (id: number) => request.post(`/api/v1/semesters/${id}/current`)

export const getCurrentSemester = (schoolId: number) => {
  return request.get<Semester>('/api/v1/semesters/current', { params: { schoolId } })
}

export const getHistorySemesters = (schoolId: number, startDate?: string, endDate?: string) => {
  const params: any = { schoolId }
  if (startDate) params.startDate = startDate
  if (endDate) params.endDate = endDate
  return request.get<Semester[]>('/api/v1/semesters/history', { params })
}