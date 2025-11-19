<template>
  <div style="padding: 24px;">
    <a-page-header title="账户对账管理" />

    <a-form layout="inline" style="margin-top: 12px;">
      <a-form-item label="时间范围">
        <a-range-picker v-model:value="dateRange" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" :loading="loading" @click="onReconcile">开始对账</a-button>
      </a-form-item>
    </a-form>

    <div style="margin-top: 16px;" v-if="reconciled">
      <a-alert type="success" message="对账完成（模拟）" show-icon />
      <a-table :data-source="diffs" row-key="id" style="margin-top: 12px;">
        <a-table-column title="ID" dataIndex="id" key="id" />
        <a-table-column title="类型" dataIndex="type" key="type" />
        <a-table-column title="平台金额" dataIndex="platformAmount" key="platformAmount" />
        <a-table-column title="银行金额" dataIndex="bankAmount" key="bankAmount" />
        <a-table-column title="差异" key="diff">
          <template #default="{ record }">
            <span :style="{ color: Number(record.diff) === 0 ? '#52c41a' : '#f5222d' }">{{ Number(record.diff).toFixed(2) }}</span>
          </template>
        </a-table-column>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'

const dateRange = ref<[string, string] | undefined>(undefined)
const loading = ref(false)
const reconciled = ref(false)
const diffs = ref<any[]>([])

async function onReconcile() {
  if (!dateRange.value) return message.warning('请选择时间范围')
  loading.value = true
  try {
    // TODO: 接入后端API：执行对账，并返回差异数据
    // const res = await accountApi.reconcile({ dateRange: dateRange.value })
    // diffs.value = res.diffs

    // 模拟数据
    diffs.value = [
      { id: 1, type: '线上充值', platformAmount: 100.0, bankAmount: 100.0, diff: 0.0 },
      { id: 2, type: '食堂消费', platformAmount: -12.5, bankAmount: -12.5, diff: 0.0 }
    ]
    reconciled.value = true
  } catch (e) {
    message.error('对账失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>