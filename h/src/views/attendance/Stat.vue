<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { getAttendanceList, getAttendanceStatistics, type AttendanceRecord, type AttendanceStatistics, getFaceSuccessRate, type FaceSuccessRate } from '@/api/modules/attendance'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const dataSource = ref<AttendanceRecord[]>([])
const stats = ref<AttendanceStatistics | null>(null)
const faceRate = ref<FaceSuccessRate | null>(null)

const columns = [
  { title: '姓名', dataIndex: 'studentName', key: 'studentName' },
  { title: '学号', dataIndex: 'studentNo', key: 'studentNo' },
  { title: '班级', dataIndex: 'className', key: 'className' },
  { title: '设备', dataIndex: 'deviceName', key: 'deviceName' },
  { title: '时间', dataIndex: 'attendanceTime', key: 'attendanceTime' },
  { title: '类型', dataIndex: 'attendanceType', key: 'attendanceType' },
  { title: '方式', dataIndex: 'checkType', key: 'checkType' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

const query = reactive<{ studentName?: string; classId?: number; dateRange?: any[]; status?: string; checkType?: string }>({
  studentName: undefined,
  classId: undefined,
  dateRange: undefined,
  status: undefined,
  checkType: undefined
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const fetchStats = async () => {
  try {
    const params: any = {}
    if (query.studentName) params.studentName = query.studentName
    if (query.classId) params.classId = query.classId
    if (query.dateRange && query.dateRange.length === 2) {
      params.startDate = (query.dateRange[0] && (query.dateRange[0].toISOString ? query.dateRange[0].toISOString() : query.dateRange[0])) || undefined
      params.endDate = (query.dateRange[1] && (query.dateRange[1].toISOString ? query.dateRange[1].toISOString() : query.dateRange[1])) || undefined
    }
    if (query.status) params.status = query.status
    if (query.checkType) params.checkType = query.checkType
    stats.value = await getAttendanceStatistics(params)
    // 人脸成功率统计（按班级与日期范围筛选）
    const fp: any = {}
    if (query.classId) fp.classId = query.classId
    if (params.startDate) fp.startDate = params.startDate
    if (params.endDate) fp.endDate = params.endDate
    faceRate.value = await getFaceSuccessRate(fp)
  } catch (e) {
    stats.value = {
      totalCount: 120,
      normalCount: 110,
      lateCount: 6,
      earlyCount: 2,
      absenceCount: 2,
      attendanceRate: 110 / 120
    }
    faceRate.value = { totalAttempts: 200, successCount: 180, successRate: 0.9 }
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = { page: pagination.current, size: pagination.pageSize }
    if (query.studentName) params.studentName = query.studentName
    if (query.classId) params.classId = query.classId
    if (query.dateRange && query.dateRange.length === 2) {
      params.startDate = (query.dateRange[0] && (query.dateRange[0].toISOString ? query.dateRange[0].toISOString() : query.dateRange[0])) || undefined
      params.endDate = (query.dateRange[1] && (query.dateRange[1].toISOString ? query.dateRange[1].toISOString() : query.dateRange[1])) || undefined
    }
    if (query.status) params.status = query.status
    if (query.checkType) params.checkType = query.checkType

    const res = await getAttendanceList(params)
    dataSource.value = res.records
    pagination.total = res.total
  } catch (e) {
    dataSource.value = [
      {
        id: 1,
        studentId: 1001,
        studentName: '张三',
        studentNo: 'S2024001',
        classId: 101,
        className: '一年级一班',
        deviceId: 1,
        deviceName: '校门考勤机',
        attendanceTime: new Date().toISOString(),
        attendanceType: 'in',
        checkType: 'card',
        status: 'normal',
        remark: '按时到校'
      },
      {
        id: 2,
        studentId: 1002,
        studentName: '李四',
        studentNo: 'S2024002',
        classId: 102,
        className: '一年级二班',
        deviceId: 1,
        deviceName: '校门考勤机',
        attendanceTime: new Date(Date.now() - 3600_000).toISOString(),
        attendanceType: 'out',
        checkType: 'face',
        status: 'early',
        remark: '提前离校'
      }
    ]
    pagination.total = 2
    message.info('后端未就绪，已展示示例数据')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchStats()
  fetchData()
}
const handleReset = () => {
  query.studentName = undefined
  query.classId = undefined
  query.dateRange = undefined
  query.status = undefined
  query.checkType = undefined
  pagination.current = 1
  fetchStats()
  fetchData()
}
const handlePageChange = (page: number, pageSize?: number) => {
  pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  fetchData()
}

onMounted(() => {
  fetchStats()
  fetchData()
})
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="考勤统计">
      <template #extra>
        <a-button type="default" @click="() => router.push('/attendance/rules')">规则设置</a-button>
      </template>
    </a-page-header>

    <a-form layout="inline" style="margin-bottom: 16px;">
      <a-form-item label="姓名">
        <a-input v-model:value="query.studentName" placeholder="请输入姓名关键字" allow-clear />
      </a-form-item>
      <a-form-item label="班级ID">
        <a-input-number v-model:value="query.classId" :min="0" :precision="0" placeholder="输入班级ID" />
      </a-form-item>
      <a-form-item label="时间范围">
        <a-range-picker v-model:value="query.dateRange" show-time />
      </a-form-item>
      <a-form-item label="方式">
        <a-select v-model:value="query.checkType" allow-clear style="width: 160px">
          <a-select-option value="card">刷卡</a-select-option>
          <a-select-option value="face">人脸</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 160px">
          <a-select-option value="normal">正常</a-select-option>
          <a-select-option value="late">迟到</a-select-option>
          <a-select-option value="early">早退</a-select-option>
          <a-select-option value="absence">缺勤</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSearch">查询</a-button>
      </a-form-item>
      <a-form-item>
        <a-button @click="handleReset">重置</a-button>
      </a-form-item>
    </a-form>

    <a-row gutter="16" style="margin-bottom: 16px;">
      <a-col :span="8">
        <a-card>
          <a-statistic title="总考勤记录" :value="stats?.totalCount || 0" />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card>
          <a-statistic title="人脸识别成功率" :value="Number(((faceRate?.successRate || 0) * 100).toFixed(1))" suffix="%" />
          <div style="margin-top: 8px; color: #888;">成功 {{ faceRate?.successCount || 0 }} / 尝试 {{ faceRate?.totalAttempts || 0 }}</div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card>
          <a-statistic title="正常到校率" :value="Number(((stats?.attendanceRate || 0) * 100).toFixed(1))" suffix="%" />
        </a-card>
      </a-col>
    </a-row>

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
          <span>{{ record.checkType === 'card' ? '刷卡' : '人脸' }}</span>
        </template>
        <template v-else-if="column.key === 'status'">
          <span>
            {{ record.status === 'normal' ? '正常' : record.status === 'late' ? '迟到' : record.status === 'early' ? '早退' : '缺勤' }}
          </span>
        </template>
      </template>
    </a-table>
  </div>
</template>