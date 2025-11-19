<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { schoolApi, type School, type SchoolLogo } from '@/api/modules/school'

const schools = ref<School[]>([])
const selectedSchoolId = ref<number | undefined>(undefined)
const loading = ref(false)
const saving = ref(false)
const logoUrl = ref<string>('')

const loadSchools = async () => {
  try {
    schools.value = await schoolApi.getEnabledList()
    if (!selectedSchoolId.value && schools.value.length > 0) selectedSchoolId.value = schools.value[0].id
  } catch (e) {
    schools.value = []
  }
}

const loadLogo = async () => {
  if (!selectedSchoolId.value) return
  loading.value = true
  try {
    const logo: SchoolLogo = await schoolApi.getLogo(selectedSchoolId.value)
    logoUrl.value = logo?.logoUrl || ''
  } catch (e) {
    logoUrl.value = ''
  } finally {
    loading.value = false
  }
}

watch(selectedSchoolId, () => loadLogo())

const saveLogo = async () => {
  if (!selectedSchoolId.value) return
  if (!logoUrl.value) {
    message.warning('请填写Logo URL')
    return
  }
  saving.value = true
  try {
    await schoolApi.setLogo(selectedSchoolId.value, logoUrl.value)
    message.success('Logo已更新')
  } catch (e: any) {
    message.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadSchools()
})
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="学校Logo设置" />

    <div style="margin-bottom: 12px; display: flex; gap: 8px; align-items: center;">
      <a-select v-model:value="selectedSchoolId" allow-clear style="width: 240px;" placeholder="选择学校">
        <a-select-option v-for="s in schools" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
      </a-select>
      <a-input v-model:value="logoUrl" style="width: 360px;" placeholder="输入 Logo URL" />
      <a-button type="primary" @click="saveLogo" :loading="saving" :disabled="!selectedSchoolId">保存</a-button>
    </div>

    <a-card title="预览" :loading="loading">
      <div style="height: 120px; display:flex; align-items:center; justify-content:center; border: 1px dashed #ddd;">
        <img v-if="logoUrl" :src="logoUrl" alt="logo" style="max-height: 100px; max-width: 100%;" />
        <span v-else>暂无Logo</span>
      </div>
    </a-card>
  </div>
</template>