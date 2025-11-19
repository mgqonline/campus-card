<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { tGetClassAttendanceStats } from '@/api/modules/teacher'
import { tExportClassAttendance } from '@/api/modules/teacher'
const route = useRoute()
const classId = Number(route.params.id)
const range = ref<'day' | 'month'>('day')
const date = ref<string>('')
const loading = ref(false)
const stats = ref<{ classId: number; range: 'day'|'month'; totalStudents: number; presentCount?: number|null; absentCount?: number|null; lateCount: number; earlyLeaveCount: number; dailyPresence: Record<string, number> } | null>(null)
const loadError = ref('')
async function load() {
  loading.value = true
  try { stats.value = await tGetClassAttendanceStats({ classId, range: range.value, date: date.value || undefined }) ; loadError.value='' } catch (e: any) { loadError.value = e?.message || '加载失败' } finally { loading.value = false }
}
function formatDate(d: Date) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}
onMounted(() => {
  // 默认日期为今天
  date.value = formatDate(new Date())
  load()
})

async function exportCsv() {
  try {
    const d = date.value ? new Date(date.value) : new Date()
    let startDate = ''
    let endDate = ''
    if (range.value === 'day') {
      const s = new Date(d); const e = new Date(d)
      startDate = s.toISOString().slice(0,10)
      endDate = e.toISOString().slice(0,10)
    } else {
      const year = d.getFullYear(); const month = d.getMonth()
      const first = new Date(year, month, 1)
      const last = new Date(year, month + 1, 0)
      startDate = first.toISOString().slice(0,10)
      endDate = last.toISOString().slice(0,10)
    }
    const blob = await tExportClassAttendance({ classId, startDate, endDate })
    const url = URL.createObjectURL(blob as any)
    const a = document.createElement('a')
    a.href = url
    a.download = `attendance_class_${classId}_${startDate}_to_${endDate}.csv`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e: any) {
    alert(e?.message || '导出失败')
  }
}
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">班级考勤统计</div>
      <div class="weui-panel__bd">
        <div class="weui-cells weui-cells_form">
          <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">统计范围</label></div>
            <div class="weui-cell__bd">
              <select class="weui-select" v-model="range" @change="load">
                <option value="day">按日</option>
                <option value="month">按月</option>
              </select>
            </div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">日期</label></div>
            <div class="weui-cell__bd"><input type="date" class="weui-input" v-model="date" @change="load" /></div>
          </div>
        </div>

        <div class="weui-cells__title">班级：{{ classId }}</div>
        <div class="weui-btn-area">
          <button class="weui-btn weui-btn_default" @click="$router.push({ name: 'ClassAttendanceRecords', params: { id: classId } })">查看班级考勤记录</button>
        </div>
        <div class="weui-btn-area"><button class="weui-btn weui-btn_default" @click="exportCsv">导出报表（CSV）</button></div>
        <div v-if="loadError" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">{{ loadError }}</span></div>
        <div v-else-if="loading" class="weui-loadmore"><i class="weui-loading"></i><span class="weui-loadmore__tips">加载中</span></div>

        <template v-else-if="stats">
          <div class="weui-cells">
            <div class="weui-cell"><div class="weui-cell__bd">学生总数</div><div class="weui-cell__ft">{{ stats!.totalStudents }}</div></div>
            <div v-if="range==='day'" class="weui-cell"><div class="weui-cell__bd">到校人数</div><div class="weui-cell__ft">{{ stats!.presentCount }}</div></div>
            <div v-if="range==='day'" class="weui-cell"><div class="weui-cell__bd">缺勤人数</div><div class="weui-cell__ft">{{ stats!.absentCount }}</div></div>
            <div class="weui-cell"><div class="weui-cell__bd">迟到次数</div><div class="weui-cell__ft">{{ stats!.lateCount }}</div></div>
            <div class="weui-cell"><div class="weui-cell__bd">早退次数</div><div class="weui-cell__ft">{{ stats!.earlyLeaveCount }}</div></div>
          </div>

          <div class="weui-cells__title">每日到校（人数）</div>
          <div class="weui-cells">
            <div v-for="(v, d) in stats!.dailyPresence" :key="d" class="weui-cell">
              <div class="weui-cell__bd">{{ d }}</div>
              <div class="weui-cell__ft">{{ v }}</div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>