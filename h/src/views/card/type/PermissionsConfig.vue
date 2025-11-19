<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'

interface PermItem { key: string; label: string }
interface CardPermConfig {
  student: string[]
  teacher: string[]
  temp: string[]
  visitor: string[]
}

const loading = ref(false)
const saving = ref(false)

const permCatalog: PermItem[] = [
  { key: 'consume', label: '消费' },
  { key: 'recharge', label: '充值' },
  { key: 'refund', label: '退款' },
  { key: 'library', label: '图书馆借阅' },
  { key: 'gym', label: '体育馆入场' },
  { key: 'bus', label: '校车乘坐' },
  { key: 'dorm', label: '宿舍门禁' }
]

const form = reactive<CardPermConfig>({
  student: ['consume','recharge','refund','library','gym','bus'],
  teacher: ['consume','recharge','refund','library','gym','bus','dorm'],
  temp: ['consume'],
  visitor: ['consume','library']
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
    message.success('卡片权限配置已保存')
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
    <a-page-header title="卡片权限配置" />
    <a-card :loading="loading">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-card title="学生卡" size="small" style="margin-bottom: 12px;">
            <a-checkbox-group v-model:value="form.student">
              <a-space direction="vertical">
                <a-checkbox v-for="p in permCatalog" :key="p.key" :value="p.key">{{ p.label }}</a-checkbox>
              </a-space>
            </a-checkbox-group>
          </a-card>

          <a-card title="临时卡" size="small">
            <a-checkbox-group v-model:value="form.temp">
              <a-space direction="vertical">
                <a-checkbox v-for="p in permCatalog" :key="p.key" :value="p.key">{{ p.label }}</a-checkbox>
              </a-space>
            </a-checkbox-group>
          </a-card>
        </a-col>
        <a-col :span="12">
          <a-card title="教师卡" size="small" style="margin-bottom: 12px;">
            <a-checkbox-group v-model:value="form.teacher">
              <a-space direction="vertical">
                <a-checkbox v-for="p in permCatalog" :key="p.key" :value="p.key">{{ p.label }}</a-checkbox>
              </a-space>
            </a-checkbox-group>
          </a-card>

          <a-card title="访客卡" size="small">
            <a-checkbox-group v-model:value="form.visitor">
              <a-space direction="vertical">
                <a-checkbox v-for="p in permCatalog" :key="p.key" :value="p.key">{{ p.label }}</a-checkbox>
              </a-space>
            </a-checkbox-group>
          </a-card>
        </a-col>
      </a-row>

      <div style="margin-top: 12px;">
        <a-space>
          <a-button type="primary" :loading="saving" @click="saveConfig">保存</a-button>
          <a-button @click="fetchConfig" :disabled="saving">重置</a-button>
        </a-space>
      </div>
    </a-card>
  </div>
</template>