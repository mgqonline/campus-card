<template>
  <div style="padding: 24px;">
    <a-page-header title="账户冻结/解冻（挂失/解挂）" />
    <a-form layout="vertical" style="margin-top: 12px; max-width: 560px;">
      <a-form-item label="账户标识" required>
        <a-input v-model:value="accountId" placeholder="请输入卡号/账户ID" allow-clear />
      </a-form-item>
      <a-form-item label="当前状态">
        <a-tag :color="statusColor">{{ statusText }}</a-tag>
        <a-button type="link" @click="fetchStatus" :loading="loading">刷新</a-button>
      </a-form-item>
      <a-form-item label="操作">
        <a-switch v-model:checked="frozen" checked-children="挂失" un-checked-children="解挂" />
      </a-form-item>
      <a-form-item label="原因">
        <a-input v-model:value="reason" placeholder="可选，填写挂失原因" />
      </a-form-item>
      <a-space>
        <a-button type="primary" :loading="loading" @click="onSubmit">提交</a-button>
        <a-button @click="onReset">重置</a-button>
      </a-space>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { getBalance, reportLoss, unloss } from '@/api/modules/card'

const accountId = ref<string>('')
const loading = ref(false)
const reason = ref('')
const status = ref<'ACTIVE' | 'LOST' | 'FROZEN' | 'CANCELLED' | undefined>(undefined)
const frozen = ref(false)

const statusText = computed(() => {
  const map: any = { ACTIVE: '正常', LOST: '挂失', FROZEN: '冻结', CANCELLED: '已注销' }
  return status.value ? map[status.value] : '未知'
})
const statusColor = computed(() => {
  const map: any = { ACTIVE: 'green', LOST: 'red', FROZEN: 'orange', CANCELLED: 'grey' }
  return status.value ? map[status.value] : 'default'
})

function onReset() {
  accountId.value = ''
  reason.value = ''
  status.value = undefined
  frozen.value = false
}

async function fetchStatus() {
  if (!accountId.value?.trim()) return message.warning('请输入账户标识')
  loading.value = true
  try {
    const info = await getBalance(accountId.value.trim())
    status.value = info.status
    frozen.value = status.value === 'LOST' || status.value === 'FROZEN'
  } catch (e: any) {
    message.error(e?.message || '获取状态失败')
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  if (!accountId.value?.trim()) return message.warning('请输入账户标识')
  loading.value = true
  try {
    if (frozen.value) {
      const res = await reportLoss({ cardNo: accountId.value.trim(), reason: reason.value.trim() || undefined })
      if (!res.success) throw new Error('挂失失败')
      message.success('挂失成功')
    } else {
      const res = await unloss({ cardNo: accountId.value.trim() })
      if (!res.success) throw new Error('解挂失败')
      message.success('解挂成功')
    }
    await fetchStatus()
  } catch (e: any) {
    message.error(e?.message || '操作失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>