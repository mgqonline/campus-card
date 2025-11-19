import http from '@/api/request'

export interface FaceConfig {
  id: number
  recognitionThreshold: number
  recognitionMode: 'ONE_TO_ONE' | 'ONE_TO_MANY'
  livenessEnabled: boolean
  libraryCapacity: number
  updatedAt?: string
}

export interface UpdateFaceConfigReq {
  recognitionThreshold?: number
  recognitionMode?: 'ONE_TO_ONE' | 'ONE_TO_MANY'
  livenessEnabled?: boolean
  libraryCapacity?: number
}

export function getFaceConfig() {
  return http.get<FaceConfig>('/api/v1/face-config')
}

export function updateFaceConfig(body: UpdateFaceConfigReq) {
  return http.put<FaceConfig>('/api/v1/face-config', body)
}