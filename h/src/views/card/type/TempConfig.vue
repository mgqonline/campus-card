<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'

interface CardTypeConfig {
  singleLimit: number | null
  allowedMerchants: string[]
  expireHours: number | null
  deposit: number | null
  autoDisable: boolean
  note: string
}

const loading = ref(false)
const saving = ref(false)
const merchants = ['食堂','超市','图书馆','体育馆']

const form = reactive<CardTypeConfig>({
  singleLimit: 30,
  allowedMerchants: ['食堂'],
  expireHours: 24,
  deposit: 50,
  autoDisable: true,
  note: ''
})

const fetchConfig = async () => {
  loading.value = true
  try {
    await new Promise(r => setTimeout(r, 300))
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const saveConfig = async () => {
  saving.value = true
  try {
    await new Promise(r => setTimeout(r, 300))
    message.success('临时卡配置已保存')
  } catch (e: any) {
    message.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(fetchConfig)
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="临时卡配置" />
    <a-card :loading="loading">
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="单笔限额">
          <a-input-number v-model:value="form.singleLimit" :min="0" :step="1" style="width: 200px" />
        </a-form-item>
        <a-form-item label="允许商户">
          <a-select v-model:value="form.allowedMerchants" mode="multiple" style="width: 360px" placeholder="选择允许的商户">
            <a-select-option v-for="m in merchants" :key="m" :value="m">{{ m }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="有效期(小时)">
          <a-input-number v-model:value="form.expireHours" :min="1" :step="1" style="width: 200px" />
        </a-form-item>
        <a-form-item label="押金">
          <a-input-number v-model:value="form.deposit" :min="0" :step="1" style="width: 200px" />
        </a-form-item>
        <a-form-item label="到期自动禁用">
          <a-switch v-model:checked="form.autoDisable" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.note" placeholder="可选备注" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" :loading="saving" @click="saveConfig">保存</a-button>
            <a-button @click="fetchConfig" :disabled="saving">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>