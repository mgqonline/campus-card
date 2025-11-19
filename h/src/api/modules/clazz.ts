import request from '../request'

export interface PageParams { page?: number; size?: number }
export interface PageResult<T> { records: T[]; total: number }

export interface ClassInfo {
  id: number
  name: string
  gradeId: number
  status?: number
  schoolId?: number
  schoolName?: string
  gradeName?: string
  headTeacherId?: number
  headTeacherName?: string
}

// 新增：班级拆分相关类型
export interface SplitClassRequest {
  newClassName: string
  targetGradeId?: number
  headTeacherId?: number
  studentIds: number[]
}

export interface SplitClassResponse {
  newClass: ClassInfo
  moved: number
}

export const getClassList = (params: PageParams & { gradeId?: number; schoolId?: number } = { page: 1, size: 100 }): Promise<PageResult<ClassInfo>> => {
  return request.get<PageResult<ClassInfo>>('/api/v1/classes', { params })
}

export const createClass = (data: Omit<ClassInfo, 'id'>) => request.post<ClassInfo>('/api/v1/classes', data)
export const updateClass = (id: number, data: Partial<ClassInfo>) => request.put<ClassInfo>(`/api/v1/classes/${id}`, data)
export const deleteClass = (id: number) => request.delete(`/api/v1/classes/${id}`)
export const enableClass = (id: number) => request.post(`/api/v1/classes/${id}/enable`)
export const disableClass = (id: number) => request.post(`/api/v1/classes/${id}/disable`)

// 班主任分配功能
export const assignHeadTeacher = (classId: number, teacherId: number) => 
  request.post(`/api/v1/classes/${classId}/head-teacher`, { teacherId })
export const removeHeadTeacher = (classId: number) => 
  request.delete(`/api/v1/classes/${classId}/head-teacher`)

// 新增：班级升级/降级、合并与人数统计
export const upgradeClass = (classId: number, targetGradeId: number) =>
  request.post(`/api/v1/classes/${classId}/upgrade`, { targetGradeId })

export const downgradeClass = (classId: number, targetGradeId: number) =>
  request.post(`/api/v1/classes/${classId}/downgrade`, { targetGradeId })

export const mergeClasses = (sourceClassIds: number[], targetClassId: number, archiveSources: boolean = true) =>
  request.post(`/api/v1/classes/merge`, { sourceClassIds, targetClassId, archiveSources })

// 新增：班级拆分功能
export const splitClass = (classId: number, data: SplitClassRequest): Promise<SplitClassResponse> =>
  request.post<SplitClassResponse>(`/api/v1/classes/${classId}/split`, data)

export const getClassStudentCount = async (classId: number): Promise<number> => {
  try {
    const res = await request.get<{ count: number }>(`/api/v1/classes/${classId}/students/count`)
    return res.count || 0
  } catch (e) {
    // 后端未实现时兜底：通过学生分页接口的 total 估算
    try {
      const page = await request.get<{ total: number }>('/api/v1/students', { params: { page: 1, size: 1, classId } })
      return (page?.total as any) || 0
    } catch (err) {
      return 0
    }
  }
}