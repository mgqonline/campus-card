<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
dayjs.locale('zh-cn')
import * as echarts from 'echarts'
import { message } from 'ant-design-vue'
import {
  getDailyStats,
  getMonthlyStats,
  getTermStats,
  getClassStats,
  getPersonalStats,
  getLateEarlyStats,
  getAbsenceStats,
  exportCsv,
  type StatsPoint,
  type ClassStatsPoint,
  type PersonalStatsPoint
} from '@/api/modules/attendance-analytics'

const loading = ref(false)
const query = reactive<{ classId?: number; studentId?: number }>({
  classId: undefined,
  studentId: undefined
})

// 使用 Dayjs 对象进行受控绑定，避免 value 为字符串时的 locale 报错
const uiStartDate = ref(dayjs().subtract(30, 'day'))
const uiEndDate = ref(dayjs())

const daily = ref<StatsPoint[]>([])
const monthly = ref<StatsPoint[]>([])
const term = ref<StatsPoint[]>([])
const classStats = ref<ClassStatsPoint[]>([])
const personalStats = ref<PersonalStatsPoint[]>([])
const lateEarly = ref<StatsPoint | null>(null)
const absence = ref<StatsPoint | null>(null)

const dailyChartEl = ref<HTMLDivElement | null>(null)
const monthlyChartEl = ref<HTMLDivElement | null>(null)
const classChartEl = ref<HTMLDivElement | null>(null)
let dailyChart: echarts.ECharts | null = null
let monthlyChart: echarts.ECharts | null = null
let classChart: echarts.ECharts | null = null

const fetchAll = async () => {
  loading.value = true
  try {
    const startDate = uiStartDate.value?.format('YYYY-MM-DD')
    const endDate = uiEndDate.value?.format('YYYY-MM-DD')
    const params = { startDate, endDate, classId: query.classId }
    daily.value = await getDailyStats(params)
    monthly.value = await getMonthlyStats(params)
    term.value = await getTermStats(params)
    classStats.value = await getClassStats({ startDate, endDate })
    personalStats.value = await getPersonalStats(params)
    lateEarly.value = await getLateEarlyStats({ startDate, endDate, classId: query.classId, studentId: query.studentId })
    absence.value = await getAbsenceStats({ startDate, endDate, classId: query.classId, studentId: query.studentId })
    updateCharts()
  } catch (e: any) {
    message.warning(e.message || '统计接口暂不可用，已加载示例数据')
    // 示例数据
    daily.value = Array.from({ length: 7 }).map((_, i) => ({ label: dayjs().subtract(6 - i, 'day').format('YYYY-MM-DD'), totalCount: 20 + i, normalCount: 15 + i, lateCount: 3, earlyCount: 2, absenceCount: 0, attendanceRate: 0.9 }))
    monthly.value = [{ label: dayjs().format('YYYY-MM'), totalCount: 300, normalCount: 250, lateCount: 30, earlyCount: 20, absenceCount: 0, attendanceRate: 0.967 }]
    term.value = [{ label: 'TERM', totalCount: 600, normalCount: 500, lateCount: 60, earlyCount: 40, absenceCount: 0, attendanceRate: 0.966 }]
    classStats.value = [{ label: '101', classId: 101, className: '高一(1)班', totalCount: 120, normalCount: 100, lateCount: 10, earlyCount: 10, absenceCount: 0, attendanceRate: 0.917 }]
    personalStats.value = [{ label: '1001', studentId: 1001, studentName: '张三', totalCount: 60, normalCount: 50, lateCount: 5, earlyCount: 5, absenceCount: 0, attendanceRate: 0.917 }]
    lateEarly.value = { label: 'LATE_EARLY', totalCount: 600, normalCount: 500, lateCount: 60, earlyCount: 40, absenceCount: 0, attendanceRate: 0.966 }
    absence.value = { label: 'ABSENCE', totalCount: 600, normalCount: 500, lateCount: 60, earlyCount: 40, absenceCount: 0, attendanceRate: 0.966 }
    updateCharts()
  } finally {
    loading.value = false
  }
}

const updateCharts = () => {
  if (dailyChartEl.value) {
    if (!dailyChart) dailyChart = echarts.init(dailyChartEl.value)
    dailyChart.setOption({
      tooltip: {},
      xAxis: { type: 'category', data: daily.value.map((d) => d.label) },
      yAxis: { type: 'value' },
      legend: { data: ['正常', '迟到', '早退', '缺勤'] },
      series: [
        { name: '正常', type: 'line', data: daily.value.map((d) => d.normalCount) },
        { name: '迟到', type: 'line', data: daily.value.map((d) => d.lateCount) },
        { name: '早退', type: 'line', data: daily.value.map((d) => d.earlyCount) },
        { name: '缺勤', type: 'line', data: daily.value.map((d) => d.absenceCount) }
      ]
    })
  }
  if (monthlyChartEl.value) {
    if (!monthlyChart) monthlyChart = echarts.init(monthlyChartEl.value)
    monthlyChart.setOption({
      tooltip: {},
      xAxis: { type: 'category', data: monthly.value.map((m) => m.label) },
      yAxis: { type: 'value' },
      legend: { data: ['正常', '迟到', '早退', '缺勤'] },
      series: [
        { name: '正常', type: 'bar', data: monthly.value.map((m) => m.normalCount) },
        { name: '迟到', type: 'bar', data: monthly.value.map((m) => m.lateCount) },
        { name: '早退', type: 'bar', data: monthly.value.map((m) => m.earlyCount) },
        { name: '缺勤', type: 'bar', data: monthly.value.map((m) => m.absenceCount) }
      ]
    })
  }
  // 班级统计 Top10 柱状图
  if (classChartEl.value) {
    if (!classChart) classChart = echarts.init(classChartEl.value)
    const top = [...classStats.value]
      .sort((a, b) => (b.totalCount || 0) - (a.totalCount || 0))
      .slice(0, 10)
    const labels = top.map((c) => c.className || String(c.classId))
    classChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['正常', '迟到', '早退', '缺勤'] },
      xAxis: { type: 'category', data: labels },
      yAxis: { type: 'value' },
      series: [
        { name: '正常', type: 'bar', stack: 'total', data: top.map((c) => c.normalCount) },
        { name: '迟到', type: 'bar', stack: 'total', data: top.map((c) => c.lateCount) },
        { name: '早退', type: 'bar', stack: 'total', data: top.map((c) => c.earlyCount) },
        { name: '缺勤', type: 'bar', stack: 'total', data: top.map((c) => c.absenceCount) }
      ]
    })
  }
}

const handleExport = async (type: 'records' | 'daily' | 'monthly' | 'class' | 'personal') => {
  const startDate = uiStartDate.value?.format('YYYY-MM-DD')
  const endDate = uiEndDate.value?.format('YYYY-MM-DD')
  const csv = await exportCsv(type, { startDate, endDate, classId: query.classId, studentId: query.studentId })
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `attendance-${type}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(() => {
  fetchAll()
})
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="考勤统计分析" sub-title="按日/月/学期/班级/个人维度聚合，并支持导出报表" />

    <a-form layout="inline" style="margin-bottom: 16px;">
      <a-form-item label="开始日期">
        <a-date-picker v-model:value="uiStartDate" />
      </a-form-item>
      <a-form-item label="结束日期">
        <a-date-picker v-model:value="uiEndDate" />
      </a-form-item>
      <a-form-item label="班级ID">
        <a-input-number v-model:value="query.classId" :min="0" :precision="0" placeholder="可选" />
      </a-form-item>
      <a-form-item label="学生ID">
        <a-input-number v-model:value="query.studentId" :min="0" :precision="0" placeholder="可选" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" :loading="loading" @click="fetchAll">查询</a-button>
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button @click="() => handleExport('records')">导出记录CSV</a-button>
          <a-button @click="() => handleExport('daily')">导出日统计CSV</a-button>
          <a-button @click="() => handleExport('monthly')">导出月统计CSV</a-button>
          <a-button @click="() => handleExport('class')">导出班级统计CSV</a-button>
          <a-button @click="() => handleExport('personal')">导出个人统计CSV</a-button>
        </a-space>
      </a-form-item>
    </a-form>

    <a-row gutter="16">
      <a-col :span="12">
        <a-card title="日统计趋势">
          <div ref="dailyChartEl" style="height: 320px;"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="月度统计柱状图">
          <div ref="monthlyChartEl" style="height: 320px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-card title="班级考勤统计排行（Top10）" style="margin-top: 16px;">
      <div ref="classChartEl" style="height: 320px;"></div>
    </a-card>

    <a-row gutter="16" style="margin-top: 16px;">
      <a-col :span="12">
        <a-card title="班级维度统计">
          <a-table :dataSource="classStats" :pagination="false" rowKey="classId">
            <a-table-column title="班级ID" dataIndex="classId" key="classId" />
            <a-table-column title="班级名称" dataIndex="className" key="className" />
            <a-table-column title="总数" dataIndex="totalCount" key="totalCount" :sorter="(a: any,b: any) => (a.totalCount||0) - (b.totalCount||0)" />
            <a-table-column title="正常" dataIndex="normalCount" key="normalCount" :sorter="(a: any,b: any) => (a.normalCount||0) - (b.normalCount||0)" />
            <a-table-column title="迟到" dataIndex="lateCount" key="lateCount" :sorter="(a: any,b: any) => (a.lateCount||0) - (b.lateCount||0)" />
            <a-table-column title="早退" dataIndex="earlyCount" key="earlyCount" :sorter="(a: any,b: any) => (a.earlyCount||0) - (b.earlyCount||0)" />
            <a-table-column title="缺勤" dataIndex="absenceCount" key="absenceCount" :sorter="(a: any,b: any) => (a.absenceCount||0) - (b.absenceCount||0)" />
            <a-table-column title="到校率" key="attendanceRate" :sorter="(a: any,b: any) => (Number(a.attendanceRate||0) - Number(b.attendanceRate||0))" :customRender="({ record }) => (Number(record.attendanceRate || 0) * 100).toFixed(1) + '%'" />
          </a-table>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="个人维度统计">
          <a-table :dataSource="personalStats" :pagination="false" rowKey="studentId">
            <a-table-column title="学生ID" dataIndex="studentId" key="studentId" />
            <a-table-column title="姓名" dataIndex="studentName" key="studentName" />
            <a-table-column title="总数" dataIndex="totalCount" key="totalCount" />
            <a-table-column title="正常" dataIndex="normalCount" key="normalCount" />
            <a-table-column title="迟到" dataIndex="lateCount" key="lateCount" />
            <a-table-column title="早退" dataIndex="earlyCount" key="earlyCount" />
            <a-table-column title="缺勤" dataIndex="absenceCount" key="absenceCount" />
            <a-table-column title="到校率" key="attendanceRate" :customRender="({ record }) => (Number(record.attendanceRate || 0) * 100).toFixed(1) + '%'" />
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>