<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getConsumeCalendar, type DailyStat } from '@/api/modules/consume'

const month = ref('')
const list = ref<DailyStat[]>([])
const loading = ref(false)
const error = ref('')

function currentChildId(): number | undefined {
  const cid = localStorage.getItem('wx_child_id')
  return cid ? Number(cid) : undefined
}

function pad(n: number) { return n < 10 ? `0${n}` : `${n}` }
function currentMonthStr() {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}`
}
function lastMonthStr() {
  const d = new Date()
  d.setMonth(d.getMonth() - 1)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}`
}
function setCurrentMonth() { month.value = currentMonthStr() }
function setLastMonth() { month.value = lastMonthStr() }

async function load() {
  if (!month.value) return
  loading.value = true
  error.value = ''
  try {
    list.value = await getConsumeCalendar({ childId: currentChildId(), month: month.value })
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!month.value) setCurrentMonth()
  load()
})

// 月份汇总指标
const totalAmountSum = computed(() => list.value.reduce((s, d) => s + (d.totalAmount || 0), 0))
const txCountSum = computed(() => list.value.reduce((s, d) => s + (d.txCount || 0), 0))
const avgPerDay = computed(() => list.value.length ? totalAmountSum.value / list.value.length : 0)
</script>

<template>
  <div class="page">
    <h3 class="section-title"><van-icon name="calendar-o" style="margin-right:6px;" />消费日历</h3>

    <!-- 查询条件（WeUI 风格） -->
    <div class="weui-cells weui-cells_form card">
      <div class="weui-cell">
        <div class="weui-cell__hd"><label class="weui-label">月份</label></div>
        <div class="weui-cell__bd"><input class="weui-input" v-model="month" type="month" placeholder="YYYY-MM" /></div>
      </div>
      <div class="weui-btn-area inline">
        <button type="button" class="weui-btn weui-btn_mini" @click="setCurrentMonth">本月</button>
        <button type="button" class="weui-btn weui-btn_mini" @click="setLastMonth">上月</button>
        <button type="button" class="weui-btn weui-btn_primary" style="margin-left:auto;" @click="load">
          <van-icon name="search" style="margin-right:4px;" />加载
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

    <!-- 日历数据（WeUI 面板与列表） -->
    <div v-else class="card" style="margin-top:12px;">
      <div class="weui-panel weui-panel_access">
        <div class="weui-panel__hd">月份概览</div>
        <div class="weui-panel__bd">
          <!-- 月份汇总指标 -->
          <div class="metric-row">
            <div class="metric">
              <div class="metric-label">总额</div>
              <div class="metric-value">￥{{ totalAmountSum.toFixed(2) }}</div>
            </div>
            <div class="metric">
              <div class="metric-label">笔数</div>
              <div class="metric-value">{{ txCountSum }}</div>
            </div>
            <div class="metric">
              <div class="metric-label">日均</div>
              <div class="metric-value">￥{{ avgPerDay.toFixed(2) }}</div>
            </div>
          </div>

          <div v-if="!list.length" class="weui-loadmore">
            <span class="weui-loadmore__tips">暂无数据，试试切换月份</span>
          </div>
          <div v-else class="weui-cells">
            <div class="weui-cell" v-for="d in list" :key="d.date">
              <div class="weui-cell__bd">
                <div class="row-line">
                  <strong>{{ d.date }}</strong>
                  <span class="amount">￥{{ (d.totalAmount || 0).toFixed(2) }}</span>
                </div>
                <div class="weui-cells__tips">笔数：{{ d.txCount }}</div>
              </div>
              <div class="weui-cell__ft"></div>
            </div>
          </div>
        </div>
        <div class="weui-panel__ft weui-cells__tips">月份：{{ month }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.row-line { display: flex; align-items: center; justify-content: space-between; }
.amount { color: #111; font-weight: 600; }
.weui-btn-area.inline { display: flex; gap: 8px; align-items: center; }
.metric-row { display: flex; gap: 12px; margin-bottom: 8px; }
.metric { flex: 1; background: #fff; border-radius: 8px; padding: 12px; box-shadow: 0 1px 2px rgba(0,0,0,0.04); }
.metric-label { color: #888; font-size: 13px; margin-bottom: 4px; }
.metric-value { font-size: 18px; font-weight: 600; color: #111; }
</style>