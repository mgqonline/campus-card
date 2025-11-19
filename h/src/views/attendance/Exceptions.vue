<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import {
  getAttendanceList,
  type AttendanceRecord,
  type AttendanceQuery,
  markAttendanceException,
  correctAttendanceRecord,
  supplementAttendanceRecord
} from '@/api/modules/attendance'

const loading = ref(false)
const dataSource = ref<AttendanceRecord[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const query = reactive<Partial<AttendanceQuery>>({ page: 1, size: 10 })

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = { page: pagination.current, size: pagination.pageSize }
    if (query.studentName) params.studentName = query.studentName
    if (query.classId) params.classId = query.classId
    if (query.status) params.status = query.status
    if (query.checkType) params.checkType = query.checkType
    const res = await getAttendanceList(params)
    dataSource.value = res.records
    pagination.total = res.total
  } catch (e) {
    message.info('后端未就绪，展示示例数据')
    dataSource.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchData())

const handlePageChange = (page: number, pageSize?: number) => {
  pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  fetchData()
}

// 异常标记
const markModal = ref<{ visible: boolean; id?: number; status?: 'normal'|'late'|'early'|'absence'; reason?: string }>({ visible: false })
const openMark = (rec: AttendanceRecord) => { markModal.value = { visible: true, id: rec.id, status: rec.status, reason: '' } }
const submitMark = async () => {
  if (!markModal.value.id) return (markModal.value.visible = false)
  try {
    await markAttendanceException(markModal.value.id!, { status: markModal.value.status, reason: markModal.value.reason })
    message.success('已标记异常')
    markModal.value.visible = false
    fetchData()
  } catch (e) { message.error('标记失败') }
}

// 修正
const correctModal = ref<{ visible: boolean; id?: number; attendanceTime?: string; attendanceType?: 'in'|'out'; status?: 'normal'|'late'|'early'|'absence'; reason?: string }>({ visible: false })
const openCorrect = (rec: AttendanceRecord) => { correctModal.value = { visible: true, id: rec.id, attendanceTime: rec.attendanceTime, attendanceType: rec.attendanceType, status: rec.status, reason: '' } }
const submitCorrect = async () => {
  if (!correctModal.value.id) return (correctModal.value.visible = false)
  try {
    const ts = (correctModal.value.attendanceTime as any)
    const attendanceTime = ts && (ts.toISOString ? ts.toISOString() : ts)
    await correctAttendanceRecord(correctModal.value.id!, {
      attendanceTime,
      attendanceType: correctModal.value.attendanceType,
      status: correctModal.value.status,
      reason: correctModal.value.reason
    })
    message.success('已修正')
    correctModal.value.visible = false
    fetchData()
  } catch (e) { message.error('修正失败') }
}

// 补录
const supplementModal = ref<{ visible: boolean; studentNo?: string; attendanceTime?: string; attendanceType?: 'in'|'out'; checkType?: 'manual'|'card'|'face'; deviceId?: number; remark?: string }>({ visible: false })
const openSupplement = () => { supplementModal.value = { visible: true, attendanceType: 'in', checkType: 'manual', remark: '' } }
const submitSupplement = async () => {
  try {
    const ts = (supplementModal.value.attendanceTime as any)
    const attendanceTime = ts && (ts.toISOString ? ts.toISOString() : ts)
    await supplementAttendanceRecord({
      studentNo: supplementModal.value.studentNo,
      attendanceTime,
      attendanceType: supplementModal.value.attendanceType,
      checkType: supplementModal.value.checkType,
      deviceId: supplementModal.value.deviceId,
      remark: supplementModal.value.remark
    })
    message.success('已补录')
    supplementModal.value.visible = false
    fetchData()
  } catch (e) { message.error('补录失败') }
}
const columns = [
  { title: '姓名', dataIndex: 'studentName', key: 'studentName' },
  { title: '学号', dataIndex: 'studentNo', key: 'studentNo' },
  { title: '班级', dataIndex: 'className', key: 'className' },
  { title: '时间', dataIndex: 'attendanceTime', key: 'attendanceTime' },
  { title: '类型', dataIndex: 'attendanceType', key: 'attendanceType' },
  { title: '方式', dataIndex: 'checkType', key: 'checkType' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action' }
]
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="考勤异常处理" sub-title="支持异常标记、补录、修正、记录原因" />

    <div style="margin-bottom: 12px;">
      <a-space>
        <a-button type="primary" @click="openSupplement">补录记录</a-button>
        <a-button @click="fetchData">刷新</a-button>
      </a-space>
    </div>

    <a-table
      :dataSource="dataSource"
      :columns="columns as any"
      rowKey="id"
      :loading="loading"
      :pagination="{ current: pagination.current, pageSize: pagination.pageSize, total: pagination.total, showSizeChanger: true }"
      @change="(p:any) => handlePageChange(p.current, p.pageSize)"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'attendanceType'">
          <span>{{ record.attendanceType === 'in' ? '进校' : '离校' }}</span>
        </template>
        <template v-else-if="column.key === 'checkType'">
          <span>{{ record.checkType === 'card' ? '刷卡' : (record.checkType === 'face' ? '人脸' : '手工') }}</span>
        </template>
        <template v-else-if="column.key === 'status'">
          <span>
            {{ record.status === 'normal' ? '正常' : record.status === 'late' ? '迟到' : record.status === 'early' ? '早退' : '缺勤' }}
          </span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="() => openMark(record)">异常标记</a-button>
            <a-button size="small" @click="() => openCorrect(record)">修正</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="markModal.visible" title="异常标记" @ok="submitMark" @cancel="() => markModal.visible = false">
      <a-form layout="vertical">
        <a-form-item label="状态">
          <a-select v-model:value="markModal.status" style="width: 200px">
            <a-select-option value="normal">正常</a-select-option>
            <a-select-option value="late">迟到</a-select-option>
            <a-select-option value="early">早退</a-select-option>
            <a-select-option value="absence">缺勤</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="异常原因">
          <a-input v-model:value="markModal.reason" placeholder="填写原因说明" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="correctModal.visible" title="修正记录" @ok="submitCorrect" @cancel="() => correctModal.visible = false">
      <a-form layout="vertical">
        <a-form-item label="时间">
          <a-date-picker v-model:value="(correctModal as any).attendanceTime" show-time />
        </a-form-item>
        <a-form-item label="类型">
          <a-select v-model:value="correctModal.attendanceType" style="width: 200px">
            <a-select-option value="in">进校</a-select-option>
            <a-select-option value="out">离校</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="correctModal.status" style="width: 200px">
            <a-select-option value="normal">正常</a-select-option>
            <a-select-option value="late">迟到</a-select-option>
            <a-select-option value="early">早退</a-select-option>
            <a-select-option value="absence">缺勤</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="修正原因">
          <a-input v-model:value="correctModal.reason" placeholder="填写原因说明" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="supplementModal.visible" title="补录考勤" @ok="submitSupplement" @cancel="() => supplementModal.visible = false">
      <a-form layout="vertical">
        <a-form-item label="学号">
          <a-input v-model:value="supplementModal.studentNo" placeholder="输入学号" />
        </a-form-item>
        <a-form-item label="时间">
          <a-date-picker v-model:value="(supplementModal as any).attendanceTime" show-time />
        </a-form-item>
        <a-form-item label="类型">
          <a-select v-model:value="supplementModal.attendanceType" style="width: 200px">
            <a-select-option value="in">进校</a-select-option>
            <a-select-option value="out">离校</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="方式">
          <a-select v-model:value="supplementModal.checkType" style="width: 200px">
            <a-select-option value="manual">手工</a-select-option>
            <a-select-option value="card">刷卡</a-select-option>
            <a-select-option value="face">人脸</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="设备ID">
          <a-input-number v-model:value="supplementModal.deviceId" :min="0" :precision="0" style="width: 200px" />
        </a-form-item>
        <a-form-item label="备注/原因">
          <a-input v-model:value="supplementModal.remark" placeholder="备注或原因说明" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>