import request from './request'

export type NotifyCategory = 'ATTENDANCE' | 'CONSUME' | 'LOW_BALANCE' | 'LEAVE_APPROVAL' | 'ANNOUNCEMENT' | 'CLASS_NOTICE'

export interface NotificationItem {
  id: number
  childId: number
  category: NotifyCategory
  title: string
  content: string
  extra?: string
  readFlag: boolean
  createdAt: string
}

export interface PageResp<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const notifyApi = {
  list: (childId: number, category?: NotifyCategory, page = 0, size = 10, unreadOnly?: boolean) => {
    const params: any = { childId, page, size }
    if (category) params.category = category
    if (unreadOnly) params.unreadOnly = true
    return request.get<PageResp<NotificationItem>>('/api/v1/notify/list', { params })
  },
  unreadCount: (childId: number, category?: NotifyCategory) => {
    const params: any = { childId }
    if (category) params.category = category
    return request.get<number>('/api/v1/notify/unread_count', { params })
  },
  markRead: (id: number) => {
    return request.post<boolean>('/api/v1/notify/mark_read', { id })
  },
  markReadBatch: (ids: number[]) => {
    return request.post<boolean>('/api/v1/notify/mark_read', { ids })
  },
  realtime: (childId: number) => {
    return request.get<NotificationItem[]>('/api/v1/notify/realtime', { params: { childId } })
  },
  publish: (payload: { childId: number; category: NotifyCategory; title: string; content: string; extra?: string }) => {
    return request.post<number>('/api/v1/notify/publish', payload)
  },
  stats: (childId: number) => {
    return request.get<{ totalUnread: number; categoryUnread: Record<string, number> }>('/api/v1/notify/stats', { params: { childId } })
  },
  item: (id: number) => {
    return request.get<NotificationItem>('/api/v1/notify/item', { params: { id } })
  },
  mockGenerate: (childId: number) => {
    return request.post<number>('/api/v1/notify/mock/generate', { childId })
  }
}