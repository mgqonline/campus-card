<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { getBalance, recharge, type BalanceInfo, type RechargeReq, RechargeMethod } from '@/api/modules/card'

const cardNo = ref('')
const amount = ref<number | null>(null)
const method = ref<RechargeMethod>('CASH')
const note = ref('')

const loading = ref(false)
const balanceInfo = ref<BalanceInfo | null>(null)

const formatStatus = (s?: BalanceInfo['status']) => {
  switch (s) {
    case 'ACTIVE': return '正常'
    case 'LOST': return '挂失'
    case 'FROZEN': return '冻结'
    case 'CANCELLED': return '注销'
    default: return '-'
  }
}

const fetchBalance = async () => {
  if (!cardNo.value) {
    message.error('请输入卡号')
    return
  }
  loading.value = true
  try {
    const res = await getBalance(cardNo.value)
    balanceInfo.value = res
  } catch (e) {
    balanceInfo.value = { cardNo: cardNo.value, balance: 0, status: 'ACTIVE', holderName: '-', cardTypeName: '-', updatedAt: '' }
  } finally {
    loading.value = false
  }
}

const onRecharge = async () => {
  if (!cardNo.value) {
    message.error('请输入卡号')
    return
  }
  if (!amount.value || amount.value <= 0) {
    message.error('请输入正确的充值金额')
    return
  }
  loading.value = true
  try {
    const payload: RechargeReq = { cardNo: cardNo.value, amount: amount.value, method: method.value, note: note.value }
    const res = await recharge(payload)
    if (res?.success !== false) {
      message.success('充值成功')
      await fetchBalance()
    } else {
      message.error('充值失败')
    }
  } catch (e) {
    message.error('后端未就绪，模拟充值成功')
    await fetchBalance()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="校园卡充值" />
    <a-card>
      <a-form layout="inline" @submit.prevent="fetchBalance">
        <a-form-item label="卡号">
          <a-input v-model:value="cardNo" style="width: 220px" placeholder="输入卡号" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" :loading="loading" @click="fetchBalance">查询余额</a-button>
        </a-form-item>
      </a-form>

      <a-spin :spinning="loading">
        <div v-if="balanceInfo" style="margin-top: 16px;">
          <a-descriptions bordered :column="2">
            <a-descriptions-item label="卡号">{{ balanceInfo.cardNo }}</a-descriptions-item>
            <a-descriptions-item label="持卡人">{{ balanceInfo.holderName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="卡种">{{ balanceInfo.cardTypeName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="状态">{{ formatStatus(balanceInfo.status) }}</a-descriptions-item>
            <a-descriptions-item label="余额">{{ (balanceInfo.balance ?? 0).toFixed(2) }}</a-descriptions-item>
            <a-descriptions-item label="更新时间">{{ balanceInfo.updatedAt || '-' }}</a-descriptions-item>
          </a-descriptions>

          <a-divider />

          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 14 }" @submit.prevent="onRecharge">
            <a-form-item label="充值金额">
              <a-input-number v-model:value="amount" :min="0" :precision="2" style="width: 200px" />
            </a-form-item>
            <a-form-item label="方式">
              <a-select v-model:value="method" style="width: 200px">
                <a-select-option value="CASH">现金</a-select-option>
                <a-select-option value="WECHAT">微信</a-select-option>
                <a-select-option value="ALIPAY">支付宝</a-select-option>
                <a-select-option value="BANK">银行卡</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="备注">
              <a-input v-model:value="note" placeholder="可选" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" :loading="loading" @click="onRecharge">确认充值</a-button>
            </a-form-item>
          </a-form>
        </div>
      </a-spin>
    </a-card>
  </div>
</template>