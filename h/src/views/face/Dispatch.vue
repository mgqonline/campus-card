<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { message } from 'ant-design-vue'
import {
  listDevices,
  listDispatchTasks,
  retryDispatchTask,
  batchDispatch,
  incrementalDispatch,
  deleteDispatch,
  listDeviceFaces,
  addDeviceFaces,
  removeDeviceFace,
  syncDeviceFaces,
  type Device,
  type DeviceFace,
  type PersonType,
  type FaceDispatchTask
} from '@/api/modules/dispatch'

const devices = ref<Device[]>([])
const loadingDevices = ref(false)
const selectedDeviceIds = ref<number[]>([])

const tasks = ref<FaceDispatchTask[]>([])
const loadingTasks = ref(false)

const faces = ref<DeviceFace[]>([])
const loadingFaces = ref(false)
const currentDeviceId = ref<number>()

const batchForm = reactive<{ personType?: PersonType; personId?: string }>({})
const incrForm = reactive<{ since?: string }>({})
const delForm = reactive<{ personIds: string[] }>({ personIds: [] })

const addFaceForm = reactive<{ personType: PersonType | undefined; personId: string }>({ personType: undefined, personId: '' })
const addingFace = ref(false)

async function loadDevices() {
  loadingDevices.value = true
  try {
    const res = await listDevices({ page: 1, size: 200 })
    devices.value = res.records || []
  } finally {
    loadingDevices.value = false
  }
}

async function loadTasks() {
  loadingTasks.value = true
  try {
    tasks.value = await listDispatchTasks()
  } finally {
    loadingTasks.value = false
  }
}

async function loadFaces(devId: number) {
  currentDeviceId.value = devId
  loadingFaces.value = true
  try {
    faces.value = await listDeviceFaces(devId)
  } finally {
    loadingFaces.value = false
  }
}

async function onBatch() {
  if (!selectedDeviceIds.value.length) return message.warning('请选择设备')
  const task = await batchDispatch({ deviceIds: selectedDeviceIds.value, personType: batchForm.personType, personId: batchForm.personId })
  message.success(`批量下发任务创建成功 #${task.id}`)
  loadTasks()
}

async function onIncremental() {
  if (!selectedDeviceIds.value.length) return message.warning('请选择设备')
  const task = await incrementalDispatch({ deviceIds: selectedDeviceIds.value, since: incrForm.since })
  message.success(`增量下发任务创建成功 #${task.id}`)
  loadTasks()
}

async function onDelete() {
  if (!selectedDeviceIds.value.length) return message.warning('请选择设备')
  if (!delForm.personIds.length) return message.warning('请输入需删除的人员ID')
  const task = await deleteDispatch({ deviceIds: selectedDeviceIds.value, personIds: delForm.personIds })
  message.success(`删除下发任务创建成功 #${task.id}`)
  loadTasks()
}

async function onRetry(taskId: number) {
  await retryDispatchTask(taskId)
  message.success('已触发失败重试')
  loadTasks()
}

async function onAddFace() {
  if (!currentDeviceId.value) return message.warning('请选择设备')
  if (!addFaceForm.personType || !addFaceForm.personId) return message.warning('请完整填写类型与人员ID')
  addingFace.value = true
  try {
    await addDeviceFaces(currentDeviceId.value, [{ personType: addFaceForm.personType, personId: addFaceForm.personId }])
    message.success('已添加到设备人脸库')
    addFaceForm.personId = ''
    await loadFaces(currentDeviceId.value)
  } finally {
    addingFace.value = false
  }
}

async function onRemoveFace(pid: string) {
  if (!currentDeviceId.value) return
  await removeDeviceFace(currentDeviceId.value, pid)
  message.success('已移除')
  loadFaces(currentDeviceId.value)
}

async function onSyncFaces() {
  if (!currentDeviceId.value) return
  await syncDeviceFaces(currentDeviceId.value)
  message.success('已发起同步')
}

onMounted(() => {
  loadDevices()
  loadTasks()
})
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="人脸数据下发" sub-title="批量/增量/删除、状态监控与失败重试" />

    <a-card style="margin-top: 12px" :loading="loadingDevices" title="选择设备">
      <a-select v-model:value="selectedDeviceIds" mode="multiple" style="width: 100%" allow-clear placeholder="请选择下发目标设备">
        <a-select-option v-for="d in devices" :key="d.id" :value="d.id">{{ d.name }}（{{ d.code }}）</a-select-option>
      </a-select>
    </a-card>

    <a-card style="margin-top: 12px" title="下发操作">
      <a-space direction="vertical" style="width: 100%">
        <div>
          <span style="margin-right:8px">批量下发：</span>
          <a-select v-model:value="batchForm.personType" allow-clear placeholder="人员类型" style="width: 160px">
            <a-select-option value="STUDENT">学生</a-select-option>
            <a-select-option value="TEACHER">教师</a-select-option>
            <a-select-option value="STAFF">职工</a-select-option>
            <a-select-option value="VISITOR">访客</a-select-option>
          </a-select>
          <a-input v-model:value="batchForm.personId" allow-clear placeholder="指定人员ID（可选）" style="width: 240px; margin: 0 8px" />
          <a-button type="primary" @click="onBatch">发起批量</a-button>
        </div>
        <div>
          <span style="margin-right:8px">增量下发：</span>
          <a-date-picker v-model:value="incrForm.since" show-time placeholder="起始时间（可选）" style="width: 240px; margin-right:8px" />
          <a-button @click="onIncremental">发起增量</a-button>
        </div>
        <div>
          <span style="margin-right:8px">删除下发：</span>
          <a-select v-model:value="delForm.personIds" mode="tags" placeholder="输入需删除的人员ID" style="min-width: 420px" />
          <a-button danger style="margin-left:8px" @click="onDelete">发起删除</a-button>
        </div>
      </a-space>
    </a-card>

    <a-card style="margin-top: 12px" :loading="loadingTasks" title="下发任务与状态">
      <a-table :dataSource="tasks" rowKey="id" :pagination="false">
        <a-table-column title="任务ID" dataIndex="id" key="id" />
        <a-table-column title="类型" dataIndex="taskType" key="taskType" />
        <a-table-column title="状态" dataIndex="status" key="status" />
        <a-table-column title="成功/失败" key="succFail" :customRender="({ record }) => `${record.successItems ?? 0}/${record.failedItems ?? 0}`" />
        <a-table-column title="操作" key="op" :customRender="({ record }) => h('a-button', { size: 'small', onClick: () => onRetry(record.id) }, '失败重试')" />
        <template #emptyText><a-empty description="暂无任务" /></template>
      </a-table>
    </a-card>

    <a-card style="margin-top: 12px" title="设备人脸库管理">
      <a-space>
        <a-select v-model:value="currentDeviceId" placeholder="请选择设备" style="width: 260px" @change="loadFaces">
          <a-select-option v-for="d in devices" :key="d.id" :value="d.id">{{ d.name }}（{{ d.code }}）</a-select-option>
        </a-select>
        <a-button @click="currentDeviceId && loadFaces(currentDeviceId)">刷新列表</a-button>
        <a-button type="dashed" @click="onSyncFaces">同步系统人脸到设备</a-button>
      </a-space>
      <div style="margin-top: 12px">
        <a-space>
          <a-select v-model:value="addFaceForm.personType" placeholder="人员类型" style="width: 160px">
            <a-select-option value="STUDENT">学生</a-select-option>
            <a-select-option value="TEACHER">教师</a-select-option>
            <a-select-option value="STAFF">职工</a-select-option>
            <a-select-option value="VISITOR">访客</a-select-option>
          </a-select>
          <a-input v-model:value="addFaceForm.personId" placeholder="人员ID" style="width: 220px" />
          <a-button type="primary" :loading="addingFace" @click="onAddFace">添加到设备</a-button>
        </a-space>
      </div>
      <a-table :dataSource="faces" :loading="loadingFaces" rowKey="personId" :pagination="false" style="margin-top: 12px">
        <a-table-column title="人员类型" dataIndex="personType" key="personType" />
        <a-table-column title="人员ID" dataIndex="personId" key="personId" />
        <a-table-column title="下发时间" dataIndex="dispatchedAt" key="dispatchedAt" />
        <a-table-column title="操作" key="op" :customRender="({ record }) => h('a-button', { danger: true, size: 'small', onClick: () => onRemoveFace(record.personId) }, '移除')" />
        <template #emptyText><a-empty description="暂无人脸库数据" /></template>
      </a-table>
    </a-card>
  </div>
</template>