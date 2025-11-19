<script setup lang="ts">
import { onMounted, ref, reactive, watch } from 'vue'
import { message } from 'ant-design-vue'
import { getStudentList, type StudentInfo, createStudent, updateStudent, deleteStudent, enableStudent, disableStudent, graduateStudent, importStudents, transferStudents, getStudentFace, setStudentFace, deleteStudentFace, getStudentPhotoUrl, uploadStudentPhoto, deleteStudentPhoto, getStudentArchive, updateStudentArchive } from '@/api/modules/student'
import { getGradeList, type GradeInfo } from '@/api/modules/grade'
import { getClassList, type ClassInfo } from '@/api/modules/clazz'
import { getParentsByStudentId, type ParentInfo } from '@/api/modules/parent'
import { schoolApi, type School } from '@/api/modules/school'
import { listCards, listCardTypes, issueCard, reportLoss, unloss, freeze, unfreeze, cancelCard, type Card, type CardType } from '@/api/modules/card'

const loading = ref(false)
const dataSource = ref<StudentInfo[]>([])
const columns = [
  { title: '学号', dataIndex: 'studentNo', key: 'studentNo', width: 140 },
  { title: '姓名', dataIndex: 'name', key: 'name', width: 140 },
  { title: '所属学校', dataIndex: 'schoolName', key: 'schoolName', width: 180 },
  { title: '年级', dataIndex: 'gradeName', key: 'gradeName', width: 120 },
  { title: '班级', dataIndex: 'className', key: 'className', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', fixed: 'right', width: 360 }
]

const query = reactive<{ name?: string; schoolId?: number; gradeId?: number; classId?: number }>({ name: undefined, schoolId: undefined, gradeId: undefined, classId: undefined })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

// 新增/编辑学生弹窗表单
const modalOpen = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const form = reactive<Partial<StudentInfo> & { parents: ParentInfo[]; faceToken?: string }>({
  id: undefined,
  studentNo: '',
  name: '',
  gradeId: undefined,
  classId: undefined,
  status: 1,
  parents: [],
  faceToken: undefined
})

const grades = ref<GradeInfo[]>([])
const classes = ref<ClassInfo[]>([])
const schools = ref<School[]>([])

// 批量导入相关状态
const importModalOpen = ref(false)
const importLoading = ref(false)
const importFile = ref<File | null>(null)
const importClassId = ref<number | undefined>(undefined)

// 照片管理状态
const photoModalOpen = ref(false)
const photoUploading = ref(false)
const photoStudentId = ref<number | null>(null)
const photoFile = ref<File | null>(null)
const photoPreviewUrl = ref<string | null>(null)

// 档案管理状态
const archiveDrawerOpen = ref(false)
const archiveLoading = ref(false)
const archiveStudentId = ref<number | null>(null)
const archiveText = ref<string>('')
const archiveIsJson = ref<boolean>(false)
const archiveError = ref<string>('')
// 批量操作状态
const bulkLoading = ref(false)

// 卡片关联管理状态
const cardModalOpen = ref(false)
const cardLoading = ref(false)
const cardStudentId = ref<number | null>(null)
const cardList = ref<Card[]>([])
const cardTypes = ref<CardType[]>([])
const issueCardForm = reactive<{ cardTypeId?: number; initialBalance?: number; note?: string }>({ cardTypeId: undefined, initialBalance: 0, note: '' })

const resetForm = () => {
  form.id = undefined
  form.studentNo = ''
  form.name = ''
  form.schoolId = undefined
  form.gradeId = undefined
  form.classId = undefined
  form.status = 1
  form.parents = []
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

const fetchGrades = async (schoolId?: number) => {
  try {
    const res = await getGradeList({ page: 1, size: 100, schoolId })
    grades.value = res.records
  } catch (e) {
    grades.value = [
      { id: 1, name: '一年级', year: 2024, status: 1, schoolId: 1, schoolName: '北京大学附属中学' },
      { id: 2, name: '二年级', year: 2023, status: 1, schoolId: 1, schoolName: '北京大学附属中学' }
    ]
  }
}

const fetchClasses = async (gradeId?: number, schoolIdOverride?: number) => {
  try {
    const res = await getClassList({ page: 1, size: 100, gradeId, schoolId: schoolIdOverride ?? form.schoolId ?? query.schoolId })
    classes.value = res.records
  } catch (e) {
    classes.value = [
      { id: 101, name: '高一(1)班', gradeId: 1, status: 1, schoolId: 1, schoolName: '北京大学附属中学' },
      { id: 102, name: '高一(2)班', gradeId: 1, status: 1, schoolId: 1, schoolName: '北京大学附属中学' },
      { id: 103, name: '高一(3)班', gradeId: 1, status: 1, schoolId: 1, schoolName: '北京大学附属中学' },
      { id: 104, name: '高一(4)班', gradeId: 1, status: 1, schoolId: 1, schoolName: '北京大学附属中学' },
      { id: 105, name: '高一(5)班', gradeId: 1, status: 1, schoolId: 1, schoolName: '北京大学附属中学' }
    ]
  }
}

watch(() => form.gradeId, (val) => fetchClasses(val))
// 新增：筛选器联动，学校/年级变化时刷新班级选项
watch(() => query.schoolId, async (sid) => {
  query.gradeId = undefined
  query.classId = undefined
  await fetchGrades(sid)
  await fetchClasses(undefined, sid)
})

watch(() => query.gradeId, async (gid) => {
  query.classId = undefined
  await fetchClasses(gid, query.schoolId)
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStudentList({ page: pagination.current, size: pagination.pageSize, name: query.name, schoolId: query.schoolId, gradeId: query.gradeId, classId: query.classId })
    dataSource.value = res.records
    pagination.total = res.total
  } catch (e) {
    message.error('获取学生列表失败')
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
  query.gradeId = undefined
  query.classId = undefined
  pagination.current = 1
  fetchData()
}
const handlePageChange = (page: number, pageSize?: number) => {
  pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  fetchData()
}

const openCreate = async () => {
  resetForm()
  isEdit.value = false
  await loadSchools()
  await fetchGrades(form.schoolId)
  await fetchClasses(form.gradeId)
  modalOpen.value = true
}

const openEdit = async (row: StudentInfo) => {
  resetForm()
  isEdit.value = true
  form.id = row.id
  form.studentNo = row.studentNo
  form.name = row.name
  form.schoolId = row.schoolId
  form.gradeId = row.gradeId
  form.classId = row.classId
  form.status = row.status ?? 1
  await loadSchools()
  await fetchGrades(form.schoolId)
  await fetchClasses(form.gradeId)
  try {
    form.parents = await getParentsByStudentId(row.id)
  } catch (e) {
    form.parents = []
  }
  try {
    form.faceToken = await getStudentFace(row.id)
  } catch (e) {
    form.faceToken = undefined
  }
  modalOpen.value = true
}

const onDelete = async (row: StudentInfo) => {
  try {
    await deleteStudent(row.id)
    message.success('删除成功')
    fetchData()
  } catch (e: any) {
    message.error(`删除失败：${e?.message || e}`)
  }
}

const onToggleStatus = async (row: StudentInfo) => {
  try {
    if ((row.status ?? 1) === 1) {
      await disableStudent(row.id)
      message.success('禁用成功')
    } else {
      await enableStudent(row.id)
      message.success('启用成功')
    }
    fetchData()
  } catch (e: any) {
    message.error(`状态切换失败：${e?.message || e}`)
  }
}

const onGraduate = async (row: StudentInfo) => {
  try {
    await graduateStudent(row.id)
    message.success('毕业处理成功')
    fetchData()
  } catch (e: any) {
    message.error(`毕业处理失败：${e?.message || e}`)
  }
}

const addParent = () => {
  form.parents.push({ id: Date.now(), name: '', phone: '', relation: 'GUARDIAN', status: 1 })
}
const removeParent = (idx: number) => {
  form.parents.splice(idx, 1)
}

const onSaveFace = async () => {
  if (!form.id) return
  try {
    await setStudentFace(form.id, form.faceToken || '')
    message.success('人脸信息已保存')
  } catch (e: any) {
    message.error(`保存人脸失败：${e?.message || e}`)
  }
}
const onDeleteFace = async () => {
  if (!form.id) return
  try {
    await deleteStudentFace(form.id)
    form.faceToken = undefined
    message.success('已删除人脸信息')
  } catch (e: any) {
    message.error(`删除人脸失败：${e?.message || e}`)
  }
}

const onSubmit = async () => {
  modalLoading.value = true
  try {
    if (isEdit.value && form.id) {
      await updateStudent(form.id, { name: form.name, studentNo: form.studentNo, schoolId: form.schoolId, gradeId: form.gradeId, classId: form.classId, status: form.status, parents: form.parents })
      message.success('修改成功')
    } else {
      await createStudent({ name: form.name!, studentNo: form.studentNo!, schoolId: form.schoolId, gradeId: form.gradeId, classId: form.classId!, status: form.status, parents: form.parents })
      message.success('新增成功')
    }
    modalOpen.value = false
    fetchData()
  } catch (e: any) {
    message.error(`保存失败：${e?.message || e}`)
  } finally {
    modalLoading.value = false
  }
}

const onCancel = () => {
  modalOpen.value = false
}

// 批量导入相关逻辑
const openImport = async () => {
  importClassId.value = query.classId
  await loadSchools()
  await fetchGrades(query.schoolId)
  await fetchClasses(query.gradeId)
  importModalOpen.value = true
}

const beforeUpload = (file: File) => {
  importFile.value = file
  return false
}

const onImportSubmit = async () => {
  importLoading.value = true
  try {
    if (!importFile.value) {
      message.warning('请选择CSV文件')
      return
    }
    const fd = new FormData()
    fd.append('file', importFile.value)
    if (importClassId.value) fd.append('classId', String(importClassId.value))
    const res: any = await importStudents(fd)
    const report = res as any
    message.success(`导入完成：成功${report?.successCount ?? 0}条，跳过${report?.skipCount ?? 0}条，错误${report?.errorCount ?? 0}条`)
    importModalOpen.value = false
    fetchData()
  } catch (e: any) {
    message.error(`导入失败：${e?.message || e}`)
  } finally {
    importLoading.value = false
    importFile.value = null
  }
}

const onImportCancel = () => {
  importModalOpen.value = false
  importFile.value = null
}

// 批量转班相关状态
const selectedRowKeys = ref<number[]>([])
const transferModalOpen = ref(false)
const transferLoading = ref(false)
const transferSchoolId = ref<number | undefined>(undefined)
const transferGradeId = ref<number | undefined>(undefined)
const transferClassId = ref<number | undefined>(undefined)

const onSelectionChange = (keys: number[]) => {
  selectedRowKeys.value = keys
}

const openTransfer = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择需要转班的学生')
    return
  }
  // 预填充当前筛选条件，提升使用体验
  if (transferSchoolId.value == null) transferSchoolId.value = query.schoolId
  if (transferGradeId.value == null) transferGradeId.value = query.gradeId
  await loadSchools()
  await fetchGrades(transferSchoolId.value)
  await fetchClasses(transferGradeId.value, transferSchoolId.value)
  transferModalOpen.value = true
}

const onTransferSubmit = async () => {
  if (!transferClassId.value) {
    message.warning('请选择目标班级')
    return
  }
  transferLoading.value = true
  try {
    const res: any = await transferStudents(selectedRowKeys.value, transferClassId.value)
    const report = res as any
    message.success(`转班完成：成功${report?.successCount ?? 0}条，错误${report?.errorCount ?? 0}条`)
    transferModalOpen.value = false
    selectedRowKeys.value = []
    fetchData()
  } catch (e: any) {
    message.error(`转班失败：${e?.message || e}`)
  } finally {
    transferLoading.value = false
    // 重置选择（失败时保留弹窗与选择便于重试）
    transferSchoolId.value = undefined
    transferGradeId.value = undefined
    transferClassId.value = undefined
  }
}

const onTransferCancel = () => {
  transferModalOpen.value = false
  transferSchoolId.value = undefined
  transferGradeId.value = undefined
  transferClassId.value = undefined
}
watch(transferSchoolId, async (sid) => {
  transferGradeId.value = undefined
  transferClassId.value = undefined
  await fetchGrades(sid)
})

watch(transferGradeId, async (gid) => {
  transferClassId.value = undefined
  await fetchClasses(gid, transferSchoolId.value)
})

// 照片管理逻辑
const openPhoto = async (row: StudentInfo) => {
  photoStudentId.value = row.id
  photoFile.value = null
  photoPreviewUrl.value = getStudentPhotoUrl(row.id)
  photoModalOpen.value = true
}
const beforePhotoUpload = (file: File) => {
  const isValidType = ['image/jpeg', 'image/png'].includes(file.type)
  if (!isValidType) {
    message.warning('仅支持 JPG/PNG 格式的图片')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.warning('图片大小需小于 2MB')
    return false
  }
  photoFile.value = file
  try {
    photoPreviewUrl.value = URL.createObjectURL(file)
  } catch {}
  return false
}
const onPhotoUpload = async () => {
  if (!photoStudentId.value || !photoFile.value) {
    message.warning('请选择要上传的照片文件')
    return
  }
  photoUploading.value = true
  try {
    await uploadStudentPhoto(photoStudentId.value, photoFile.value)
    message.success('照片上传成功')
    photoPreviewUrl.value = getStudentPhotoUrl(photoStudentId.value)
    photoFile.value = null
  } catch (e: any) {
    message.error(`上传失败：${e?.message || e}`)
  } finally {
    photoUploading.value = false
  }
}
const onPhotoDelete = async () => {
  if (!photoStudentId.value) return
  try {
    await deleteStudentPhoto(photoStudentId.value)
    photoPreviewUrl.value = null
    message.success('已删除学生照片')
  } catch (e: any) {
    message.error(`删除失败：${e?.message || e}`)
  }
}
const onPhotoCancel = () => {
  photoModalOpen.value = false
  if (photoPreviewUrl.value && photoPreviewUrl.value.startsWith('blob:')) {
    try { URL.revokeObjectURL(photoPreviewUrl.value) } catch {}
  }
  photoFile.value = null
}

// 档案管理逻辑
const openArchive = async (row: StudentInfo) => {
  archiveStudentId.value = row.id
  archiveText.value = ''
  archiveError.value = ''
  archiveLoading.value = true
  archiveDrawerOpen.value = true
  try {
    const text = await getStudentArchive(row.id)
    archiveText.value = typeof text === 'string' ? text : JSON.stringify(text, null, 2)
    // 基于当前内容尝试判断是否为JSON
    try {
      const parsed = JSON.parse(archiveText.value)
      archiveIsJson.value = true
      archiveText.value = JSON.stringify(parsed, null, 2)
    } catch {
      archiveIsJson.value = false
    }
  } catch (e: any) {
    archiveText.value = ''
    archiveIsJson.value = false
    message.error(`获取档案失败：${e?.message || e}`)
  } finally {
    archiveLoading.value = false
  }
}
const onArchiveSave = async () => {
  archiveLoading.value = true
  try {
    if (archiveIsJson.value) {
      try {
        JSON.parse(archiveText.value)
      } catch (err: any) {
        archiveError.value = `JSON格式错误：${err?.message || err}`
        return
      }
    }
    await updateStudentArchive(archiveStudentId.value!, archiveText.value)
    message.success('档案保存成功')
    archiveDrawerOpen.value = false
  } catch (e: any) {
    message.error(`保存档案失败：${e?.message || e}`)
  } finally {
    archiveLoading.value = false
  }
}
const onArchiveCancel = () => {
  archiveDrawerOpen.value = false
}

// 卡片关联管理逻辑
const fetchStudentCards = async () => {
  if (!cardStudentId.value) return
  try {
    const res = await listCards({ holderType: 'STUDENT', holderId: String(cardStudentId.value), page: 1, size: 100 })
    cardList.value = res.records
  } catch (e) {
    cardList.value = []
  }
}
const openCardModal = async (row: StudentInfo) => {
  cardStudentId.value = row.id
  cardModalOpen.value = true
  cardLoading.value = true
  try {
    cardTypes.value = await listCardTypes()
    await fetchStudentCards()
  } catch (e: any) {
    message.error(`加载卡片信息失败：${e?.message || e}`)
  } finally {
    cardLoading.value = false
  }
}
const onIssueCard = async () => {
  if (!cardStudentId.value || !issueCardForm.cardTypeId) {
    message.warning('请选择卡片类型')
    return
  }
  cardLoading.value = true
  try {
    const res = await issueCard({ holderType: 'STUDENT', holderId: String(cardStudentId.value), cardTypeId: issueCardForm.cardTypeId!, initialBalance: issueCardForm.initialBalance, note: issueCardForm.note })
    if (res.success) {
      message.success(`发卡成功${res.cardNo ? '：' + res.cardNo : ''}`)
      await fetchStudentCards()
      issueCardForm.cardTypeId = undefined
      issueCardForm.initialBalance = 0
      issueCardForm.note = ''
    } else {
      message.error('发卡失败')
    }
  } catch (e: any) {
    message.error(`发卡失败：${e?.message || e}`)
  } finally {
    cardLoading.value = false
  }
}
const onLoss = async (cardNo: string) => {
  cardLoading.value = true
  try {
    const ok = await reportLoss({ cardNo })
    if (ok.success) {
      message.success('已挂失')
      await fetchStudentCards()
    } else {
      message.error('挂失失败')
    }
  } catch (e: any) {
    message.error(`挂失失败：${e?.message || e}`)
  } finally {
    cardLoading.value = false
  }
}
const onUnloss = async (cardNo: string) => {
  cardLoading.value = true
  try {
    const ok = await unloss({ cardNo })
    if (ok.success) {
      message.success('已解除挂失')
      await fetchStudentCards()
    } else {
      message.error('解除挂失失败')
    }
  } catch (e: any) {
    message.error(`解除挂失失败：${e?.message || e}`)
  } finally {
    cardLoading.value = false
  }
}
const onFreeze = async (cardNo: string) => {
  cardLoading.value = true
  try {
    const ok = await freeze({ cardNo })
    if ((ok as any)?.success !== false) {
      message.success('已冻结')
      await fetchStudentCards()
    } else {
      message.error('冻结失败')
    }
  } catch (e: any) {
    message.error(`冻结失败：${e?.message || e}`)
  } finally {
    cardLoading.value = false
  }
}
const onUnfreeze = async (cardNo: string) => {
  cardLoading.value = true
  try {
    const ok = await unfreeze({ cardNo })
    if ((ok as any)?.success !== false) {
      message.success('已解冻')
      await fetchStudentCards()
    } else {
      message.error('解冻失败')
    }
  } catch (e: any) {
    message.error(`解冻失败：${e?.message || e}`)
  } finally {
    cardLoading.value = false
  }
}
const onCancelCard = async (cardNo: string) => {
  cardLoading.value = true
  try {
    const ok = await cancelCard({ cardNo, refundAll: true })
    if (ok.success) {
      message.success('已销卡并退回余额')
      await fetchStudentCards()
    } else {
      message.error('销卡失败')
    }
  } catch (e: any) {
    message.error(`销卡失败：${e?.message || e}`)
  } finally {
    cardLoading.value = false
  }
}
const onCardModalCancel = () => {
  cardModalOpen.value = false
  cardStudentId.value = null
  cardList.value = []
}

// 单个转班/转学入口，复用批量转班逻辑
const openTransferSingle = async (row: StudentInfo) => {
  selectedRowKeys.value = [row.id]
  // 预填充当前行所在的学校/年级，便于选择
  transferSchoolId.value = row.schoolId
  transferGradeId.value = row.gradeId
  await loadSchools()
  await fetchGrades(transferSchoolId.value)
  await fetchClasses(transferGradeId.value, transferSchoolId.value)
  transferModalOpen.value = true
}

// 导出当前筛选结果为CSV
const exportCsv = () => {
  const header = ['学号','姓名','学校','年级','班级','状态']
  const lines = dataSource.value.map(s => [
    s.studentNo,
    s.name,
    s.schoolName || (schools.value.find(x => x.id === s.schoolId)?.name) || '',
    s.gradeName || (grades.value.find(x => x.id === s.gradeId)?.name) || '',
    s.className || (classes.value.find(x => x.id === s.classId)?.name) || '',
    s.status === 2 ? '毕业' : s.status === 1 ? '启用' : '禁用'
  ].map(v => (v ?? '').toString().replace(/"/g,'""')))
  const csv = [header.join(','), ...lines.map(row => row.map(v => `"${v}"`).join(','))].join('\n')
  const blob = new Blob(["\uFEFF" + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `students_${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

const onBulkAction = async (action: 'enable' | 'disable' | 'graduate') => {
  if (selectedRowKeys.value.length === 0) { message.warning('请先选择学生'); return }
  bulkLoading.value = true
  try {
    const calls = selectedRowKeys.value.map(id => action === 'enable' ? enableStudent(id) : action === 'disable' ? disableStudent(id) : graduateStudent(id))
    const results = await Promise.allSettled(calls)
    const successCount = results.filter(r => r.status === 'fulfilled').length
    const errorCount = results.length - successCount
    message.success(`批量${action === 'enable' ? '启用' : action === 'disable' ? '禁用' : '毕业'}完成：成功${successCount}，错误${errorCount}`)
    selectedRowKeys.value = []
    fetchData()
  } catch (e: any) {
    message.error(`批量操作失败：${e?.message || e}`)
  } finally {
    bulkLoading.value = false
  }
}

onMounted(() => { loadSchools(); fetchGrades(query.schoolId); fetchClasses(); fetchData() })
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="学生管理" />

    <div style="margin-bottom: 12px; display: flex; gap: 8px;">
      <a-button type="primary" @click="openCreate">新增学生</a-button>
      <a-button @click="openImport">批量导入</a-button>
      <a-button :disabled="selectedRowKeys.length === 0" @click="openTransfer">批量转班</a-button>
      <a-popconfirm title="确认批量启用选中学生？" @confirm="() => onBulkAction('enable')">
        <a-button :disabled="selectedRowKeys.length === 0" :loading="bulkLoading">批量启用</a-button>
      </a-popconfirm>
      <a-popconfirm title="确认批量禁用选中学生？" @confirm="() => onBulkAction('disable')">
        <a-button :disabled="selectedRowKeys.length === 0" :loading="bulkLoading">批量禁用</a-button>
      </a-popconfirm>
      <a-popconfirm title="确认批量毕业所选学生？" @confirm="() => onBulkAction('graduate')">
        <a-button :disabled="selectedRowKeys.length === 0" :loading="bulkLoading">批量毕业</a-button>
      </a-popconfirm>
      <a-button @click="exportCsv">导出CSV</a-button>
      <a-tag color="blue" style="margin-left:8px">已选 {{ selectedRowKeys.length }} 项</a-tag>
      <a-button type="link" @click="selectedRowKeys = []">清空选择</a-button>
    </div>

    <a-form layout="inline" style="margin-bottom: 16px;">
      <a-form-item label="姓名">
        <a-input v-model:value="query.name" placeholder="请输入姓名关键字" allow-clear />
      </a-form-item>
      <a-form-item label="所属学校">
        <a-select v-model:value="query.schoolId" allow-clear style="width: 180px;" placeholder="选择学校" @change="(v:any) => { query.gradeId = undefined; query.classId = undefined; fetchGrades(v) }">
          <a-select-option v-for="s in schools" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="年级">
        <a-select v-model:value="query.gradeId" allow-clear style="width: 180px;" placeholder="选择年级" @change="(v:any) => { query.classId = undefined; fetchClasses(v) }">
          <a-select-option v-for="g in grades" :key="g.id" :value="g.id">{{ g.name }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="班级">
        <a-select v-model:value="query.classId" allow-clear style="width: 180px;" placeholder="选择班级">
          <a-select-option v-for="c in classes" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
        </a-select>
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
      :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectionChange }"
      :scroll="{ x: 'max-content' }"
      @change="(p:any) => handlePageChange(p.current, p.pageSize)"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'schoolName'">
          <span>{{ record.schoolName || (schools.find(s => s.id === record.schoolId)?.name) || '-' }}</span>
          </template>
        <template v-else-if="column.key === 'gradeName'">
          <span>{{ record.gradeName || (grades.find(g => g.id === record.gradeId)?.name) || '-' }}</span>
        </template>
        <template v-else-if="column.key === 'className'">
          <span>{{ record.className || (classes.find(c => c.id === record.classId)?.name) || '-' }}</span>
        </template>
        <template v-else-if="column.key === 'status'">
          <span>{{ record.status === 2 ? '毕业' : record.status === 1 ? '启用' : '禁用' }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除该学生？" @confirm="() => onDelete(record)">
              <a-button type="link">删除</a-button>
            </a-popconfirm>
            <a-popconfirm :title="(record.status ?? 1) === 1 ? '确认禁用该学生？' : '确认启用该学生？'" @confirm="() => onToggleStatus(record)">
              <a-button type="link">{{ (record.status ?? 1) === 1 ? '禁用' : '启用' }}</a-button>
            </a-popconfirm>
            <a-popconfirm title="确认毕业处理该学生？" @confirm="() => onGraduate(record)">
              <a-button type="link">毕业处理</a-button>
            </a-popconfirm>
            <a-button type="link" @click="openPhoto(record)">照片</a-button>
            <a-button type="link" @click="openArchive(record)">档案</a-button>
            <a-button type="link" @click="openTransferSingle(record)">转班/转学</a-button>
            <a-button type="link" @click="openCardModal(record)">卡片</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:open="modalOpen"
      :confirm-loading="modalLoading"
      :title="isEdit ? '编辑学生' : '新增学生'"
      okText="保存"
      cancelText="取消"
      @ok="onSubmit"
      @cancel="onCancel"
      destroyOnClose
    >
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="学号" required>
              <a-input v-model:value="form.studentNo" placeholder="如 S2024001" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="姓名" required>
              <a-input v-model:value="form.name" placeholder="请输入姓名" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="学校">
              <a-select v-model:value="form.schoolId" allow-clear placeholder="选择学校" @change="(v:any) => { form.gradeId = undefined; form.classId = undefined; fetchGrades(v) }">
                <a-select-option v-for="s in schools" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="年级">
              <a-select v-model:value="form.gradeId" allow-clear placeholder="选择年级" @change="(v:any) => fetchClasses(v)">
                <a-select-option v-for="g in grades" :key="g.id" :value="g.id">{{ g.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="班级" required>
              <a-select v-model:value="form.classId" placeholder="选择班级">
                <a-select-option v-for="c in classes" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="状态">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-divider orientation="left">家长信息</a-divider>
        <div style="margin-bottom: 8px;">
          <a-button type="dashed" @click="addParent">添加家长</a-button>
        </div>
        <a-row :gutter="8" v-for="(p, idx) in form.parents" :key="p.id" style="margin-bottom: 8px;">
          <a-col :span="6">
            <a-input v-model:value="p.name" placeholder="家长姓名" />
          </a-col>
          <a-col :span="6">
            <a-input v-model:value="p.phone" placeholder="联系电话" />
          </a-col>
          <a-col :span="6">
            <a-select v-model:value="p.relation" style="width: 100%;">
              <a-select-option value="FATHER">父亲</a-select-option>
              <a-select-option value="MOTHER">母亲</a-select-option>
              <a-select-option value="GUARDIAN">监护人</a-select-option>
              <a-select-option value="OTHER">其他</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="6">
            <a-button danger type="dashed" @click="removeParent(idx)">移除</a-button>
          </a-col>
        </a-row>

        <a-divider orientation="left">人脸信息</a-divider>
        <a-form-item label="人脸状态">
          <a-space>
            <a-tag color="green" v-if="form.faceToken">已设置</a-tag>
            <a-tag color="red" v-else>未设置</a-tag>
          </a-space>
        </a-form-item>
        <a-form-item label="人脸Token">
          <a-input v-model:value="form.faceToken" placeholder="如需手动设置，可填入Token" allow-clear />
          <div style="margin-top:8px;">
            <a-space>
              <a-button type="primary" @click="onSaveFace" :disabled="!form.id">保存人脸</a-button>
              <a-popconfirm title="确认删除该学生人脸信息？" @confirm="onDeleteFace">
                <a-button danger :disabled="!form.id || !form.faceToken">删除人脸</a-button>
              </a-popconfirm>
            </a-space>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="importModalOpen"
      :confirm-loading="importLoading"
      title="批量导入学生"
      okText="开始导入"
      cancelText="取消"
      @ok="onImportSubmit"
      @cancel="onImportCancel"
      destroyOnClose
    >
      <a-form layout="vertical">
        <a-form-item label="默认班级（可选，若CSV中包含classId则覆盖）">
          <a-select v-model:value="importClassId" allow-clear placeholder="选择默认班级">
            <a-select-option v-for="c in classes" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-alert type="info" show-icon message="CSV格式：studentNo,name[,classId][,status]；首行可为表头(学号,姓名,...)" />
        <a-form-item label="选择CSV文件">
          <a-upload :accept="'.csv'" :before-upload="beforeUpload" :max-count="1" :show-upload-list="true">
            <a-button>选择文件</a-button>
          </a-upload>
          <div v-if="importFile" style="margin-top:8px;color:#666;">已选择：{{ importFile?.name }}</div>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="transferModalOpen"
      :confirm-loading="transferLoading"
      title="批量转班"
      okText="确认转班"
      cancelText="取消"
      @ok="onTransferSubmit"
      @cancel="onTransferCancel"
      destroyOnClose
    >
      <a-form layout="vertical">
        <a-form-item label="目标学校">
          <a-select v-model:value="transferSchoolId" allow-clear placeholder="选择学校">
            <a-select-option v-for="s in schools" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="目标年级">
          <a-select v-model:value="transferGradeId" allow-clear placeholder="选择年级">
            <a-select-option v-for="g in grades" :key="g.id" :value="g.id">{{ g.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="目标班级" required>
          <a-select v-model:value="transferClassId" placeholder="选择班级">
            <a-select-option v-for="c in classes" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-alert type="info" show-icon message="将把所选学生的班级变更为目标班级" />
      </a-form>
    </a-modal>

    <!-- 档案编辑 -->
    <a-drawer
      v-model:open="archiveDrawerOpen"
      :loading="archiveLoading"
      title="学生档案管理"
      placement="right"
      width="520"
    >
      <a-form layout="vertical">
        <a-form-item label="档案内容(支持文本或JSON)">
          <a-textarea v-model:value="archiveText" :rows="14" placeholder="可粘贴或编辑文本/JSON" />
        </a-form-item>
        <a-form-item label="内容为JSON">
          <a-switch v-model:checked="archiveIsJson" checked-children="JSON" un-checked-children="文本" />
        </a-form-item>
        <a-alert v-if="archiveError" type="error" show-icon :message="archiveError" style="margin-bottom:12px;" />
        <a-space>
          <a-button type="primary" @click="onArchiveSave" :loading="archiveLoading">保存</a-button>
          <a-button @click="onArchiveCancel">取消</a-button>
        </a-space>
      </a-form>
    </a-drawer>

    <!-- 卡片关联 -->
    <a-modal
      v-model:open="cardModalOpen"
      :confirm-loading="cardLoading"
      title="学生卡片关联"
      :width="720"
      okText="关闭"
      cancelText="关闭"
      @ok="onCardModalCancel"
      @cancel="onCardModalCancel"
      destroyOnClose
    >
      <a-form layout="vertical">
        <a-divider orientation="left">已关联卡片</a-divider>
        <div class="modal-table-scroll">
          <a-table :dataSource="cardList" :pagination="false" rowKey="cardNo" size="small" :scroll="{ x: 'max-content' }">
          <a-table-column title="卡号" data-index="cardNo" />
          <a-table-column title="类型" key="typeId">
            <template #default="{ record }">
              {{ (cardTypes.find(t => t.id === record.typeId)?.name) || record.typeId }}
            </template>
          </a-table-column>
          <a-table-column title="状态" data-index="status" />
          <a-table-column title="余额" data-index="balance" />
          <a-table-column title="操作" key="ops">
            <template #default="{ record }">
              <a-space size="small">
                <a-popconfirm title="确认挂失该卡？" @confirm="() => onLoss(record.cardNo)">
                  <a-button type="link" :disabled="record.status !== 'ACTIVE'">挂失</a-button>
                </a-popconfirm>
                <a-popconfirm title="确认解除挂失？" @confirm="() => onUnloss(record.cardNo)">
                  <a-button type="link" :disabled="record.status !== 'LOST'">解挂</a-button>
                </a-popconfirm>
                <a-popconfirm title="确认冻结该卡？" @confirm="() => onFreeze(record.cardNo)">
                  <a-button type="link" :disabled="record.status !== 'ACTIVE'">冻结</a-button>
                </a-popconfirm>
                <a-popconfirm title="确认解冻该卡？" @confirm="() => onUnfreeze(record.cardNo)">
                  <a-button type="link" :disabled="record.status !== 'FROZEN'">解冻</a-button>
                </a-popconfirm>
                <a-popconfirm title="确认销卡并退余额？" @confirm="() => onCancelCard(record.cardNo)">
                  <a-button danger type="link">销卡</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table-column>
          </a-table>
        </div>

        <a-divider orientation="left">发卡</a-divider>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="卡片类型" required>
              <a-select v-model:value="issueCardForm.cardTypeId" placeholder="选择卡片类型">
                <a-select-option v-for="t in cardTypes" :key="t.id" :value="t.id">{{ t.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="初始余额">
              <a-input-number v-model:value="issueCardForm.initialBalance" :min="0" :step="1" style="width:100%;" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-input v-model:value="issueCardForm.note" placeholder="可选备注" />
        </a-form-item>
        <a-button type="primary" @click="onIssueCard" :loading="cardLoading" :disabled="!cardStudentId">发卡</a-button>
      </a-form>
    </a-modal>


    <a-modal
      v-model:open="modalOpen"
      :confirm-loading="modalLoading"
      :title="isEdit ? '编辑学生' : '新增学生'"
      okText="保存"
      cancelText="取消"
      @ok="onSubmit"
      @cancel="onCancel"
      destroyOnClose
    >
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="学号" required>
              <a-input v-model:value="form.studentNo" placeholder="如 S2024001" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="姓名" required>
              <a-input v-model:value="form.name" placeholder="请输入姓名" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="学校">
              <a-select v-model:value="form.schoolId" allow-clear placeholder="选择学校" @change="(v:any) => { form.gradeId = undefined; form.classId = undefined; fetchGrades(v) }">
                <a-select-option v-for="s in schools" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="年级">
              <a-select v-model:value="form.gradeId" allow-clear placeholder="选择年级" @change="(v:any) => fetchClasses(v)">
                <a-select-option v-for="g in grades" :key="g.id" :value="g.id">{{ g.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="班级" required>
              <a-select v-model:value="form.classId" placeholder="选择班级">
                <a-select-option v-for="c in classes" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="状态">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-divider orientation="left">家长信息</a-divider>
        <div style="margin-bottom: 8px;">
          <a-button type="dashed" @click="addParent">添加家长</a-button>
        </div>
        <a-row :gutter="8" v-for="(p, idx) in form.parents" :key="p.id" style="margin-bottom: 8px;">
          <a-col :span="6">
            <a-input v-model:value="p.name" placeholder="家长姓名" />
          </a-col>
          <a-col :span="6">
            <a-input v-model:value="p.phone" placeholder="联系电话" />
          </a-col>
          <a-col :span="6">
            <a-select v-model:value="p.relation" style="width: 100%;">
              <a-select-option value="FATHER">父亲</a-select-option>
              <a-select-option value="MOTHER">母亲</a-select-option>
              <a-select-option value="GUARDIAN">监护人</a-select-option>
              <a-select-option value="OTHER">其他</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="6">
            <a-button danger type="dashed" @click="removeParent(idx)">移除</a-button>
          </a-col>
        </a-row>

        <a-divider orientation="left">人脸信息</a-divider>
        <a-form-item label="人脸状态">
          <a-space>
            <a-tag color="green" v-if="form.faceToken">已设置</a-tag>
            <a-tag color="red" v-else>未设置</a-tag>
          </a-space>
        </a-form-item>
        <a-form-item label="人脸Token">
          <a-input v-model:value="form.faceToken" placeholder="如需手动设置，可填入Token" allow-clear />
          <div style="margin-top:8px;">
            <a-space>
              <a-button type="primary" @click="onSaveFace" :disabled="!form.id">保存人脸</a-button>
              <a-popconfirm title="确认删除该学生人脸信息？" @confirm="onDeleteFace">
                <a-button danger :disabled="!form.id || !form.faceToken">删除人脸</a-button>
              </a-popconfirm>
            </a-space>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="importModalOpen"
      :confirm-loading="importLoading"
      title="批量导入学生"
      okText="开始导入"
      cancelText="取消"
      @ok="onImportSubmit"
      @cancel="onImportCancel"
      destroyOnClose
    >
      <a-form layout="vertical">
        <a-form-item label="默认班级（可选，若CSV中包含classId则覆盖）">
          <a-select v-model:value="importClassId" allow-clear placeholder="选择默认班级">
            <a-select-option v-for="c in classes" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-alert type="info" show-icon message="CSV格式：studentNo,name[,classId][,status]；首行可为表头(学号,姓名,...)" />
        <a-form-item label="选择CSV文件">
          <a-upload :accept="'.csv'" :before-upload="beforeUpload" :max-count="1" :show-upload-list="true">
            <a-button>选择文件</a-button>
          </a-upload>
          <div v-if="importFile" style="margin-top:8px;color:#666;">已选择：{{ importFile?.name }}</div>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="transferModalOpen"
      :confirm-loading="transferLoading"
      title="批量转班"
      okText="确认转班"
      cancelText="取消"
      @ok="onTransferSubmit"
      @cancel="onTransferCancel"
      destroyOnClose
    >
      <a-form layout="vertical">
        <a-form-item label="目标学校">
          <a-select v-model:value="transferSchoolId" allow-clear placeholder="选择学校">
            <a-select-option v-for="s in schools" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="目标年级">
          <a-select v-model:value="transferGradeId" allow-clear placeholder="选择年级">
            <a-select-option v-for="g in grades" :key="g.id" :value="g.id">{{ g.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="目标班级" required>
          <a-select v-model:value="transferClassId" placeholder="选择班级">
            <a-select-option v-for="c in classes" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-alert type="info" show-icon message="将把所选学生的班级变更为目标班级" />
      </a-form>
    </a-modal>


  </div>
</template>

<style scoped>
.modal-table-scroll {
  max-width: 100%;
  overflow-x: auto;
}
/* 保证表格在内容很多时仍可滚动查看，而不挤出弹窗 */
.modal-table-scroll :deep(.ant-table) {
  min-width: 680px;
}
</style>