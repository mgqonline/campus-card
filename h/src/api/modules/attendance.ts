import request from '../request'

export interface PageParams { page: number; size: number }
export interface PageResult<T> { records: T[]; total: number }

export interface AttendanceRecord {
  id: number
  studentId: number
  studentName: string
  studentNo: string
  classId: number
  className: string
  deviceId: number
  deviceName: string
  attendanceTime: string
  attendanceType: 'in' | 'out'
  checkType: 'card' | 'face' | 'manual'
  photoUrl?: string
  status: 'normal' | 'late' | 'early' | 'absence'
  remark?: string
}

export interface AttendanceQuery extends PageParams {
  studentId?: number
  classId?: number
  startDate?: string
  endDate?: string
  status?: string
  checkType?: string
  studentName?: string
}

export interface AttendanceStatistics {
  totalCount: number
  normalCount: number
  lateCount: number
  earlyCount: number
  absenceCount: number
  attendanceRate: number
}

export interface AttendanceRule {
  id?: number
  scenario: 'SCHOOL' | 'ENTERPRISE'
  workDays: string // e.g. MON,TUE,WED,THU,FRI
  workStart: string // HH:mm
  workEnd: string // HH:mm
  lateGraceMin?: number
  earlyGraceMin?: number
  enabled: boolean
}

export interface FaceSuccessRate {
  totalAttempts: number
  successCount: number
  successRate: number
}

export const getAttendanceList = (params: AttendanceQuery) => {
  return request.get<PageResult<AttendanceRecord>>('/api/v1/attendance/records', { params })
}

export const getAttendanceStatistics = (params: Omit<AttendanceQuery, 'page' | 'size'>) => {
  return request.get<AttendanceStatistics>('/api/v1/attendance/statistics', { params })
}

export const getFaceSuccessRate = (params: { startDate?: string; endDate?: string; classId?: number }) => {
  return request.get<FaceSuccessRate>('/api/v1/attendance/face/success-rate', { params })
}

export const getAttendanceRule = () => {
  return request.get<AttendanceRule>('/api/v1/attendance/rules')
}

export const saveAttendanceRule = (data: AttendanceRule) => {
  return request.post<AttendanceRule>('/api/v1/attendance/rules', data)
}

export const markAttendanceException = (id: number, data: { status?: 'normal' | 'late' | 'early' | 'absence'; reason?: string }) => {
  return request.put<AttendanceRecord>(`/api/v1/attendance/records/${id}/exception`, data)
}

export const correctAttendanceRecord = (id: number, data: { attendanceTime?: string; attendanceType?: 'in' | 'out'; status?: 'normal' | 'late' | 'early' | 'absence'; reason?: string }) => {
  return request.put<AttendanceRecord>(`/api/v1/attendance/records/${id}/correct`, data)
}

export const supplementAttendanceRecord = (data: { studentId?: number; studentNo?: string; attendanceType?: 'in' | 'out'; attendanceTime?: string; checkType?: 'manual' | 'card' | 'face'; deviceId?: number; remark?: string }) => {
  return request.post<AttendanceRecord>('/api/v1/attendance/records/supplement', data)
}