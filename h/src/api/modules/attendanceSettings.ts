import request from '../request'

export interface TimePeriod { name: string; start: string; end: string }
export interface Holiday { name: string; startDate: string; endDate: string }
export interface Adjustment { date: string; type: 'WORK' | 'OFF'; name?: string }

export interface AttendanceSettings {
  periods: TimePeriod[]
  absenceThresholdMin: number
  leaveTypes: string[]
  holidays: Holiday[]
  adjustments: Adjustment[]
}

export const getAttendanceSettings = () => {
  return request.get<AttendanceSettings>('/api/v1/attendance/settings')
}

export const saveAttendanceSettings = (data: AttendanceSettings) => {
  return request.put<AttendanceSettings>('/api/v1/attendance/settings', data)
}