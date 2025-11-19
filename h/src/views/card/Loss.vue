<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { reportLoss, unloss } from '@/api/modules/card'

const cardNo = ref('')
const reason = ref('')
const loadingLoss = ref(false)
const loadingUnloss = ref(false)

const onLoss = async () => {
  if (!cardNo.value) {
    message.error('请输入卡号')
    return
  }
  loadingLoss.value = true
  try {
    await reportLoss({ cardNo: cardNo.value, reason: reason.value })
    message.success('挂失成功')
    reason.value = ''
  } catch (e) {
    message.error('挂失失败（后端未就绪或接口异常）')
  } finally {
    loadingLoss.value = false
  }
}

const onUnloss = async () => {
  if (!cardNo.value) {
    message.error('请输入卡号')
    return
  }
  loadingUnloss.value = true
  try {
    await unloss({ cardNo: cardNo.value })
    message.success('解挂成功')
  } catch (e) {
    message.error('解挂失败（后端未就绪或接口异常）')
  } finally {
    loadingUnloss.value = false
  }
}
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="卡片挂失 / 解挂" />
    <a-card>
      <a-form layout="inline">
        <a-form-item label="卡号">
          <a-input v-model:value="cardNo" style="width: 220px" placeholder="输入卡号" />
        </a-form-item>
        <a-form-item label="原因">
          <a-input v-model:value="reason" style="width: 220px" placeholder="可选挂失原因" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" :loading="loadingLoss" @click="onLoss">挂失</a-button>
        </a-form-item>
        <a-form-item>
          <a-button :loading="loadingUnloss" @click="onUnloss">解挂</a-button>
        </a-form-item>
      </a-form>
      <div style="margin-top: 12px; color: #999;">提示：挂失后将不可消费；解挂恢复正常使用。</div>
    </a-card>
  </div>
</template>