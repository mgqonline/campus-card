<template>
  <div>
    <a-card title="家长管理" :bordered="false">
      <template #extra>
        <a-button type="primary" @click="onCreate">新增家长</a-button>
      </template>
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
          <template v-if="column.key === 'actions'">
            <a-space>
              <a-button size="small" @click="onEdit(record)">编辑</a-button>
              <a-button size="small" @click="openRelation(record)">关联学生</a-button>
              <a-popconfirm title="确认删除该家长？" @confirm="onDelete(record)">
                <a-button size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑 -->
    <a-modal v-model:open="modal.open" :title="modal.title" @ok="submitForm" :confirm-loading="modal.loading">
      <a-form :model="form" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item label="姓名" required>
          <a-input v-model:value="form.name" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="form.phone" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model:value="form.email" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" allow-clear />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 关联学生抽屉 -->
    <a-drawer v-model:open="drawer.open" title="家长与学生关联" width="560px">
      <div v-if="drawer.parent">
        <div style="margin-bottom: 12px;">当前家长：<b>{{ drawer.parent.name }}</b>（ID：{{ drawer.parent.id }}）</div>
        <a-list :data-source="relations" :loading="drawer.loading" bordered>
          <template #renderItem="{ item }">
            <a-list-item>
              <a-space style="width: 100%; justify-content: space-between;">
                <div>
                  学生：<b>{{ item.studentName }}</b>（ID：{{ item.studentId }}）
                  <span style="margin-left: 8px;">关系：</span>
                  <a-select v-model:value="item.relation" :options="relationOptions" style="width: 120px;" @change="(val) => onUpdateRelation(item, val as any)" />
                </div>
                <a-popconfirm title="确认解除关联？" @confirm="onUnlink(item)">
                  <a-button size="small" danger>解除关联</a-button>
                </a-popconfirm>
              </a-space>
            </a-list-item>
          </template>
        </a-list>

        <a-divider>新增关联</a-divider>
        <a-form layout="inline">
          <a-form-item label="学号">
            <a-input v-model:value="linkForm.studentNo" placeholder="请输入学号" />
          </a-form-item>
          <a-form-item label="关系">
            <a-select v-model:value="linkForm.relation" :options="relationOptions" style="width: 140px;" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" :loading="drawer.linkLoading" @click="onLink">绑定</a-button>
          </a-form-item>
        </a-form>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { getParentList, createParent, updateParent, deleteParent, type ParentInfo, type ParentQuery, type ParentStudentItem, getStudentsByParentId, updateParentRelation, unlinkParentFromStudent, linkParentToStudent } from '@/api/modules/parent'
import { getStudentByNo } from '@/api/modules/student'

const loading = ref(false)
const list = ref<ParentInfo[]>([])
const pagination = reactive<TablePaginationConfig>({ current: 1, pageSize: 10, total: 0 })
const query = reactive<ParentQuery>({ page: 1, size: 10, name: '' })

const columns = [
  { title: 'ID', dataIndex: 'id' },
  { title: '姓名', dataIndex: 'name' },
  { title: '手机号', dataIndex: 'phone' },
  { title: '邮箱', dataIndex: 'email' },
  { title: '状态', dataIndex: 'status' },
  { title: '操作', key: 'actions' }
]

const statusOptions = [
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 }
]
const relationOptions = [
  { label: '父亲', value: 'FATHER' },
  { label: '母亲', value: 'MOTHER' },
  { label: '监护人', value: 'GUARDIAN' },
  { label: '其他', value: 'OTHER' }
]

const modal = reactive({ open: false, title: '新增家长', loading: false })
const form = reactive<Partial<ParentInfo>>({ id: undefined, name: '', phone: '', email: '', status: 1 })

const drawer = reactive({ open: false, parent: null as ParentInfo | null, loading: false, linkLoading: false })
const relations = ref<ParentStudentItem[]>([])
const linkForm = reactive<{ studentNo?: string; relation: 'FATHER' | 'MOTHER' | 'GUARDIAN' | 'OTHER' }>({ relation: 'OTHER' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getParentList(query)
    list.value = res.records
    pagination.total = res.total
    pagination.current = query.page
    pagination.pageSize = query.size
  } catch (e: any) {
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const onTableChange = (p: TablePaginationConfig) => {
  query.page = p.current || 1
  query.size = p.pageSize || 10
  loadData()
}

const onCreate = () => {
  Object.assign(form, { id: undefined, name: '', phone: '', email: '', status: 1 })
  modal.title = '新增家长'
  modal.open = true
}

const onEdit = (record: ParentInfo) => {
  Object.assign(form, record)
  modal.title = '编辑家长'
  modal.open = true
}

const submitForm = async () => {
  if (!form.name || !form.name.trim()) {
    return message.warning('请填写姓名')
  }
  modal.loading = true
  try {
    if (form.id) await updateParent(form.id, form)
    else await createParent({ name: form.name!, phone: form.phone, email: form.email, status: form.status })
    message.success('保存成功')
    modal.open = false
    loadData()
  } catch (e: any) {
    message.error(e.message || '保存失败')
  } finally {
    modal.loading = false
  }
}

const onDelete = async (record: ParentInfo) => {
  try {
    await deleteParent(record.id)
    message.success('删除成功')
    loadData()
  } catch (e: any) {
    message.error(e.message || '删除失败')
  }
}

const openRelation = async (record: ParentInfo) => {
  drawer.parent = record
  drawer.open = true
  drawer.loading = true
  try {
    relations.value = await getStudentsByParentId(record.id)
  } catch (e: any) {
    message.error(e.message || '加载关联失败')
  } finally {
    drawer.loading = false
  }
}

const onUpdateRelation = async (item: ParentStudentItem, relation: 'FATHER' | 'MOTHER' | 'GUARDIAN' | 'OTHER') => {
  try {
    await updateParentRelation(item.studentId, drawer.parent!.id, relation)
    message.success('关系已更新')
  } catch (e: any) {
    message.error(e.message || '更新失败')
  }
}

const onUnlink = async (item: ParentStudentItem) => {
  try {
    await unlinkParentFromStudent(item.studentId, drawer.parent!.id)
    relations.value = relations.value.filter((i) => i.studentId !== item.studentId)
    message.success('已解除关联')
  } catch (e: any) {
    message.error(e.message || '解除失败')
  }
}

const onLink = async () => {
  if (!linkForm.studentNo || !linkForm.studentNo.trim()) return message.warning('请填写学号')
  drawer.linkLoading = true
  try {
    const stu = await getStudentByNo(linkForm.studentNo.trim())
    await linkParentToStudent(stu.id, { parentId: drawer.parent!.id, relation: linkForm.relation })
    relations.value = await getStudentsByParentId(drawer.parent!.id)
    message.success('绑定成功')
    linkForm.studentNo = undefined
    linkForm.relation = 'OTHER'
  } catch (e: any) {
    const notFound = e?.status === 404 || e?.code === 404
    const msg = notFound ? '学号不存在或未找到对应学生' : (e?.message || '绑定失败')
    message.error(msg)
  } finally {
    drawer.linkLoading = false
  }
}

onMounted(loadData)
</script>