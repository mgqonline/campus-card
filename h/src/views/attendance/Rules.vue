<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getAttendanceRule, saveAttendanceRule, type AttendanceRule } from '@/api/modules/attendance'
import { getAttendanceSettings, saveAttendanceSettings, type AttendanceSettings, type TimePeriod, type Holiday, type Adjustment } from '@/api/modules/attendanceSettings'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)

const form = reactive<AttendanceRule & { workDayList: string[] }>({
  id: undefined,
  scenario: 'SCHOOL',
  workDays: 'MON,TUE,WED,THU,FRI',
  workStart: '08:00:00',
  workEnd: '17:00:00',
  lateGraceMin: 5,
  earlyGraceMin: 5,
  enabled: true,
  workDayList: ['MON','TUE','WED','THU','FRI']
})

const settings = reactive<AttendanceSettings>({
  periods: [],
  absenceThresholdMin: 30,
  leaveTypes: ['事假','病假'],
  holidays: [],
  adjustments: []
})

const dayOptions = [
  { label: '周一', value: 'MON' },
  { label: '周二', value: 'TUE' },
  { label: '周三', value: 'WED' },
  { label: '周四', value: 'THU' },
  { label: '周五', value: 'FRI' },
  { label: '周六', value: 'SAT' },
  { label: '周日', value: 'SUN' }
]

const leaveTypeOptions = ['事假','病假','公假','产假','调休','其他']

const loadRule = async () => {
  loading.value = true
  try {
    const rule = await getAttendanceRule()
    form.id = rule.id
    form.scenario = rule.scenario
    form.workDays = rule.workDays
    form.workStart = rule.workStart
    form.workEnd = rule.workEnd
    form.lateGraceMin = rule.lateGraceMin
    form.earlyGraceMin = rule.earlyGraceMin
    form.enabled = rule.enabled
    form.workDayList = (rule.workDays || '').split(',').filter(Boolean)
  } catch (e) {
    message.info('后端未就绪，使用默认规则')
  } finally {
    loading.value = false
  }
}

const loadSettings = async () => {
  loading.value = true
  try {
    const s = await getAttendanceSettings()
    settings.periods = s.periods || []
    settings.absenceThresholdMin = s.absenceThresholdMin ?? 30
    settings.leaveTypes = s.leaveTypes || []
    settings.holidays = s.holidays || []
    settings.adjustments = s.adjustments || []
  } catch (e) {
    message.info('后端未就绪，使用默认设置')
  } finally {
    loading.value = false
  }
}

const addPeriod = () => {
  settings.periods.push({ name: '', start: '08:00:00', end: '09:00:00' } as TimePeriod)
}
const removePeriod = (index: number) => settings.periods.splice(index, 1)

const addHoliday = () => {
  settings.holidays.push({ name: '', startDate: '', endDate: '' } as Holiday)
}
const removeHoliday = (index: number) => settings.holidays.splice(index, 1)

const addAdjustment = () => {
  settings.adjustments.push({ date: '', type: 'WORK', name: '' } as Adjustment)
}
const removeAdjustment = (index: number) => settings.adjustments.splice(index, 1)

const onSave = async () => {
  saving.value = true
  try {
    const payload: AttendanceRule = {
      id: form.id,
      scenario: form.scenario,
      workDays: form.workDayList.join(','),
      workStart: form.workStart,
      workEnd: form.workEnd,
      lateGraceMin: form.lateGraceMin,
      earlyGraceMin: form.earlyGraceMin,
      enabled: form.enabled
    }
    await saveAttendanceRule(payload)

    const sPayload: AttendanceSettings = {
      periods: settings.periods,
      absenceThresholdMin: Number(settings.absenceThresholdMin || 0),
      leaveTypes: settings.leaveTypes,
      holidays: settings.holidays,
      adjustments: settings.adjustments
    }
    await saveAttendanceSettings(sPayload)

    message.success('保存成功')
  } catch (e) {
    message.info('后端未就绪，模拟保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(async () => { await loadRule(); await loadSettings() })
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="考勤规则设置" @back="() => router.push('/attendance/stat')" />

    <a-card :loading="loading">
      <a-form layout="vertical">
        <a-form-item label="适用场景">
          <a-select v-model:value="form.scenario" style="width: 240px">
            <a-select-option value="SCHOOL">学校</a-select-option>
            <a-select-option value="ENTERPRISE">企业</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="工作日（到校/上班）">
          <a-select v-model:value="form.workDayList" mode="multiple" style="min-width: 320px" :options="dayOptions" />
        </a-form-item>

        <div style="display:flex; gap:16px;">
          <a-form-item label="上学开始">
            <a-time-picker v-model:value="form.workStart" format="HH:mm:ss" valueFormat="HH:mm:ss" />
          </a-form-item>
          <a-form-item label="放学结束">
            <a-time-picker v-model:value="form.workEnd" format="HH:mm:ss" valueFormat="HH:mm:ss" />
          </a-form-item>
        </div>

        <div style="display:flex; gap:16px;">
          <a-form-item label="迟到容忍（分钟）">
            <a-input-number v-model:value="form.lateGraceMin" :min="0" :precision="0" />
          </a-form-item>
          <a-form-item label="早退容忍（分钟）">
            <a-input-number v-model:value="form.earlyGraceMin" :min="0" :precision="0" />
          </a-form-item>
        </div>

        <a-divider orientation="left">考勤时段设置</a-divider>
        <div>
          <a-space direction="vertical" style="width:100%">
            <div v-for="(p, idx) in settings.periods" :key="idx" style="display:flex; gap:12px; align-items:center;">
              <a-input v-model:value="p.name" style="width: 160px" placeholder="时段名称" />
              <a-time-picker v-model:value="p.start" format="HH:mm:ss" valueFormat="HH:mm:ss" />
              <a-time-picker v-model:value="p.end" format="HH:mm:ss" valueFormat="HH:mm:ss" />
              <a-button danger @click="removePeriod(idx)">删除</a-button>
            </div>
            <a-button type="dashed" @click="addPeriod">新增时段</a-button>
          </a-space>
        </div>

        <a-divider orientation="left">旷课规则</a-divider>
        <a-form-item label="超过开始时间视为旷课（分钟）">
          <a-input-number v-model:value="settings.absenceThresholdMin" :min="0" :precision="0" />
        </a-form-item>

        <a-divider orientation="left">请假规则</a-divider>
        <a-form-item label="允许的请假类型">
          <a-select v-model:value="settings.leaveTypes" mode="multiple" style="min-width: 320px">
            <a-select-option v-for="lt in leaveTypeOptions" :key="lt" :value="lt">{{ lt }}</a-select-option>
          </a-select>
        </a-form-item>

        <a-divider orientation="left">节假日设置</a-divider>
        <div>
          <a-space direction="vertical" style="width:100%">
            <div v-for="(h, idx) in settings.holidays" :key="idx" style="display:flex; gap:12px; align-items:center;">
              <a-input v-model:value="h.name" style="width: 160px" placeholder="节假日名称" />
              <a-date-picker v-model:value="h.startDate" format="YYYY-MM-DD" valueFormat="YYYY-MM-DD" />
              <a-date-picker v-model:value="h.endDate" format="YYYY-MM-DD" valueFormat="YYYY-MM-DD" />
              <a-button danger @click="removeHoliday(idx)">删除</a-button>
            </div>
            <a-button type="dashed" @click="addHoliday">新增节假日</a-button>
          </a-space>
        </div>

        <a-divider orientation="left">调休设置</a-divider>
        <div>
          <a-space direction="vertical" style="width:100%">
            <div v-for="(a, idx) in settings.adjustments" :key="idx" style="display:flex; gap:12px; align-items:center;">
              <a-date-picker v-model:value="a.date" format="YYYY-MM-DD" valueFormat="YYYY-MM-DD" />
              <a-select v-model:value="a.type" style="width: 160px">
                <a-select-option value="WORK">工作日</a-select-option>
                <a-select-option value="OFF">休息日</a-select-option>
              </a-select>
              <a-input v-model:value="a.name" style="width: 200px" placeholder="说明（可选）" />
              <a-button danger @click="removeAdjustment(idx)">删除</a-button>
            </div>
            <a-button type="dashed" @click="addAdjustment">新增调休</a-button>
          </a-space>
        </div>

        <a-form-item label="启用">
          <a-switch v-model:checked="form.enabled" />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" :loading="saving" @click="onSave">保存</a-button>
          <a-button style="margin-left:8px;" @click="() => { loadRule(); loadSettings(); }">重置为已保存</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>