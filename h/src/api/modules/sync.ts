import request from '@/api/request'

export type SyncType = 'PERSONS' | 'FACE_PHOTOS' | 'FACE_FEATURES' | 'CARDS' | 'PERMISSIONS' | 'TIME_GROUPS' | 'ATTENDANCE_RULES'

export interface SyncTask {
  id: string
  type: SyncType
  status: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED'
  scope?: string
  payloadSummary?: string
  progress?: number
  message?: string
  createdAt?: string
  updatedAt?: string
}

export interface DispatchReq {
  scope?: string
  payloadSummary?: string
}

export const dispatchPersons = (body: DispatchReq) => request.post<SyncTask>('/api/v1/sync/persons', body)
export const dispatchFacePhotos = (body: DispatchReq) => request.post<SyncTask>('/api/v1/sync/face-photos', body)
export const dispatchFaceFeatures = (body: DispatchReq) => request.post<SyncTask>('/api/v1/sync/face-features', body)
export const dispatchCards = (body: DispatchReq) => request.post<SyncTask>('/api/v1/sync/cards', body)
export const dispatchPermissions = (body: DispatchReq) => request.post<SyncTask>('/api/v1/sync/permissions', body)
export const dispatchTimeGroups = (body: DispatchReq) => request.post<SyncTask>('/api/v1/sync/time-groups', body)
export const dispatchAttendanceRules = (body: DispatchReq) => request.post<SyncTask>('/api/v1/sync/attendance-rules', body)

export const listTasks = (limit = 50) => request.get<SyncTask[]>('/api/v1/sync/tasks', { params: { limit } })
export const getTask = (id: string) => request.get<SyncTask>(`/api/v1/sync/tasks/${id}`)