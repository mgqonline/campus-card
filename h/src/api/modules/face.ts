import http from '@/api/request'

export type PersonType = 'STUDENT' | 'TEACHER' | 'STAFF' | 'VISITOR'

export interface FaceInfo {
  id: number
  personType: PersonType
  personId: string
  photoBase64?: string
  features?: string
  qualityScore?: number
  createdAt?: string
  updatedAt?: string
}

export interface PageResult<T> {
  total: number
  records: T[]
}

export interface CreateFaceReq {
  personType: PersonType
  personId: string
  photoBase64?: string
  features?: string
}

export interface UpdateFaceReq {
  photoBase64?: string
  features?: string
}

export interface QualityReq {
  photoBase64: string
}

export interface QualityResult {
  success: boolean
  score: number
  message: string
}

export function listFaces(params: { page?: number; size?: number; personType?: PersonType; personId?: string }) {
  const query = { page: 1, size: 10, ...params }
  return http.get<PageResult<FaceInfo>>(`/api/v1/faces`, { params: query })
}

export function getFace(id: number) {
  return http.get<FaceInfo>(`/api/v1/faces/${id}`)
}

export function createFace(body: CreateFaceReq) {
  return http.post<FaceInfo>(`/api/v1/faces`, body)
}

export function updateFace(id: number, body: UpdateFaceReq) {
  return http.put<FaceInfo>(`/api/v1/faces/${id}`, body)
}

export function deleteFace(id: number) {
  return http.delete<void>(`/api/v1/faces/${id}`)
}

export function qualityCheck(body: QualityReq) {
  return http.post<QualityResult>(`/api/v1/faces/quality`, body)
}

export function getFacePhoto(id: number) {
  return http.get<{ photoBase64: string }>(`/api/v1/faces/${id}/photo`)
}