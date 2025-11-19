import request from '../request'

export interface PageParams { page?: number; size?: number }
export interface PageResult<T> { records: T[]; total: number }

export interface GradeInfo {
  id: number
  name: string
  year?: number
  status?: number
  schoolId?: number
  schoolName?: string
}

export const getGradeList = (params: PageParams & { name?: string; schoolId?: number } = { page: 1, size: 100 }): Promise<PageResult<GradeInfo>> => {
  return request.get<PageResult<GradeInfo>>('/api/v1/grades', { params })
}

export const createGrade = (data: Omit<GradeInfo, 'id'>) => request.post<GradeInfo>('/api/v1/grades', data)
export const updateGrade = (id: number, data: Partial<GradeInfo>) => request.put<GradeInfo>(`/api/v1/grades/${id}`, data)
export const deleteGrade = (id: number) => request.delete(`/api/v1/grades/${id}`)
export const enableGrade = (id: number) => request.post(`/api/v1/grades/${id}/enable`)
export const disableGrade = (id: number) => request.post(`/api/v1/grades/${id}/disable`)