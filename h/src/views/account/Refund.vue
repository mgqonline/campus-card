<template>
  <div style="padding: 24px;">
    <a-page-header title="账户退款" />
    <a-form layout="vertical" style="margin-top: 12px; max-width: 560px;">
      <a-form-item label="账户标识" required>
        <a-input v-model:value="accountId" placeholder="请输入卡号/账户ID" allow-clear />
      </a-form-item>
      <a-form-item label="退款金额" required>
        <a-input-number v-model:value="amount" :min="0" :precision="2" style="width: 100%" placeholder="请输入金额" />
      </a-form-item>
      <a-form-item label="退款方式">
        <a-select v-model:value="method" placeholder="请选择退款方式">
          <a-select-option value="cash">现金</a-select-option>
          <a-select-option value="online">线上</a-select-option>
          <a-select-option value="wechat">微信退款</a-select-option>
          <a-select-option value="alipay">支付宝退款</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="备注">
        <a-input v-model:value="remark" placeholder="可选，填写备注" />
      </a-form-item>
      <a-space>
        <a-button type="primary" :loading="loading" @click="onSubmit">提交退款</a-button>
        <a-button @click="onReset">重置</a-button>
      </a-space>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { refund, type RechargeMethod } from '@/api/modules/card'

const accountId = ref<string>('')
const amount = ref<number | null>(null)
const method = ref<'cash' | 'online' | 'wechat' | 'alipay' | undefined>(undefined)
const remark = ref<string>('')
const loading = ref(false)

function onReset() {
  accountId.value = ''
  amount.value = null
  method.value = undefined
  remark.value = ''
}

async function onSubmit() {
  if (!accountId.value?.trim()) return message.warning('请输入账户标识')
  if (!amount.value || amount.value <= 0) return message.warning('请输入有效的退款金额')
  loading.value = true
  try {
    const methodMap: Record<string, RechargeMethod> = {
      cash: 'CASH', online: 'ONLINE', wechat: 'WECHAT', alipay: 'ALIPAY'
    }
    const res = await refund({ cardNo: accountId.value.trim(), amount: Number(amount.value), method: method.value ? methodMap[method.value] : undefined, note: remark.value.trim() || undefined })
    if (res?.success) {
      message.success('退款成功')
      onReset()
    } else {
      throw new Error('退款失败')
    }
  } catch (e: any) {
    message.error(e?.message || '退款失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>