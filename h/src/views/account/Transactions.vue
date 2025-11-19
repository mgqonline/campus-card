<template>
  <div style="padding: 24px;">
    <a-page-header title="账户流水查询" />
    <a-form layout="inline" style="margin-top: 12px;">
      <a-form-item label="账户标识" required>
        <a-input v-model:value="accountId" placeholder="请输入卡号/账户ID" allow-clear style="width: 240px;" />
      </a-form-item>
      <a-form-item label="类型">
        <a-select v-model:value="type" allow-clear placeholder="全部" style="width: 160px;">
          <a-select-option value="CONSUME">消费</a-select-option>
          <a-select-option value="RECHARGE">充值</a-select-option>
          <a-select-option value="REFUND">退款</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="时间范围">
        <a-range-picker v-model:value="range" show-time />
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" :loading="loading" @click="onSearch">查询</a-button>
          <a-button @click="onReset">重置</a-button>
        </a-space>
      </a-form-item>
    </a-form>

    <a-table :data-source="records" :loading="loading" :pagination="pagination" row-key="id" style="margin-top: 16px;">
      <a-table-column title="卡号" dataIndex="cardNo" key="cardNo" />
      <a-table-column title="类型" key="type" :customRender="renderType" />
      <a-table-column title="金额" key="amount" :customRender="renderAmount" />
      <a-table-column title="余额" dataIndex="balanceAfter" key="balanceAfter" />
      <a-table-column title="商户" dataIndex="merchant" key="merchant" />
      <a-table-column title="时间" dataIndex="occurredAt" key="occurredAt" />
      <a-table-column title="备注" dataIndex="note" key="note" />
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import dayjs, { Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import { listTransactions, type TransactionRecord, type TransactionType } from '@/api/modules/card'

const accountId = ref<string>('')
const type = ref<TransactionType | undefined>(undefined)
const range = ref<[Dayjs, Dayjs] | null>(null)
const loading = ref(false)
const records = ref<TransactionRecord[]>([])

const page = ref(1)
const size = ref(10)
const total = ref(0)

const pagination = computed(() => ({
  current: page.value,
  pageSize: size.value,
  total: total.value,
  onChange: (p: number, s: number) => {
    page.value = p
    size.value = s
    onSearch()
  }
}))

function onReset() {
  accountId.value = ''
  type.value = undefined
  range.value = null
  page.value = 1
  size.value = 10
  total.value = 0
  records.value = []
}

async function onSearch() {
  if (!accountId.value?.trim()) return message.warning('请输入账户标识')
  loading.value = true
  try {
    const start = range.value ? range.value[0].toISOString() : undefined
    const end = range.value ? range.value[1].toISOString() : undefined
    const res = await listTransactions({ cardNo: accountId.value.trim(), type: type.value, start, end, page: page.value, size: size.value })
    records.value = res.records
    total.value = res.total
  } catch (e: any) {
    message.error(e?.message || '查询失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function renderType({ text }: { text: TransactionType }) {
  const map: Record<TransactionType, string> = { CONSUME: '消费', RECHARGE: '充值', REFUND: '退款' }
  return map[text] || text
}

function renderAmount({ text, record }: { text: number; record: TransactionRecord }) {
  const sign = record.type === 'CONSUME' ? '-' : '+'
  return `${sign}${Number(text || 0).toFixed(2)}`
}
</script>