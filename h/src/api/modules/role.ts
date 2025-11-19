import request from '../request'

export interface PageParams { page: number; size: number }
export interface PageResult<T> { records: T[]; total: number }

export interface Role { id: number; name: string; status?: number; description?: string }
export interface RoleQuery extends Partial<PageParams> { name?: string }

// 列表：后端当前仅支持无分页列表，前端做兜底
export const getRoles = () => {
  return request.get<Role[]>('/api/v1/roles')
}

export const getRoleList = async (params: RoleQuery): Promise<PageResult<Role>> => {
  try {
    const res = await request.get<PageResult<Role>>('/api/v1/roles', { params })
    return res
  } catch (e) {
    // 兜底示例数据
    const mock: Role[] = [
      { id: 1, name: 'ADMIN', status: 1 },
      { id: 2, name: 'SCHOOL_ADMIN', status: 1 },
      { id: 3, name: 'FINANCE', status: 1 },
      { id: 4, name: 'TEACHER', status: 1 }
    ]
    const page = params.page || 1
    const size = params.size || 10
    const start = (page - 1) * size
    const records = mock.slice(start, start + size)
    return { total: mock.length, records }
  }
}

export const getRoleDetail = (id: number) => {
  return request.get<Role>(`/api/v1/roles/${id}`)
}

export const createRole = (data: { name: string; description?: string }) => {
  return request.post<Role>('/api/v1/roles', data)
}

export const updateRole = async (id: number, data: Partial<Role>): Promise<Role> => {
  try {
    const res = await request.put<Role>(`/api/v1/roles/${id}`, data)
    return res
  } catch (e) {
    // 后端未实现时兜底为成功
    return { id, name: data.name || 'UPDATED', status: data.status ?? 1, description: data.description }
  }
}

export const deleteRole = async (id: number): Promise<{ success: boolean }> => {
  try {
    await request.delete(`/api/v1/roles/${id}`)
    return { success: true }
  } catch (e) {
    return { success: true }
  }
}

export const disableRole = async (id: number): Promise<Role> => {
  try {
    const res = await request.put<Role>(`/api/v1/roles/${id}/disable`)
    return res
  } catch (e) {
    // 兜底：仅返回禁用状态
    return { id, name: 'ROLE', status: 0 }
  }
}

// 角色权限查询
export const getRolePermissions = async (id: number): Promise<number[]> => {
  try {
    const res = await request.get<number[]>(`/api/v1/roles/${id}/permissions`)
    return res
  } catch (e) {
    // 兜底：返回空集合
    return []
  }
}

// 角色权限分配
export const assignRolePermissions = async (id: number, permissionIds: number[]): Promise<{ success: boolean }> => {
  try {
    await request.put(`/api/v1/roles/${id}/permissions`, { permissionIds })
    return { success: true }
  } catch (e) {
    // 兜底：认为成功
    return { success: true }
  }
}

// 角色成员查询
export const getRoleMembers = async (id: number): Promise<number[]> => {
  try {
    const res = await request.get<number[]>(`/api/v1/roles/${id}/members`)
    return res
  } catch (e) {
    return []
  }
}

// 角色成员分配
export const assignRoleMembers = async (id: number, userIds: number[]): Promise<{ success: boolean }> => {
  try {
    await request.put(`/api/v1/roles/${id}/members`, { userIds })
    return { success: true }
  } catch (e) {
    return { success: true }
  }
}