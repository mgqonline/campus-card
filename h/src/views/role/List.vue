<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { getRoleList, createRole, updateRole, deleteRole, disableRole, getRolePermissions, assignRolePermissions, type Role, getRoleMembers, assignRoleMembers } from '@/api/modules/role'
import { getPermissionList, type PermissionInfo } from '@/api/modules/permission'
import { getUserList, type UserInfo } from '@/api/modules/user'

const loading = ref(false)
const dataSource = ref<Role[]>([])
const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id' },
  { title: '角色名', dataIndex: 'name', key: 'name' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action' }
]

const query = reactive<{ name?: string }>({ name: undefined })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const modalVisible = ref(false)
const editing = ref<null | Role>(null)
const formModel = reactive<{ name: string; description?: string }>({ name: '', description: '' })

// 权限分配
const assignVisible = ref(false)
const assigning = ref(false)
const assignRoleRef = ref<Role | null>(null)
const permLoading = ref(false)
const permOptions = ref<{ key: string; title: string; description?: string }[]>([])
const targetKeys = ref<string[]>([])

// 成员分配
const membersVisible = ref(false)
const membersSubmitting = ref(false)
const membersRoleRef = ref<Role | null>(null)
const membersLoading = ref(false)
const memberOptions = ref<{ key: string; title: string }[]>([])
const memberTargetKeys = ref<string[]>([])

const openCreate = () => {
  editing.value = null
  formModel.name = ''
  formModel.description = ''
  modalVisible.value = true
}

const openEdit = (row: Role) => {
  editing.value = row
  formModel.name = row.name
  formModel.description = row.description || ''
  modalVisible.value = true
}

const submitForm = async () => {
  if (!formModel.name.trim()) {
    return message.warning('请输入角色名')
  }
  try {
    if (editing.value) {
      await updateRole(editing.value.id, { name: formModel.name, description: formModel.description })
      message.success('修改成功')
    } else {
      await createRole({ name: formModel.name, description: formModel.description })
      message.success('新增成功')
    }
    modalVisible.value = false
    fetchData()
  } catch (e) {
    message.info('后端未就绪，已模拟成功')
    modalVisible.value = false
    fetchData()
  }
}

const onDelete = async (row: Role) => {
  try {
    await deleteRole(row.id)
    message.success('删除成功')
    fetchData()
  } catch (e) {
    message.info('后端未就绪，已模拟删除成功')
    fetchData()
  }
}

const onDisable = async (row: Role) => {
  try {
    await disableRole(row.id)
    message.success('禁用成功')
    fetchData()
  } catch (e) {
    message.info('后端未就绪，已模拟禁用成功')
    fetchData()
  }
}

const openAssign = async (row: Role) => {
  assignRoleRef.value = row
  assignVisible.value = true
  permLoading.value = true
  try {
    const [permRes, assigned] = await Promise.all([
      getPermissionList({ page: 1, size: 999 }),
      getRolePermissions(row.id)
    ])
    permOptions.value = (permRes.records || []).map((p: PermissionInfo) => ({ key: String(p.id), title: `${p.name} (${p.code})` }))
    targetKeys.value = (assigned || []).map(id => String(id))
  } catch (e) {
    permOptions.value = [
      { key: '1', title: '查看学生 (student:view)' },
      { key: '2', title: '管理卡片 (card:manage)' }
    ]
    targetKeys.value = []
    message.info('后端未就绪，使用示例权限数据')
  } finally {
    permLoading.value = false
  }
}

const onAssignSubmit = async () => {
  if (!assignRoleRef.value) return
  assigning.value = true
  try {
    await assignRolePermissions(assignRoleRef.value.id, targetKeys.value.map(k => Number(k)))
    message.success('权限分配成功')
    assignVisible.value = false
  } catch (e) {
    message.info('后端未就绪，模拟分配成功')
    assignVisible.value = false
  } finally {
    assigning.value = false
  }
}

const openMembers = async (row: Role) => {
  membersRoleRef.value = row
  membersVisible.value = true
  membersLoading.value = true
  try {
    const [userRes, assigned] = await Promise.all([
      getUserList({ page: 1, size: 999 }),
      getRoleMembers(row.id)
    ])
    memberOptions.value = (userRes.records || []).map((u: UserInfo) => ({ key: String(u.id), title: u.username }))
    memberTargetKeys.value = (assigned || []).map(id => String(id))
  } catch (e) {
    memberOptions.value = [
      { key: '1', title: 'admin' },
      { key: '2', title: 'teacher01' }
    ]
    memberTargetKeys.value = []
    message.info('后端未就绪，使用示例成员数据')
  } finally {
    membersLoading.value = false
  }
}

const onMembersSubmit = async () => {
  if (!membersRoleRef.value) return
  membersSubmitting.value = true
  try {
    await assignRoleMembers(membersRoleRef.value.id, memberTargetKeys.value.map(k => Number(k)))
    message.success('成员分配成功')
    membersVisible.value = false
  } catch (e) {
    message.info('后端未就绪，模拟分配成功')
    membersVisible.value = false
  } finally {
    membersSubmitting.value = false
  }
}

const handleMemberTransferChange = (nextTargetKeys: string[]) => {
  memberTargetKeys.value = nextTargetKeys
}

const handleTransferChange = (nextTargetKeys: string[]) => {
  targetKeys.value = nextTargetKeys
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRoleList({ page: pagination.current, size: pagination.pageSize, name: query.name })
    dataSource.value = res.records
    pagination.total = res.total
  } catch (e) {
    dataSource.value = [
      { id: 1, name: 'ADMIN', status: 1 },
      { id: 2, name: 'TEACHER', status: 1 },
      { id: 3, name: 'FINANCE', status: 1 }
    ]
    pagination.total = dataSource.value.length
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
    <a-page-header title="角色管理" />

    <div style="margin-bottom: 12px; display: flex; gap: 8px;">
      <a-button type="primary" @click="openCreate">新增角色</a-button>
    </div>

    <a-form layout="inline" style="margin-bottom: 16px;">
      <a-form-item label="角色名">
        <a-input v-model:value="query.name" placeholder="请输入角色名关键字" allow-clear />
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
          <a-tag :color="record.status === 0 ? 'red' : 'green'">{{ record.status === 0 ? '禁用' : '启用' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">修改</a-button>
            <a-popconfirm title="确认删除该角色？" @confirm="() => onDelete(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
            <a-popconfirm title="确认禁用该角色？" @confirm="() => onDisable(record)">
              <a-button type="link" danger :disabled="record.status === 0">禁用</a-button>
            </a-popconfirm>
            <a-button type="link" @click="openAssign(record)">权限分配</a-button>
            <a-button type="link" @click="openMembers(record)">成员管理</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="assignVisible" title="权限分配" :confirmLoading="assigning" @ok="onAssignSubmit" @cancel="() => (assignVisible = false)">
      <a-spin :spinning="permLoading">
        <a-transfer
          :data-source="permOptions"
          :target-keys="targetKeys"
          :titles="['未分配','已分配']"
          :list-style="{ width: '45%', height: '300px' }"
          :render="item => item.title"
          @change="handleTransferChange"
        />
      </a-spin>
    </a-modal>

    <a-modal v-model:open="membersVisible" title="成员管理" :confirmLoading="membersSubmitting" @ok="onMembersSubmit" @cancel="() => (membersVisible = false)">
      <a-spin :spinning="membersLoading">
        <a-transfer
          :data-source="memberOptions"
          :target-keys="memberTargetKeys"
          :titles="['未加入','已加入']"
          :list-style="{ width: '45%', height: '300px' }"
          :render="item => item.title"
          @change="handleMemberTransferChange"
        />
      </a-spin>
    </a-modal>

    <a-modal v-model:open="modalVisible" :title="editing ? '修改角色' : '新增角色'" @ok="submitForm" @cancel="() => (modalVisible = false)">
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item label="角色名" required>
          <a-input v-model:value="formModel.name" placeholder="请输入角色名" />
        </a-form-item>
        <a-form-item label="描述">
          <a-input v-model:value="formModel.description" placeholder="可选描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>