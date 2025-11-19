import request from '../request'

export interface ConsumeItem {
  date: string
  merchant: string
  amount: number
  detail: string
  channel: string
  txId: string
}

export interface ConsumeStats {
  totalAmount: number
  txCount: number
  avgPerDay: number
}

export interface DailyStat {
  date: string
  totalAmount: number
  txCount: number
}

export interface TrendPoint { date: string; value: number }

export const getConsumeList = (params: { childId?: number; startDate?: string; endDate?: string; page?: number; size?: number }) =>
  request.get<ConsumeItem[]>('/api/v1/consume/list', { params })

export const getConsumeStats = (params: { childId?: number; startDate?: string; endDate?: string }) =>
  request.get<ConsumeStats>('/api/v1/consume/stats', { params })

export const getConsumeCalendar = (params: { childId?: number; month: string }) =>
  request.get<DailyStat[]>('/api/v1/consume/calendar', { params })

export const getConsumeTrend = (params: { childId?: number; startDate?: string; endDate?: string }) =>
  request.get<TrendPoint[]>('/api/v1/consume/trend', { params })