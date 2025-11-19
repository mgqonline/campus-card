import request from '@/api/request'

export interface StatsResp {
  totalAmount: number
  txCount: number
}

export interface DayStat {
  day: number
  totalAmount: number
  txCount: number
}

export interface MonthlyResp extends StatsResp {
  year: number
  month: number
  series: DayStat[]
}

export interface StudentStat {
  studentId: number
  studentName: string
  studentNo: string
  totalAmount: number
  txCount: number
}

export interface ClassConsumeResp extends StatsResp {
  classId: number
  className?: string
  studentCount: number
  breakdown: StudentStat[]
}

export interface PersonalResp extends StatsResp {
  studentId: number
  studentNo: string
  studentName: string
  series: DayStat[]
}

export interface RankingItem {
  studentId: number
  studentName: string
  studentNo: string
  totalAmount: number
}

export const getDailyConsume = (params: { date: string }) => {
  return request.get<StatsResp>('/api/v1/reports/consume/daily', { params })
}

export const getMonthlyConsume = (params: { year: number; month: number }) => {
  return request.get<MonthlyResp>('/api/v1/reports/consume/monthly', { params })
}

export const getClassConsume = (params: { classId: number; startDate?: string; endDate?: string }) => {
  return request.get<ClassConsumeResp>('/api/v1/reports/consume/class', { params })
}

export const getPersonalConsume = (params: { studentId?: number; studentNo?: string; startDate?: string; endDate?: string }) => {
  return request.get<PersonalResp>('/api/v1/reports/consume/personal', { params })
}

export const getConsumeRanking = (params: { startDate?: string; endDate?: string; limit?: number }) => {
  return request.get<RankingItem[]>('/api/v1/reports/consume/ranking', { params })
}

export const getConsumeTrend = (params: { startDate?: string; endDate?: string }) => {
  return request.get<DayStat[]>('/api/v1/reports/consume/trend', { params })
}