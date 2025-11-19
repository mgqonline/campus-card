<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getRechargeRecords, type RechargeRecord } from '@/api/modules/recharge'

const records = ref<RechargeRecord[]>([])
const loading = ref(false)
const error = ref('')

function currentChildId(): number | undefined {
  const cid = localStorage.getItem('wx_child_id')
  return cid ? Number(cid) : undefined
}

function statusLabel(s: string) {
  const t = (s || '').toUpperCase()
  if (t.includes('SUCCESS') || t.includes('PAID')) return '已支付'
  if (t.includes('PENDING') || t.includes('PROCESS')) return '处理中'
  if (t.includes('FAIL') || t.includes('CLOSE') || t.includes('REFUND')) return '异常/退款'
  return s || '未知'
}
function statusClass(s: string) {
  const t = (s || '').toUpperCase()
  if (t.includes('SUCCESS') || t.includes('PAID')) return 'ok'
  if (t.includes('PENDING') || t.includes('PROCESS')) return 'pending'
  if (t.includes('FAIL') || t.includes('CLOSE') || t.includes('REFUND')) return 'fail'
  return 'pending'
}
function formatTime(t: string) {
  if (!t) return ''
  // 简单展示到分钟
  return t.replace('T', ' ').slice(0, 16)
}

async function load() {
  loading.value = true
  try {
    records.value = await getRechargeRecords({ childId: currentChildId(), page: 1, size: 20 })
    error.value = ''
  } catch (e: any) {
    error.value = e?.message || '获取记录失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">充值记录</div>
      <div class="weui-panel__bd">
        <!-- 顶部错误提示条 -->
        <div v-if="error" class="weui-toptips weui-toptips_warn" style="display: block; margin-bottom: 12px;">
          <i class="fa fa-exclamation-triangle"></i> {{ error }}
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="weui-loadmore" style="margin: 12px 0;">
          <i class="weui-loading"></i>
          <span class="weui-loadmore__tips">加载中</span>
        </div>

        <!-- 空状态 -->
        <div v-else-if="!records || records.length === 0" class="weui-loadmore weui-loadmore_line" style="margin: 12px 0;">
          <span class="weui-loadmore__tips">暂无记录</span>
        </div>

        <!-- 记录列表（WeUI cells） -->
        <div v-else class="weui-cells weui-cells_access">
          <div class="weui-cells__title">最近记录</div>
          <a v-for="r in records" :key="r.orderId" href="javascript:;" class="weui-cell weui-cell_access">
            <div class="weui-cell__bd">
              <p class="row-line">
                <span class="order">订单号：{{ r.orderId }}</span>
                <span class="amount">￥{{ r.amount }}</span>
              </p>
              <p class="meta">
                <span class="weui-badge" :class="statusClass(r.status)">{{ statusLabel(r.status) }}</span>
                <span class="time">{{ formatTime(r.time) }}</span>
              </p>
            </div>
            <div class="weui-cell__ft"></div>
          </a>
        </div>

        <!-- 操作区：刷新与去充值 -->
        <div class="weui-form__opr-area" style="padding:12px;">
          <a href="javascript:;" class="weui-btn weui-btn_default" @click="load"><i class="fa fa-refresh" style="margin-right:4px;"></i>刷新</a>
          <router-link to="/recharge" class="weui-btn weui-btn_primary" style="margin-left:8px;"><i class="fa fa-credit-card" style="margin-right:4px;"></i>去充值</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.row-line { display: flex; align-items: center; justify-content: space-between; }
.order { color: #333; font-weight: 600; }
.amount { color: #333; font-weight: 600; }
.meta { margin-top: 4px; color: #666; font-size: 13px; display:flex; align-items:center; gap:8px; }
.weui-badge { display:inline-block; min-width: 34px; text-align:center; padding: 2px 6px; border-radius: 12px; }
.weui-badge.ok { background-color: #1aad19; color: #fff; }
.weui-badge.pending { background-color: #ffe58f; color: #8b6100; }
.weui-badge.fail { background-color: #f44336; color: #fff; }
.time { color: #888; }
</style>