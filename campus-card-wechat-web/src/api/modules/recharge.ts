import http from '../request'

export interface RechargeOptions { suggested: number[]; min: number; max: number }
export interface CreateOrderReq { childId: number; amount: number }
export interface CreateOrderResp { orderId: string; childId: number; amount: number; payParams: Record<string, string> }
export interface RechargeRecord { orderId: string; childId: number; amount: number; status: string; time: string }

export function getRechargeOptions() {
  return http.get<RechargeOptions>('/api/v1/recharge/options')
}

export function createRechargeOrder(data: CreateOrderReq) {
  return http.post<CreateOrderResp>('/api/v1/recharge/create', data)
}

export function getRechargeRecords(params: { childId?: number; page?: number; size?: number }) {
  return http.get<RechargeRecord[]>('/api/v1/recharge/records', { params })
}

export function applyInvoice(data: { orderId: string; email: string }) {
  return http.post<string>('/api/v1/recharge/invoice', data)
}