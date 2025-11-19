<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getFaceConfig, updateFaceConfig, type FaceConfig } from '@/api/modules/faceConfig'
import { listFaces } from '@/api/modules/face'

const loading = ref(false)
const saving = ref(false)
const cfg = ref<FaceConfig | null>(null)
const totalFaces = ref<number>(0)

async function load() {
  loading.value = true
  try {
    cfg.value = await getFaceConfig()
    const r = await listFaces({ page: 1, size: 1 })
    totalFaces.value = (r as any)?.total ?? r.total
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!cfg.value) return
  saving.value = true
  try {
    const body = {
      recognitionThreshold: cfg.value.recognitionThreshold,
      recognitionMode: cfg.value.recognitionMode,
      livenessEnabled: cfg.value.livenessEnabled,
      libraryCapacity: cfg.value.libraryCapacity
    }
    const updated = await updateFaceConfig(body)
    cfg.value = updated
    message.success('配置已保存')
  } catch (e: any) {
    message.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  load()
})
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="人脸识别配置" sub-title="阈值、模式、活体与容量管理" />

    <a-card style="margin-top: 12px" :loading="loading">
      <a-form layout="vertical" v-if="cfg">
        <a-form-item label="识别阈值 (0-100)">
          <a-input-number v-model:value="cfg.recognitionThreshold" :min="0" :max="100" :precision="0" style="width: 160px" />
        </a-form-item>

        <a-form-item label="识别模式">
          <a-select v-model:value="cfg.recognitionMode" style="width: 180px">
            <a-select-option value="ONE_TO_ONE">1:1</a-select-option>
            <a-select-option value="ONE_TO_MANY">1:N</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="活体检测">
          <a-switch v-model:checked="cfg.livenessEnabled" />
        </a-form-item>

        <a-form-item label="人脸库容量">
          <a-input-number v-model:value="cfg.libraryCapacity" :min="1" :precision="0" style="width: 200px" />
          <div style="margin-top: 8px; color: #888;">
            当前库记录数：{{ totalFaces }} / 容量上限：{{ cfg.libraryCapacity }}
          </div>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" :loading="saving" @click="save">保存配置</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>