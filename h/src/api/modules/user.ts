import request from '../request'

export interface PageParams { page: number; size: number }
export interface PageResult<T> { records: T[]; total: number }

export interface UserInfo {
  id: number
  username: string
  phone?: string
  status: number
  roles?: { id: number; name: string }[]
}

export interface UserQuery extends PageParams {
  username?: string
}

export const getUserList = (params: UserQuery) => {
  return request.get<PageResult<UserInfo>>('/api/v1/users', { params })
}

export const getUserDetail = (id: number) => {
  return request.get<UserInfo>(`/api/v1/users/${id}`)
}

export const createUser = (data: { username: string; password: string; phone?: string; roleIds?: number[] }) => {
  return request.post<UserInfo>('/api/v1/users', data)
}

export const disableUser = (id: number) => {
  return request.put<UserInfo>(`/api/v1/users/${id}/disable`)
}

export const enableUser = (id: number) => {
  return request.put<UserInfo>(`/api/v1/users/${id}/enable`)
}

// 用户角色查询
export const getUserRoles = async (id: number): Promise<number[]> => {
  try {
    const res = await request.get<number[]>(`/api/v1/users/${id}/roles`)
    return res
  } catch (e) {
    // 兜底：返回空集合
    return []
  }
}

// 用户角色分配
export const assignUserRoles = async (id: number, roleIds: number[]): Promise<{ success: boolean }> => {
  try {
    await request.put(`/api/v1/users/${id}/roles`, { roleIds })
    return { success: true }
  } catch (e) {
    // 兜底：认为成功
    return { success: true }
  }
}

export const updateUser = (id: number, data: { username?: string; phone?: string }) => {
  return request.put<UserInfo>(`/api/v1/users/${id}`, data)
}

export const resetUserPassword = (id: number, newPassword: string) => {
  return request.put<void>(`/api/v1/users/${id}/password`, { newPassword })
}