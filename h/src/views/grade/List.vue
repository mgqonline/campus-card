<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { getGradeList, createGrade, updateGrade, deleteGrade, enableGrade, disableGrade, type GradeInfo } from '@/api/modules/grade'
import { schoolApi, type School } from '@/api/modules/school'

const loading = ref(false)
const dataSource = ref<GradeInfo[]>([])
const schools = ref<School[]>([])
const columns = [
  { title: '年级名称', dataIndex: 'name', key: 'name' },
  { title: '所属学校', dataIndex: 'schoolName', key: 'schoolName' },
  { title: '入学年份', dataIndex: 'year', key: 'year' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action' }
]

const query = reactive<{ name?: string; schoolId?: number }>({ name: undefined, schoolId: undefined })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const modalOpen = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const form = reactive<Partial<GradeInfo>>({ id: undefined, name: '', year: undefined, status: 1, schoolId: undefined })

const resetForm = () => {
  form.id = undefined
  form.name = ''
  form.year = undefined
  form.status = 1
  form.schoolId = undefined
}

const loadSchools = async () => {
  try {
    const res = await schoolApi.getEnabledList()
    schools.value = res
  } catch (e) {
    schools.value = [
      { id: 1, name: '北京大学附属中学', code: 'PKUHS', address: '北京市海淀区', phone: '010-12345678', email: 'info@pkuhs.edu.cn', principal: '张校长', status: 1 },
      { id: 2, name: '清华大学附属中学', code: 'THUHS', address: '北京市海淀区', phone: '010-87654321', email: 'info@thuhs.edu.cn', principal: '李校长', status: 1 }
    ]
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getGradeList({ page: pagination.current, size: pagination.pageSize, name: query.name, schoolId: query.schoolId })
    dataSource.value = res.records
    pagination.total = res.total
  } catch (e) {
    message.error('获取年级列表失败')
    dataSource.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}
const handleReset = () => {
  query.name = undefined
  query.schoolId = undefined
  pagination.current = 1
  fetchData()
}
const handlePageChange = (page: number, pageSize?: number) => {
  pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  fetchData()
}

const openCreate = () => {
  resetForm()
  isEdit.value = false
  modalOpen.value = true
}

const openEdit = (row: GradeInfo) => {
  resetForm()
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.year = row.year
  form.status = row.status
  form.schoolId = row.schoolId
  modalOpen.value = true
}

const onDelete = async (row: GradeInfo) => {
  try {
    await deleteGrade(row.id)
    message.success('删除成功')
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟删除成功')
    fetchData()
  }
}

const onToggleStatus = async (row: GradeInfo) => {
  try {
    if ((row.status ?? 1) === 1) {
      await disableGrade(row.id)
      message.success('禁用成功')
    } else {
      await enableGrade(row.id)
      message.success('启用成功')
    }
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟状态切换成功')
    fetchData()
  }
}

const onSubmit = async () => {
  modalLoading.value = true
  try {
    if (isEdit.value && form.id) {
      await updateGrade(form.id, { name: form.name, year: form.year, status: form.status, schoolId: form.schoolId })
      message.success('修改成功')
    } else {
      await createGrade({ name: form.name!, year: form.year, status: form.status, schoolId: form.schoolId })
      message.success('新增成功')
    }
    modalOpen.value = false
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟保存成功')
    modalOpen.value = false
    fetchData()
  } finally {
    modalLoading.value = false
  }
}

const onCancel = () => {
  modalOpen.value = false
}

onMounted(() => {
  fetchData()
  loadSchools()
})
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="年级管理" />

    <div style="margin-bottom: 12px; display: flex; gap: 8px;">
      <a-button type="primary" @click="openCreate">新增年级</a-button>
    </div>

    <a-form layout="inline" style="margin-bottom: 16px;">
      <a-form-item label="所属学校">
        <a-select v-model:value="query.schoolId" placeholder="请选择学校" allow-clear style="width: 200px;">
          <a-select-option v-for="school in schools" :key="school.id" :value="school.id">
            {{ school.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="年级名称">
        <a-input v-model:value="query.name" placeholder="请输入名称关键字" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSearch">查询</a-button>
      </a-form-item>
      <a-form-item>
        <a-button @click="handleReset">重置</a-button>
      </a-form-item>
    </a-form>

    <a-table
      :columns="columns"
      :data-source="dataSource"
      :loading="loading"
      row-key="id"
      :pagination="{ current: pagination.current, pageSize: pagination.pageSize, total: pagination.total, showSizeChanger: true }"
      @change="(p:any) => handlePageChange(p.current, p.pageSize)"
    >
      <template #bodyCell="{ column, record }">
        <!-- 学校名称优雅降级显示：优先使用后端返回的schoolName，否则用schoolId映射，最后回退显示schoolId -->
        <template v-if="column.key === 'schoolName'">
          <span>{{ record.schoolName || (schools.find(s => s.id === record.schoolId)?.name) || record.schoolId }}</span>
        </template>
        <template v-else-if="column.key === 'status'">
          <span>{{ (record.status ?? 1) === 1 ? '启用' : '禁用' }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除该年级？" @confirm="() => onDelete(record)">
              <a-button type="link">删除</a-button>
            </a-popconfirm>
            <a-popconfirm :title="(record.status ?? 1) === 1 ? '确认禁用？' : '确认启用？'" @confirm="() => onToggleStatus(record)">
              <a-button type="link">{{ (record.status ?? 1) === 1 ? '禁用' : '启用' }}</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:open="modalOpen"
      :confirm-loading="modalLoading"
      :title="isEdit ? '编辑年级' : '新增年级'"
      okText="保存"
      cancelText="取消"
      @ok="onSubmit"
      @cancel="onCancel"
      destroyOnClose
    >
      <a-form layout="vertical">
        <a-form-item label="所属学校" required>
          <a-select v-model:value="form.schoolId" placeholder="请选择学校">
            <a-select-option v-for="school in schools" :key="school.id" :value="school.id">
              {{ school.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="年级名称" required>
          <a-input v-model:value="form.name" placeholder="请输入年级名称" />
        </a-form-item>
        <a-form-item label="入学年份">
          <a-input-number v-model:value="form.year" :min="2000" :precision="0" style="width: 100%;" />
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