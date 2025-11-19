import http from '../request'

export interface BalanceResp {
  childId: number
  balance: number
  currency: string
}

export function getBalance(params: { childId?: number }) {
  return http.get<BalanceResp>('/api/v1/account/balance', { params })
}