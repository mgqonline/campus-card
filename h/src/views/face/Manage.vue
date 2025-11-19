<template>
  <div>
    <a-page-header title="人脸信息管理" sub-title="采集、录入、质量检测与查看" />

    <a-card style="margin-top: 12px">
      <a-form layout="inline">
        <a-form-item label="人员类型">
          <a-select v-model:value="query.personType" style="width: 160px" allow-clear placeholder="请选择">
            <a-select-option value="STUDENT">学生</a-select-option>
            <a-select-option value="TEACHER">教师</a-select-option>
            <a-select-option value="STAFF">职工</a-select-option>
            <a-select-option value="VISITOR">访客</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="人员ID">
          <a-input v-model:value="query.personId" placeholder="学号/工号/职工号/访客标识" style="width: 220px" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="load">查询</a-button>
        </a-form-item>
        <a-form-item>
          <a-button @click="reset">重置</a-button>
        </a-form-item>
        <a-form-item>
          <a-button type="dashed" @click="openCreate">新增人脸</a-button>
        </a-form-item>
      </a-form>

      <a-table :dataSource="list.records" :pagination="false" rowKey="id" style="margin-top: 12px">
        <a-table-column title="ID" dataIndex="id" key="id" />
        <a-table-column title="人员类型" dataIndex="personType" key="personType">
          <template #default="{ text }">
            {{ personTypeLabel(text) }}
          </template>
        </a-table-column>
        <a-table-column title="人员ID" dataIndex="personId" key="personId" />
        <a-table-column title="质量分" dataIndex="qualityScore" key="qualityScore" />
        <a-table-column title="创建时间" dataIndex="createdAt" key="createdAt" />
        <a-table-column title="操作" key="actions">
          <template #default="{ record }">
            <a @click="viewPhoto(record)">查看照片</a>
            <a-divider type="vertical" />
            <a @click="openEdit(record)">编辑</a>
            <a-divider type="vertical" />
            <a @click="confirmDelete(record)" style="color:#ff4d4f">删除</a>
          </template>
        </a-table-column>
      </a-table>

      <div style="text-align: right; margin-top: 12px">
        <a-pagination
          :current="page"
          :pageSize="size"
          :total="list.total"
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
          :showSizeChanger="true"
          :pageSizeOptions="['10','20','50']"
        />
      </div>
    </a-card>

    <a-modal v-model:open="createOpen" title="新增人脸信息" :confirmLoading="saving" @ok="saveCreate" @cancel="closeCreate" width="720px">
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item label="人员类型" required>
          <a-select v-model:value="form.personType" placeholder="请选择">
            <a-select-option value="STUDENT">学生</a-select-option>
            <a-select-option value="TEACHER">教师</a-select-option>
            <a-select-option value="STAFF">职工</a-select-option>
            <a-select-option value="VISITOR">访客</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="人员ID" required>
          <a-input v-model:value="form.personId" placeholder="学号/工号/职工号/访客标识" />
        </a-form-item>
        <a-form-item label="照片采集">
          <a-upload :beforeUpload="handleBeforeUpload" :showUploadList="false" accept="image/*">
            <a-button>选择照片文件</a-button>
          </a-upload>
          <div v-if="photoPreview" style="margin-top: 8px">
            <img :src="photoPreview" style="max-width: 100%; max-height: 240px; border: 1px solid #eee" />
          </div>
        </a-form-item>
        <a-form-item label="照片Base64">
          <a-textarea v-model:value="form.photoBase64" :rows="4" placeholder="data:image/jpeg;base64,..." />
        </a-form-item>
        <a-form-item label="质量检测">
          <div style="display: flex; align-items: center; gap: 8px">
            <a-button @click="checkQuality" :disabled="!form.photoBase64">检测质量</a-button>
            <span v-if="quality.score !== undefined">分数：{{ quality.score }}（{{ quality.message }}）</span>
          </div>
        </a-form-item>
        <a-form-item label="特征（可选）">
          <a-textarea v-model:value="form.features" :rows="2" placeholder="预留字段，可对接第三方返回特征" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="editOpen" title="编辑人脸信息" :confirmLoading="saving" @ok="saveEdit" @cancel="closeEdit" width="720px">
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item label="照片更新">
          <a-upload :beforeUpload="handleBeforeUploadEdit" :showUploadList="false" accept="image/*">
            <a-button>选择照片文件</a-button>
          </a-upload>
          <div v-if="photoPreviewEdit" style="margin-top: 8px">
            <img :src="photoPreviewEdit" style="max-width: 100%; max-height: 240px; border: 1px solid #eee" />
          </div>
        </a-form-item>
        <a-form-item label="照片Base64">
          <a-textarea v-model:value="editForm.photoBase64" :rows="4" placeholder="data:image/jpeg;base64,..." />
        </a-form-item>
        <a-form-item label="质量检测">
          <div style="display: flex; align-items: center; gap: 8px">
            <a-button @click="checkQualityEdit" :disabled="!editForm.photoBase64">检测质量</a-button>
            <span v-if="qualityEdit.score !== undefined">分数：{{ qualityEdit.score }}（{{ qualityEdit.message }}）</span>
          </div>
        </a-form-item>
        <a-form-item label="特征（可选）">
          <a-textarea v-model:value="editForm.features" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="photoOpen" title="人脸照片查看" footer="null" @cancel="photoOpen = false" width="720px">
      <div style="text-align:center">
        <img :src="photoView" style="max-width: 100%; max-height: 520px; border: 1px solid #eee" />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { PersonType, FaceInfo, PageResult, CreateFaceReq, UpdateFaceReq, QualityResult } from '@/api/modules/face'
import { listFaces, createFace, updateFace, deleteFace, qualityCheck, getFacePhoto } from '@/api/modules/face'
import { getStudentPhotoUrl } from '@/api/modules/student'

const page = ref(1)
const size = ref(10)
const query = reactive<{ personType?: PersonType; personId?: string }>({})
const list = reactive<PageResult<FaceInfo>>({ total: 0, records: [] })

const createOpen = ref(false)
const editOpen = ref(false)
const photoOpen = ref(false)
const saving = ref(false)

const form = reactive<CreateFaceReq>({ personType: undefined as any, personId: '', photoBase64: '', features: '' })
const editForm = reactive<UpdateFaceReq>({ photoBase64: '', features: '' })
const currentId = ref<number | null>(null)

const photoPreview = ref<string>('')
const photoPreviewEdit = ref<string>('')
const photoView = ref<string>('')

const quality = reactive<Partial<QualityResult>>({})
const qualityEdit = reactive<Partial<QualityResult>>({})

function personTypeLabel(t?: string) {
  switch (t) {
    case 'STUDENT': return '学生'
    case 'TEACHER': return '教师'
    case 'STAFF': return '职工'
    case 'VISITOR': return '访客'
    default: return t || ''
  }
}

function onPageChange(p: number) { page.value = p; load() }
function onPageSizeChange(p: number, s: number) { page.value = p; size.value = s; load() }

async function load() {
  try {
    const personIdParam = query.personId && query.personId.trim() !== '' ? query.personId.trim() : undefined
    const data = await listFaces({ page: page.value, size: size.value, personType: query.personType, personId: personIdParam })
    list.total = data.total
    list.records = data.records
  } catch (e: any) {
    message.error(e.message || '查询失败')
  }
}

function reset() {
  query.personType = undefined
  query.personId = undefined
  page.value = 1
  size.value = 10
  load()
}

function openCreate() {
  createOpen.value = true
  form.personType = undefined as any
  form.personId = ''
  form.photoBase64 = ''
  form.features = ''
  photoPreview.value = ''
  quality.score = undefined
  quality.message = undefined
}

function closeCreate() { createOpen.value = false }

function dataUrlFromBytes(bytes: string) {
  // 若用户仅给纯base64，无前缀，尝试添加jpeg前缀
  if (bytes && bytes.trim() !== '' && !bytes.startsWith('data:image')) {
    return `data:image/jpeg;base64,${bytes}`
  }
  return bytes || ''
}

function handleBeforeUpload(file: File) {
  const reader = new FileReader()
  reader.onload = () => {
    const res = reader.result as string
    photoPreview.value = res
    form.photoBase64 = res as string
  }
  reader.readAsDataURL(file)
  return false
}

async function checkQuality() {
  try {
    const payload = form.photoBase64 || ''
    const res = await qualityCheck({ photoBase64: payload })
    quality.score = res.score
    quality.message = res.message
    if ((res.score || 0) < 60) message.warning('质量偏低，建议重新采集')
    else message.success('质量合格')
  } catch (e: any) {
    message.error(e.message || '质量检测失败')
  }
}

async function saveCreate() {
  if (!form.personType || !form.personId) {
    message.warning('请填写人员类型与ID')
    return
  }
  saving.value = true
  try {
    await createFace({ ...form, photoBase64: form.photoBase64 ? dataUrlFromBytes(form.photoBase64) : undefined })
    message.success('新增成功')
    createOpen.value = false
    load()
  } catch (e: any) {
    message.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function openEdit(row: FaceInfo) {
  currentId.value = row.id
  editOpen.value = true
  editForm.photoBase64 = ''
  editForm.features = row.features || ''
  photoPreviewEdit.value = ''
  qualityEdit.score = undefined
  qualityEdit.message = undefined
}
function closeEdit() { editOpen.value = false }

function handleBeforeUploadEdit(file: File) {
  const reader = new FileReader()
  reader.onload = () => {
    const res = reader.result as string
    photoPreviewEdit.value = res
    editForm.photoBase64 = res as string
  }
  reader.readAsDataURL(file)
  return false
}

async function checkQualityEdit() {
  try {
    const payload = editForm.photoBase64 || ''
    const res = await qualityCheck({ photoBase64: payload })
    qualityEdit.score = res.score
    qualityEdit.message = res.message
    if ((res.score || 0) < 60) message.warning('质量偏低，建议重新采集')
    else message.success('质量合格')
  } catch (e: any) {
    message.error(e.message || '质量检测失败')
  }
}

async function saveEdit() {
  if (!currentId.value) return
  saving.value = true
  try {
    await updateFace(currentId.value, { ...editForm, photoBase64: editForm.photoBase64 ? dataUrlFromBytes(editForm.photoBase64) : undefined })
    message.success('更新成功')
    editOpen.value = false
    load()
  } catch (e: any) {
    message.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function confirmDelete(row: FaceInfo) {
  Modal.confirm({
    title: '确认删除此人脸信息？',
    content: `人员类型：${personTypeLabel(row.personType)}，人员ID：${row.personId}`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        await deleteFace(row.id)
        message.success('删除成功')
        load()
      } catch (e: any) {
        message.error(e.message || '删除失败')
      }
    }
  })
}

async function viewPhoto(row: FaceInfo) {
  try {
    const data = await getFacePhoto(row.id)
    const photoBase64 = data.photoBase64 || ''
    // 确保photoBase64有正确的data URL前缀
    const dataUrl = dataUrlFromBytes(photoBase64)
    if (dataUrl && dataUrl.length > 30) {
      photoView.value = dataUrl
    } else {
      // 后端无图时：学生优先使用学生照片URL，否则使用网络占位图
      if (row.personType === 'STUDENT' && /^\d+$/.test(row.personId)) {
        try {
          photoView.value = getStudentPhotoUrl(Number(row.personId))
        } catch {
          photoView.value = 'https://placehold.co/640x480?text=Face+Photo+Not+Found'
        }
      } else {
        photoView.value = 'https://placehold.co/640x480?text=Face+Photo+Not+Found'
      }
    }
    photoOpen.value = true
  } catch (e: any) {
    message.error(e.message || '获取照片失败')
  }
}

// 移除 renderActions JSX 实现，改用模板插槽

onMounted(load)
</script>