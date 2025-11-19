<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getClassList, assignHeadTeacher, removeHeadTeacher, type ClassInfo } from '@/api/modules/clazz'
import { getGradeList, type GradeInfo } from '@/api/modules/grade'
import { getTeacherList, type TeacherInfo } from '@/api/modules/teacher'
import { listSubjects, createSubject, type Subject } from '@/api/modules/subject'
import { listClassSubjectTeachers, assignSubjectTeacher, removeSubjectTeacherById } from '@/api/modules/classSubjectTeacher'
import { schoolApi, type School } from '@/api/modules/school'

const loading = ref(false)
const classes = ref<ClassInfo[]>([])
const grades = ref<GradeInfo[]>([])
const teachers = ref<TeacherInfo[]>([])
const subjects = ref<Subject[]>([])
const schools = ref<School[]>([])

const query = ref<{ schoolId?: number; gradeId?: number }>({})
const selectedClass = ref<ClassInfo | null>(null)

// 班主任分配
const headTeacherModalOpen = ref(false)
const headTeacherModalLoading = ref(false)
const selectedHeadTeacherId = ref<number | undefined>()

// 任课分配
const subjectTeacherList = ref<any[]>([])
const assignSubjectId = ref<number | undefined>()
const assignTeacherId = ref<number | undefined>()
const assignLoading = ref(false)

// 关闭弹窗处理（避免在模板中直接赋值 ref 导致异常）
const closeHeadTeacherModal = () => { headTeacherModalOpen.value = false }
const closeSubjectModal = () => { subjectModalOpen.value = false }

// 新增学科
const subjectModalOpen = ref(false)
const subjectModalLoading = ref(false)
const subjectName = ref('')

const fetchGrades = async (schoolId?: number) => {
  try {
    const res = await getGradeList({ page: 1, size: 200, schoolId })
    grades.value = res.records
  } catch (e) {
    grades.value = []
  }
}

const fetchTeachers = async () => {
  try {
    const res = await getTeacherList({ page: 1, size: 200 })
    teachers.value = res.records
  } catch (e) {
    teachers.value = []
  }
}

const fetchSubjects = async () => {
  if (!query.value.schoolId) { subjects.value = []; return }
  try {
    const res = await listSubjects({ page: 1, size: 200, schoolId: query.value.schoolId })
    subjects.value = res.records
  } catch (e) {
    subjects.value = []
  }
}

const fetchClasses = async () => {
  loading.value = true
  try {
    const res = await getClassList({ page: 1, size: 200, gradeId: query.value.gradeId, schoolId: query.value.schoolId })
    classes.value = res.records
  } catch (e) {
    classes.value = []
  } finally {
    loading.value = false
  }
}

const fetchSchools = async () => {
  try {
    schools.value = await schoolApi.getEnabledList()
  } catch (e) {
    schools.value = []
  }
}

const onSelectClass = async (c: ClassInfo) => {
  selectedClass.value = c
  selectedHeadTeacherId.value = c.headTeacherId
  await loadSubjectTeachers()
}

const loadSubjectTeachers = async () => {
  if (!selectedClass.value) return
  try {
    const list = await listClassSubjectTeachers(selectedClass.value.id)
    subjectTeacherList.value = list
  } catch (e) {
    subjectTeacherList.value = []
  }
}

const openHeadTeacherModal = async () => {
  if (!selectedClass.value) { message.warning('请先选择班级'); return }
  await fetchTeachers()
  headTeacherModalOpen.value = true
}

const onAssignHeadTeacher = async () => {
  if (!selectedClass.value || !selectedHeadTeacherId.value) { message.error('请选择教师'); return }
  headTeacherModalLoading.value = true
  try {
    await assignHeadTeacher(selectedClass.value.id, selectedHeadTeacherId.value)
    message.success('班主任分配成功')
    headTeacherModalOpen.value = false
    // 更新本地
    selectedClass.value.headTeacherId = selectedHeadTeacherId.value
    const t = teachers.value.find(x => x.id === selectedHeadTeacherId.value)
    selectedClass.value.headTeacherName = t?.name
  } catch (e) {
    message.error('分配失败')
  } finally {
    headTeacherModalLoading.value = false
  }
}

const onRemoveHeadTeacher = async () => {
  if (!selectedClass.value) return
  try {
    await removeHeadTeacher(selectedClass.value.id)
    message.success('班主任已移除')
    selectedClass.value.headTeacherId = undefined
    selectedClass.value.headTeacherName = undefined
  } catch (e) {
    message.error('移除失败')
  }
}

const onAssignSubjectTeacher = async () => {
  if (!selectedClass.value) { message.warning('请先选择班级'); return }
  if (!assignSubjectId.value || !assignTeacherId.value) { message.warning('请选择学科与教师'); return }
  assignLoading.value = true
  try {
    await assignSubjectTeacher(selectedClass.value.id, { subjectId: assignSubjectId.value, teacherId: assignTeacherId.value })
    message.success('任课分配成功')
    assignSubjectId.value = undefined
    assignTeacherId.value = undefined
    await loadSubjectTeachers()
  } catch (e) {
    message.error('分配失败')
  } finally {
    assignLoading.value = false
  }
}

const onRemoveSubjectTeacher = async (row: any) => {
  if (!selectedClass.value) return
  try {
    await removeSubjectTeacherById(selectedClass.value.id, row.id)
    message.success('已移除任课')
    await loadSubjectTeachers()
  } catch (e) {
    message.error('移除失败')
  }
}

const openCreateSubject = () => {
  if (!query.value.schoolId) { message.warning('请先选择学校'); return }
  subjectName.value = ''
  subjectModalOpen.value = true
}

const onCreateSubject = async () => {
  if (!query.value.schoolId) { message.error('未选择学校'); return }
  const name = subjectName.value.trim()
  if (!name) { message.warning('请输入学科名称'); return }
  subjectModalLoading.value = true
  try {
    await createSubject({ name, schoolId: query.value.schoolId, status: 1 })
    message.success('学科已创建')
    subjectModalOpen.value = false
    await fetchSubjects()
  } catch (e: any) {
    message.error(e?.message || '创建失败')
  } finally {
    subjectModalLoading.value = false
  }
}

onMounted(async () => {
  await fetchSchools()
  await fetchGrades()
  await fetchTeachers()
  await fetchSubjects()
  await fetchClasses()
})
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="班级组织架构" />

    <a-form layout="inline" style="margin-bottom: 16px;">
      <a-form-item label="所属学校">
        <a-select v-model:value="query.schoolId" allow-clear style="width: 200px;" placeholder="选择学校" @change="(v:any) => { query.schoolId = v; query.gradeId = undefined; fetchGrades(v); fetchSubjects(); fetchClasses(); }">
          <a-select-option v-for="s in schools" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="所属年级">
        <a-select v-model:value="query.gradeId" allow-clear style="width: 200px;" placeholder="选择年级" @change="() => fetchClasses()">
          <a-select-option v-for="g in grades" :key="g.id" :value="g.id">{{ g.name }}</a-select-option>
        </a-select>
      </a-form-item>
    </a-form>

    <div style="display: grid; grid-template-columns: 320px 1fr; gap: 16px;">
      <a-card title="班级列表" :loading="loading">
        <a-list :data-source="classes" size="small" bordered>
          <template #renderItem="{ item }">
            <a-list-item style="cursor: pointer;" :class="{ 'ant-list-item-selected': selectedClass?.id === item.id }" @click="() => onSelectClass(item)">
              <div style="display:flex; justify-content: space-between; width:100%;">
                <span>{{ item.name }}</span>
                <span style="color:#888;">{{ item.headTeacherName || '未分配班主任' }}</span>
              </div>
            </a-list-item>
          </template>
        </a-list>
      </a-card>

      <div>
        <a-card title="班主任分配" style="margin-bottom: 16px;">
          <div v-if="selectedClass">
            <p>当前班级：<b>{{ selectedClass.name }}</b></p>
            <p>当前班主任：<b>{{ selectedClass.headTeacherName || '未分配' }}</b></p>
            <a-space>
              <a-button type="primary" @click="openHeadTeacherModal">分配/更换班主任</a-button>
              <a-button danger :disabled="!selectedClass.headTeacherId" @click="onRemoveHeadTeacher">移除班主任</a-button>
            </a-space>
          </div>
          <div v-else style="color:#888;">请选择左侧班级</div>
        </a-card>

        <a-card title="任课教师分配">
          <div v-if="selectedClass">
            <a-form layout="inline" style="margin-bottom: 12px; display:flex; gap:8px; align-items:flex-end;">
              <a-form-item label="学科">
                <a-select v-model:value="assignSubjectId" style="width: 200px;" placeholder="选择学科" :disabled="!subjects.length">
                  <a-select-option v-for="s in subjects" :key="s.id" :value="s.id">{{ s.name }}</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item>
                <a-button @click="openCreateSubject" :disabled="!query.schoolId">新增学科</a-button>
              </a-form-item>
              <a-form-item label="教师">
                <a-select v-model:value="assignTeacherId" style="width: 200px;" placeholder="选择教师">
                  <a-select-option v-for="t in teachers" :key="t.id" :value="t.id">{{ t.name }}</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item>
                <a-button
                  type="primary"
                  :loading="assignLoading"
                  @click="onAssignSubjectTeacher"
                  :disabled="!selectedClass || !assignSubjectId || !assignTeacherId"
                >分配任课</a-button>
              </a-form-item>
            </a-form>

            <div v-if="query.schoolId && subjects.length === 0" style="margin-bottom: 12px; color:#888;">
              当前学校暂无学科，请先新增学科。
            </div>

            <a-table :data-source="subjectTeacherList" row-key="id">
              <a-table-column title="学科" key="subject">
                <template #default="{ record }">
                  <span>{{ (subjects.find(s => s.id === record.subjectId)?.name) || record.subjectId }}</span>
                </template>
              </a-table-column>
              <a-table-column title="教师" key="teacher">
                <template #default="{ record }">
                  <span>{{ (teachers.find(t => t.id === record.teacherId)?.name) || record.teacherId }}</span>
                </template>
              </a-table-column>
              <a-table-column title="操作" key="action">
                <template #default="{ record }">
                  <a-button type="link" danger @click="() => onRemoveSubjectTeacher(record)">移除</a-button>
                </template>
              </a-table-column>
            </a-table>
          </div>
          <div v-else style="color:#888;">请选择左侧班级</div>
        </a-card>
      </div>
    </div>

    <!-- 班主任分配 Modal -->
    <a-modal v-model:open="headTeacherModalOpen" title="分配班主任" :confirm-loading="headTeacherModalLoading" @ok="onAssignHeadTeacher" @cancel="closeHeadTeacherModal">
      
      <a-form layout="vertical">
        <a-form-item label="当前班级">
          <a-input :value="selectedClass ? selectedClass.name : ''" disabled />
        </a-form-item>
        <a-form-item label="选择教师" required>
          <a-select v-model:value="selectedHeadTeacherId" placeholder="请选择班主任">
            <a-select-option v-for="t in teachers" :key="t.id" :value="t.id">{{ t.name }}</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 新增学科 Modal -->
    <a-modal v-model:open="subjectModalOpen" title="新增学科" :confirm-loading="subjectModalLoading" @ok="onCreateSubject" @cancel="closeSubjectModal">
      <a-form layout="vertical">
        <a-form-item label="所属学校">
          <a-input :value="schools.find(s => s.id === query.schoolId)?.name || '未选择'" disabled />
        </a-form-item>
        <a-form-item label="学科名称" required>
          <a-input v-model:value="subjectName" placeholder="请输入学科名称，如语文/数学/英语" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>