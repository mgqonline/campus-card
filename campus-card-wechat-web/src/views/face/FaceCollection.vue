<template>
  <div class="face-collection">
    <!-- 头部 -->
    <div class="weui-navigation-bar">
      <div class="weui-navigation-bar__inner">
        <div class="weui-navigation-bar__center">
          <strong>人像采集</strong>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="page-content">
      <!-- 当前状态卡片 -->
      <div class="status-card" v-if="progress.hasRecord">
        <div class="status-header">
          <h3>当前状态</h3>
          <span class="status-badge" :class="getStatusClass(progress.status)">
            {{ getStatusText(progress.status) }}
          </span>
        </div>
        
        <div class="status-content">
          <div class="photo-preview" v-if="progress">
            <img 
              :src="resolvePhotoSrc(progress.photoUrl)" 
              alt="人像照片" 
              loading="lazy" 
              decoding="async" 
              @error="onImgError($event)" 
            />
          </div>
          
          <div class="status-info">
            <div class="info-item" v-if="progress.qualityScore">
              <span class="label">质量评分：</span>
              <span class="value">{{ progress.qualityScore.toFixed(1) }}分</span>
            </div>
            <div class="info-item" v-if="progress.createdTime">
              <span class="label">提交时间：</span>
              <span class="value">{{ formatTime(progress.createdTime) }}</span>
            </div>
            <div class="info-item" v-if="progress.auditComment">
              <span class="label">审核意见：</span>
              <span class="value">{{ progress.auditComment }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮区域 -->
      <div class="action-section">
        <div class="weui-cells weui-cells_form">
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <h4>采集方式</h4>
              <p class="weui-cell__desc">请选择人像采集方式</p>
            </div>
          </div>
        </div>

        <div class="action-buttons">
          <button 
            class="weui-btn weui-btn_primary action-btn"
            @click="showUploadDialog = true"
            :disabled="isProcessing"
          >
            <van-icon name="photograph" size="18" />
            上传照片
          </button>

          <button 
            class="weui-btn weui-btn_default action-btn"
            @click="showCameraDialog = true"
            :disabled="isProcessing"
          >
            <van-icon name="video-o" size="18" />
            现场拍摄
          </button>
        </div>

        <div class="tips">
          <div class="weui-cells__tips">
            <p>• 请确保照片清晰，光线充足</p>
            <p>• 人脸正对镜头，表情自然</p>
            <p>• 背景简洁，避免复杂环境</p>
            <p>• 支持 JPG、PNG 格式，大小不超过 5MB</p>
          </div>
        </div>
      </div>

      <!-- 历史记录 -->
      <div class="history-section" v-if="historyList.length > 0">
        <div class="weui-cells__title">历史记录</div>
        <div class="weui-cells">
          <div 
            class="weui-cell weui-cell_access history-item"
            v-for="item in historyList"
            :key="item.id"
            @click="viewHistoryDetail(item)"
          >
            <div class="weui-cell__hd">
              <img 
                :src="resolvePhotoSrc(item.photoUrl)" 
                class="history-thumb" 
                loading="lazy" 
                decoding="async" 
                @error="onImgError($event)" 
              />
            </div>
            <div class="weui-cell__bd">
              <p>{{ getCollectionTypeText(item.collectionType) }}</p>
              <p class="weui-cell__desc">
                {{ formatTime(item.createdTime) }} · 
                评分: {{ item.qualityScore.toFixed(1) }}
              </p>
            </div>
            <div class="weui-cell__ft">
              <span class="status-badge" :class="getStatusClass(item.status)">
                {{ getStatusText(item.status) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 上传对话框 -->
    <van-popup v-model:show="showUploadDialog" position="bottom" :style="{ height: '60%' }">
      <div class="upload-dialog">
        <div class="dialog-header">
          <h3>上传照片</h3>
          <van-icon name="cross" @click="showUploadDialog = false" />
        </div>
        
        <div class="upload-area">
          <input 
            type="file" 
            ref="fileInput" 
            accept="image/*" 
            @change="handleFileSelect"
            style="display: none"
          />
          
          <div class="upload-zone" @click="fileInput?.click()">
            <van-icon name="plus" size="40" />
            <p>点击选择照片</p>
          </div>
          
          <div class="preview-area" v-if="selectedFile">
            <img :src="previewUrl" alt="预览" />
            <div class="file-info">
              <p>{{ selectedFile.name }}</p>
              <p>{{ formatFileSize(selectedFile.size) }}</p>
            </div>
          </div>

          <!-- 上传进度显示 -->
          <div class="progress-area" v-if="uploading">
            <div class="progress-container">
              <div class="progress-bar" :style="{ width: uploadProgress + '%' }"></div>
            </div>
            <p class="progress-text">{{ uploadProgress }}%</p>
          </div>
          <p v-if="uploadError" class="error-text">{{ uploadError }}</p>
        </div>
        
        <div class="dialog-actions">
          <button 
            class="weui-btn weui-btn_primary"
            @click="uploadPhoto"
            :disabled="!selectedFile || uploading"
          >
            {{ uploading ? '上传中...' : '确认上传' }}
          </button>
        </div>
      </div>
    </van-popup>

    <!-- 拍摄对话框 -->
    <van-popup v-model:show="showCameraDialog" position="bottom" :style="{ height: '80%' }">
      <div class="camera-dialog">
        <div class="dialog-header">
          <h3>现场拍摄</h3>
          <van-icon name="cross" @click="closeCameraDialog" />
        </div>
        
        <div class="camera-area">
          <video 
            ref="videoElement" 
            autoplay 
            playsinline
            :style="{ width: '100%', height: '300px', objectFit: 'cover' }"
          ></video>
          
          <canvas 
            ref="canvasElement" 
            style="display: none"
          ></canvas>
          
          <div class="captured-preview" v-if="capturedImage">
            <img :src="capturedImage" alt="拍摄预览" />
          </div>
        </div>
        
        <div class="camera-controls">
          <button 
            class="weui-btn weui-btn_default"
            @click="startCamera"
            v-if="!cameraActive"
          >
            启动摄像头
          </button>
          
          <button 
            class="weui-btn weui-btn_primary"
            @click="capturePhoto"
            v-if="cameraActive && !capturedImage"
          >
            拍摄
          </button>
          
          <div class="capture-actions" v-if="capturedImage">
            <button class="weui-btn weui-btn_default" @click="retakePhoto">
              重拍
            </button>
            <button 
              class="weui-btn weui-btn_primary"
              @click="submitCapture"
              :disabled="submitting"
            >
              {{ submitting ? '提交中...' : '确认提交' }}
            </button>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 加载提示 -->
    <van-loading v-if="loading" type="spinner" color="#1989fa">加载中...</van-loading>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { showToast, showDialog } from 'vant'
import { 
  getFaceProgress, 
  getFaceHistory, 
  uploadFacePhoto, 
  captureFacePhoto,
  type ProgressResp,
  type HistoryItem 
} from '@/api/face'
import type { AxiosProgressEvent } from 'axios'

// 占位人脸图片（base64）
const fallbackFace = 'data:image/jpeg;base64,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'

// API 基础地址，用于拼接相对图片路径
const API_BASE = import.meta.env.VITE_API_BASE || ''
function resolvePhotoSrc(url?: string): string {
  if (!url) return '/face-placeholder.jpg'
  if (url.startsWith('data:') || /^https?:\/\//.test(url)) return url
  if (url.startsWith('/')) return `${API_BASE}${url}`
  return url
}

function onImgError(e: Event) {
  const img = e.target as HTMLImageElement
  const placeholder = '/face-placeholder.jpg'
  if (img && img.src !== placeholder) {
    img.src = placeholder
  }
}

// 响应式数据
const loading = ref(false)
const progress = ref<ProgressResp>({
  childId: 2001,
  hasRecord: false,
  status: 'NOT_COLLECTED'
})
const historyList = ref<HistoryItem[]>([])

// 上传相关
const showUploadDialog = ref(false)
const selectedFile = ref<File | null>(null)
const previewUrl = ref('')
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadError = ref('')
const fileInput = ref<HTMLInputElement>()

// 拍摄相关
const showCameraDialog = ref(false)
const videoElement = ref<HTMLVideoElement>()
const canvasElement = ref<HTMLCanvasElement>()
const cameraActive = ref(false)
const capturedImage = ref('')
const submitting = ref(false)
const mediaStream = ref<MediaStream | null>(null)

// 计算属性
const isProcessing = ref(false)

// 页面加载
onMounted(() => {
  loadData()
})

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const childId = 2001 // 模拟学生ID
    
    // 加载进度
    const progressData = await getFaceProgress(childId)
    progress.value = progressData
    
    // 加载历史记录
    const historyData = await getFaceHistory(childId)
    historyList.value = historyData
    
  } catch (error) {
    console.error('加载数据失败:', error)
    showToast('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 文件选择处理
function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  // 文件大小检查
  if (file.size > 5 * 1024 * 1024) {
    showToast('文件大小不能超过5MB')
    return
  }
  
  // 文件类型检查
  if (!file.type.startsWith('image/')) {
    showToast('请选择图片文件')
    return
  }
  
  selectedFile.value = file
  
  // 生成预览
  const reader = new FileReader()
  reader.onload = (e) => {
    previewUrl.value = e.target?.result as string
  }
  reader.readAsDataURL(file)
}

// 上传照片
async function uploadPhoto() {
  if (!selectedFile.value) return
  
  uploading.value = true
  uploadProgress.value = 0
  uploadError.value = ''
  try {
    const result = await uploadFacePhoto(
      selectedFile.value,
      progress.value.childId,
      {
        onUploadProgress: (e: AxiosProgressEvent) => {
          if (typeof e.progress === 'number') {
            uploadProgress.value = Math.round(e.progress * 100)
          } else if (e.total && e.loaded) {
            uploadProgress.value = Math.round((e.loaded / e.total) * 100)
          }
        }
      }
    )
    
    showToast('上传成功')
    showUploadDialog.value = false
    
    // 重新加载数据
    await loadData()
    
  } catch (error) {
    console.error('上传失败:', error)
    uploadError.value = (error as Error).message || '上传失败'
    showToast(uploadError.value)
  } finally {
    uploading.value = false
    selectedFile.value = null
    previewUrl.value = ''
    uploadProgress.value = 0
    uploadError.value = ''
  }
}

// 启动摄像头
async function startCamera() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ 
      video: { facingMode: 'user' } 
    })
    
    mediaStream.value = stream
    
    await nextTick()
    if (videoElement.value) {
      videoElement.value.srcObject = stream
      cameraActive.value = true
    }
  } catch (error) {
    console.error('启动摄像头失败:', error)
    showToast('无法访问摄像头')
  }
}

// 拍摄照片
function capturePhoto() {
  if (!videoElement.value || !canvasElement.value) return
  
  const video = videoElement.value
  const canvas = canvasElement.value
  const ctx = canvas.getContext('2d')
  
  if (!ctx) return
  
  // 设置画布尺寸
  canvas.width = video.videoWidth
  canvas.height = video.videoHeight
  
  // 绘制视频帧到画布
  ctx.drawImage(video, 0, 0)
  
  // 获取base64数据
  capturedImage.value = canvas.toDataURL('image/jpeg', 0.8)
}

// 重新拍摄
function retakePhoto() {
  capturedImage.value = ''
}

// 提交拍摄
async function submitCapture() {
  if (!capturedImage.value) return
  
  submitting.value = true
  try {
    const base64Data = capturedImage.value.split(',')[1] // 移除data:image/jpeg;base64,前缀
    
    const result = await captureFacePhoto({
      childId: progress.value.childId,
      imageData: base64Data
    })
    
    showToast('拍摄提交成功')
    closeCameraDialog()
    
    // 重新加载数据
    await loadData()
    
  } catch (error) {
    console.error('提交失败:', error)
    showToast('提交失败')
  } finally {
    submitting.value = false
  }
}

// 关闭摄像头对话框
function closeCameraDialog() {
  showCameraDialog.value = false
  
  // 停止摄像头
  if (mediaStream.value) {
    mediaStream.value.getTracks().forEach(track => track.stop())
    mediaStream.value = null
  }
  
  cameraActive.value = false
  capturedImage.value = ''
}

// 查看历史详情
function viewHistoryDetail(item: HistoryItem) {
  showDialog({
    title: '详情',
    message: `
      采集方式：${getCollectionTypeText(item.collectionType)}
      状态：${getStatusText(item.status)}
      质量评分：${item.qualityScore.toFixed(1)}分
      提交时间：${formatTime(item.createdTime)}
      ${item.auditComment ? `审核意见：${item.auditComment}` : ''}
    `
  })
}

// 工具函数
function getStatusText(status: string): string {
  const statusMap: Record<string, string> = {
    'NOT_COLLECTED': '未采集',
    'PENDING': '待审核',
    'PROCESSING': '处理中',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

function getStatusClass(status: string): string {
  const classMap: Record<string, string> = {
    'NOT_COLLECTED': 'status-gray',
    'PENDING': 'status-orange',
    'PROCESSING': 'status-blue',
    'APPROVED': 'status-green',
    'REJECTED': 'status-red',
    'CANCELLED': 'status-gray'
  }
  return classMap[status] || 'status-gray'
}

function getCollectionTypeText(type: string): string {
  return type === 'UPLOAD' ? '上传照片' : '现场拍摄'
}

function formatTime(timeStr: string): string {
  return new Date(timeStr).toLocaleString('zh-CN')
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}
</script>

<style scoped>
.face-collection {
  min-height: 100vh;
  background-color: #f7f7f7;
}

.page-content {
  padding: 16px;
  padding-top: 60px; /* 为导航栏留出空间 */
}

.status-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.progress-area {
  margin-top: 12px;
}
.progress-container {
  width: 100%;
  height: 8px;
  background: #f2f3f5;
  border-radius: 4px;
  overflow: hidden;
}
.progress-bar {
  height: 100%;
  width: 0%;
  background: #1989fa;
  transition: width 0.2s ease;
}
.progress-text {
  font-size: 12px;
  color: #666;
  margin-top: 6px;
  text-align: right;
}
.error-text {
  margin-top: 8px;
  color: #ee0a24;
  font-size: 12px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.status-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-green { background: #f0f9ff; color: #10b981; }
.status-orange { background: #fef3c7; color: #f59e0b; }
.status-blue { background: #dbeafe; color: #3b82f6; }
.status-red { background: #fee2e2; color: #ef4444; }
.status-gray { background: #f3f4f6; color: #6b7280; }

.status-content {
  display: flex;
  gap: 12px;
}

.photo-preview {
  flex-shrink: 0;
}

.photo-preview img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.status-info {
  flex: 1;
}

.info-item {
  margin-bottom: 8px;
  font-size: 14px;
}

.info-item .label {
  color: #666;
}

.info-item .value {
  color: #333;
  font-weight: 500;
}

.action-section {
  background: white;
  border-radius: 8px;
  margin-bottom: 16px;
}

.action-buttons {
  padding: 16px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 12px;
  align-items: stretch;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 48px;
  padding: 0 16px;
  line-height: 1;
  box-sizing: border-box;
  font-size: 16px;
}

/* 修正 WeUI 按钮默认外边距，保证横向对齐 */
.action-buttons .weui-btn {
  margin: 0;
  width: 100%;
  height: 48px;
  line-height: 1;
}

/* 统一按钮内图标的视觉尺寸与对齐 */
.action-btn .van-icon {
  font-size: 18px;
  line-height: 1;
}

/* 保证不同按钮风格也一致对齐 */
.action-buttons .weui-btn_default,
.action-buttons .weui-btn_primary {
  display: flex;
  align-items: center;
}

.tips {
  padding: 0 16px 16px;
}

.history-section {
  background: white;
  border-radius: 8px;
}

.history-item {
  padding: 12px 16px;
}

.history-thumb {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
}

/* 对话框样式 */
.upload-dialog,
.camera-dialog {
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.upload-area,
.camera-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upload-zone {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-zone:hover {
  border-color: #1989fa;
}

.upload-zone p {
  margin: 8px 0 0;
  color: #666;
}

.preview-area {
  text-align: center;
}

.preview-area img {
  max-width: 100%;
  max-height: 200px;
  border-radius: 8px;
}

.file-info {
  margin-top: 8px;
}

.file-info p {
  margin: 4px 0;
  font-size: 14px;
  color: #666;
}

.camera-controls {
  padding: 16px 0;
  text-align: center;
}

.capture-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.captured-preview {
  text-align: center;
}

.captured-preview img {
  max-width: 100%;
  max-height: 200px;
  border-radius: 8px;
}

.dialog-actions {
  padding: 16px 0 0;
}
</style>