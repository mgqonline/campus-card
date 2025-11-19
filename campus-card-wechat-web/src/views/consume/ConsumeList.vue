<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getConsumeList, type ConsumeItem } from '@/api/modules/consume'
import { getRechargeOptions, createRechargeOrder, type RechargeOptions } from '@/api/modules/recharge'
import { Tab, Tabs } from 'vant'

const activeTab = ref(0)
const list = ref<ConsumeItem[]>([])
const loading = ref(false)
const error = ref('')

// 充值相关状态
const rechargeOptions = ref<RechargeOptions | null>(null)
const rechargeAmount = ref<number | null>(null)
const rechargeCreating = ref(false)
const rechargeResultMsg = ref('')
const rechargeError = ref('')

const today = new Date()
const pad = (n: number) => (n < 10 ? `0${n}` : `${n}`)
const yyyy = today.getFullYear()
const mm = pad(today.getMonth() + 1)
const dd = pad(today.getDate())
const endDate = ref(`${yyyy}-${mm}-${dd}`)
const startDate = ref(`${yyyy}-${mm}-01`)

function currentChildId(): number | undefined {
  const cid = localStorage.getItem('wx_child_id')
  return cid ? Number(cid) : undefined
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    list.value = await getConsumeList({ childId: currentChildId(), startDate: startDate.value, endDate: endDate.value })
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function loadRechargeOptions() {
  try {
    rechargeOptions.value = await getRechargeOptions()
  } catch (e: any) {
    rechargeError.value = e?.message || '获取充值选项失败'
  }
}

async function doRecharge() {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    rechargeError.value = '请输入或选择有效金额'
    return
  }
  rechargeCreating.value = true
  try {
    const resp = await createRechargeOrder({ childId: currentChildId() || 2001, amount: rechargeAmount.value })
    rechargeResultMsg.value = `订单已创建：${resp.orderId} 金额：${resp.amount}`
    rechargeError.value = ''
  } catch (e: any) {
    rechargeError.value = e?.message || '创建订单失败'
  } finally {
    rechargeCreating.value = false
  }
}

onMounted(() => {
  load()
  loadRechargeOptions()
})
</script>

<template>
  <div class="page">
    <h3 class="section-title"><van-icon name="credit-card-o" style="margin-right:6px;" />消费与充值</h3>

    <van-tabs v-model:active="activeTab" class="card">
      <!-- 消费记录 Tab -->
      <van-tab title="消费记录" name="consume">
        <div class="tab-content">
          <div class="weui-cells__title">查询条件</div>
          <div class="weui-cells weui-cells_form">
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

          <!-- 列表（WeUI 风格） -->
          <div v-else class="weui-cells weui-cells_access" style="margin-top:12px;">
            <a v-for="item in list" :key="item.txId" href="javascript:;" class="weui-cell weui-cell_access">
              <div class="weui-cell__bd">
                <div class="row-line">
                  <strong>{{ item.merchant }}</strong>
                  <span class="amount">￥{{ (item.amount || 0).toFixed(2) }}</span>
                </div>
                <p class="weui-cells__tips">{{ item.date }}｜{{ item.detail }}</p>
              </div>
              <div class="weui-cell__ft"></div>
            </a>
          </div>
        </div>
      </van-tab>

      <!-- 在线充值 Tab -->
      <van-tab title="在线充值" name="recharge">
        <div class="tab-content">
          <div v-if="rechargeError" class="weui-toptips weui-toptips_warn" style="display: block; margin-bottom: 12px;">
            <i class="fa fa-exclamation-triangle"></i> {{ rechargeError }}
          </div>

          <div class="weui-cells__title">快速选择</div>
          <div class="weui-btn-area inline">
            <button
              v-for="s in rechargeOptions?.suggested || []"
              :key="s"
              type="button"
              class="weui-btn weui-btn_mini"
              :class="{ 'weui-btn_primary': rechargeAmount === s, 'weui-btn_default': rechargeAmount !== s }"
              @click="rechargeAmount = s"
            >{{ s }} 元</button>
          </div>

          <div class="weui-cells weui-cells_form">
            <div class="weui-cell">
              <div class="weui-cell__hd"><label class="weui-label">自定义金额</label></div>
              <div class="weui-cell__bd"><input class="weui-input" type="number" v-model.number="rechargeAmount" placeholder="输入金额" /></div>
            </div>
            <div class="weui-btn-area">
              <button type="button" class="weui-btn weui-btn_primary" :disabled="rechargeCreating" @click="doRecharge">
                <van-icon name="cash-o" style="margin-right:4px;" />{{ rechargeCreating ? '创建中...' : '立即充值' }}
              </button>
            </div>
          </div>

          <div v-if="rechargeResultMsg" class="weui-cells__tips" style="margin-top: 4px;">{{ rechargeResultMsg }}</div>
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<style scoped>
.tab-content { padding: 16px; }
.row-line { display: flex; align-items: center; justify-content: space-between; }
.amount { color: #333; }
.weui-btn-area.inline { display: flex; flex-wrap: wrap; gap: 8px; }
.weui-btn.weui-btn_mini { min-width: 72px; }
</style>
