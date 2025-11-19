import request from '@/api/request'

export interface PageResult<T> { total: number; records: T[] }
export type PersonType = 'STUDENT' | 'TEACHER' | 'STAFF' | 'VISITOR'

export interface Device { id: number; name: string; code: string; location?: string; status?: number }
export interface DeviceFace { id?: number; deviceId?: number; personType: PersonType; personId: string; dispatchedAt?: string; status?: number }

export interface FaceDispatchTask {
  id: number
  taskType: 'BATCH' | 'INCREMENTAL' | 'DELETE'
  deviceIds: string
  description?: string
  totalItems?: number
  successItems?: number
  failedItems?: number
  failedPersonIds?: string
  status: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'PARTIAL_FAILED' | 'FAILED'
  createdAt?: string
  updatedAt?: string
}

export const listDevices = (params: { page?: number; size?: number; name?: string; code?: string } = { page: 1, size: 10 }) => {
  const query = { page: 1, size: 10, ...params }
  return request.get<PageResult<Device>>('/api/v1/devices', { params: query })
}
export const createDevice = (data: Partial<Device>) => request.post<Device>('/api/v1/devices', data)
export const updateDevice = (id: number, data: Partial<Device>) => request.put<Device>(`/api/v1/devices/${id}`, data)
export const deleteDevice = (id: number) => request.delete<void>(`/api/v1/devices/${id}`)

export const listDeviceFaces = (deviceId: number) => request.get<DeviceFace[]>(`/api/v1/devices/${deviceId}/faces`)
export const addDeviceFaces = (deviceId: number, faces: DeviceFace[]) => request.post<number>(`/api/v1/devices/${deviceId}/faces`, faces)
export const removeDeviceFace = (deviceId: number, personId: string) => request.delete<void>(`/api/v1/devices/${deviceId}/faces/${personId}`)
export const syncDeviceFaces = (deviceId: number) => request.post<number>(`/api/v1/devices/${deviceId}/sync`)

export const batchDispatch = (data: { deviceIds: number[]; personType?: PersonType; personId?: string; faceIds?: number[] }) => {
  return request.post<FaceDispatchTask>('/api/v1/face-dispatch/batch', data)
}
export const incrementalDispatch = (data: { deviceIds: number[]; since?: string }) => {
  return request.post<FaceDispatchTask>('/api/v1/face-dispatch/incremental', data)
}
export const deleteDispatch = (data: { deviceIds: number[]; personIds: string[] }) => {
  return request.post<FaceDispatchTask>('/api/v1/face-dispatch/delete', data)
}
export const listDispatchTasks = () => request.get<FaceDispatchTask[]>(`/api/v1/face-dispatch/tasks`)
export const retryDispatchTask = (id: number) => request.post<FaceDispatchTask>(`/api/v1/face-dispatch/tasks/${id}/retry`)