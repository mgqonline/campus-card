<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'

interface CardTypeConfig {
  singleLimit: number | null
  allowedMerchants: string[]
  expireDays: number | null
  requireIdCheck: boolean
  accessAreas: string[]
  note: string
}

const loading = ref(false)
const saving = ref(false)
const merchants = ['食堂','超市']
const areas = ['A区','B区','图书馆','体育馆']

const form = reactive<CardTypeConfig>({
  singleLimit: 50,
  allowedMerchants: ['食堂'],
  expireDays: 7,
  requireIdCheck: true,
  accessAreas: ['A区','图书馆'],
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
    message.success('访客卡配置已保存')
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
    <a-page-header title="访客卡配置" />
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
        <a-form-item label="有效期(天)">
          <a-input-number v-model:value="form.expireDays" :min="1" :step="1" style="width: 200px" />
        </a-form-item>
        <a-form-item label="入校证件校验">
          <a-switch v-model:checked="form.requireIdCheck" />
        </a-form-item>
        <a-form-item label="可访问区域">
          <a-select v-model:value="form.accessAreas" mode="multiple" style="width: 360px">
            <a-select-option v-for="a in areas" :key="a" :value="a">{{ a }}</a-select-option>
          </a-select>
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