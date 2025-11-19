<template>
  <div style="padding: 24px;">
    <a-page-header title="账户余额查询" />
    <a-form layout="inline" style="margin-top: 12px;">
      <a-form-item label="账户标识">
        <a-input v-model:value="accountId" placeholder="请输入账户ID/卡号/学号" allow-clear style="width: 280px;" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" :loading="loading" @click="onQuery">查询</a-button>
      </a-form-item>
    </a-form>

    <div v-if="queried" style="margin-top: 16px;">
      <a-card>
        <a-row :gutter="16">
          <a-col :span="8">
            <div>账户名称：{{ accountName || '-' }}</div>
          </a-col>
          <a-col :span="8">
            <div>当前余额：<span style="font-weight: 700; color: #1677ff;">{{ balanceDisplay }}</span></div>
          </a-col>
          <a-col :span="8">
            <div>状态：{{ statusText }}</div>
          </a-col>
        </a-row>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'

const accountId = ref<string>('')
const loading = ref(false)
const queried = ref(false)
const accountName = ref<string>('')
const balance = ref<number>(0)
const status = ref<number>(1) // 1正常 0冻结

const balanceDisplay = computed(() => `${(balance.value ?? 0).toFixed(2)} 元`)
const statusText = computed(() => (status.value === 1 ? '正常' : '冻结'))

async function onQuery() {
  if (!accountId.value?.trim()) {
    message.warning('请输入账户标识')
    return
  }
  loading.value = true
  try {
    // TODO: 接入后端API：根据账户标识查询余额与状态
    // const res = await accountApi.getBalance({ accountId: accountId.value })
    // accountName.value = res.name
    // balance.value = res.balance
    // status.value = res.status

    // 模拟数据
    accountName.value = '张三'
    balance.value = 123.45
    status.value = 1
    queried.value = true
  } catch (e) {
    message.error('查询失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>