<script setup lang="ts">
import { onMounted, ref, reactive, watch } from 'vue'
import { message } from 'ant-design-vue'
import { schoolApi, type School, type SchoolConfig } from '@/api/modules/school'

const schools = ref<School[]>([])
const selectedSchoolId = ref<number | undefined>(undefined)
const loading = ref(false)
const saving = ref(false)

// 使用键值对编辑配置
interface KV { key: string; value: string }
const kvList = ref<KV[]>([])

const loadSchools = async () => {
  try {
    schools.value = await schoolApi.getEnabledList()
    if (!selectedSchoolId.value && schools.value.length > 0) selectedSchoolId.value = schools.value[0].id
  } catch (e) {
    schools.value = []
  }
}

const loadConfig = async () => {
  if (!selectedSchoolId.value) return
  loading.value = true
  try {
    const cfgs: SchoolConfig[] = await schoolApi.listConfigs(selectedSchoolId.value)
    kvList.value = (cfgs || []).map(c => ({ key: c.key, value: c.value || '' }))
  } catch (e) {
    kvList.value = []
  } finally {
    loading.value = false
  }
}

watch(selectedSchoolId, () => loadConfig())

const addRow = () => {
  kvList.value.push({ key: '', value: '' })
}

const deleteRow = (index: number) => {
  kvList.value.splice(index, 1)
}

const saveConfig = async () => {
  if (!selectedSchoolId.value) return
  saving.value = true
  try {
    for (const kv of kvList.value) {
      if (kv.key) await schoolApi.setConfig(selectedSchoolId.value, kv.key, kv.value)
    }
    message.success('配置已保存')
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
    <a-page-header title="学校参数配置" />

    <div style="margin-bottom: 12px; display: flex; gap: 8px; align-items: center;">
      <a-select v-model:value="selectedSchoolId" allow-clear style="width: 240px;" placeholder="选择学校">
        <a-select-option v-for="s in schools" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
      </a-select>
      <a-space>
        <a-button type="primary" @click="addRow" :disabled="!selectedSchoolId">新增参数</a-button>
        <a-button type="primary" @click="saveConfig" :loading="saving" :disabled="!selectedSchoolId">保存</a-button>
      </a-space>
    </div>

    <a-table :data-source="kvList" :loading="loading" :pagination="false" row-key="key">
      <a-table-column title="参数键">
        <template #default="{ record, index }">
          <a-input v-model:value="kvList[index].key" placeholder="如 semesterStartDate" />
        </template>
      </a-table-column>
      <a-table-column title="参数值">
        <template #default="{ record, index }">
          <a-input v-model:value="kvList[index].value" placeholder="值" />
        </template>
      </a-table-column>
      <a-table-column title="操作">
        <template #default="{ index }">
          <a-button type="link" danger @click="deleteRow(index)">删除</a-button>
        </template>
      </a-table-column>
    </a-table>
  </div>
</template>