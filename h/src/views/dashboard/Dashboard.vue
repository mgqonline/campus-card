<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import * as echarts from 'echarts'

import { getStudentList } from '@/api/modules/student'
import { getTeacherList } from '@/api/modules/teacher'
import { getUserList } from '@/api/modules/user'
import { getClassList } from '@/api/modules/clazz'
import { getGradeList } from '@/api/modules/grade'
import { schoolApi } from '@/api/modules/school'
import { listCards } from '@/api/modules/card'
import { getDeviceList } from '@/api/modules/attendanceDevice'
import { listDevices as listGenericDevices } from '@/api/modules/dispatch'
import { getAttendanceStatistics, getAttendanceList, getFaceSuccessRate } from '@/api/modules/attendance'
import { getDailyStats } from '@/api/modules/attendance-analytics'

const router = useRouter()
const go = (path: string) => router.push(path)

// 指标数据
const loading = ref(false)
const metrics = ref({
  studentTotal: 0,
  teacherTotal: 0,
  userTotal: 0,
  classTotal: 0,
  gradeTotal: 0,
  schoolTotal: 0,
  cardTotal: 0,
  deviceTotal: 0,
  deviceOnline: 0,
  attendanceToday: 0,
  attendanceRateToday: 0,
  lateToday: 0,
  earlyToday: 0,
  absenceToday: 0,
  faceSuccessRate: 0
})

// 最近记录
const recentRecords = ref<any[]>([])

// 趋势图
const dailyChartEl = ref<HTMLDivElement | null>(null)
let dailyChart: echarts.ECharts | null = null

const fetchMetrics = async () => {
  loading.value = true
  try {
    const today = dayjs().format('YYYY-MM-DD')
    // totals（尽量使用分页 total，后端未就绪时兜底为0）
    try { metrics.value.studentTotal = (await getStudentList({ page: 1, size: 1 })).total || 0 } catch {}
    try { metrics.value.teacherTotal = (await getTeacherList({ page: 1, size: 1 })).total || 0 } catch {}
    try { metrics.value.userTotal = (await getUserList({ page: 1, size: 1 })).total || 0 } catch {}
    try { metrics.value.classTotal = (await getClassList({ page: 1, size: 1 })).total || 0 } catch {}
    try { metrics.value.gradeTotal = (await getGradeList({ page: 1, size: 1 })).total || 0 } catch {}
    try { metrics.value.schoolTotal = (await schoolApi.getList({ page: 1, size: 1 })).total || 0 } catch {}
    try { metrics.value.cardTotal = (await listCards({ page: 1, size: 1 })).total || 0 } catch {}

    // 设备数量（两套接口兼容）
    try {
      const list = await getDeviceList({ page: 1, size: 1000 })
      const total = Array.isArray(list) ? list.length : 0
      metrics.value.deviceTotal = total
      metrics.value.deviceOnline = Array.isArray(list) ? list.filter((d: any) => d.status === 1).length : 0
    } catch {
      try {
        const page = await listGenericDevices({ page: 1, size: 100 })
        metrics.value.deviceTotal = page.total || (page.records || []).length
        metrics.value.deviceOnline = (page.records || []).filter((d: any) => d.status === 1).length
      } catch {}
    }

    // 今日考勤统计
    try {
      const stat = await getAttendanceStatistics({ startDate: today, endDate: today })
      metrics.value.attendanceToday = stat.totalCount || 0
      metrics.value.attendanceRateToday = Number(stat.attendanceRate || 0)
      metrics.value.lateToday = stat.lateCount || 0
      metrics.value.earlyToday = stat.earlyCount || 0
      metrics.value.absenceToday = stat.absenceCount || 0
    } catch {}

    // 人脸识别成功率（今日）
    try {
      const rate = await getFaceSuccessRate({ startDate: today, endDate: today })
      metrics.value.faceSuccessRate = Number(rate.successRate || 0)
    } catch {}

    // 最近考勤记录
    try {
      const page = await getAttendanceList({ page: 1, size: 10 })
      recentRecords.value = page.records || []
    } catch {}

    // 日趋势（近7天）
    try {
      const start = dayjs().subtract(6, 'day').format('YYYY-MM-DD')
      const end = today
      const daily = await getDailyStats({ startDate: start, endDate: end })
      updateDailyChart(daily)
    } catch (e: any) {
      // 兜底示例数据
      const fallback = Array.from({ length: 7 }).map((_, i) => ({ label: dayjs().subtract(6 - i, 'day').format('YYYY-MM-DD'), totalCount: 20 + i, normalCount: 15 + i, lateCount: 3, earlyCount: 2, absenceCount: 0 }))
      updateDailyChart(fallback as any)
    }
  } catch (e: any) {
    message.warning(e.message || '仪表盘部分数据加载失败，已做兼容')
  } finally {
    loading.value = false
  }
}

const updateDailyChart = (daily: { label: string; normalCount: number; lateCount: number; earlyCount: number; absenceCount: number }[]) => {
  if (dailyChartEl.value) {
    if (!dailyChart) dailyChart = echarts.init(dailyChartEl.value)
    dailyChart.setOption({
      tooltip: {},
      legend: { data: ['正常', '迟到', '早退', '缺勤'] },
      xAxis: { type: 'category', data: daily.map(d => d.label) },
      yAxis: { type: 'value' },
      series: [
        { name: '正常', type: 'line', data: daily.map(d => d.normalCount) },
        { name: '迟到', type: 'line', data: daily.map(d => d.lateCount) },
        { name: '早退', type: 'line', data: daily.map(d => d.earlyCount) },
        { name: '缺勤', type: 'line', data: daily.map(d => d.absenceCount) }
      ]
    })
  }
}

onMounted(() => {
  fetchMetrics()
})
</script>

<template>
  <div style="padding: 16px;">
    <a-page-header title="仪表盘" sub-title="关键指标与近期动态" />

    <a-row gutter="16" style="margin-top: 12px;">
      <a-col :span="6">
        <a-card :loading="loading">
          <a-statistic title="学生总数" :value="metrics.studentTotal" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :loading="loading">
          <a-statistic title="教师总数" :value="metrics.teacherTotal" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :loading="loading">
          <a-statistic title="用户总数" :value="metrics.userTotal" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :loading="loading">
          <a-statistic title="班级总数" :value="metrics.classTotal" />
        </a-card>
      </a-col>
    </a-row>

    <a-row gutter="16" style="margin-top: 12px;">
      <a-col :span="6">
        <a-card :loading="loading">
          <a-statistic title="年级总数" :value="metrics.gradeTotal" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :loading="loading">
          <a-statistic title="学校总数" :value="metrics.schoolTotal" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :loading="loading">
          <a-statistic title="卡片总数" :value="metrics.cardTotal" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :loading="loading">
          <a-statistic title="设备在线/总数" :value="metrics.deviceOnline" :suffix="` / ${metrics.deviceTotal}`" />
        </a-card>
      </a-col>
    </a-row>

    <a-row gutter="16" style="margin-top: 12px;">
      <a-col :span="6">
        <a-card :loading="loading">
          <a-statistic title="今日考勤总数" :value="metrics.attendanceToday" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :loading="loading">
          <a-statistic title="今日到校率" :value="(metrics.attendanceRateToday * 100).toFixed(1)" suffix="%" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :loading="loading">
          <a-statistic title="今日迟到" :value="metrics.lateToday" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :loading="loading">
          <a-statistic title="今日早退" :value="metrics.earlyToday" />
        </a-card>
      </a-col>
    </a-row>

    <a-row gutter="16" style="margin-top: 12px;">
      <a-col :span="6">
        <a-card :loading="loading">
          <a-statistic title="今日缺勤" :value="metrics.absenceToday" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :loading="loading">
          <a-statistic title="人脸识别成功率(今日)" :value="(metrics.faceSuccessRate * 100).toFixed(1)" suffix="%" />
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="近7日考勤趋势" :loading="loading">
          <div ref="dailyChartEl" style="height: 260px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-card title="最近考勤记录" style="margin-top: 16px;" :loading="loading">
      <a-table :dataSource="recentRecords" :pagination="false" rowKey="id">
        <a-table-column title="姓名" dataIndex="studentName" key="studentName" />
        <a-table-column title="学号" dataIndex="studentNo" key="studentNo" />
        <a-table-column title="班级" dataIndex="className" key="className" />
        <a-table-column title="设备" dataIndex="deviceName" key="deviceName" />
        <a-table-column title="时间" dataIndex="attendanceTime" key="attendanceTime" />
        <a-table-column title="类型" dataIndex="attendanceType" key="attendanceType" />
        <a-table-column title="方式" dataIndex="checkType" key="checkType" />
        <a-table-column title="状态" dataIndex="status" key="status" />
      </a-table>
    </a-card>

    <a-card title="快捷导航" style="margin-top: 16px;">
      <a-space wrap>
        <a-button type="primary" @click="go('/student/list')" v-perm="'student:list'">学生管理</a-button>
        <a-button type="primary" @click="go('/user/list')" v-perm="'user:list'">用户管理</a-button>
        <a-button type="primary" @click="go('/role/list')" v-perm="'role:list'">角色管理</a-button>
        <a-button type="primary" @click="go('/permission/list')" v-perm="'permission:list'">权限管理</a-button>
        <a-button @click="go('/attendance/stat')" v-perm="'attendance:list'">考勤统计</a-button>
        <a-button @click="go('/attendance/analytics')" v-perm="'attendance:list'">统计分析</a-button>
        <a-button @click="go('/card/list')" v-perm="'card:list'">校园卡</a-button>
        <a-button @click="go('/account/transactions')" v-perm="'account:list'">账户流水</a-button>
      </a-space>
    </a-card>
  </div>
</template>