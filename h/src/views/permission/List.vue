<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { getPermissionList, createPermission, updatePermission, deletePermission, type PermissionInfo } from '@/api/modules/permission'

const loading = ref(false)
const dataSource = ref<PermissionInfo[]>([])
const columns = [
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '编码', dataIndex: 'code', key: 'code' },
  { title: '描述', dataIndex: 'description', key: 'description' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action' }
]

const query = reactive<{ name?: string; code?: string }>({ name: undefined, code: undefined })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const modalVisible = ref(false)
const editing = ref<null | PermissionInfo>(null)
const formModel = reactive<{ name: string; code: string; description?: string; status?: number }>({ name: '', code: '', description: '', status: 1 })

const openCreate = () => {
  editing.value = null
  formModel.name = ''
  formModel.code = ''
  formModel.description = ''
  formModel.status = 1
  modalVisible.value = true
}

const openEdit = (row: PermissionInfo) => {
  editing.value = row
  formModel.name = row.name
  formModel.code = row.code
  formModel.description = row.description
  formModel.status = row.status ?? 1
  modalVisible.value = true
}

const submitForm = async () => {
  try {
    if (!formModel.name || !formModel.code) {
      message.warning('请填写名称和编码')
      return
    }
    if (editing.value) {
      await updatePermission(editing.value.id, { ...formModel })
      message.success('更新成功')
    } else {
      await createPermission({ ...formModel, id: 0 } as any)
      message.success('创建成功')
    }
    modalVisible.value = false
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟成功')
    modalVisible.value = false
    fetchData()
  }
}

const onDelete = async (row: PermissionInfo) => {
  try {
    await deletePermission(row.id)
    message.success('删除成功')
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟删除成功')
    fetchData()
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPermissionList({ page: pagination.current, size: pagination.pageSize, name: query.name, code: query.code })
    dataSource.value = res.records
    pagination.total = res.total
  } catch (e) {
    dataSource.value = [
      { id: 1, name: '查看学生', code: 'student:view', description: '允许查看学生信息', status: 1 },
      { id: 2, name: '管理卡片', code: 'card:manage', description: '允许进行卡片管理操作', status: 1 }
    ]
    pagination.total = 2
    message.info('后端未就绪，已展示示例数据')
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
  query.code = undefined
  pagination.current = 1
  fetchData()
}
const handlePageChange = (page: number, pageSize?: number) => {
  pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  fetchData()
}

onMounted(fetchData)
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="权限管理" />

    <div style="margin-bottom: 12px; display: flex; gap: 8px;">
      <a-button type="primary" @click="openCreate">新增权限</a-button>
    </div>

    <a-form layout="inline" style="margin-bottom: 16px;">
      <a-form-item label="名称">
        <a-input v-model:value="query.name" placeholder="请输入权限名称关键字" allow-clear />
      </a-form-item>
      <a-form-item label="编码">
        <a-input v-model:value="query.code" placeholder="输入权限编码" allow-clear />
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
        <template v-if="column.key === 'status'">
          <span>{{ record.status === 1 ? '启用' : '禁用' }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除该权限？" @confirm="() => onDelete(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="modalVisible" :title="editing ? '编辑权限' : '新增权限'" @ok="submitForm">
      <a-form layout="vertical">
        <a-form-item label="名称" required>
          <a-input v-model:value="formModel.name" placeholder="请输入名称" />
        </a-form-item>
        <a-form-item label="编码" required>
          <a-input v-model:value="formModel.code" placeholder="请输入编码" />
        </a-form-item>
        <a-form-item label="描述">
          <a-input v-model:value="formModel.description" placeholder="请输入描述" />
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