import request from '@/api/request'

export type ConfigCategory = 'PARAM' | 'INTERFACE' | 'PUSH' | 'STORAGE' | 'BACKUP'

export interface SystemConfig {
  id?: number
  key: string
  value?: string
  category: ConfigCategory
  updatedAt?: string
}

export const getCategories = () => {
  return request.get<string[]>('/api/v1/system/configs/categories')
}

export const listByCategory = (category: ConfigCategory) => {
  return request.get<SystemConfig[]>(`/api/v1/system/configs/${category}`)
}

export const setConfig = (category: ConfigCategory, key: string, value: string) => {
  return request.put<SystemConfig>(`/api/v1/system/configs/${category}/${key}`, { value })
}