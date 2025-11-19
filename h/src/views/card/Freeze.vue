<template>
  <div class="page-card">
    <h2>账户冻结 / 解冻</h2>

    <section class="form-section">
      <div class="form-row">
        <label for="cardNo">卡号</label>
        <input id="cardNo" v-model="cardNo" placeholder="请输入卡号" />
        <button class="secondary" @click="fetchBalance" :disabled="loading || !cardNo">查询当前余额</button>
      </div>

      <div class="form-row">
        <label for="reason">原因</label>
        <input id="reason" v-model="reason" placeholder="可选填写冻结原因" />
      </div>

      <div class="form-actions">
        <button class="danger" @click="onFreeze" :disabled="loading || !cardNo">冻结</button>
        <button class="primary" @click="onUnfreeze" :disabled="loading || !cardNo">解冻</button>
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
import { freeze, unfreeze, getBalance, type BalanceInfo } from '@/api/modules/card'

const loading = ref(false)
const cardNo = ref('')
const reason = ref('')
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

async function onFreeze() {
  if (!cardNo.value) return alert('请先填写卡号')
  loading.value = true
  try {
    const ok = await freeze({ cardNo: cardNo.value, reason: reason.value })
    if (ok?.success) {
      alert('冻结成功')
      await fetchBalance()
    } else {
      alert('冻结失败')
    }
  } catch (e) {
    alert('冻结异常，请稍后再试')
  } finally {
    loading.value = false
  }
}

async function onUnfreeze() {
  if (!cardNo.value) return alert('请先填写卡号')
  loading.value = true
  try {
    const ok = await unfreeze({ cardNo: cardNo.value })
    if (ok?.success) {
      alert('解冻成功')
      await fetchBalance()
    } else {
      alert('解冻失败')
    }
  } catch (e) {
    alert('解冻异常，请稍后再试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page-card { padding: 20px; }
.form-section { margin-top: 12px; }
.form-row { display: flex; gap: 12px; align-items: center; margin-bottom: 12px; }
.form-row label { width: 88px; color: #444; }
.form-row input { flex: 1; padding: 6px 8px; }
.form-actions { display: flex; gap: 12px; margin-top: 8px; }
.primary { background: #3b82f6; color: #fff; border: none; padding: 8px 12px; cursor: pointer; }
.secondary { background: #f3f4f6; color: #111827; border: 1px solid #d1d5db; padding: 8px 12px; cursor: pointer; }
.danger { background: #ef4444; color: #fff; border: none; padding: 8px 12px; cursor: pointer; }
.balance-section { margin-top: 18px; }
.balance-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; padding: 10px; border: 1px solid #e5e7eb; border-radius: 6px; }
</style>