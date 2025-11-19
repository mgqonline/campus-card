<!-- 重复的模板已移除，以下使用统一的页面结构 -->
<script setup lang="ts">
import { onMounted, ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import { getUserList, disableUser, enableUser, createUser, getUserDetail, type UserInfo, getUserRoles, assignUserRoles } from '@/api/modules/user'
import { getRoleList, type Role } from '@/api/modules/role'
import { updateUser, resetUserPassword } from '@/api/modules/user'

const loading = ref(false)
const dataSource = ref<UserInfo[]>([])
const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 100 },
  { title: '用户名', dataIndex: 'username', key: 'username', width: 160 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '角色', dataIndex: 'roles', key: 'roles', width: 200 },
  { title: '操作', key: 'action', fixed: 'right', width: 260 }
]

const query = reactive<{ username?: string }>({ username: undefined })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

// 角色分配相关状态
const assignVisible = ref(false)
const assignLoading = ref(false)
const currentUser = ref<UserInfo | null>(null)
const allRoles = ref<Role[]>([])
const selectedRoleKeys = ref<string[]>([])
const roleTransferData = computed(() => allRoles.value.map(r => ({ key: String(r.id), title: r.name })))

// 新增用户相关状态
const createVisible = ref(false)
const creating = ref(false)
const createForm = reactive<{ username: string; password: string; phone?: string; roleIds: string[] }>({ username: '', password: '', phone: '', roleIds: [] })

// 查看详情相关状态
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailInfo = ref<UserInfo | null>(null)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserList({ page: pagination.current, size: pagination.pageSize, username: query.username })
    dataSource.value = res.records
    pagination.total = res.total
  } catch (e) {
    // 后端异常时展示示例数据
    dataSource.value = [
      { id: 1, username: 'admin', status: 1, phone: '13800000000', roles: [{ id: 1, name: 'ADMIN' }] },
      { id: 2, username: 'teacher', status: 1, phone: '13900000000', roles: [{ id: 2, name: 'TEACHER' }] }
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
  query.username = undefined
  pagination.current = 1
  fetchData()
}
const handlePageChange = (page: number, pageSize?: number) => {
  pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  fetchData()
}

const onDisable = async (row: UserInfo) => {
  try {
    await disableUser(row.id)
    message.success('禁用成功')
    fetchData()
  } catch (e) {
    message.error('禁用失败')
  }
}

const onEnable = async (row: UserInfo) => {
  try {
    await enableUser(row.id)
    message.success('启用成功')
    fetchData()
  } catch (e) {
    message.error('启用失败')
  }
}

const openAssign = async (row: UserInfo) => {
  currentUser.value = row
  assignLoading.value = true
  try {
    if (allRoles.value.length === 0) {
      const res = await getRoleList({ page: 1, size: 1000 })
      allRoles.value = res.records
    }
    const roleIds = await getUserRoles(row.id)
    selectedRoleKeys.value = roleIds.map(id => String(id))
    assignVisible.value = true
  } catch (e) {
    message.info('后端未就绪，使用示例数据')
    if (allRoles.value.length === 0) {
      allRoles.value = [
        { id: 1, name: 'ADMIN' },
        { id: 2, name: 'TEACHER' },
        { id: 3, name: 'FINANCE' }
      ]
    }
    selectedRoleKeys.value = (row.roles || []).map(r => String(r.id))
    assignVisible.value = true
  } finally {
    assignLoading.value = false
  }
}

const onAssignSubmit = async () => {
  if (!currentUser.value) return
  assignLoading.value = true
  try {
    await assignUserRoles(currentUser.value.id, selectedRoleKeys.value.map(k => Number(k)))
    message.success('角色分配成功')
    assignVisible.value = false
    currentUser.value = null
    fetchData()
  } catch (e) {
    message.error('角色分配失败')
  } finally {
    assignLoading.value = false
  }
}

const onAssignCancel = () => {
  assignVisible.value = false
  currentUser.value = null
}

const openCreate = async () => {
  try {
    if (allRoles.value.length === 0) {
      const res = await getRoleList({ page: 1, size: 1000 })
      allRoles.value = res.records
    }
  } catch (e) {
    // 兜底：提供示例角色
    if (allRoles.value.length === 0) {
      allRoles.value = [
        { id: 1, name: 'ADMIN' },
        { id: 2, name: 'TEACHER' }
      ] as any
    }
  }
  createForm.username = ''
  createForm.password = ''
  createForm.phone = ''
  createForm.roleIds = []
  createVisible.value = true
}

const onCreateSubmit = async () => {
  if (!createForm.username || !createForm.password) {
    message.warning('请填写用户名和密码')
    return
  }
  creating.value = true
  try {
    await createUser({
      username: createForm.username.trim(),
      password: createForm.password,
      phone: createForm.phone?.trim() || undefined,
      roleIds: createForm.roleIds.map(k => Number(k))
    })
    message.success('创建成功')
    createVisible.value = false
    fetchData()
  } catch (e) {
    message.error('创建失败')
  } finally {
    creating.value = false
  }
}

const onCreateCancel = () => {
  createVisible.value = false
}

const openDetail = async (row: UserInfo) => {
  detailLoading.value = true
  try {
    const info = await getUserDetail(row.id)
    detailInfo.value = info
    detailVisible.value = true
  } catch (e) {
    message.error('获取详情失败')
  } finally {
    detailLoading.value = false
  }
}

const onDetailClose = () => {
  detailVisible.value = false
  detailInfo.value = null
}

// 编辑用户相关状态与方法
const editVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive<{ username?: string; phone?: string }>({ username: '', phone: '' })

const openEdit = (row: UserInfo) => {
  currentUser.value = row
  editForm.username = row.username
  editForm.phone = row.phone || ''
  editVisible.value = true
}

const onEditSubmit = async () => {
  if (!currentUser.value) return
  editLoading.value = true
  try {
    await updateUser(currentUser.value.id, {
      username: editForm.username?.trim() || undefined,
      phone: editForm.phone?.trim() || undefined,
    })
    message.success('更新成功')
    editVisible.value = false
    currentUser.value = null
    fetchData()
  } catch (e) {
    message.error('更新失败')
  } finally {
    editLoading.value = false
  }
}

const onEditCancel = () => {
  editVisible.value = false
  currentUser.value = null
}
const onResetCancel = () => {
  resetVisible.value = false
  currentUser.value = null
}

// 重置密码相关状态与方法
const resetVisible = ref(false)
const resetLoading = ref(false)
const resetForm = reactive<{ newPassword: string }>({ newPassword: '' })

const openReset = (row: UserInfo) => {
  currentUser.value = row
  resetForm.newPassword = ''
  resetVisible.value = true
}

const onResetSubmit = async () => {
  if (!currentUser.value) return
  if (!resetForm.newPassword || resetForm.newPassword.trim() === '') {
    message.warning('请输入新密码')
    return
  }
  resetLoading.value = true
  try {
    await resetUserPassword(currentUser.value.id, resetForm.newPassword)
    message.success('密码已重置')
    resetVisible.value = false
    currentUser.value = null
  } catch (e) {
    message.error('重置失败')
  } finally {
    resetLoading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="用户管理" />

    <div style="margin-bottom: 12px; display: flex; gap: 8px;">
      <a-button type="primary" @click="openCreate">新增用户</a-button>
    </div>

    <a-form layout="inline" style="margin-bottom: 16px;">
      <a-form-item label="用户名">
        <a-input v-model:value="query.username" placeholder="请输入用户名关键字" allow-clear />
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
      :scroll="{ x: 'max-content' }"
      @change="(p:any) => handlePageChange(p.current, p.pageSize)"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <span>{{ record.status === 1 ? '启用' : '禁用' }}</span>
        </template>
        <template v-else-if="column.key === 'roles'">
          <span>{{ (record.roles && record.roles.length) ? record.roles.map(r => r.name).join(', ') : '-' }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <template v-if="record.status === 1">
            <a-popconfirm title="确认禁用该用户？" @confirm="() => onDisable(record)">
              <a-button type="link">禁用</a-button>
            </a-popconfirm>
          </template>
          <template v-else>
            <a-popconfirm title="确认启用该用户？" @confirm="() => onEnable(record)">
              <a-button type="link">启用</a-button>
            </a-popconfirm>
          </template>
          <a-divider type="vertical" />
          <a-button type="link" @click="openAssign(record)">角色分配</a-button>
          <a-divider type="vertical" />
          <a-button type="link" @click="openDetail(record)">详情</a-button>
          <a-divider type="vertical" />
          <a-button type="link" @click="openEdit(record)">编辑</a-button>
          <a-divider type="vertical" />
          <a-button type="link" @click="openReset(record)">重置密码</a-button>
        </template>
      </template>
    </a-table>

    <!-- 角色分配弹窗 -->
    <a-modal
      v-model:open="assignVisible"
      title="角色分配"
      :confirmLoading="assignLoading"
      @ok="onAssignSubmit"
      @cancel="onAssignCancel"
      okText="保存"
      cancelText="取消"
    >
      <a-checkbox-group v-model:value="selectedRoleKeys">
        <a-space direction="vertical">
          <a-checkbox v-for="r in allRoles" :key="r.id" :value="String(r.id)">{{ r.name }}</a-checkbox>
        </a-space>
      </a-checkbox-group>
    </a-modal>

    <!-- 用户详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      :confirmLoading="detailLoading"
      title="用户详情"
      okText="关闭"
      cancelText="取消"
      @ok="onDetailClose"
      @cancel="onDetailClose"
    >
      <a-descriptions bordered :column="1" v-if="detailInfo">
        <a-descriptions-item label="ID">{{ detailInfo.id }}</a-descriptions-item>
        <a-descriptions-item label="用户名">{{ detailInfo.username }}</a-descriptions-item>
        <a-descriptions-item label="手机号">{{ detailInfo.phone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ detailInfo.status === 1 ? '启用' : '禁用' }}</a-descriptions-item>
        <a-descriptions-item label="角色">{{ (detailInfo.roles && detailInfo.roles.length) ? detailInfo.roles.map(r => r.name).join(', ') : '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 新增用户弹窗 -->
    <a-modal
      v-model:open="createVisible"
      :confirmLoading="creating"
      title="新增用户"
      okText="创建"
      cancelText="取消"
      @ok="onCreateSubmit"
      @cancel="onCreateCancel"
    >
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item label="用户名" required>
          <a-input v-model:value="createForm.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="密码" required>
          <a-input-password v-model:value="createForm.password" placeholder="请输入密码" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="createForm.phone" placeholder="可选手机号" />
        </a-form-item>
        <a-form-item label="角色">
          <a-select v-model:value="createForm.roleIds" mode="multiple" placeholder="可选角色">
            <a-select-option v-for="r in allRoles" :key="r.id" :value="String(r.id)">{{ r.name }}</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 编辑用户弹窗 -->
    <a-modal
      v-model:open="editVisible"
      :confirmLoading="editLoading"
      title="编辑用户"
      okText="保存"
      cancelText="取消"
      @ok="onEditSubmit"
      @cancel="onEditCancel"
    >
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item label="用户名">
          <a-input v-model:value="editForm.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="editForm.phone" placeholder="请输入手机号" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 重置密码弹窗 -->
    <a-modal
      v-model:open="resetVisible"
      :confirmLoading="resetLoading"
      title="重置密码"
      okText="重置"
      cancelText="取消"
      @ok="onResetSubmit"
      @cancel="onResetCancel"
    >
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item label="新密码" required>
          <a-input-password v-model:value="resetForm.newPassword" placeholder="请输入新密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>