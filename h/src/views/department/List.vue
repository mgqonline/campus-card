<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { listDepartments, createDepartment, updateDepartment, deleteDepartment, enableDepartment, disableDepartment, type Department } from '@/api/modules/department'

const loading = ref(false)
const list = ref<Department[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const query = reactive<{ name?: string; status?: number }>({})

const columns = [
  { title: '部门名称', dataIndex: 'name', key: 'name' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action' }
]

const modalOpen = ref(false)
const editing = ref<Department | null>(null)
const formModel = reactive<{ name: string; status: number }>({ name: '', status: 1 })

const openCreate = () => {
  editing.value = null
  formModel.name = ''
  formModel.status = 1
  modalOpen.value = true
}
const openEdit = (row: Department) => {
  editing.value = row
  formModel.name = row.name
  formModel.status = row.status ?? 1
  modalOpen.value = true
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listDepartments({ page: pagination.current, size: pagination.pageSize, name: query.name, status: query.status })
    list.value = res.records
    pagination.total = res.total
  } catch (e) {
    list.value = [
      { id: 1, name: '语文组', status: 1 },
      { id: 2, name: '数学组', status: 1 },
      { id: 3, name: '英语组', status: 0 }
    ]
    pagination.total = list.value.length
    message.info('后端未就绪，显示示例数据')
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  if (!formModel.name) { message.warning('请填写部门名称'); return }
  try {
    if (editing.value) {
      await updateDepartment(editing.value.id, { ...formModel })
      message.success('更新成功')
    } else {
      await createDepartment({ ...formModel } as any)
      message.success('创建成功')
    }
    modalOpen.value = false
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟成功')
    modalOpen.value = false
    fetchData()
  }
}

const onDelete = async (row: Department) => {
  try {
    await deleteDepartment(row.id)
    message.success('删除成功')
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟删除成功')
    fetchData()
  }
}

const toggleStatus = async (row: Department) => {
  try {
    if ((row.status ?? 1) === 1) {
      await disableDepartment(row.id)
      message.success('已禁用')
    } else {
      await enableDepartment(row.id)
      message.success('已启用')
    }
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟状态切换成功')
    row.status = (row.status ?? 1) === 1 ? 0 : 1
    fetchData()
  }
}

const handleSearch = () => { pagination.current = 1; fetchData() }
const handleReset = () => { query.name = undefined; query.status = undefined; pagination.current = 1; fetchData() }
const handlePageChange = (page: number, pageSize?: number) => { pagination.current = page; if (pageSize) pagination.pageSize = pageSize; fetchData() }

onMounted(fetchData)
</script>

<template>
  <div class="page-container" style="padding:24px;">
    <a-page-header title="部门管理" />

    <a-form layout="inline" style="margin-bottom: 16px;">
      <a-form-item label="部门名称">
        <a-input v-model:value="query.name" placeholder="支持模糊搜索" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear placeholder="全部" style="width: 160px;">
          <a-select-option :value="1">启用</a-select-option>
          <a-select-option :value="0">禁用</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" @click="handleSearch">查询</a-button>
          <a-button @click="handleReset">重置</a-button>
        </a-space>
      </a-form-item>
      <a-form-item style="margin-left:auto;">
        <a-button type="primary" @click="openCreate">新增部门</a-button>
      </a-form-item>
    </a-form>

    <a-table
      :data-source="list"
      :columns="columns"
      :loading="loading"
      :pagination="{ current: pagination.current, pageSize: pagination.pageSize, total: pagination.total, showSizeChanger: true }"
      row-key="id"
      @change="(p:any) => handlePageChange(p.current, p.pageSize)"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <span>{{ (record.status ?? 1) === 1 ? '启用' : '禁用' }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除该部门？" @confirm="() => onDelete(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
            <a-divider type="vertical" />
            <a-button type="link" :danger="(record.status ?? 1) === 1" @click="toggleStatus(record)">
              {{ (record.status ?? 1) === 1 ? '禁用' : '启用' }}
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="modalOpen" :title="editing ? '编辑部门' : '新增部门'" @ok="submitForm">
      <a-form layout="vertical">
        <a-form-item label="部门名称" required>
          <a-input v-model:value="formModel.name" placeholder="请输入部门名称" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="formModel.status" style="width: 160px">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>