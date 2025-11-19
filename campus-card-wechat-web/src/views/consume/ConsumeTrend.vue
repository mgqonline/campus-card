<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getConsumeTrend, type TrendPoint } from '@/api/modules/consume'

const startDate = ref('')
const endDate = ref('')
const series = ref<TrendPoint[]>([])
const loading = ref(false)
const error = ref('')

function currentChildId(): number | undefined {
  const cid = localStorage.getItem('wx_child_id')
  return cid ? Number(cid) : undefined
}

function pad(n: number) { return n < 10 ? `0${n}` : `${n}` }
function fmtDate(d: Date) { return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` }
function setQuickRange(days: number) {
  const today = new Date()
  const start = new Date(today)
  start.setDate(today.getDate() - (days - 1))
  startDate.value = fmtDate(start)
  endDate.value = fmtDate(today)
}

function barHeight(val: number) {
  const max = Math.max(...(series.value.map(s => s.value)), 1)
  const h = Math.round((val / max) * 80)
  return `${Math.max(h, 2)}px`
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    series.value = await getConsumeTrend({ childId: currentChildId(), startDate: startDate.value || undefined, endDate: endDate.value || undefined })
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!startDate.value || !endDate.value) {
    setQuickRange(30)
  }
  load()
})
</script>

<template>
  <div class="page">
    <h3 class="section-title"><van-icon name="chart-trending-o" style="margin-right:6px;" />消费趋势</h3>

    <!-- 查询条件（WeUI 风格） -->
    <div class="weui-cells weui-cells_form card">
      <div class="weui-cell">
        <div class="weui-cell__hd"><label class="weui-label">开始日期</label></div>
        <div class="weui-cell__bd"><input class="weui-input" v-model="startDate" type="date" /></div>
      </div>
      <div class="weui-cell">
        <div class="weui-cell__hd"><label class="weui-label">结束日期</label></div>
        <div class="weui-cell__bd"><input class="weui-input" v-model="endDate" type="date" /></div>
      </div>
      <div class="weui-btn-area inline">
        <button type="button" class="weui-btn weui-btn_mini" @click="setQuickRange(7)">近7天</button>
        <button type="button" class="weui-btn weui-btn_mini" @click="setQuickRange(30)">近30天</button>
        <button type="button" class="weui-btn weui-btn_primary" style="margin-left:auto;" @click="load">
          <van-icon name="search" style="margin-right:4px;" />查询
        </button>
      </div>
    </div>

    <!-- 加载/错误提示 -->
    <div v-if="loading" class="weui-loadmore" style="margin-top:12px;">
      <i class="weui-loading"></i>
      <span class="weui-loadmore__tips">加载中</span>
    </div>
    <div v-else-if="error" class="weui-toptips weui-toptips_warn" style="display: block; margin-top: 12px;">
      <i class="fa fa-exclamation-triangle"></i> {{ error }}
    </div>

    <!-- 趋势图（WeUI 风格卡片 + 简易柱状图） -->
    <div v-else class="card" style="margin-top:12px;">
      <div class="weui-panel weui-panel_access">
        <div class="weui-panel__hd">趋势图</div>
        <div class="weui-panel__bd">
          <div v-if="!series.length" class="weui-loadmore">
            <span class="weui-loadmore__tips">暂无数据，调整时间范围后重试</span>
          </div>
          <div v-else class="chart-wrap">
            <div class="chart-bars">
              <div v-for="p in series" :key="p.date" class="bar" :title="`${p.date}｜￥${(p.value || 0).toFixed(2)}`" :style="{ height: barHeight(p.value) }"></div>
            </div>
            <div class="chart-legend weui-cells__tips">
              <span>区间：{{ startDate }} 至 {{ endDate }}</span>
              <span style="margin-left:auto;">共 {{ series.length }} 天</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 数据表（补充详情） -->
      <div class="weui-cells" style="margin-top:8px;">
        <div class="weui-cell weui-cell_active" v-for="p in series" :key="p.date">
          <div class="weui-cell__bd">
            <div class="row-line">
              <strong>{{ p.date }}</strong>
              <span class="amount">￥{{ (p.value || 0).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chart-wrap { padding: 8px 4px 4px; }
.chart-bars { display: flex; align-items: flex-end; gap: 4px; height: 88px; }
.bar { width: 8px; background: #07c160; border-radius: 2px 2px 0 0; }
.chart-legend { display: flex; align-items: center; justify-content: space-between; margin-top: 6px; }
.row-line { display: flex; align-items: center; justify-content: space-between; }
.amount { color: #333; }
.weui-btn-area.inline { display: flex; gap: 8px; align-items: center; }
</style>