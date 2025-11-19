<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { message, Upload } from 'ant-design-vue'
import { getTeacherList, createTeacher, updateTeacher, deleteTeacher, type TeacherInfo } from '@/api/modules/teacher'
import { enableTeacher, disableTeacher, importTeachers, getTeacherPhotoUrl, uploadTeacherPhoto, deleteTeacherPhoto, getTeacherArchive, updateTeacherArchive } from '@/api/modules/teacher'
import { listDepartments, type Department } from '@/api/modules/department'
import { listCardTypes, type Card, type CardType } from '@/api/modules/card'
import { listTeacherCards, issueTeacherCard, listTeacherFaces, listHeadClassesByTeacher, listSubjectClassesByTeacher, type TeacherCardIssueReq } from '@/api/modules/teacher'

const loading = ref(false)
const dataSource = ref<TeacherInfo[]>([])
const columns = [
  { title: '工号', dataIndex: 'teacherNo', key: 'teacherNo', width: 140 },
  { title: '姓名', dataIndex: 'name', key: 'name', width: 140 },
  { title: '部门', dataIndex: 'department', key: 'department', width: 180 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', fixed: 'right', width: 360 }
]

const query = reactive<{ name?: string; department?: string }>({ name: undefined, department: undefined })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const modalVisible = ref(false)
const editing = ref<null | TeacherInfo>(null)
const formModel = reactive<{ name: string; teacherNo: string; department?: string; phone?: string; status?: number }>({ name: '', teacherNo: '', department: '', phone: '', status: 1 })

const openCreate = () => {
  editing.value = null
  formModel.name = ''
  formModel.teacherNo = ''
  formModel.department = ''
  formModel.phone = ''
  formModel.status = 1
  modalVisible.value = true
}

const openEdit = (row: TeacherInfo) => {
  editing.value = row
  formModel.name = row.name
  formModel.teacherNo = row.teacherNo
  formModel.department = row.department
  formModel.phone = row.phone
  formModel.status = row.status ?? 1
  modalVisible.value = true
}

const submitForm = async () => {
  try {
    if (!formModel.name || !formModel.teacherNo) {
      message.warning('请填写姓名和工号')
      return
    }
    if (editing.value) {
      await updateTeacher(editing.value.id, { ...formModel })
      message.success('更新成功')
    } else {
      await createTeacher({ ...formModel, id: 0 } as any)
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

const onDelete = async (row: TeacherInfo) => {
  try {
    await deleteTeacher(row.id)
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
    const res = await getTeacherList({ page: pagination.current, size: pagination.pageSize, name: query.name, department: query.department })
    // 校验返回结构，避免因后端结构不一致导致运行时错误
    if (!res || !Array.isArray((res as any).records)) throw new Error('数据结构不符合预期')
    dataSource.value = (res as any).records
    pagination.total = Number((res as any).total) || dataSource.value.length
  } catch (e: any) {
    dataSource.value = [
      { id: 1, teacherNo: 'T2024001', name: '王老师', department: '数学组', phone: '13811112222', status: 1 },
      { id: 2, teacherNo: 'T2024002', name: '李老师', department: '语文组', phone: '13933334444', status: 1 }
    ]
    pagination.total = 2
    message.info(e?.message ? `加载失败：${e.message}，已展示示例数据` : '后端未就绪，已展示示例数据')
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
  query.department = undefined
  pagination.current = 1
  fetchData()
}
const handlePageChange = (page: number, pageSize?: number) => {
  pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  fetchData()
}

const departmentOptions = ref<Department[]>([])
const loadDepartments = async () => {
  try {
    const res = await listDepartments({ page: 1, size: 200, status: 1 })
    departmentOptions.value = res.records || []
  } catch (e) {
    departmentOptions.value = [
      { id: 1, name: '语文组', status: 1 },
      { id: 2, name: '数学组', status: 1 },
      { id: 3, name: '英语组', status: 1 }
    ]
  }
}

onMounted(() => { fetchData(); loadDepartments(); loadCardTypes() })

const importVisible = ref(false)
const importFile = ref<File | null>(null)
const importUploading = ref(false)

const openImport = () => {
  importFile.value = null
  importVisible.value = true
}
const beforeImportUpload = (file: File) => {
  importFile.value = file
  return false
}
const submitImport = async () => {
  if (!importFile.value) {
    message.warning('请选择文件')
    return
  }
  importUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', importFile.value)
    await importTeachers(fd)
    message.success('导入成功')
    importVisible.value = false
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟导入成功')
    importVisible.value = false
    fetchData()
  } finally {
    importUploading.value = false
  }
}
const cancelImport = () => {
  importVisible.value = false
}

const downloadImportTemplate = () => {
  const rows = [
    ['teacherNo','name','department','phone','status'],
    ['T2025001','张老师', departmentOptions.value[0]?.name || '语文组','13800000000','1']
  ]
  const csv = rows.map(r => r.map(v => `"${String(v).replace(/"/g,'""')}"`).join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '教师导入模板.csv'
  a.click()
  URL.revokeObjectURL(url)
}
const photoVisible = ref(false)
const photoTeacherId = ref<number | null>(null)
const photoUploading = ref(false)

const openPhoto = (row: TeacherInfo) => {
  photoTeacherId.value = row.id
  photoVisible.value = true
}
const beforePhotoUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('请上传图片文件')
    return Upload.LIST_IGNORE as any
  }
  return false
}
const submitPhoto = async () => {
  if (!photoTeacherId.value || !(uploadRef as any)?.fileList?.[0]?.originFileObj) {
    message.warning('请选择照片文件')
    return
  }
  photoUploading.value = true
  try {
    const file = (uploadRef as any).fileList[0].originFileObj as File
    await uploadTeacherPhoto(photoTeacherId.value, file)
    message.success('上传成功')
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟上传成功')
    fetchData()
  } finally {
    photoUploading.value = false
  }
}
const deletePhotoHandler = async () => {
  if (!photoTeacherId.value) return
  try {
    await deleteTeacherPhoto(photoTeacherId.value)
    message.success('已删除照片')
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟删除成功')
    fetchData()
  }
}
const cancelPhoto = () => {
  photoVisible.value = false
}
const uploadRef = ref()

const archiveOpen = ref(false)
const archiveTeacherId = ref<number | null>(null)
const archiveLoading = ref(false)
const archiveContent = ref<string>('')

const openArchive = async (row: TeacherInfo) => {
  archiveTeacherId.value = row.id
  archiveOpen.value = true
  archiveLoading.value = true
  try {
    const content = await getTeacherArchive(row.id)
    archiveContent.value = content || ''
  } catch (e) {
    archiveContent.value = '{\n  "entryDate": "2020-09-01",\n  "title": "高级教师",\n  "notes": "示例档案"\n}'
  } finally {
    archiveLoading.value = false
  }
}
const saveArchive = async () => {
  if (!archiveTeacherId.value) return
  try {
    await updateTeacherArchive(archiveTeacherId.value, archiveContent.value)
    message.success('档案已保存')
    archiveOpen.value = false
  } catch (e) {
    message.info('后端未就绪，模拟保存成功')
    archiveOpen.value = false
  }
}
const cancelArchive = () => {
  archiveOpen.value = false
}

const toggleStatus = async (row: TeacherInfo) => {
  try {
    if ((row.status ?? 1) === 1) {
      await disableTeacher(row.id)
      message.success('已设置为离职')
    } else {
      await enableTeacher(row.id)
      message.success('已设置为在职')
    }
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟状态切换成功')
    row.status = (row.status ?? 1) === 1 ? 0 : 1
    fetchData()
  }
}

// 教师卡片/人脸/关联 功能所需的状态与方法
const selectedTeacherId = ref<number | null>(null)

// 卡片相关
const cardDrawerOpen = ref(false)
const cardLoading = ref(false)
const cardList = ref<Card[]>([])
const cardTypes = ref<CardType[]>([])
const issueTeacherCardForm = reactive<TeacherCardIssueReq & { note?: string }>({ typeId: undefined as any, initialBalance: 0, note: '' })

const loadCardTypes = async () => {
  try {
    const res = await listCardTypes()
    cardTypes.value = res || []
  } catch (e) {
    cardTypes.value = []
  }
}

const refreshTeacherCards = async () => {
  if (!selectedTeacherId.value) return
  cardLoading.value = true
  try {
    const res = await listTeacherCards(selectedTeacherId.value)
    cardList.value = res || []
  } catch (e) {
    cardList.value = []
  } finally {
    cardLoading.value = false
  }
}

const submitIssueTeacherCard = async () => {
  if (!selectedTeacherId.value) return
  if (!issueTeacherCardForm.typeId) {
    message.warning('请选择卡种')
    return
  }
  try {
    await issueTeacherCard(selectedTeacherId.value, issueTeacherCardForm)
    message.success('发卡成功')
    await refreshTeacherCards()
  } catch (e) {
    message.info('后端未就绪，模拟发卡成功')
    await refreshTeacherCards()
  }
}

const openCards = async (row: TeacherInfo) => {
  selectedTeacherId.value = row.id
  await loadCardTypes()
  cardDrawerOpen.value = true
  refreshTeacherCards()
}

// 人脸相关
const facesDrawerOpen = ref(false)
const facesLoading = ref(false)
const facesList = ref<any[]>([])

const openFaces = async (row: TeacherInfo) => {
  selectedTeacherId.value = row.id
  facesDrawerOpen.value = true
  facesLoading.value = true
  try {
    const res = await listTeacherFaces(row.id)
    facesList.value = res || []
  } catch (e) {
    facesList.value = []
  } finally {
    facesLoading.value = false
  }
}

// 关联相关
const assocDrawerOpen = ref(false)
const headClasses = ref<any[]>([])
const subjectClasses = ref<any[]>([])

const openAssociations = async (row: TeacherInfo) => {
  selectedTeacherId.value = row.id
  assocDrawerOpen.value = true
  try {
    const heads = await listHeadClassesByTeacher(row.id)
    headClasses.value = heads || []
  } catch (e) {
    headClasses.value = []
  }
  try {
    const subs = await listSubjectClassesByTeacher(row.id)
    subjectClasses.value = subs || []
  } catch (e) {
    subjectClasses.value = []
  }
}

</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="教师管理" />

    <div style="margin-bottom: 12px; display: flex; gap: 8px;">
      <a-button type="primary" @click="openCreate">新增教师</a-button>
      <a-button @click="openImport">批量导入</a-button>
    </div>

    <a-form layout="inline" style="margin-bottom: 16px;">
      <a-form-item label="姓名">
        <a-input v-model:value="query.name" placeholder="请输入姓名关键字" allow-clear />
      </a-form-item>
      <a-form-item label="部门">
        <a-select v-model:value="query.department" placeholder="选择部门" allow-clear style="width: 160px" :show-search="true" :filter-option="(input, option) => (option?.value ?? '').toLowerCase().includes(input.toLowerCase())">
          <a-select-option v-for="d in departmentOptions" :key="d.id" :value="d.name">{{ d.name }}</a-select-option>
        </a-select>
        <a-button type="link" @click="$router.push('/department/list')">管理部门</a-button>
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
      :scroll="{ x: 'max-content' }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <span>{{ record.status === 1 ? '在职' : '离职' }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除该教师？" @confirm="() => onDelete(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
            <a-divider type="vertical" />
            <a-button type="link" @click="openPhoto(record)">照片</a-button>
            <a-button type="link" @click="openArchive(record)">档案</a-button>
            <a-button type="link" :danger="(record.status ?? 1) === 1" @click="toggleStatus(record)">
              {{ (record.status ?? 1) === 1 ? '离职' : '在职' }}
            </a-button>
            <a-divider type="vertical" />
            <a-button type="link" @click="openCards(record)">卡片</a-button>
            <a-button type="link" @click="openFaces(record)">人脸</a-button>
            <a-button type="link" @click="openAssociations(record)">关联</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="importVisible" title="批量导入教师" :confirm-loading="importUploading" @ok="submitImport" @cancel="cancelImport">
      <a-upload :beforeUpload="beforeImportUpload" :maxCount="1" :accept="'.xls,.xlsx,.csv,.tsv'">
        <a-button>选择文件</a-button>
      </a-upload>
      <div style="margin-top: 8px; color: #999;">支持 .xls/.xlsx/.csv/.tsv</div>
      <a-button type="link" @click="downloadImportTemplate">下载模板</a-button>
    </a-modal>

    <a-modal v-model:open="photoVisible" title="教师照片" :confirm-loading="photoUploading" @ok="submitPhoto" @cancel="cancelPhoto">
      <div v-if="photoTeacherId" style="margin-bottom: 12px;">
        <img :src="getTeacherPhotoUrl(photoTeacherId)" alt="教师照片" style="width: 120px; height: 120px; object-fit: cover; border: 1px solid #eee;" />
      </div>
      <a-upload ref="uploadRef" :beforeUpload="beforePhotoUpload" :maxCount="1" :accept="'image/*'">
        <a-button>选择新照片</a-button>
      </a-upload>
      <div style="margin-top: 12px;">
        <a-popconfirm title="确认删除该教师照片？" @confirm="deletePhotoHandler">
          <a-button danger>删除照片</a-button>
        </a-popconfirm>
      </div>
    </a-modal>

    <a-drawer v-model:open="archiveOpen" title="教师档案" width="560" :maskClosable="true" @close="cancelArchive">
      <a-spin :spinning="archiveLoading">
        <a-form layout="vertical">
          <a-form-item label="档案内容">
            <a-textarea v-model:value="archiveContent" :rows="12" placeholder="可填写文本或JSON" />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="saveArchive">保存</a-button>
              <a-button @click="cancelArchive">取消</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-spin>
    </a-drawer>

    <a-modal v-model:open="modalVisible" :title="editing ? '编辑教师' : '新增教师'" @ok="submitForm">
      <a-form layout="vertical">
        <a-form-item label="姓名" required>
          <a-input v-model:value="formModel.name" placeholder="请输入姓名" />
        </a-form-item>
        <a-form-item label="工号" required>
          <a-input v-model:value="formModel.teacherNo" placeholder="请输入工号" />
        </a-form-item>
        <a-form-item label="部门">
          <a-select v-model:value="formModel.department" placeholder="选择部门" :show-search="true" :filter-option="(input, option) => (option?.value ?? '').toLowerCase().includes(input.toLowerCase())">
            <a-select-option v-for="d in departmentOptions" :key="d.id" :value="d.name">{{ d.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="formModel.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="formModel.status" style="width: 160px">
            <a-select-option :value="1">在职</a-select-option>
            <a-select-option :value="0">离职</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer v-model:open="assocDrawerOpen" title="教师关联" width="640" :maskClosable="true">
      <h4>担任班主任的班级</h4>
      <a-table :data-source="headClasses" row-key="id" size="small" style="margin-bottom: 16px;">
        <a-table-column title="班级ID" data-index="id" key="id" />
        <a-table-column title="班级名称" data-index="name" key="name" />
        <a-table-column title="年级ID" data-index="gradeId" key="gradeId" />
        <a-table-column title="学校ID" data-index="schoolId" key="schoolId" />
      </a-table>
      <h4>任教的班级-学科</h4>
      <a-table :data-source="subjectClasses" row-key="id" size="small">
        <a-table-column title="班级ID" data-index="classId" key="classId" />
        <a-table-column title="学科ID" data-index="subjectId" key="subjectId" />
      </a-table>
    </a-drawer>

  </div>
</template>