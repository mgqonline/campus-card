import http from '../request'

export type LeaveType = 'SICK' | 'PERSONAL' | 'OTHER'
export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELED'

export interface ApplyReq {
  childId?: number
  type: LeaveType
  startTime: string
  endTime: string
  reason: string
  attachments?: string[]
}

export interface ApplyResp { id: number; status: LeaveStatus }

export interface LeaveRecord {
  id: number
  childId: number
  type: LeaveType
  startTime: string
  endTime: string
  reason: string
  attachments: string[]
  status: LeaveStatus
  applyTime: string
}

export function uploadAttachment(file: File) {
  const form = new FormData()
  form.append('file', file)
  return http.post<{ url: string; name: string }>('/api/v1/leave/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function applyLeave(data: ApplyReq) {
  return http.post<ApplyResp>('/api/v1/leave/apply', data)
}

export function getLeaveRecords(params: { childId?: number; startDate?: string; endDate?: string; page?: number; size?: number }) {
  return http.get<LeaveRecord[]>('/api/v1/leave/records', { params })
}

export function cancelLeave(data: { childId?: number; id: number }) {
  return http.post<string>('/api/v1/leave/cancel', data)
}

export function getLeaveStatus(childId?: number) {
  return http.get<Array<{ id: number; status: LeaveStatus }>>('/api/v1/leave/status', { params: { childId } })
}