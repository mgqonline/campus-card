<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getRechargeOptions, createRechargeOrder, type RechargeOptions } from '@/api/modules/recharge'

const options = ref<RechargeOptions | null>(null)
const amount = ref<number | null>(null)
const creating = ref(false)
const resultMsg = ref('')
const error = ref('')

function currentChildId(): number | undefined {
  const cid = localStorage.getItem('wx_child_id')
  return cid ? Number(cid) : undefined
}

async function loadOptions() {
  try {
    options.value = await getRechargeOptions()
  } catch (e: any) {
    error.value = e?.message || '获取充值选项失败'
  }
}

async function doCreate() {
  if (!amount.value || amount.value <= 0) {
    error.value = '请输入或选择有效金额'
    return
  }
  creating.value = true
  try {
    const resp = await createRechargeOrder({ childId: currentChildId() || 2001, amount: amount.value })
    resultMsg.value = `订单已创建：${resp.orderId} 金额：${resp.amount}`
    error.value = ''
  } catch (e: any) {
    error.value = e?.message || '创建订单失败'
  } finally {
    creating.value = false
  }
}

onMounted(loadOptions)
</script>
<template>
  <div class="page">
    <h3 class="section-title"><van-icon name="cash-o" style="margin-right:6px;" />校园卡充值</h3>

    <!-- 错误提示采用 WeUI 顶部提示条 -->
    <div v-if="error" class="weui-toptips weui-toptips_warn" style="display: block; margin-bottom: 12px;">
      <i class="fa fa-exclamation-triangle"></i> {{ error }}
    </div>

    <div class="card">
      <div class="weui-cells__title">快速选择</div>
      <div class="weui-btn-area inline">
        <button
          v-for="s in options?.suggested || []"
          :key="s"
          type="button"
          class="weui-btn weui-btn_mini"
          :class="{ 'weui-btn_primary': amount === s, 'weui-btn_default': amount !== s }"
          @click="amount = s"
        >{{ s }} 元</button>
      </div>

      <div class="weui-cells weui-cells_form" style="margin-top: 12px;">
        <div class="weui-cell">
          <div class="weui-cell__hd"><label class="weui-label">自定义金额</label></div>
          <div class="weui-cell__bd"><input class="weui-input" type="number" v-model.number="amount" placeholder="输入金额" /></div>
        </div>
        <div class="weui-btn-area">
          <button type="button" class="weui-btn weui-btn_primary" :disabled="creating" @click="doCreate">
            <van-icon name="cash-o" style="margin-right:4px;" />{{ creating ? '创建中...' : '立即充值' }}
          </button>
        </div>
      </div>

      <div v-if="resultMsg" class="weui-cells__tips" style="margin-top: 4px;">{{ resultMsg }}</div>
    </div>
  </div>
</template>

<style scoped>
.weui-btn-area.inline { display: flex; flex-wrap: wrap; gap: 8px; }
.weui-btn.weui-btn_mini { min-width: 72px; }
</style>