import request from '../request'

export interface LoginReq {
  phone: string
  code: string
}

export interface LoginResp {
  token: string
  expireIn: number
}

export interface ProfileResp {
  parentId: number
  name: string
  phone?: string
  email?: string
  address?: string
  schoolName?: string
  boundChildren: Array<{ id: number; name: string; classId: number; className?: string }>
}

export interface BindReq {
  openId: string
  phone?: string
}

export interface UpdateProfileReq {
  name?: string
  phone?: string
  email?: string
  address?: string
}

export const login = (data: LoginReq) => {
  return request.post<LoginResp>('/api/v1/auth/login', data)
}

export const getProfile = () => {
  return request.get<ProfileResp>('/api/v1/auth/profile')
}

export const bindWeChat = (data: BindReq) => {
  return request.post<string>('/api/v1/auth/bind', data)
}

export const updateProfile = (data: UpdateProfileReq) => {
  return request.put<ProfileResp>('/api/v1/auth/profile', data)
}