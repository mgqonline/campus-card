import request from '../request'

export interface StatsPoint {
  label: string
  totalCount: number
  normalCount: number
  lateCount: number
  earlyCount: number
  absenceCount: number
  attendanceRate: number
}

export interface ClassStatsPoint extends StatsPoint {
  classId: number
  className?: string
}

export interface PersonalStatsPoint extends StatsPoint {
  studentId: number
  studentName?: string
}

export const getDailyStats = (params: { startDate?: string; endDate?: string; classId?: number }) => {
  return request.get<StatsPoint[]>('/api/v1/attendance/analytics/daily', { params })
}

export const getMonthlyStats = (params: { startDate?: string; endDate?: string; classId?: number }) => {
  return request.get<StatsPoint[]>('/api/v1/attendance/analytics/monthly', { params })
}

export const getTermStats = (params: { startDate?: string; endDate?: string; classId?: number }) => {
  return request.get<StatsPoint[]>('/api/v1/attendance/analytics/term', { params })
}

export const getClassStats = (params: { startDate?: string; endDate?: string }) => {
  return request.get<ClassStatsPoint[]>('/api/v1/attendance/analytics/classes', { params })
}

export const getPersonalStats = (params: { startDate?: string; endDate?: string; classId?: number }) => {
  return request.get<PersonalStatsPoint[]>('/api/v1/attendance/analytics/personal', { params })
}

export const getLateEarlyStats = (params: { startDate?: string; endDate?: string; classId?: number; studentId?: number }) => {
  return request.get<StatsPoint>('/api/v1/attendance/analytics/late-early', { params })
}

export const getAbsenceStats = (params: { startDate?: string; endDate?: string; classId?: number; studentId?: number }) => {
  return request.get<StatsPoint>('/api/v1/attendance/analytics/absence', { params })
}

export const exportCsv = async (type: 'records' | 'daily' | 'monthly' | 'class' | 'personal', params: { startDate?: string; endDate?: string; classId?: number; studentId?: number }) => {
  const url = '/api/v1/attendance/analytics/export'
  const resp = await request.get<string>(url, {
    params: { type, ...(params as any) },
    headers: { Accept: 'text/csv' },
    responseType: 'text'
  } as any)
  return resp as unknown as string
}