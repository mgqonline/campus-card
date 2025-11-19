import request from '../request'

// 学校信息接口
export interface School {
  id?: number
  name: string
  code: string
  address?: string
  phone?: string
  email?: string
  principal?: string
  description?: string
  status: number // 1: 启用, 0: 禁用
  createTime?: string
  updateTime?: string
}

// 学校查询参数
export interface SchoolQuery {
  name?: string
  code?: string
  status?: number
  page?: number
  size?: number
}

// 学校列表响应（与现有页面契合）
export interface SchoolListResponse {
  list: School[]
  total: number
  page: number
  size: number
}

// 校区信息
export interface Campus {
  id?: number
  schoolId: number
  name: string
  code?: string
  address?: string
  status?: number
}

// 学校配置
export interface SchoolConfig { id?: number; schoolId: number; key: string; value?: string }

// Logo
export interface SchoolLogo { schoolId: number; logoUrl: string; updatedAt?: string }

export const schoolApi = {
  // 获取学校列表，映射 PageResult -> 视图期望结构
  async getList(params: SchoolQuery = {}): Promise<SchoolListResponse> {
    const page = params.page || 1
    const size = params.size || 10
    const res = await request.get<{ records: School[]; total: number }>(
      '/api/v1/schools',
      { params: { page, size, name: params.name, code: params.code } }
    )
    return { list: res.records || [], total: res.total || 0, page, size }
  },

  // 详情
  getDetail(id: number) {
    return request.get<School>(`/api/v1/schools/${id}`)
  },

  // 创建
  create(data: Omit<School, 'id' | 'createTime' | 'updateTime'>) {
    return request.post<School>('/api/v1/schools', data)
  },

  // 更新
  update(id: number, data: Partial<School>) {
    return request.put<School>(`/api/v1/schools/${id}`, data)
  },

  // 删除
  delete(id: number) {
    return request.delete(`/api/v1/schools/${id}`)
  },

  // 状态切换：根据目标状态命中 enable/disable 接口
  async updateStatus(id: number, status: number) {
    if (status === 1) {
      return request.post(`/api/v1/schools/${id}/enable`)
    } else {
      return request.post(`/api/v1/schools/${id}/disable`)
    }
  },

  // 校区管理
  listCampuses(schoolId: number) {
    return request.get<Campus[]>(`/api/v1/schools/${schoolId}/campuses`)
  },
  addCampus(schoolId: number, campus: Omit<Campus, 'id' | 'schoolId'>) {
    return request.post<Campus>(`/api/v1/schools/${schoolId}/campuses`, campus)
  },
  deleteCampus(schoolId: number, campusId: number) {
    return request.delete(`/api/v1/schools/${schoolId}/campuses/${campusId}`)
  },

  // 配置管理
  listConfigs(schoolId: number) {
    return request.get<SchoolConfig[]>(`/api/v1/schools/${schoolId}/configs`)
  },
  setConfig(schoolId: number, key: string, value: string) {
    return request.put<SchoolConfig>(`/api/v1/schools/${schoolId}/configs/${key}`, { value })
  },

  // Logo 管理
  getLogo(schoolId: number) {
    return request.get<SchoolLogo>(`/api/v1/schools/${schoolId}/logo`)
  },
  setLogo(schoolId: number, url: string) {
    return request.put<SchoolLogo>(`/api/v1/schools/${schoolId}/logo`, { url })
  },
  // 获取所有启用的学校（用于下拉选择）
  async getEnabledList(): Promise<School[]> {
    try {
      const res = await request.get<{ records: School[]; total: number }>(
        '/api/v1/schools',
        { params: { page: 1, size: 1000 } }
      )
      return (res.records || []).filter(s => s.status === 1)
    } catch (e) {
      return []
    }
  }
}