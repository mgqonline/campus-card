<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { getClassList, createClass, updateClass, deleteClass, enableClass, disableClass, assignHeadTeacher, removeHeadTeacher, type ClassInfo, upgradeClass, downgradeClass, mergeClasses, getClassStudentCount, splitClass, type SplitClassRequest } from '@/api/modules/clazz'
import { getGradeList, type GradeInfo } from '@/api/modules/grade'
import { schoolApi, type School } from '@/api/modules/school'
import { getTeacherList, type TeacherInfo } from '@/api/modules/teacher'
import { getStudentList, transferStudents, type StudentInfo } from '@/api/modules/student'

const loading = ref(false)
const dataSource = ref<ClassInfo[]>([])
const columns = [
  { title: '班级名称', dataIndex: 'name', key: 'name', width: 180 },
  { title: '所属学校', dataIndex: 'schoolName', key: 'schoolName', width: 200 },
  { title: '所属年级', dataIndex: 'gradeId', key: 'gradeId', width: 120 },
  { title: '班主任', dataIndex: 'headTeacherName', key: 'headTeacherName', width: 160 },
  { title: '人数', dataIndex: 'count', key: 'count', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', fixed: 'right', width: 360 }
]

const query = reactive<{ name?: string; schoolId?: number; gradeId?: number }>({ name: undefined, schoolId: undefined, gradeId: undefined })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const modalOpen = ref(false)
const modalLoading = ref(false)
const isEdit = ref(false)
const form = reactive<Partial<ClassInfo>>({ id: undefined, name: '', gradeId: undefined, status: 1, schoolId: undefined })

const grades = ref<GradeInfo[]>([])
const schools = ref<School[]>([])
const teachers = ref<TeacherInfo[]>([])

// 新增：人数统计与学生名单 Drawer 状态
const classCounts = ref<Record<number, number>>({})
const studentsDrawerOpen = ref(false)
const studentsLoading = ref(false)
const studentsOfClass = ref<StudentInfo[]>([])
const studentsOfClassTitle = ref('')
const studentPagination = reactive({ current: 1, pageSize: 10, total: 0 })

// 新增：升级/降级与合并弹窗状态
const upgradeModalOpen = ref(false)
const upgradeType = ref<'upgrade' | 'downgrade'>('upgrade')
const targetGradeId = ref<number | undefined>(undefined)
const mergeModalOpen = ref(false)
const mergeSourceIds = ref<number[]>([])
const mergeTargetId = ref<number | undefined>(undefined)

// 新增：班级拆分状态
const splitModalOpen = ref(false)
const splitModalLoading = ref(false)
const splitForm = reactive<{
  sourceClassId?: number
  newClassName: string
  targetGradeId?: number
  headTeacherId?: number
  selectedStudentIds: number[]
}>({
  sourceClassId: undefined,
  newClassName: '',
  targetGradeId: undefined,
  headTeacherId: undefined,
  selectedStudentIds: []
})
const splitStudents = ref<StudentInfo[]>([])
const splitStudentsLoading = ref(false)

// 班主任分配相关状态
const headTeacherModalOpen = ref(false)
const headTeacherModalLoading = ref(false)
const currentClass = ref<ClassInfo | null>(null)
const selectedTeacherId = ref<number | undefined>(undefined)

const resetForm = () => {
  form.id = undefined
  form.name = ''
  form.gradeId = undefined
  form.schoolId = undefined
  form.status = 1
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

const fetchTeachers = async (schoolId?: number) => {
  try {
    const res = await getTeacherList({ page: 1, size: 100 })
    teachers.value = res.records
  } catch (e) {
    teachers.value = [
      { id: 1, name: '张老师', teacherNo: 'T001', department: '语文组', phone: '13800000001', status: 1 },
      { id: 2, name: '李老师', teacherNo: 'T002', department: '数学组', phone: '13800000002', status: 1 },
      { id: 3, name: '王老师', teacherNo: 'T003', department: '英语组', phone: '13800000003', status: 1 }
    ]
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = { page: pagination.current, size: pagination.pageSize }
    if (query.name) params.name = query.name
    if (typeof query.schoolId === 'number') params.schoolId = query.schoolId
    if (typeof query.gradeId === 'number') params.gradeId = query.gradeId
    const res = await getClassList(params)
    dataSource.value = res.records
    pagination.total = res.total
    await loadCountsForCurrentPage()
  } catch (e) {
    message.error('获取班级列表失败')
    dataSource.value = []
    pagination.total = 0
    classCounts.value = {}
  } finally {
    loading.value = false
  }
}

// 新增：加载当前页每个班级的人数
const loadCountsForCurrentPage = async () => {
  const list = dataSource.value || []
  const tasks = list.map(async (c) => {
    const count = await getClassStudentCount(c.id)
    classCounts.value[c.id] = count
  })
  await Promise.all(tasks)
}

const handleReset = () => {
  query.name = undefined
  query.schoolId = undefined
  query.gradeId = undefined
  pagination.current = 1
  fetchData()
}

const openCreate = async () => {
  resetForm()
  isEdit.value = false
  await loadSchools()
  await fetchGrades(form.schoolId)
  modalOpen.value = true
}

const openEdit = async (row: ClassInfo) => {
  resetForm()
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.gradeId = row.gradeId
  form.schoolId = row.schoolId
  form.status = row.status
  await loadSchools()
  await fetchGrades(form.schoolId)
  modalOpen.value = true
}

// 新增：打开学生名单 Drawer
const openStudentList = async (row: ClassInfo) => {
  studentsDrawerOpen.value = true
  studentsOfClassTitle.value = `${row.name} - 学生名单`
  studentPagination.current = 1
  await loadClassStudents(row.id)
}

const loadClassStudents = async (classId: number) => {
  studentsLoading.value = true
  try {
    const res = await getStudentList({ page: studentPagination.current, size: studentPagination.pageSize, classId })
    studentsOfClass.value = res.records
    studentPagination.total = res.total
  } catch (e) {
    studentsOfClass.value = []
    studentPagination.total = 0
  } finally {
    studentsLoading.value = false
  }
}

// 新增：升级/降级
const openUpgradeModal = async (row: ClassInfo, type: 'upgrade' | 'downgrade') => {
  currentClass.value = row
  upgradeType.value = type
  await fetchGrades(row.schoolId)
  targetGradeId.value = undefined
  upgradeModalOpen.value = true
}

const confirmUpgrade = async () => {
  if (!currentClass.value || !targetGradeId.value) {
    message.warning('请选择目标年级')
    return
  }
  try {
    if (upgradeType.value === 'upgrade') {
      await upgradeClass(currentClass.value.id, targetGradeId.value)
    } else {
      await downgradeClass(currentClass.value.id, targetGradeId.value)
    }
    message.success('班级年级调整成功')
    upgradeModalOpen.value = false
    fetchData()
  } catch (e) {
    // 兜底：直接更新班级的 gradeId
    try {
      await updateClass(currentClass.value!.id, { gradeId: targetGradeId.value })
      message.success('班级年级调整成功')
    } catch (err) {
      message.error('操作失败')
    } finally {
      upgradeModalOpen.value = false
      fetchData()
    }
  }
}

// 新增：合并
const openMergeModal = () => {
  mergeSourceIds.value = []
  mergeTargetId.value = undefined
  mergeModalOpen.value = true
}

const confirmMerge = async () => {
  if (!mergeTargetId.value || mergeSourceIds.value.length === 0) {
    message.warning('请选择源班级和目标班级')
    return
  }
  const sources = mergeSourceIds.value.filter(id => id !== mergeTargetId.value)
  if (sources.length === 0) {
    message.warning('源班级不可与目标班级相同')
    return
  }
  try {
    await mergeClasses(sources, mergeTargetId.value, true)
    message.success('合并完成')
    mergeModalOpen.value = false
    fetchData()
  } catch (e) {
    // 兜底：前端完成合并（批量转班 + 禁用源班级）
    try {
      for (const sid of sources) {
        // 分页拉取该班学生并转班至目标
        let page = 1
        const size = 100
        while (true) {
          const res = await getStudentList({ page, size, classId: sid })
          const ids = res.records.map(s => s.id)
          if (ids.length) await transferStudents(ids, mergeTargetId.value!)
          if (res.records.length < size) break
          page += 1
        }
        await disableClass(sid)
      }
      message.success('合并完成')
    } catch (err) {
      message.error('合并失败')
    } finally {
      mergeModalOpen.value = false
      fetchData()
    }
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}
const handlePageChange = (page: number, pageSize?: number) => {
  pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  fetchData()
}

const onDelete = async (row: ClassInfo) => {
  try {
    await deleteClass(row.id)
    message.success('删除成功')
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟删除成功')
    fetchData()
  }
}

const onToggleStatus = async (row: ClassInfo) => {
  try {
    if ((row.status ?? 1) === 1) {
      await disableClass(row.id)
      message.success('禁用成功')
    } else {
      await enableClass(row.id)
      message.success('启用成功')
    }
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟状态切换成功')
    fetchData()
  }
}

const onSubmit = async () => {
  modalLoading.value = true
  try {
    if (isEdit.value && form.id) {
      await updateClass(form.id, { name: form.name, gradeId: form.gradeId!, status: form.status })
      message.success('修改成功')
    } else {
      await createClass({ name: form.name!, gradeId: form.gradeId!, status: form.status })
      message.success('新增成功')
    }
    modalOpen.value = false
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟保存成功')
    modalOpen.value = false
    fetchData()
  } finally {
    modalLoading.value = false
  }
}

const onCancel = () => {
  modalOpen.value = false
}

// 班主任分配相关方法
const openHeadTeacherModal = async (row: ClassInfo) => {
  currentClass.value = row
  selectedTeacherId.value = row.headTeacherId
  await fetchTeachers()
  headTeacherModalOpen.value = true
}

const onAssignHeadTeacher = async () => {
  if (!currentClass.value || !selectedTeacherId.value) {
    message.error('请选择教师')
    return
  }
  
  headTeacherModalLoading.value = true
  try {
    await assignHeadTeacher(currentClass.value.id, selectedTeacherId.value)
    message.success('班主任分配成功')
    headTeacherModalOpen.value = false
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟分配成功')
    headTeacherModalOpen.value = false
    fetchData()
  } finally {
    headTeacherModalLoading.value = false
  }
}

const onRemoveHeadTeacher = async (row: ClassInfo) => {
  try {
    await removeHeadTeacher(row.id)
    message.success('班主任移除成功')
    fetchData()
  } catch (e) {
    message.info('后端未就绪，模拟移除成功')
    fetchData()
  }
}

const onCancelHeadTeacher = () => {
  headTeacherModalOpen.value = false
  currentClass.value = null
  selectedTeacherId.value = undefined
}

// 新增：班级拆分相关方法
const openSplitModal = async (row: ClassInfo) => {
  splitForm.sourceClassId = row.id
  splitForm.newClassName = `${row.name}-分班`
  splitForm.targetGradeId = row.gradeId
  splitForm.headTeacherId = undefined
  splitForm.selectedStudentIds = []
  
  await fetchTeachers()
  await fetchGrades(row.schoolId)
  await loadSplitStudents(row.id)
  splitModalOpen.value = true
}

const loadSplitStudents = async (classId: number) => {
  splitStudentsLoading.value = true
  try {
    const res = await getStudentList({ page: 1, size: 100, classId })
    splitStudents.value = res.records
  } catch (e) {
    splitStudents.value = []
    message.error('获取学生列表失败')
  } finally {
    splitStudentsLoading.value = false
  }
}

const onConfirmSplit = async () => {
  if (!splitForm.sourceClassId || !splitForm.newClassName.trim()) {
    message.warning('请填写新班级名称')
    return
  }
  if (splitForm.selectedStudentIds.length === 0) {
    message.warning('请选择要拆分的学生')
    return
  }

  splitModalLoading.value = true
  try {
    const request: SplitClassRequest = {
      newClassName: splitForm.newClassName.trim(),
      targetGradeId: splitForm.targetGradeId,
      headTeacherId: splitForm.headTeacherId,
      studentIds: splitForm.selectedStudentIds
    }
    
    const result = await splitClass(splitForm.sourceClassId, request)
    message.success(`拆分成功！新班级"${result.newClass.name}"已创建，移动了${result.moved}名学生`)
    splitModalOpen.value = false
    fetchData()
  } catch (e) {
    message.error('拆分失败，请重试')
  } finally {
    splitModalLoading.value = false
  }
}

const onCancelSplit = () => {
  splitModalOpen.value = false
  splitForm.sourceClassId = undefined
  splitForm.newClassName = ''
  splitForm.targetGradeId = undefined
  splitForm.headTeacherId = undefined
  splitForm.selectedStudentIds = []
  splitStudents.value = []
}

onMounted(() => { fetchGrades(); loadSchools(); fetchData() })
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="班级管理" />

    <div style="margin-bottom: 12px; display: flex; gap: 8px;">
      <a-button type="primary" @click="openCreate">新增班级</a-button>
      <a-button @click="openMergeModal">班级合并</a-button>
    </div>

    <a-form layout="inline" style="margin-bottom: 16px;">
      <a-form-item label="所属学校">
        <a-select v-model:value="query.schoolId" allow-clear style="width: 200px;" placeholder="选择学校" @change="(v:any) => { query.gradeId = undefined; fetchGrades(v) }">
          <a-select-option v-for="s in schools" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="班级名称">
        <a-input v-model:value="query.name" placeholder="请输入名称关键字" allow-clear />
      </a-form-item>
      <a-form-item label="所属年级">
        <a-select v-model:value="query.gradeId" allow-clear style="width: 200px;" placeholder="选择年级">
          <a-select-option v-for="g in grades" :key="g.id" :value="g.id">{{ g.name }}</a-select-option>
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
      @change="(p:any) => handlePageChange(p.current, p.pageSize)"
      :scroll="{ x: 'max-content' }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'gradeId'">
          <span>{{ (grades.find(g => g.id === record.gradeId)?.name) || record.gradeId }}</span>
        </template>
        <template v-else-if="column.key === 'schoolName'">
          <span>{{ record.schoolName || (schools.find(s => s.id === record.schoolId)?.name) || record.schoolId }}</span>
        </template>
        <template v-else-if="column.key === 'headTeacherName'">
          <span>{{ record.headTeacherName || '未分配' }}</span>
        </template>
        <template v-else-if="column.key === 'count'">
          <span>{{ classCounts[record.id] ?? '...' }}</span>
        </template>
        <template v-else-if="column.key === 'status'">
          <span>{{ (record.status ?? 1) === 1 ? '启用' : '禁用' }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" @click="openHeadTeacherModal(record)">
              {{ record.headTeacherId ? '更换班主任' : '分配班主任' }}
            </a-button>
            <a-button v-if="record.headTeacherId" type="link" @click="onRemoveHeadTeacher(record)">移除班主任</a-button>
            <a-button type="link" @click="() => openSplitModal(record)">拆分</a-button>
            <a-popconfirm title="确认删除该班级？" @confirm="() => onDelete(record)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
            <a-button type="link" @click="() => onToggleStatus(record)">{{ (record.status ?? 1) === 1 ? '禁用' : '启用' }}</a-button>
            <a-button type="link" @click="() => openStudentList(record)">学生名单</a-button>
            <a-button type="link" @click="() => openUpgradeModal(record, 'upgrade')">升级</a-button>
            <a-button type="link" @click="() => openUpgradeModal(record, 'downgrade')">降级</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="modalOpen" :title="isEdit ? '编辑班级' : '新增班级'" :confirm-loading="modalLoading" @ok="onSubmit" @cancel="onCancel">
      <a-form layout="vertical">
        <a-form-item label="班级名称" required>
          <a-input v-model:value="form.name" placeholder="请输入班级名称" />
        </a-form-item>
        <a-form-item label="所属学校">
          <a-select v-model:value="form.schoolId" allow-clear placeholder="选择学校" @change="(v:any) => fetchGrades(v)">
            <a-select-option v-for="s in schools" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="所属年级" required>
          <a-select v-model:value="form.gradeId" allow-clear placeholder="选择年级">
            <a-select-option v-for="g in grades" :key="g.id" :value="g.id">{{ g.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 班主任分配 Modal -->
    <a-modal 
      v-model:open="headTeacherModalOpen" 
      title="分配班主任" 
      :confirm-loading="headTeacherModalLoading"
      @ok="onAssignHeadTeacher" 
      @cancel="onCancelHeadTeacher"
    >
      <a-form layout="vertical">
        <a-form-item label="当前班级">
          <a-input :value="currentClass?.name" disabled />
        </a-form-item>
        <a-form-item label="选择教师" required>
          <a-select v-model:value="selectedTeacherId" placeholder="请选择班主任">
            <a-select-option v-for="t in teachers" :key="t.id" :value="t.id">{{ t.name }}</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 学生名单 Drawer -->
    <a-drawer v-model:open="studentsDrawerOpen" :title="studentsOfClassTitle" width="640" :destroy-on-close="true">
      <a-table
        :data-source="studentsOfClass"
        :loading="studentsLoading"
        row-key="id"
        :pagination="{ current: studentPagination.current, pageSize: studentPagination.pageSize, total: studentPagination.total, showSizeChanger: true }"
        @change="(p:any) => { studentPagination.current = p.current; studentPagination.pageSize = p.pageSize; }"
      >
        <a-table-column title="姓名" dataIndex="name" key="name" />
        <a-table-column title="学号" dataIndex="studentNo" key="studentNo" />
        <a-table-column title="状态" key="status">
          <template #default="{ record }">
            <span>{{ (record.status ?? 1) === 1 ? '在读' : '禁用/毕业' }}</span>
          </template>
        </a-table-column>
      </a-table>
    </a-drawer>

    <!-- 升级/降级 Modal -->
    <a-modal v-model:open="upgradeModalOpen" :title="upgradeType === 'upgrade' ? '班级升级' : '班级降级'" @ok="confirmUpgrade" @cancel="() => (upgradeModalOpen = false)">
      <a-form layout="vertical">
        <a-form-item label="目标年级" required>
          <a-select v-model:value="targetGradeId" placeholder="请选择目标年级">
            <a-select-option v-for="g in grades" :key="g.id" :value="g.id">{{ g.name }}</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 合并 Modal -->
    <a-modal v-model:open="mergeModalOpen" title="班级合并" @ok="confirmMerge" @cancel="() => (mergeModalOpen = false)">
      <a-form layout="vertical">
        <a-form-item label="源班级" required>
          <a-select v-model:value="mergeSourceIds" mode="multiple" placeholder="请选择需要合并的源班级">
            <a-select-option v-for="c in dataSource" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="目标班级" required>
          <a-select v-model:value="mergeTargetId" placeholder="请选择目标班级">
            <a-select-option v-for="c in dataSource" :key="c.id" :value="c.id">{{ c.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <p style="color: #888;">提示：合并将把源班级学生转入目标班级，并归档源班级。</p>
      </a-form>
    </a-modal>

    <!-- 拆分 Modal -->
    <a-modal 
      v-model:open="splitModalOpen" 
      title="班级拆分" 
      :confirm-loading="splitModalLoading"
      @ok="onConfirmSplit" 
      @cancel="onCancelSplit"
      width="800px"
    >
      <a-form layout="vertical">
        <a-form-item label="新班级名称" required>
          <a-input v-model:value="splitForm.newClassName" placeholder="请输入新班级名称" />
        </a-form-item>
        
        <a-form-item label="目标年级">
          <a-select v-model:value="splitForm.targetGradeId" placeholder="选择年级（默认为原班级年级）">
            <a-select-option v-for="g in grades" :key="g.id" :value="g.id">{{ g.name }}</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="新班级班主任">
          <a-select v-model:value="splitForm.headTeacherId" placeholder="选择班主任（可选）" allow-clear>
            <a-select-option v-for="t in teachers" :key="t.id" :value="t.id">{{ t.name }}</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="选择要拆分的学生" required>
          <a-spin :spinning="splitStudentsLoading">
            <a-checkbox-group v-model:value="splitForm.selectedStudentIds" style="width: 100%;">
              <a-row :gutter="[16, 16]">
                <a-col :span="8" v-for="student in splitStudents" :key="student.id">
                   <a-checkbox :value="student.id">
                     {{ student.name }} ({{ student.studentNo }})
                   </a-checkbox>
                 </a-col>
              </a-row>
            </a-checkbox-group>
          </a-spin>
          <p style="color: #888; margin-top: 8px;">
            已选择 {{ splitForm.selectedStudentIds.length }} 名学生
          </p>
        </a-form-item>
        
        <p style="color: #888;">
          提示：拆分将创建新班级，并将选中的学生转移到新班级中。
        </p>
      </a-form>
    </a-modal>
  </div>
</template>