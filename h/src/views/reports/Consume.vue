<template>
  <div class="page">
    <a-page-header title="消费报表" />

    <a-card title="日消费统计" class="mb16">
      <div class="row">
        <a-date-picker v-model:value="dailyDate" format="YYYY-MM-DD" @change="fetchDaily" />
        <a-button type="primary" class="ml8" @click="fetchDaily">查询</a-button>
      </div>
      <div class="mt12 stats">
        <a-statistic title="消费总额(元)" :value="(daily?.totalAmount||0).toFixed(2)" />
        <a-statistic title="交易笔数" :value="daily?.txCount||0" />
      </div>
    </a-card>

    <a-card title="月消费统计" class="mb16">
      <div class="row">
        <a-date-picker v-model:value="monthValue" picker="month" format="YYYY-MM" @change="fetchMonthly" />
        <a-button type="primary" class="ml8" @click="fetchMonthly">查询</a-button>
      </div>
      <div class="mt12 stats">
        <a-statistic title="消费总额(元)" :value="(monthly?.totalAmount||0).toFixed(2)" />
        <a-statistic title="交易笔数" :value="monthly?.txCount||0" />
      </div>
      <a-table class="mt12" :data-source="monthly?.series||[]" :columns="monthlyColumns" row-key="day" size="small" />
    </a-card>

    <a-card title="班级消费统计" class="mb16">
      <div class="row wrap">
        <a-select v-model:value="selectedClassId" style="min-width:220px" placeholder="选择班级" @focus="loadClasses">
          <a-select-option v-for="c in classes" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
        </a-select>
        <a-range-picker v-model:value="classRange" format="YYYY-MM-DD" class="ml8" />
        <a-button type="primary" class="ml8" @click="fetchClass">查询</a-button>
      </div>
      <div class="mt12 stats">
        <a-statistic title="消费总额(元)" :value="(classStat?.totalAmount||0).toFixed(2)" />
        <a-statistic title="交易笔数" :value="classStat?.txCount||0" />
        <a-statistic title="学生人数" :value="classStat?.studentCount||0" />
      </div>
      <a-table class="mt12" :data-source="classStat?.breakdown||[]" :columns="classColumns" row-key="studentId" size="small" />
    </a-card>

    <a-card title="个人消费统计" class="mb16">
      <div class="row wrap">
        <a-input v-model:value="studentNo" placeholder="学号(可选)" style="max-width:200px" />
        <a-input-number v-model:value="studentId" placeholder="学生ID(可选)" class="ml8" />
        <a-range-picker v-model:value="personalRange" format="YYYY-MM-DD" class="ml8" />
        <a-button type="primary" class="ml8" @click="fetchPersonal">查询</a-button>
      </div>
      <div class="mt12 stats">
        <a-statistic title="消费总额(元)" :value="(personal?.totalAmount||0).toFixed(2)" />
        <a-statistic title="交易笔数" :value="personal?.txCount||0" />
      </div>
      <a-table class="mt12" :data-source="personal?.series||[]" :columns="dailyColumns" row-key="day" size="small" />
    </a-card>

    <a-card title="消费排行榜" class="mb16">
      <div class="row wrap">
        <a-range-picker v-model:value="rankRange" format="YYYY-MM-DD" />
        <a-input-number v-model:value="rankLimit" :min="1" :max="100" class="ml8" />
        <a-button type="primary" class="ml8" @click="fetchRanking">查询</a-button>
      </div>
      <a-table class="mt12" :data-source="ranking" :columns="rankColumns" row-key="studentId" size="small" />
    </a-card>

    <a-card title="消费趋势分析">
      <div class="row wrap">
        <a-range-picker v-model:value="trendRange" format="YYYY-MM-DD" />
        <a-button type="primary" class="ml8" @click="fetchTrend">查询</a-button>
      </div>
      <a-table class="mt12" :data-source="trend" :columns="dailyColumns" row-key="day" size="small" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import dayjs, { Dayjs } from 'dayjs'
import { getDailyConsume, getMonthlyConsume, getClassConsume, getPersonalConsume, getConsumeRanking, getConsumeTrend, type DayStat, type MonthlyResp, type ClassConsumeResp, type PersonalResp, type RankingItem } from '@/api/modules/reports'
import { getClassList, type ClassInfo } from '@/api/modules/clazz'

const dailyDate = ref<Dayjs>(dayjs())
const monthValue = ref<Dayjs>(dayjs())
const classRange = ref<[Dayjs, Dayjs] | null>(null)
const personalRange = ref<[Dayjs, Dayjs] | null>(null)
const rankRange = ref<[Dayjs, Dayjs] | null>(null)
const trendRange = ref<[Dayjs, Dayjs] | null>(null)

const daily = ref<{ totalAmount: number; txCount: number } | null>(null)
const monthly = ref<MonthlyResp | null>(null)
const classes = ref<ClassInfo[]>([])
const selectedClassId = ref<number | null>(null)
const classStat = ref<ClassConsumeResp | null>(null)

const studentId = ref<number | null>(null)
const studentNo = ref<string>('')
const personal = ref<PersonalResp | null>(null)

const ranking = ref<RankingItem[]>([])
const rankLimit = ref<number>(20)
const trend = ref<DayStat[]>([])

const dailyColumns = [
  { title: '日期', dataIndex: 'day', key: 'day' },
  { title: '消费总额(元)', dataIndex: 'totalAmount', key: 'totalAmount', customRender: ({ text }: any) => (Number(text || 0)).toFixed(2) },
  { title: '交易笔数', dataIndex: 'txCount', key: 'txCount' }
]

const monthlyColumns = dailyColumns

const classColumns = [
  { title: '学生ID', dataIndex: 'studentId', key: 'studentId' },
  { title: '学号', dataIndex: 'studentNo', key: 'studentNo' },
  { title: '姓名', dataIndex: 'studentName', key: 'studentName' },
  { title: '消费总额(元)', dataIndex: 'totalAmount', key: 'totalAmount', customRender: ({ text }: any) => (Number(text || 0)).toFixed(2) },
  { title: '交易笔数', dataIndex: 'txCount', key: 'txCount' }
]

const rankColumns = [
  { title: '排名', key: 'rank', customRender: ({ index }: any) => index + 1 },
  { title: '学号', dataIndex: 'studentNo', key: 'studentNo' },
  { title: '姓名', dataIndex: 'studentName', key: 'studentName' },
  { title: '消费总额(元)', dataIndex: 'totalAmount', key: 'totalAmount', customRender: ({ text }: any) => (Number(text || 0)).toFixed(2) }
]

function fmt(d: Dayjs | null): string | undefined {
  return d ? d.format('YYYY-MM-DD') : undefined
}

async function loadClasses() {
  if (classes.value.length) return
  const res = await getClassList({ page: 1, size: 200 })
  classes.value = res.records || []
}

async function fetchDaily() {
  daily.value = await getDailyConsume({ date: fmt(dailyDate.value)! })
}

async function fetchMonthly() {
  const y = monthValue.value.year()
  const m = monthValue.value.month() + 1
  monthly.value = await getMonthlyConsume({ year: y, month: m })
}

async function fetchClass() {
  if (!selectedClassId.value) return
  const [start, end] = classRange.value || []
  classStat.value = await getClassConsume({ classId: selectedClassId.value!, startDate: fmt(start||null), endDate: fmt(end||null) })
}

async function fetchPersonal() {
  const [start, end] = personalRange.value || []
  personal.value = await getPersonalConsume({ studentId: studentId.value || undefined, studentNo: studentNo.value || undefined, startDate: fmt(start||null), endDate: fmt(end||null) })
}

async function fetchRanking() {
  const [start, end] = rankRange.value || []
  ranking.value = await getConsumeRanking({ startDate: fmt(start||null), endDate: fmt(end||null), limit: rankLimit.value })
}

async function fetchTrend() {
  const [start, end] = trendRange.value || []
  trend.value = await getConsumeTrend({ startDate: fmt(start||null), endDate: fmt(end||null) })
}

onMounted(() => {
  fetchDaily()
  fetchMonthly()
})
</script>

<style scoped>
.page { padding: 12px; }
.row { display: flex; align-items: center; }
.wrap { flex-wrap: wrap; gap: 8px; }
.ml8 { margin-left: 8px; }
.mt12 { margin-top: 12px; }
.mb16 { margin-bottom: 16px; }
.stats { display: grid; grid-template-columns: repeat(3, minmax(160px, 1fr)); gap: 16px; }
@media (max-width: 768px) {
  .stats { grid-template-columns: repeat(2, minmax(160px, 1fr)); }
}
</style>