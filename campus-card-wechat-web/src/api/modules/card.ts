import http from '../request'

export type CardRequestType = 'LOSS' | 'UNLOSS' | 'REPLACE'
export type CardRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface CardRequestItem {
  id: number
  childId: number
  type: CardRequestType
  reason?: string
  contact?: string
  status: CardRequestStatus
  createdAt: string
}

export function submitCardRequest(data: { childId?: number; type: CardRequestType; reason?: string; contact?: string }) {
  return http.post<CardRequestItem>('/api/v1/card/request', data)
}

export function getCardRequests(childId?: number) {
  return http.get<CardRequestItem[]>('/api/v1/card/requests', { params: { childId } })
}