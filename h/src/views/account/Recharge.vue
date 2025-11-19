<template>
  <div style="padding: 24px;">
    <a-page-header title="账户充值（现金/线上）" />
    <a-form layout="vertical" style="margin-top: 12px; max-width: 560px;">
      <a-form-item label="账户标识" required>
        <a-input v-model:value="accountId" placeholder="请输入账户ID/卡号/学号" allow-clear />
      </a-form-item>
      <a-form-item label="充值方式" required>
        <a-select v-model:value="method" placeholder="请选择充值方式">
          <a-select-option value="cash">现金</a-select-option>
          <a-select-option value="online">线上</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="充值金额" required>
        <a-input-number v-model:value="amount" :min="0" :precision="2" style="width: 100%" placeholder="请输入金额" />
      </a-form-item>
      <a-form-item label="备注">
        <a-input v-model:value="remark" placeholder="可选，填写备注" />
      </a-form-item>
      <a-space>
        <a-button type="primary" :loading="loading" @click="onSubmit">提交充值</a-button>
        <a-button @click="onReset">重置</a-button>
      </a-space>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { recharge, type RechargeMethod } from '@/api/modules/card'

const accountId = ref<string>('')
const method = ref<'cash' | 'online' | undefined>(undefined)
const amount = ref<number | null>(null)
const remark = ref<string>('')
const loading = ref(false)

function onReset() {
  accountId.value = ''
  method.value = undefined
  amount.value = null
  remark.value = ''
}

async function onSubmit() {
  if (!accountId.value?.trim()) return message.warning('请输入账户标识')
  if (!method.value) return message.warning('请选择充值方式')
  if (!amount.value || amount.value <= 0) return message.warning('请输入有效的充值金额')
  loading.value = true
  try {
    const m = (method.value.toUpperCase() as RechargeMethod)
    const res = await recharge({ cardNo: accountId.value.trim(), amount: Number(amount.value), method: m, note: remark.value.trim() })
    if (res?.success) {
      message.success('充值成功')
      onReset()
    } else {
      throw new Error('充值失败')
    }
  } catch (e: any) {
    message.error(e?.message || '充值失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>