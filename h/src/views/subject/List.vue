<template>
  <div class="page-container">
    <div class="page-header">
      <h2>学科管理</h2>
    </div>

    <a-space direction="vertical" style="width: 100%">
      <!-- Filters -->
      <a-form layout="inline">
        <a-form-item label="所属学校">
          <a-select
            v-model:value="query.schoolId"
            placeholder="请选择学校"
            style="min-width: 220px"
            :options="schoolOptions"
            allow-clear
            @change="onSchoolChange"
          />
        </a-form-item>
        <a-form-item label="学科名称">
          <a-input v-model:value="query.name" placeholder="支持模糊搜索" style="min-width: 220px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="resetQuery">重置</a-button>
          </a-space>
        </a-form-item>
        <a-form-item style="margin-left: auto">
          <a-space>
            <a-button type="primary" @click="openCreate">新增学科</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- Table -->
      <a-table
        :data-source="list"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'schoolId'">
            <span>{{ schoolMap[record.schoolId || 0] || record.schoolId }}</span>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-switch :checked="record.status === 1" @change="(checked:boolean) => onToggleStatus(record, checked)" />
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" @click="openEdit(record)">编辑</a-button>
              <a-popconfirm title="确认删除该学科？" @confirm="() => onDelete(record.id)">
                <a-button type="link" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-space>

    <!-- Create Modal -->
    <a-modal v-model:open="createOpen" title="新增学科" :confirm-loading="createLoading" @ok="onCreate" @cancel="closeCreate">
      <a-form :model="createForm" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item label="所属学校" required>
          <a-select v-model:value="createForm.schoolId" :options="schoolOptions" placeholder="请选择学校" />
        </a-form-item>
        <a-form-item label="学科名称" required>
          <a-input v-model:value="createForm.name" placeholder="请输入学科名称，如语文/数学" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="createForm.statusChecked" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- Edit Modal -->
    <a-modal v-model:open="editOpen" title="编辑学科" :confirm-loading="editLoading" @ok="onEdit" @cancel="closeEdit">
      <a-form :model="editForm" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item label="所属学校" required>
          <a-select v-model:value="editForm.schoolId" :options="schoolOptions" placeholder="请选择学校" />
        </a-form-item>
        <a-form-item label="学科名称" required>
          <a-input v-model:value="editForm.name" placeholder="请输入学科名称" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="editForm.statusChecked" />
        </a-form-item>
      </a-form>
    </a-modal>
    <!-- 空态提示：选择了学校但暂无学科 -->
    <a-alert v-if="query.schoolId && !loading && list.length === 0" type="info" show-icon message="当前学校暂无学科" description="请点击右上角“新增学科”按钮添加。" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { listSubjects, createSubject, updateSubject, deleteSubject, type Subject } from '@/api/modules/subject'
import { schoolApi } from '@/api/modules/school'

interface Query {
  schoolId?: number
  name?: string
  page: number
  size: number
}

const loading = ref(false)
const list = ref<Subject[]>([])
const total = ref(0)
const schoolOptions = ref<{ label: string; value: number }[]>([])
const schoolMap = computed<Record<number, string>>(() => {
  const map: Record<number, string> = {}
  schoolOptions.value.forEach(opt => { map[opt.value] = opt.label })
  return map
})

const query = reactive<Query>({ page: 1, size: 10 })

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '50'],
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '学科名称', dataIndex: 'name', key: 'name' },
  { title: '所属学校', dataIndex: 'schoolId', key: 'schoolId', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '操作', key: 'actions', width: 160 },
]

function toStatus(checked: boolean) { return checked ? 1 : 0 }
function fromStatus(status?: number) { return status === 1 }

async function fetchSchools() {
  const res = await schoolApi.getEnabledList()
  schoolOptions.value = (res || []).map((s: any) => ({ label: s.name, value: s.id }))
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listSubjects({ schoolId: query.schoolId, name: query.name, page: query.page, size: query.size })
    list.value = res?.records || []
    total.value = res?.total || 0
    pagination.total = total.value
    pagination.current = query.page
    pagination.pageSize = query.size
  } catch (e: any) {
    // 兜底：后端未就绪时提供示例数据，避免页面报错
    list.value = [
      { id: 1, name: '语文', schoolId: query.schoolId || 1, status: 1 },
      { id: 2, name: '数学', schoolId: query.schoolId || 1, status: 1 },
    ]
    total.value = 2
    pagination.total = total.value
    message.info(e?.message || '后端未就绪，已展示示例数据')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.schoolId = undefined
  query.name = undefined
  query.page = 1
  query.size = 10
  fetchData()
}

function onTableChange(pag: any) {
  query.page = pag.current
  query.size = pag.pageSize
  fetchData()
}

function onSchoolChange() {
  query.page = 1
  fetchData()
}

// Create Modal state
const createOpen = ref(false)
const createLoading = ref(false)
const createForm = reactive<{ schoolId?: number; name: string; statusChecked: boolean }>({ name: '', statusChecked: true })

function openCreate() {
  createForm.schoolId = query.schoolId
  createForm.name = ''
  createForm.statusChecked = true
  createOpen.value = true
}
function closeCreate() { createOpen.value = false }

async function onCreate() {
  if (!createForm.schoolId) {
    message.warning('请先选择学校')
    return
  }
  if (!createForm.name || !createForm.name.trim()) {
    message.warning('请输入学科名称')
    return
  }
  createLoading.value = true
  try {
    await createSubject({ schoolId: createForm.schoolId, name: createForm.name.trim(), status: toStatus(createForm.statusChecked) })
    message.success('新增成功')
    createOpen.value = false
    // 回到第一页，刷新列表
    query.page = 1
    await fetchData()
  } catch (e: any) {
    message.error(e?.message || '新增失败')
  } finally {
    createLoading.value = false
  }
}

// Edit Modal state
const editOpen = ref(false)
const editLoading = ref(false)
const editForm = reactive<{ id?: number; schoolId?: number; name: string; statusChecked: boolean }>({ name: '', statusChecked: true })

function openEdit(rec: Subject) {
  editForm.id = rec.id
  editForm.schoolId = rec.schoolId
  editForm.name = rec.name
  editForm.statusChecked = fromStatus(rec.status)
  editOpen.value = true
}
function closeEdit() { editOpen.value = false }

async function onEdit() {
  if (!editForm.id) return
  if (!editForm.schoolId) {
    message.warning('请选择学校')
    return
  }
  if (!editForm.name || !editForm.name.trim()) {
    message.warning('请输入学科名称')
    return
  }
  editLoading.value = true
  try {
    await updateSubject(editForm.id, { schoolId: editForm.schoolId, name: editForm.name.trim(), status: toStatus(editForm.statusChecked) })
    message.success('更新成功')
    editOpen.value = false
    await fetchData()
  } catch (e: any) {
    message.error(e?.message || '更新失败')
  } finally {
    editLoading.value = false
  }
}

async function onDelete(id?: number) {
  if (!id) return
  try {
    await deleteSubject(id)
    message.success('删除成功')
    // 如果当前页删空了，回退一页
    if (list.value.length <= 1 && query.page > 1) {
      query.page -= 1
    }
    fetchData()
  } catch (e: any) {
    message.error(e?.message || '删除失败')
  }
}

// 新增：状态切换快捷操作
const onToggleStatus = async (rec: Subject, checked: boolean) => {
  try {
    const payload: Subject = { ...rec, status: checked ? 1 : 0 }
    await updateSubject(rec.id!, payload)
    rec.status = checked ? 1 : 0
    message.success(checked ? '已启用' : '已停用')
  } catch (e:any) {
    message.error(e?.message || '状态更新失败')
  }
}

onMounted(async () => {
  await fetchSchools()
  await fetchData()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
</style>