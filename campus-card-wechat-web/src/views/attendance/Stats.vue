<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getStats, type StatsResp } from '@/api/modules/attendance'
import { showToast } from 'vant'

const stats = ref<StatsResp | null>(null)

const fetchStats = async () => {
  try {
    stats.value = await getStats({ range: 'month' })
  } catch (e: any) {
    showToast(e?.message || '获取统计失败')
  }
}

onMounted(fetchStats)
</script>

<template>
  <div class="page">
    <h3 class="section-title">考勤统计（月度）</h3>
    <div class="card" v-if="stats">
      <div class="grid">
        <div class="cell"><div class="label">应到天数</div><div class="val">{{ stats!.totalDays }}</div></div>
        <div class="cell"><div class="label">实到天数</div><div class="val">{{ stats!.presentDays }}</div></div>
        <div class="cell"><div class="label">迟到次数</div><div class="val warn">{{ stats!.lateCount }}</div></div>
        <div class="cell"><div class="label">早退次数</div><div class="val warn">{{ stats!.earlyLeaveCount }}</div></div>
        <div class="cell"><div class="label">缺勤天数</div><div class="val danger">{{ stats!.absentDays }}</div></div>
      </div>
      <h4 class="section-title" style="margin-top:12px;">近7日到校概览</h4>
      <div class="daily">
        <div class="day" v-for="(v, d) in stats!.dailyPresence" :key="d">
          <div class="date">{{ d }}</div>
          <div class="present" :class="{on: v === 1}">{{ v === 1 ? '到校' : '未到' }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.grid { display:grid; grid-template-columns: repeat(2,1fr); gap:12px; }
.cell { background:#fff; border:1px solid #eee; border-radius:12px; padding:12px; }
.label { color:#666; font-size:13px; }
.val { font-size:20px; font-weight:600; margin-top:4px; }
.val.warn { color:#f0ad4e; }
.val.danger { color:#d9534f; }
.daily { display:grid; grid-template-columns: repeat(1,1fr); gap:8px; }
.day { display:flex; align-items:center; justify-content:space-between; padding:8px 12px; border:1px dashed #eee; border-radius:12px; background:#fff; }
.date { color:#666; font-size:13px; }
.present { font-size:13px; color:#999; }
.present.on { color:#2bb24c; font-weight:600; }
</style>