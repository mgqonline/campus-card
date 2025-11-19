<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { tGetProfile, tGetClassStudents, type TProfileResp, tTeacherHelpApply } from '@/api/modules/teacher'

const router = useRouter()
const profile = ref<TProfileResp | null>(null)
const classId = ref<number | null>(null)
const students = ref<Array<{ id: number; name: string }>>([])
const loadError = ref('')

const form = ref({
  childId: 0,
  type: 'SICK',
  startTime: '',
  endTime: '',
  reason: '',
  attachments: [] as string[]
})
const submitting = ref(false)

function toDatetimeLocalString(d: Date) {
  const pad = (n: number) => (n < 10 ? '0' + n : '' + n)
  const year = d.getFullYear()
  const month = pad(d.getMonth() + 1)
  const day = pad(d.getDate())
  const hours = pad(d.getHours())
  const minutes = pad(d.getMinutes())
  return `${year}-${month}-${day}T${hours}:${minutes}`
}

async function loadStudents() {
  if (!classId.value) return
  try {
    const list = await tGetClassStudents(classId.value)
    students.value = list.map(i => ({ id: i.id, name: i.name }))
  } catch (e: any) {
    loadError.value = e?.message || '学生列表加载失败'
  }
}

async function submit() {
  if (!form.value.childId) { alert('请选择学生'); return }
  if (!form.value.startTime || !form.value.endTime) { alert('请填写开始与结束时间'); return }
  if (!form.value.reason) { alert('请填写原因'); return }
  submitting.value = true
  try {
    const resp = await tTeacherHelpApply({
      childId: form.value.childId,
      type: form.value.type,
      startTime: form.value.startTime,
      endTime: form.value.endTime,
      reason: form.value.reason,
      attachments: form.value.attachments
    })
    alert('帮请成功，状态：' + resp.status)
    router.replace('/t/leave')
  } catch (e: any) {
    alert(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (!localStorage.getItem('t_token')) { router.replace('/t/login'); return }
  try {
    profile.value = await tGetProfile()
    // 时间默认：开始=当前，结束=次日
    const now = new Date()
    const end = new Date(now.getTime() + 24 * 60 * 60 * 1000)
    form.value.startTime = toDatetimeLocalString(now)
    form.value.endTime = toDatetimeLocalString(end)
    // 班级与学生必须由老师自行选择，不做默认
    classId.value = null
  } catch (e: any) {
    loadError.value = e?.message || '加载失败'
  }
})

watch(classId, async (val) => { if (val) { form.value.childId = 0; await loadStudents() } else { students.value = []; form.value.childId = 0 } })
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">老师帮请</div>
      <div class="weui-panel__bd">
        <div v-if="loadError" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">{{ loadError }}</span></div>
        <div v-else-if="!profile" class="weui-loadmore"><i class="weui-loading"></i><span class="weui-loadmore__tips">加载中</span></div>
        <template v-else>
          <div class="weui-cells__title">选择学生</div>
          <div class="weui-cells weui-cells_form">
            <div class="weui-cell">
              <div class="weui-cell__hd"><label class="weui-label">班级<span class="req">*</span></label></div>
              <div class="weui-cell__bd">
                <select class="weui-input" v-model="classId">
                  <option :value="null">请选择班级</option>
                  <option v-for="c in (profile!.assignedClasses||[])" :key="c.classId" :value="c.classId">
                    {{ (c as any).className || c.classId }}
                  </option>
                </select>
              </div>
            </div>
            <div class="weui-cell">
              <div class="weui-cell__hd"><label class="weui-label">学生<span class="req">*</span></label></div>
              <div class="weui-cell__bd">
                <select class="weui-input" v-model.number="form.childId">
                  <option :value="0">请选择学生</option>
                  <option v-for="s in students" :key="s.id" :value="s.id">{{ s.name }}（{{ s.id }}）</option>
                </select>
              </div>
            </div>
          </div>

          <div class="weui-cells__title">请假信息</div>
          <div class="weui-cells weui-cells_form">
            <div class="weui-cell">
              <div class="weui-cell__hd"><label class="weui-label">类型</label></div>
              <div class="weui-cell__bd">
                <select class="weui-input" v-model="form.type">
                  <option value="SICK">病假</option>
                  <option value="PERSONAL">事假</option>
                  <option value="OTHER">其他</option>
                </select>
              </div>
            </div>
            <div class="weui-cell">
              <div class="weui-cell__hd"><label class="weui-label">开始时间</label></div>
              <div class="weui-cell__bd"><input class="weui-input" type="datetime-local" v-model="form.startTime" /></div>
            </div>
            <div class="weui-cell">
              <div class="weui-cell__hd"><label class="weui-label">结束时间</label></div>
              <div class="weui-cell__bd"><input class="weui-input" type="datetime-local" v-model="form.endTime" /></div>
            </div>
            <div class="weui-cell">
              <div class="weui-cell__hd"><label class="weui-label">原因<span class="req">*</span></label></div>
              <div class="weui-cell__bd"><input class="weui-input" v-model="form.reason" placeholder="请填写原因" /></div>
            </div>
          </div>

          <div class="weui-btn-area">
            <button class="weui-btn weui-btn_primary" :disabled="submitting" @click="submit">提交帮请</button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { padding-bottom: 16px; }
.req { color:#f33; margin-left:4px; }
</style>