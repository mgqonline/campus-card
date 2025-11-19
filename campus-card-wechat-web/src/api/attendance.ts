import request from './request'

// 考勤相关接口类型定义
export interface AttendanceStatus {
  childId: number
  hasNew: boolean
  event: {
    time: string
    type: string
    gate: string
    photoUrl: string
    late: boolean
    earlyLeave: boolean
  } | null
}

export interface AttendanceRecord {
  id: number
  checkTime: string
  type: 'check_in' | 'check_out'
  status: 'normal' | 'late' | 'early' | 'absent'
  location?: string
}

export interface AttendanceStats {
  totalDays: number
  presentDays: number
  lateDays: number
  absentDays: number
  attendanceRate: number
}

export interface AttendanceAlert {
  id: number
  type: 'late' | 'absent' | 'early'
  title: string
  message: string
  createTime: string
}

export interface AttendanceRecordsReq {
  date?: string
  startDate?: string
  endDate?: string
}

// 考勤API
export const attendanceApi = {
  // 获取实时考勤状态
  getRealtime: () => {
    return request.get<AttendanceStatus>('/api/v1/attendance/realtime')
  },

  // 打卡签到
  checkIn: () => {
    return request.post('/api/v1/attendance/checkin')
  },

  // 获取考勤记录
  getRecords: (params: AttendanceRecordsReq) => {
    return request.get<AttendanceRecord[]>('/api/v1/attendance/records', { params })
  },

  // 获取考勤统计
  getStats: () => {
    return request.get<AttendanceStats>('/api/v1/attendance/stats')
  },

  // 获取考勤提醒
  getAlerts: () => {
    return request.get<AttendanceAlert[]>('/api/v1/attendance/alerts')
  },

  // 查看考勤照片
  getPhoto: (recordId: number) => {
    return request.get(`/api/v1/attendance/photos/${recordId}`)
  }
}