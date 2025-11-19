<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, h } from 'vue'
import { message } from 'ant-design-vue'
import { 
  dispatchPersons, dispatchFacePhotos, dispatchFaceFeatures, dispatchCards,
  dispatchPermissions, dispatchTimeGroups, dispatchAttendanceRules,
  listTasks, type SyncTask
} from '@/api/modules/sync'

const loading = ref(false)
const loadingTasks = ref(false)
const tasks = ref<SyncTask[]>([])
const autoRefresh = ref(true)
let refreshTimer: number | null = null

const startAutoRefresh = () => {
  if (refreshTimer != null) return
  refreshTimer = window.setInterval(() => {
    loadTasks()
  }, 5000)
}

const stopAutoRefresh = () => {
  if (refreshTimer != null) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

const onToggleAutoRefresh = (checked: boolean) => {
  autoRefresh.value = checked
  if (checked) startAutoRefresh()
  else stopAutoRefresh()
}
const form = reactive({
  scope: '',
  payloadSummary: '',
  types: [] as string[]
})

const typeOptions = [
  { label: '人员信息', value: 'PERSONS' },
  { label: '人脸照片', value: 'FACE_PHOTOS' },
  { label: '人脸特征', value: 'FACE_FEATURES' },
  { label: '卡片信息', value: 'CARDS' },
  { label: '权限信息', value: 'PERMISSIONS' },
  { label: '时间组', value: 'TIME_GROUPS' },
  { label: '考勤规则', value: 'ATTENDANCE_RULES' }
]

const loadTasks = async () => {
  loadingTasks.value = true
  try {
    tasks.value = await listTasks(50)
  } catch (e: any) {
    tasks.value = []
  } finally {
    loadingTasks.value = false
  }
}

const onDispatch = async () => {
  if (!form.types.length) {
    message.error('请选择至少一个下发类型')
    return
  }
  loading.value = true
  try {
    const body = { scope: form.scope || undefined, payloadSummary: form.payloadSummary || undefined }
    for (const t of form.types) {
      if (t === 'PERSONS') await dispatchPersons(body)
      else if (t === 'FACE_PHOTOS') await dispatchFacePhotos(body)
      else if (t === 'FACE_FEATURES') await dispatchFaceFeatures(body)
      else if (t === 'CARDS') await dispatchCards(body)
      else if (t === 'PERMISSIONS') await dispatchPermissions(body)
      else if (t === 'TIME_GROUPS') await dispatchTimeGroups(body)
      else if (t === 'ATTENDANCE_RULES') await dispatchAttendanceRules(body)
    }
    message.success('下发任务已创建')
    await loadTasks()
  } catch (e: any) {
    message.error(e?.message || '下发失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTasks()
  if (autoRefresh.value) startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<template>
  <div>
    <a-card title="基础数据下发">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="下发类型" required>
              <a-select v-model:value="form.types" mode="multiple" :options="typeOptions" placeholder="选择需要下发的数据类型" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="下发范围（如 school:1 / device:1001）">
              <a-input v-model:value="form.scope" placeholder="示例：school:1" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="载荷概要（备注，如条数说明等）">
              <a-input v-model:value="form.payloadSummary" placeholder="示例：下发人员100条" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-space>
          <a-button type="primary" :loading="loading" @click="onDispatch">立即下发</a-button>
          <a-button @click="loadTasks">刷新任务列表</a-button>
          <a-switch v-model:checked="autoRefresh" @change="onToggleAutoRefresh" checked-children="自动刷新" un-checked-children="手动刷新" />
        </a-space>
      </a-form>
    </a-card>

    <a-card style="margin-top: 12px" :loading="loadingTasks" title="下发任务与状态">
      <a-table :dataSource="tasks" rowKey="id" :pagination="false" :scroll="{ x: 1200 }">
        <a-table-column title="任务ID" dataIndex="id" key="id" width="180" />
        <a-table-column title="类型" key="type" width="120" :customRender="({ record }) => {
          const typeMap = {
            'PERSONS': '人员信息',
            'FACE_PHOTOS': '人脸照片',
            'FACE_FEATURES': '人脸特征',
            'CARDS': '卡片信息',
            'PERMISSIONS': '权限信息',
            'TIME_GROUPS': '时间组',
            'ATTENDANCE_RULES': '考勤规则'
          }
          return typeMap[record.type] || record.type
        }" />
        <a-table-column title="状态" key="status" width="100" :customRender="({ record }) => {
          const statusMap = {
            'PENDING': { color: 'orange', text: '等待中' },
            'RUNNING': { color: 'blue', text: '执行中' },
            'SUCCESS': { color: 'green', text: '成功' },
            'FAILED': { color: 'red', text: '失败' }
          }
          const status = statusMap[record.status] || { color: 'default', text: record.status }
          return h('a-tag', { color: status.color }, status.text)
        }" />
        <a-table-column title="进度" key="progress" width="80" :customRender="({ record }) => (record.progress ?? 0) + '%'" />
        <a-table-column title="范围" dataIndex="scope" key="scope" width="120" />
        <a-table-column title="摘要" dataIndex="payloadSummary" key="payloadSummary" width="200" />
        <a-table-column title="信息" dataIndex="message" key="message" width="150" />
        <a-table-column title="创建时间" key="createdAt" width="160" :customRender="({ record }) => record.createdAt ? new Date(record.createdAt).toLocaleString() : '—'" />
        <a-table-column title="更新时间" key="updatedAt" width="160" :customRender="({ record }) => record.updatedAt ? new Date(record.updatedAt).toLocaleString() : '—'" />
        <a-table-column title="操作" key="op" width="80" :customRender="({ record }) => h('span', {}, '—')" />
        <template #emptyText><a-empty description="暂无任务" /></template>
      </a-table>
    </a-card>
  </div>
</template>

<style scoped>
</style>