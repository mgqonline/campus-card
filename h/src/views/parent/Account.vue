<template>
  <div>
    <a-card title="家长账号管理" :bordered="false">
      <a-form layout="inline" @submit.prevent>
        <a-form-item label="姓名">
          <a-input v-model:value="query.name" placeholder="请输入姓名关键词" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="loadData">查询</a-button>
        </a-form-item>
      </a-form>
      <a-table :data-source="list" :columns="columns" :loading="loading" row-key="id" :pagination="pagination" @change="onTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">{{ record.status === 1 ? '启用' : '禁用' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'wechat'">
            <div v-if="wechatMap[record.id]">
              <a-avatar :src="getAvatarSrc(record.id)" size="small" style="margin-right: 6px" @error="onAvatarError(record.id)"></a-avatar>
              <span>{{ wechatMap[record.id].nickname || wechatMap[record.id].openId }}</span>
            </div>
            <div v-else>未绑定</div>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button size="small" @click="toggleStatus(record)">{{ record.status === 1 ? '禁用' : '启用' }}</a-button>
              <a-button size="small" @click="openPermissions(record)">权限设置</a-button>
              <a-button size="small" @click="openWechat(record)">微信绑定</a-button>
              <a-button size="small" @click="openChildren(record)">多子女关联</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 权限设置弹窗 -->
    <a-modal v-model:open="permModal.open" title="家长权限设置" :confirm-loading="permModal.loading" @ok="savePermissions">
      <a-form :model="permForm" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="查看考勤">
          <a-switch v-model:checked="permForm.viewAttendance" />
        </a-form-item>
        <a-form-item label="查看消费">
          <a-switch v-model:checked="permForm.viewConsumption" />
        </a-form-item>
        <a-form-item label="查看成绩">
          <a-switch v-model:checked="permForm.viewGrades" />
        </a-form-item>
        <a-form-item label="联系老师">
          <a-switch v-model:checked="permForm.messageTeacher" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 微信绑定弹窗 -->
    <a-modal v-model:open="wechatModal.open" :title="wechatModal.title" :confirm-loading="wechatModal.loading" @ok="saveWechat">
      <template #footer>
        <a-space>
          <a-button @click="wechatModal.open = false">取消</a-button>
          <a-popconfirm title="确认解绑当前微信？" v-if="wechatForm.parentId && wechatForm.openId" @confirm="doUnbind">
            <a-button danger>解绑</a-button>
          </a-popconfirm>
          <a-button type="primary" @click="saveWechat">保存</a-button>
        </a-space>
      </template>
      <a-form :model="wechatForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="OpenID" required>
          <a-input v-model:value="wechatForm.openId" placeholder="微信OpenID" />
        </a-form-item>
        <a-form-item label="UnionID">
          <a-input v-model:value="wechatForm.unionId" placeholder="微信UnionID（可选）" />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model:value="wechatForm.nickname" placeholder="微信昵称（可选）" />
        </a-form-item>
        <a-form-item label="头像URL">
          <a-input v-model:value="wechatForm.avatarUrl" placeholder="头像链接（可选）" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 多子女关联抽屉 -->
    <a-drawer v-model:open="childrenDrawer.open" title="多子女关联管理" width="560">
      <div v-if="currentParent">
        <p>家长：{{ currentParent.name }}（ID: {{ currentParent.id }}）</p>
        <a-table :data-source="childrenList" :columns="childColumns" row-key="studentId" size="small" />
        <a-divider />
        <a-alert type="info" message="如需新增关联，请到学生管理页面在该学生下添加家长关联。" show-icon />
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { getParentList, type ParentInfo, type ParentQuery, enableParent, disableParent, getParentPermissions, updateParentPermissions, type ParentPermission, getParentWechat, bindParentWechat, unbindParentWechat, type ParentWechat, getStudentsByParentId, type ParentStudentItem } from '@/api/modules/parent'

const query = reactive<ParentQuery>({ page: 1, size: 10, name: '' })
const list = ref<ParentInfo[]>([])
const loading = ref(false)
const total = ref(0)
const pagination = reactive<TablePaginationConfig>({ current: 1, pageSize: 10, total: 0 })

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '姓名', dataIndex: 'name', key: 'name' },
  { title: '电话', dataIndex: 'phone', key: 'phone' },
  { title: '状态', key: 'status', width: 100 },
  { title: '微信', key: 'wechat' },
  { title: '操作', key: 'actions', width: 280 }
]

const wechatMap = reactive<Record<number, ParentWechat | undefined>>({})

const loadWechat = async (parentId: number) => {
  try {
    wechatMap[parentId] = await getParentWechat(parentId)
  } catch (e) {
    wechatMap[parentId] = undefined
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getParentList(query)
    list.value = res.records
    total.value = res.total
    pagination.total = res.total
    // 预取微信绑定信息
    for (const p of list.value) {
      loadWechat(p.id)
    }
  } catch (e: any) {
    message.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const onTableChange = (pg: TablePaginationConfig) => {
  pagination.current = pg.current || 1
  pagination.pageSize = pg.pageSize || 10
  query.page = pagination.current
  query.size = pagination.pageSize
  loadData()
}

onMounted(loadData)

// 状态切换
const toggleStatus = async (p: ParentInfo) => {
  try {
    if (p.status === 1) {
      await disableParent(p.id)
      p.status = 0
    } else {
      await enableParent(p.id)
      p.status = 1
    }
    message.success('操作成功')
  } catch (e: any) {
    message.error(e?.message || '操作失败')
  }
}

// 权限设置
const permModal = reactive({ open: false, loading: false })
const permForm = reactive<ParentPermission>({ parentId: 0, viewAttendance: true, viewConsumption: true, viewGrades: true, messageTeacher: true })
const currentParent = ref<ParentInfo | null>(null)
const openPermissions = async (p: ParentInfo) => {
  try {
    currentParent.value = p
    const perms = await getParentPermissions(p.id)
    permForm.parentId = p.id
    permForm.viewAttendance = !!perms.viewAttendance
    permForm.viewConsumption = !!perms.viewConsumption
    permForm.viewGrades = !!perms.viewGrades
    permForm.messageTeacher = !!perms.messageTeacher
    permModal.open = true
  } catch (e: any) {
    message.error(e?.message || '加载权限失败')
  }
}
const savePermissions = async () => {
  if (!currentParent.value) return
  permModal.loading = true
  try {
    await updateParentPermissions(currentParent.value.id, permForm)
    message.success('保存成功')
    permModal.open = false
  } catch (e: any) {
    message.error(e?.message || '保存失败')
  } finally {
    permModal.loading = false
  }
}

// 微信绑定
  const wechatModal = reactive({ open: false, loading: false, title: '微信绑定' })
  const wechatForm = reactive<ParentWechat>({ parentId: 0, openId: '', unionId: '', nickname: '', avatarUrl: '', status: 1, bindTime: '' })
  const isValidUrl = (u?: string) => {
    if (!u) return false
    try {
      const url = new URL(u)
      return url.protocol === 'http:' || url.protocol === 'https:'
    } catch {
      return false
    }
  }
  const getAvatarSrc = (parentId: number) => {
    // 统一使用本地占位头像，避免外链被 ORB 阻止
    return '/avatar-placeholder.jpg'
  }
  const onAvatarError = (parentId: number) => {
    const w = wechatMap[parentId]
    if (w) w.avatarUrl = '/avatar-placeholder.jpg'
    return false
  }
const openWechat = async (p: ParentInfo) => {
  currentParent.value = p
  wechatModal.title = `家长（${p.name}）微信绑定`
  try {
    const w = await getParentWechat(p.id)
    wechatForm.parentId = p.id
    wechatForm.openId = w.openId
    wechatForm.unionId = w.unionId
    wechatForm.nickname = w.nickname
    wechatForm.avatarUrl = w.avatarUrl
  } catch (e) {
    wechatForm.parentId = p.id
    wechatForm.openId = ''
    wechatForm.unionId = ''
    wechatForm.nickname = ''
    wechatForm.avatarUrl = ''
  }
  wechatModal.open = true
}
const saveWechat = async () => {
  if (!currentParent.value) return
  if (!wechatForm.openId) return message.error('OpenID不能为空')
  wechatModal.loading = true
  try {
    const w = await bindParentWechat(currentParent.value.id, {
      openId: wechatForm.openId,
      unionId: wechatForm.unionId,
      nickname: wechatForm.nickname,
      avatarUrl: wechatForm.avatarUrl
    })
    wechatMap[currentParent.value.id] = w
    message.success('绑定成功')
    wechatModal.open = false
  } catch (e: any) {
    message.error(e?.message || '绑定失败')
  } finally {
    wechatModal.loading = false
  }
}
const doUnbind = async () => {
  if (!currentParent.value) return
  try {
    await unbindParentWechat(currentParent.value.id)
    wechatMap[currentParent.value.id] = undefined
    message.success('解绑成功')
    wechatModal.open = false
  } catch (e: any) {
    message.error(e?.message || '解绑失败')
  }
}

// 多子女关联管理
const childrenDrawer = reactive({ open: false })
const childrenList = ref<ParentStudentItem[]>([])
const childColumns = [
  { title: '学生ID', dataIndex: 'studentId' },
  { title: '学生姓名', dataIndex: 'studentName' },
  { title: '关系', dataIndex: 'relation' },
  { title: '班级ID', dataIndex: 'classId' },
]
const openChildren = async (p: ParentInfo) => {
  currentParent.value = p
  childrenDrawer.open = true
  try {
    childrenList.value = await getStudentsByParentId(p.id)
  } catch (e) {
    childrenList.value = []
  }
}
</script>