<script setup lang="ts">
import { ref } from 'vue'
import { getConsumeStats, type ConsumeStats } from '@/api/modules/consume'
import { Cell, CellGroup } from 'vant'

const today = new Date()
const pad = (n: number) => (n < 10 ? `0${n}` : `${n}`)
const yyyy = today.getFullYear()
const mm = pad(today.getMonth() + 1)
const dd = pad(today.getDate())
const endDate = ref(`${yyyy}-${mm}-${dd}`)
const startDate = ref(`${yyyy}-${mm}-01`)
const stats = ref<ConsumeStats | null>(null)
const loading = ref(false)
const error = ref('')

function currentChildId(): number | undefined {
  const cid = localStorage.getItem('wx_child_id')
  return cid ? Number(cid) : undefined
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    stats.value = await getConsumeStats({ childId: currentChildId(), startDate: startDate.value || undefined, endDate: endDate.value || undefined })
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page">
    <h3 class="section-title"><van-icon name="bar-chart-o" style="margin-right:6px;" />消费统计分析</h3>

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
      <div class="weui-btn-area">
        <button type="button" class="weui-btn weui-btn_primary" @click="load">
          <van-icon name="chart-trending-o" style="margin-right:4px;" />统计
        </button>
      </div>
    </div>

    <!-- 加载/错误提示 -->
    <div v-if="loading" class="weui-loadmore" style="margin-top:12px;">
      <i class="weui-loading"></i>
      <span class="weui-loadmore__tips">统计中</span>
    </div>
    <div v-else-if="error" class="weui-toptips weui-toptips_warn" style="display: block; margin-top: 12px;">
      <i class="fa fa-exclamation-triangle"></i> {{ error }}
    </div>

    <!-- 统计结果（WeUI 风格卡片） -->
    <div v-else-if="stats" class="card" style="margin-top:12px;">
      <div class="metric-row">
        <div class="metric">
          <div class="metric-label">总额</div>
          <div class="metric-value">￥{{ stats.totalAmount.toFixed(2) }}</div>
        </div>
        <div class="metric">
          <div class="metric-label">笔数</div>
          <div class="metric-value">{{ stats.txCount }}</div>
        </div>
        <div class="metric">
          <div class="metric-label">日均</div>
          <div class="metric-value">￥{{ stats.avgPerDay.toFixed(2) }}</div>
        </div>
      </div>
      <div class="weui-cells__tips" style="margin-top:8px;">区间：{{ startDate }} 至 {{ endDate }}</div>
    </div>
  </div>
</template>

<style scoped>
.metric-row { display: flex; gap: 12px; }
.metric { flex: 1; background: #fff; border-radius: 8px; padding: 12px; box-shadow: 0 1px 2px rgba(0,0,0,0.04); }
.metric-label { color: #888; font-size: 13px; margin-bottom: 4px; }
.metric-value { font-size: 18px; font-weight: 600; color: #111; }
</style>