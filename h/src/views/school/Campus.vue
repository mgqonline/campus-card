<script setup lang="ts">
import { onMounted, ref, reactive, watch } from 'vue'
import { message } from 'ant-design-vue'
import { schoolApi, type School, type Campus } from '@/api/modules/school'

const loading = ref(false)
const dataSource = ref<Campus[]>([])
const schools = ref<School[]>([])
const columns = [
  { title: '校区名称', dataIndex: 'name', key: 'name' },
  { title: '校区代码', dataIndex: 'code', key: 'code' },
  { title: '地址', dataIndex: 'address', key: 'address' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action' }
]

const selectedSchoolId = ref<number | undefined>(undefined)
const modalOpen = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const form = reactive<Partial<Campus>>({ id: undefined, name: '', code: '', address: '', status: 1 })

const resetForm = () => {
  form.id = undefined
  form.name = ''
  form.code = ''
  form.address = ''
  form.status = 1
}

const loadSchools = async () => {
  try {
    schools.value = await schoolApi.getEnabledList()
    if (!selectedSchoolId.value && schools.value.length > 0) {
      selectedSchoolId.value = schools.value[0].id
    }
  } catch (e) {
    schools.value = []
  }
}

const fetchData = async () => {
  if (!selectedSchoolId.value) return
  loading.value = true
  try {
    const res = await schoolApi.listCampuses(selectedSchoolId.value)
    dataSource.value = res
  } catch (e) {
    message.error('获取校区列表失败')
    dataSource.value = []
  } finally {
    loading.value = false
  }
}

watch(selectedSchoolId, () => fetchData())

const openCreate = () => {
  resetForm()
  isEdit.value = false
  modalOpen.value = true
}

const openEdit = (row: Campus) => {
  Object.assign(form, row)
  isEdit.value = true
  modalOpen.value = true
}

const submitForm = async () => {
  modalLoading.value = true
  try {
    if (!form.name) {
      message.warning('请填写校区名称')
      return
    }
    if (!selectedSchoolId.value) {
      message.warning('请先选择学校')
      return
    }
    if (isEdit.value && form.id) {
      // 简化：使用删除+新增模拟更新；后端目前未提供更新接口
      await schoolApi.deleteCampus(selectedSchoolId.value, form.id)
      await schoolApi.addCampus(selectedSchoolId.value, { name: form.name!, code: form.code, address: form.address, status: form.status })
      message.success('更新成功')
    } else {
      await schoolApi.addCampus(selectedSchoolId.value, { name: form.name!, code: form.code, address: form.address, status: form.status })
      message.success('创建成功')
    }
    modalOpen.value = false
    fetchData()
  } catch (e: any) {
    message.error(e.message || '操作失败')
  } finally {
    modalLoading.value = false
  }
}

const onDelete = async (row: Campus) => {
  try {
    await schoolApi.deleteCampus(selectedSchoolId.value!, row.id!)
    message.success('删除成功')
    fetchData()
  } catch (e) {
    message.error('删除失败')
  }
}

onMounted(() => {
  loadSchools()
})
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="校区管理" />

    <div style="margin-bottom: 12px; display: flex; gap: 8px; align-items: center;">
      <a-select v-model:value="selectedSchoolId" allow-clear style="width: 240px;" placeholder="选择学校">
        <a-select-option v-for="s in schools" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
      </a-select>
      <a-button type="primary" @click="openCreate" :disabled="!selectedSchoolId">新增校区</a-button>
    </div>

    <a-table
      :columns="columns"
      :data-source="dataSource"
      :loading="loading"
      row-key="id"
      :pagination="false"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag color="green" v-if="record.status === 1">启用</a-tag>
          <a-tag color="red" v-else>禁用</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确定删除该校区吗？" @confirm="() => onDelete(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="modalOpen" :title="isEdit ? '编辑校区' : '新增校区'" :confirm-loading="modalLoading" @ok="submitForm" @cancel="() => (modalOpen = false)">
      <a-form layout="vertical">
        <a-form-item label="校区名称" required>
          <a-input v-model:value="form.name" placeholder="请输入校区名称" />
        </a-form-item>
        <a-form-item label="校区代码">
          <a-input v-model:value="form.code" placeholder="请输入校区代码" />
        </a-form-item>
        <a-form-item label="地址">
          <a-input v-model:value="form.address" placeholder="请输入地址" />
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>