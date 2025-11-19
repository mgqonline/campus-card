<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { getBalance, type BalanceInfo } from '@/api/modules/card'

const cardNo = ref('')
const loading = ref(false)
const info = ref<BalanceInfo | null>(null)

const formatStatus = (s?: BalanceInfo['status']) => {
  switch (s) {
    case 'ACTIVE': return '正常'
    case 'LOST': return '挂失'
    case 'FROZEN': return '冻结'
    case 'CANCELLED': return '注销'
    default: return '-'
  }
}

const onSearch = async () => {
  if (!cardNo.value) {
    message.error('请输入卡号')
    return
  }
  loading.value = true
  try {
    const res = await getBalance(cardNo.value)
    info.value = res
  } catch (e: any) {
    message.error(e?.message || '查询失败，请稍后重试')
    info.value = null
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="账户余额" />
    <a-card>
      <a-form layout="inline" @submit.prevent="onSearch">
        <a-form-item label="卡号">
          <a-input v-model:value="cardNo" style="width: 220px" placeholder="输入卡号" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" :loading="loading" @click="onSearch">查询</a-button>
        </a-form-item>
      </a-form>

      <a-spin :spinning="loading" style="margin-top: 12px;">
        <a-descriptions v-if="info" bordered :column="2">
          <a-descriptions-item label="卡号">{{ info.cardNo }}</a-descriptions-item>
          <a-descriptions-item label="持卡人">{{ info.holderName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="卡种">{{ info.cardTypeName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="状态">{{ formatStatus(info.status) }}</a-descriptions-item>
          <a-descriptions-item label="余额">{{ (info.balance ?? 0).toFixed(2) }}</a-descriptions-item>
          <a-descriptions-item label="更新时间">{{ info.updatedAt || '-' }}</a-descriptions-item>
        </a-descriptions>
      </a-spin>
    </a-card>
  </div>
</template>