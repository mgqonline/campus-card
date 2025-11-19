import request from '../request'

export interface EventItem {
  time: string
  type: '进校' | '离校'
  gate: string
  photoUrl?: string
  late?: boolean
  earlyLeave?: boolean
}

export interface RealtimeResp {
  childId: number
  hasNew: boolean
  event: EventItem | null
}

export interface StatsResp {
  childId: number
  range: 'day' | 'month'
  totalDays: number
  presentDays: number
  lateCount: number
  earlyLeaveCount: number
  absentDays: number
  dailyPresence: Record<string, number>
}

export interface PhotoItem {
  time: string
  url: string
  type: '进校' | '离校'
  gate: string
}

export interface AlertItem {
  title: string
  time: string
  txtdesc: string
}

export interface CheckInResp {
  success: boolean
  message: string
  time: string
  late: boolean
}

export const getRealtime = (childId?: number) => {
  return request.get<RealtimeResp>('/api/v1/attendance/realtime', { params: { childId } })
}

export const getRecords = (params: { childId?: number; startDate?: string; endDate?: string; page?: number; size?: number }) => {
  return request.get<EventItem[]>('/api/v1/attendance/records', { params })
}

export const getStats = (params: { childId?: number; range?: 'day' | 'month' }) => {
  return request.get<StatsResp>('/api/v1/attendance/stats', { params })
}

export const getPhotos = (params: { childId?: number; date?: string }) => {
  return request.get<PhotoItem[]>('/api/v1/attendance/photos', { params })
}

export const getAlerts = (childId?: number) => {
  return request.get<AlertItem[]>('/api/v1/attendance/alerts', { params: { childId } })
}

export const checkIn = (childId?: number) => {
  // 后端按 @RequestParam 接收 childId；此处直接作为 body 传递也兼容（默认 childId 将回落到 2001）
  return request.post<CheckInResp>('/api/v1/attendance/checkin', childId ? { childId } : undefined)
}