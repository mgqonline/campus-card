import request from './request'
import type { AxiosRequestConfig, AxiosProgressEvent } from 'axios'

// 人像采集相关接口

export interface UploadResp {
  id: number
  url: string
  status: string
  qualityScore: number
  qualityIssues: string
}

export interface CaptureReq {
  childId: number
  imageData: string // base64编码的图片数据
}

export interface CaptureResp {
  id: number
  url: string
  status: string
  qualityScore: number
  qualityIssues: string
}

export interface ProgressResp {
  childId: number
  hasRecord: boolean
  status: string
  qualityScore?: number
  createdTime?: string
  auditComment?: string
  photoUrl?: string
}

export interface HistoryItem {
  id: number
  photoUrl: string
  collectionType: string
  status: string
  qualityScore: number
  createdTime: string
  auditComment?: string
}

/**
 * 上传人脸照片
 */
export function uploadFacePhoto(
  file: File,
  childId: number,
  config?: AxiosRequestConfig
): Promise<UploadResp> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('childId', childId.toString())
  
  const axiosConfig: AxiosRequestConfig = {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    ...(config || {})
  }

  return request.post('/api/v1/face/upload', formData, axiosConfig)
}

/**
 * 拍摄人脸照片
 */
export function captureFacePhoto(data: CaptureReq): Promise<CaptureResp> {
  return request.post('/api/v1/face/capture', data)
}

/**
 * 查询人像采集进度
 */
export function getFaceProgress(childId: number): Promise<ProgressResp> {
  return request.get('/api/v1/face/progress', { params: { childId } })
}

/**
 * 查询人像采集历史记录
 */
export function getFaceHistory(childId: number): Promise<HistoryItem[]> {
  return request.get('/api/v1/face/history', { params: { childId } })
}

/**
 * 重新提交人像采集
 */
export function resubmitFace(childId: number): Promise<string> {
  return request.post('/api/v1/face/resubmit', null, { params: { childId } })
}