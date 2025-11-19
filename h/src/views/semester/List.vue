<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { getSemesterList, createSemester, updateSemester, deleteSemester, enableSemester, disableSemester, setSemesterCurrent, type Semester } from '@/api/modules/semester'
import { getHistorySemesters } from '@/api/modules/semester'
import { schoolApi, type School } from '@/api/modules/school'

const loading = ref(false)
const dataSource = ref<Semester[]>([])
const schools = ref<School[]>([])
const columns = [
  { title: '学期名称', dataIndex: 'name', key: 'name' },
  { title: '所属学校', dataIndex: 'schoolId', key: 'schoolId' },
  { title: '学期代码', dataIndex: 'code', key: 'code' },
  { title: '起始日期', dataIndex: 'startDate', key: 'startDate' },
  { title: '结束日期', dataIndex: 'endDate', key: 'endDate' },
  { title: '当前学期', dataIndex: 'current', key: 'current' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action' }
]

const query = reactive<{ name?: string; schoolId?: number }>({ name: undefined, schoolId: undefined })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const historyMode = ref(false)
const dateRange = ref<[string, string] | undefined>(undefined)
const fullHistory = ref<Semester[]>([])

const modalOpen = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const form = reactive<Partial<Semester>>({ id: undefined, name: '', code: '', schoolId: undefined, startDate: undefined, endDate: undefined, status: 1, current: false })

const resetForm = () => {
  form.id = undefined
  form.name = ''
  form.code = ''
  form.schoolId = undefined
  form.startDate = undefined
  form.endDate = undefined
  form.status = 1
  form.current = false
}

const loadSchools = async () => {
  try {
    const res = await schoolApi.getEnabledList()
    schools.value = res
  } catch (e) {
    schools.value = []
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    if (historyMode.value) {
      await fetchHistoryData()
    } else {
      const res = await getSemesterList({ page: pagination.current, size: pagination.pageSize, name: query.name, schoolId: query.schoolId })
      dataSource.value = res.records
      pagination.total = res.total
    }
  } catch (e) {
    message.error('获取学期列表失败')
    dataSource.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const fetchHistoryData = async () => {
  try {
    const res = await getHistorySemesters(query.schoolId as number, dateRange.value?.[0], dateRange.value?.[1])
    fullHistory.value = res || []
    pagination.total = fullHistory.value.length
    const start = (pagination.current - 1) * pagination.pageSize
    const end = start + pagination.pageSize
    dataSource.value = fullHistory.value.slice(start, end)
  } catch (e) {
    fullHistory.value = []
    dataSource.value = []
    pagination.total = 0
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}
const handleReset = () => {
  query.name = undefined
  query.schoolId = undefined
  dateRange.value = undefined
  historyMode.value = false
  pagination.current = 1
  fetchData()
}
const handlePageChange = (page: number, pageSize?: number) => {
  pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  if (historyMode.value) {
    fetchHistoryData()
  } else {
    fetchData()
  }
}

const openCreate = () => {
  resetForm()
  isEdit.value = false
  modalOpen.value = true
}

const openEdit = (row: Semester) => {
  Object.assign(form, row)
  isEdit.value = true
  modalOpen.value = true
}

const submitForm = async () => {
  modalLoading.value = true
  try {
    if (!form.name || !form.code || !form.schoolId) {
      message.warning('请填写名称、编码并选择学校')
      return
    }
    if (isEdit.value && form.id) {
      await updateSemester(form.id, { ...form })
      message.success('更新成功')
    } else {
      await createSemester({ ...(form as any) })
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

const onDelete = async (row: Semester) => {
  try {
    await deleteSemester(row.id)
    message.success('删除成功')
    fetchData()
  } catch (e) {
    message.error('删除失败')
  }
}

const toggleStatus = async (row: Semester) => {
  try {
    if (row.status === 1) {
      await disableSemester(row.id)
      message.success('禁用成功')
    } else {
      await enableSemester(row.id)
      message.success('启用成功')
    }
    fetchData()
  } catch (e) {
    message.error('操作失败')
  }
}

const setCurrent = async (row: Semester) => {
  try {
    await setSemesterCurrent(row.id)
    message.success('已设置为当前学期')
    fetchData()
  } catch (e) {
    message.error('设置失败')
  }
}

onMounted(() => {
  fetchData()
  loadSchools()
})
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="学期管理" />

    <div style="margin-bottom: 12px; display: flex; gap: 8px;">
      <a-button type="primary" @click="openCreate">新增学期</a-button>
    </div>

    <a-form layout="inline" style="margin-bottom: 16px;">
      <a-form-item label="所属学校">
        <a-select v-model:value="query.schoolId" placeholder="请选择学校" allow-clear style="width: 200px;">
          <a-select-option v-for="school in schools" :key="school.id" :value="school.id">
            {{ school.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="学期名称">
        <a-input v-model:value="query.name" placeholder="请输入名称关键字" allow-clear />
      </a-form-item>
      <a-form-item label="历史模式">
        <a-switch v-model:checked="historyMode" @change="() => { pagination.current = 1; fetchData() }" />
      </a-form-item>
      <a-form-item v-if="historyMode" label="时间范围">
        <a-range-picker v-model:value="(dateRange as any)" value-format="YYYY-MM-DD" />
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
        <template v-if="column.key === 'schoolId'">
          {{ schools.find(s => s.id === record.schoolId)?.name || record.schoolId }}
        </template>
        <template v-else-if="column.key === 'current'">
          <a-tag color="blue" v-if="record.current">当前</a-tag>
          <span v-else>否</span>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag color="green" v-if="record.status === 1">启用</a-tag>
          <a-tag color="red" v-else>禁用</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确定删除该学期吗？" @confirm="() => onDelete(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
            <a-button type="link" @click="() => toggleStatus(record)">{{ record.status === 1 ? '禁用' : '启用' }}</a-button>
            <a-button type="link" @click="() => setCurrent(record)" :disabled="record.current">设为当前</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增/编辑模态框保留原逻辑 -->
    <a-modal v-model:open="modalOpen" :title="isEdit ? '编辑学期' : '新增学期'" :confirm-loading="modalLoading" @ok="submitForm" @cancel="() => (modalOpen = false)">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="学期名称" required>
              <a-input v-model:value="form.name" placeholder="请输入学期名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="学期代码" required>
              <a-input v-model:value="form.code" placeholder="请输入学期代码" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="所属学校" required>
          <a-select v-model:value="form.schoolId" placeholder="请选择学校">
            <a-select-option v-for="s in schools" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="起始日期">
              <a-date-picker v-model:value="(form as any).startDate" value-format="YYYY-MM-DD" style="width: 100%;" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束日期">
              <a-date-picker v-model:value="(form as any).endDate" value-format="YYYY-MM-DD" style="width: 100%;" />
            </a-form-item>
          </a-col>
        </a-row>
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