import request from '../request'

export interface LoginReq { username: string; password: string }
export interface LoginResp { token: string; expireIn: number }

export const login = (data: LoginReq) => {
  return request.post<LoginResp>('/api/v1/auth/login', data)
}

export const logout = () => {
  return request.post<void>('/api/v1/auth/logout')
}

export const profile = (username: string) => {
  return request.get<{ id: number; username: string; phone?: string; status: number }>(
    '/api/v1/auth/profile',
    { params: { username } }
  )
}

// 新增：重置指定用户密码（后端未声明权限，开发环境用于修复）
export const resetPasswordById = (id: number, newPassword: string) => {
  return request.put<void>(`/api/v1/users/${id}/password`, { newPassword })
}

// 新增：当前用户完整权限视图
export interface MeInfo {
  id: number
  username: string
  roles: string[]
  permissions: string[]
  menus: string[]
}

export const me = () => {
  return request.get<MeInfo>('/api/v1/auth/me')
}

// 忘记密码：发送验证码
export const sendForgotCode = (username: string) => {
  return request.post<{ sentAt: number; expireIn: number; phoneMasked: string }>(
    '/api/v1/auth/forgot/send-code',
    { username }
  )
}

// 忘记密码：校验验证码并重置
export const resetPasswordWithCode = (username: string, code: string, newPassword: string) => {
  return request.post<{ success: boolean }>(
    '/api/v1/auth/forgot/reset',
    { username, code, newPassword }
  )
}