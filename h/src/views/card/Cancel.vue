<template>
  <div class="page-card">
    <h2>退卡注销</h2>

    <section class="form-section">
      <div class="form-row">
        <label for="cardNo">卡号</label>
        <input id="cardNo" v-model="cardNo" placeholder="请输入卡号" />
        <button class="secondary" @click="fetchBalance" :disabled="loading || !cardNo">查询当前余额</button>
      </div>

      <div class="form-row">
        <label>
          <input type="checkbox" v-model="refundAll" /> 退回剩余余额
        </label>
      </div>

      <div v-if="refundAll" class="form-row">
        <label for="method">退款方式</label>
        <select id="method" v-model="method">
          <option value="CASH">现金</option>
          <option value="ONLINE">线上</option>
        </select>
      </div>

      <div class="form-row">
        <label for="note">备注</label>
        <input id="note" v-model="note" placeholder="可选填写备注或原因" />
      </div>

      <div class="form-actions">
        <button class="danger" @click="onCancel" :disabled="loading || !cardNo">注销</button>
      </div>
    </section>

    <section v-if="balanceInfo" class="balance-section">
      <h3>账户信息</h3>
      <div class="balance-grid">
        <div><strong>卡号：</strong>{{ balanceInfo.cardNo }}</div>
        <div><strong>持卡人：</strong>{{ balanceInfo.holderName || '-' }}</div>
        <div><strong>卡种：</strong>{{ balanceInfo.cardTypeName || '-' }}</div>
        <div><strong>状态：</strong>{{ balanceInfo.status || '-' }}</div>
        <div><strong>余额：</strong>{{ balanceInfo.balance.toFixed(2) }}</div>
        <div><strong>更新时间：</strong>{{ formatTime(balanceInfo.updatedAt) }}</div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { cancelCard, getBalance, type BalanceInfo, type RechargeMethod } from '@/api/modules/card'

const loading = ref(false)
const cardNo = ref('')
const refundAll = ref(true)
const method = ref<RechargeMethod>('CASH')
const note = ref('')
const balanceInfo = ref<BalanceInfo | null>(null)

function formatTime(t?: string) {
  return t ? new Date(t).toLocaleString() : '-'
}

async function fetchBalance() {
  if (!cardNo.value) return
  loading.value = true
  try {
    balanceInfo.value = await getBalance(cardNo.value)
  } catch (e) {
    // 兜底在API层已处理
  } finally {
    loading.value = false
  }
}

async function onCancel() {
  if (!cardNo.value) return alert('请先填写卡号')
  loading.value = true
  try {
    const ok = await cancelCard({ cardNo: cardNo.value, refundAll: refundAll.value, method: refundAll.value ? method.value : undefined, note: note.value })
    if (ok?.success) {
      alert('注销成功')
      await fetchBalance()
    } else {
      alert('注销失败')
    }
  } catch (e) {
    alert('注销异常，请稍后再试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page-card { padding: 20px; }
.form-section { margin-top: 12px; }
.form-row { display: flex; gap: 12px; align-items: center; margin-bottom: 12px; }
.form-row label { width: 120px; color: #444; }
.form-row input, .form-row select { flex: 1; padding: 6px 8px; }
.form-actions { display: flex; gap: 12px; margin-top: 8px; }
.danger { background: #ef4444; color: #fff; border: none; padding: 8px 12px; cursor: pointer; }
.secondary { background: #f3f4f6; color: #111827; border: 1px solid #d1d5db; padding: 8px 12px; cursor: pointer; }
.balance-section { margin-top: 18px; }
.balance-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; padding: 10px; border: 1px solid #e5e7eb; border-radius: 6px; }
</style>