<template>
  <div class="page system-config">
    <a-card title="系统配置">
      <a-form :model="editValues" @finish="onFinish">
      <a-tabs v-model:activeKey="activeTab" @change="onTabChange">
        <a-tab-pane key="PARAM" tab="系统参数" />
        <a-tab-pane key="INTERFACE" tab="接口配置" />
        <a-tab-pane key="PUSH" tab="消息推送配置" />
        <a-tab-pane key="STORAGE" tab="文件存储配置" />
        <a-tab-pane key="BACKUP" tab="数据备份配置" />
      </a-tabs>
      <div class="toolbar">
        <a-space>
          <a-button type="primary" @click="reload" :loading="loading">刷新</a-button>
          <a-button @click="resetEdits" :disabled="loading">重置编辑</a-button>
          <a-button type="primary" html-type="submit" :loading="saving" :disabled="loading || changedCount === 0">
            提交更改{{ changedCount ? `（${changedCount}）` : '' }}
          </a-button>
        </a-space>
      </div>
      <a-table :key="tableVersion" :dataSource="configs" :loading="loading" rowKey="key" :pagination="false">
        <a-table-column title="Key" dataIndex="key" key="key" />
        <a-table-column title="Value" key="value" v-slot="{ record }">
          <component :is="renderEditor(record)" />
        </a-table-column>
        <a-table-column title="操作" key="actions" v-slot="{ record }">
          <a-button 
            type="primary" 
            size="default" 
            html-type="submit"
            :disabled="(editValues[record.key] ?? '') === (record.value ?? '')"
            @click="() => { submitKey = record.key }"
          >
            保存
          </a-button>
        </a-table-column>
      </a-table>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { h, reactive, ref, onMounted, computed } from 'vue'
import { message, Switch, InputNumber, Input, Select } from 'ant-design-vue'
import type { ConfigCategory, SystemConfig } from '@/api/modules/systemConfig'
import { getCategories, listByCategory, setConfig } from '@/api/modules/systemConfig'

const activeTab = ref<ConfigCategory>('PARAM')
const loading = ref(false)
const saving = ref(false)
const configs = ref<SystemConfig[]>([])
const categories = ref<string[]>([])
const editValues = reactive<Record<string, string>>({})
const tableVersion = ref(0)
let submitKey: string | null = null

// 计算已修改项数量
const changedKeys = computed(() => {
  return configs.value
    .filter(c => (editValues[c.key] ?? '') !== (c.value ?? ''))
    .map(c => c.key)
})
const changedCount = computed(() => changedKeys.value.length)

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (e: any) {
    console.error('加载分类失败:', e)
    message.error('加载分类失败: ' + (e.message || e.toString()))
  }
}

const loadConfigs = async () => {
  loading.value = true
  try {
    const data = await listByCategory(activeTab.value)
    configs.value = data || []
    // 初始化编辑值
    Object.keys(editValues).forEach(k => delete editValues[k])
    configs.value.forEach(c => editValues[c.key] = c.value ?? '')
  } catch (e: any) {
    console.error('加载配置失败:', e)
    message.error('加载配置失败: ' + (e.message || e.toString()))
    configs.value = []
  } finally {
    loading.value = false
    tableVersion.value++
  }
}

const onTabChange = async () => {
  await loadConfigs()
}

const resetEdits = () => {
  configs.value.forEach(c => editValues[c.key] = c.value ?? '')
  tableVersion.value++
  message.success('已重置编辑')
}

const getFieldType = (key: string): 'boolean' | 'number' | 'url' | 'cron' | 'select' | 'text' => {
  if (key.endsWith('enabled')) return 'boolean'
  if (key.endsWith('timeoutMs') || key.endsWith('retentionDays')) return 'number'
  if (key.endsWith('baseUrl') || key.endsWith('webhookUrl')) return 'url'
  if (key.endsWith('cron')) return 'cron'
  if (key.endsWith('locale')) return 'select'
  return 'text'
}

const renderEditor = (record: SystemConfig) => {
  const val = editValues[record.key] ?? record.value ?? ''
  const type = getFieldType(record.key)
  const placeholder = val ? '' : '请输入值'
  switch (type) {
    case 'boolean':
      return h(Switch, {
        checked: (val === 'true'),
        'onUpdate:checked': (checked: boolean) => { editValues[record.key] = checked ? 'true' : 'false' }
      })
    case 'number':
      return h(InputNumber, {
        value: val ? Number(val) : undefined,
        min: 0,
        placeholder,
        style: 'width: 100%',
        'onUpdate:value': (n: number | null) => { editValues[record.key] = (n == null ? '' : String(n)) }
      })
    case 'url':
      return h(Input, {
        value: val,
        allowClear: true,
        placeholder: '请输入URL，例如 https://api.example.com',
        style: 'width: 100%',
        'onUpdate:value': (v: string) => { editValues[record.key] = v ?? '' }
      })
    case 'cron':
      return h(Input, {
        value: val,
        allowClear: true,
        placeholder: '秒 分 时 日 月 周（6段）例如 0 0 2 * * *',
        style: 'width: 100%',
        'onUpdate:value': (v: string) => { editValues[record.key] = v ?? '' }
      })
    case 'select':
      return h(Select, {
        value: val,
        options: [
          { value: 'zh-CN', label: '中文 (zh-CN)' },
          { value: 'en-US', label: 'English (en-US)' },
        ],
        placeholder: '请选择语言',
        style: 'width: 100%',
        'onUpdate:value': (v: string) => { editValues[record.key] = v ?? '' }
      })
    default:
      return h(Input, {
        value: val,
        allowClear: true,
        placeholder,
        style: 'width: 100%',
        'onUpdate:value': (v: string) => { editValues[record.key] = v ?? '' }
      })
  }
}

const validateValue = (key: string, val: string): string | null => {
  const type = getFieldType(key)
  if (type === 'boolean') {
    if (val !== 'true' && val !== 'false') return '布尔值仅支持 true/false'
  }
  if (type === 'number') {
    if (!/^\d+$/.test(val)) return '需为非负整数'
  }
  if (type === 'url') {
    if (!val) return 'URL不能为空'
    try { new URL(val) } catch { return 'URL格式不正确' }
  }
  if (type === 'cron') {
    const parts = val.trim().split(/\s+/)
    if (parts.length !== 6) return 'Cron表达式需为6段：秒 分 时 日 月 周'
  }
  return null
}

const reload = async () => {
  await loadConfigs()
  message.success('已刷新')
}

const save = async (record: SystemConfig) => {
  try {
    const val = editValues[record.key] ?? ''
    const err = validateValue(record.key, val)
    if (err) {
      message.warning(err)
      return
    }
    saving.value = true
    await setConfig(activeTab.value, record.key, val)
    message.success('保存成功')
    await loadConfigs()
  } catch (e: any) {
    console.error('保存失败:', e)
    message.error('保存失败: ' + (e.message || e.toString()))
  } finally {
    saving.value = false
  }
}

const saveAll = async () => {
  if (changedKeys.value.length === 0) {
    message.info('没有需要保存的更改')
    return
  }
  // 校验所有更改项
  for (const key of changedKeys.value) {
    const val = editValues[key] ?? ''
    const err = validateValue(key, val)
    if (err) {
      message.warning(`字段 ${key} 校验失败：${err}`)
      return
    }
  }
  saving.value = true
  try {
    const results = await Promise.allSettled(
      changedKeys.value.map(key => setConfig(activeTab.value, key, editValues[key] ?? ''))
    )
    const failed: string[] = []
    results.forEach((r, idx) => { if (r.status === 'rejected') failed.push(changedKeys.value[idx]) })
    if (failed.length === 0) {
      message.success(`全部保存成功（${changedKeys.value.length} 项）`)
    } else if (failed.length < changedKeys.value.length) {
      message.warning(`部分保存成功，失败 ${failed.length} 项：${failed.join(', ')}`)
    } else {
      message.error('保存失败，请稍后重试')
    }
    await loadConfigs()
  } catch (e: any) {
    console.error('保存过程中发生错误:', e)
    message.error('保存过程中发生错误: ' + (e.message || e.toString()))
  } finally {
    saving.value = false
  }
}

const onFinish = async () => {
  // 若指定了单行提交，则只提交该键；否则执行批量保存
  if (submitKey) {
    try {
      const val = editValues[submitKey] ?? ''
      const err = validateValue(submitKey, val)
      if (err) {
        message.warning(`字段 ${submitKey} 校验失败：${err}`)
        submitKey = null
        return
      }
      saving.value = true
      await setConfig(activeTab.value, submitKey, val)
      message.success('保存成功')
      await loadConfigs()
    } catch (e: any) {
      console.error('保存失败:', e)
      message.error('保存失败: ' + (e.message || e.toString()))
    } finally {
      saving.value = false
      submitKey = null
    }
  } else {
    await saveAll()
  }
}

onMounted(async () => {
  await loadCategories()
  await loadConfigs()
})
</script>

<style scoped>
.page.system-config { padding: 12px; }
.toolbar { margin: 8px 0 12px; }
</style>