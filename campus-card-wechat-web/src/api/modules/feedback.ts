import http from '../request'

export interface FeedbackItem {
  id: number
  childId: number
  category?: string
  content: string
  contact?: string
  status?: string
  createdAt: string
}

export function submitFeedback(data: { childId?: number; category?: string; content: string; contact?: string }) {
  return http.post<FeedbackItem>('/api/v1/feedback/submit', data)
}

export function getFeedbackList(childId?: number) {
  return http.get<FeedbackItem[]>('/api/v1/feedback/list', { params: { childId } })
}