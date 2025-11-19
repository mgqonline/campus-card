import http from '../request'

export interface HelpArticle {
  id: number
  title: string
  content: string
  category?: string
}

export function getHelpArticles() {
  return http.get<HelpArticle[]>('/api/v1/help/articles')
}