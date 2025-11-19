<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { applyLeave, uploadAttachment, type LeaveType } from '@/api/modules/leave'

const type = ref<LeaveType>('SICK')
const startTime = ref('')
const endTime = ref('')
const reason = ref('')
const attachments = ref<string[]>([])
const attachmentItems = ref<Array<{ url: string; name?: string }>>([])
const uploading = ref(false)
const submitting = ref(false)
const error = ref('')
const successMsg = ref('')
const router = useRouter()

// 改善时间选择体验：使用弹层选择器（移动端更友好）
const showStartPicker = ref(false)
const showEndPicker = ref(false)
const now = new Date()
const minDate = new Date(now.getFullYear() - 1, 0, 1)
const maxDate = new Date(now.getFullYear() + 1, 11, 31)
const pad = (n: number) => String(n).padStart(2, '0')
const toLocalISO = (d: Date) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`

// 默认值：开始时间为当前，结束时间为次日
startTime.value = toLocalISO(now)
const tomorrow = new Date(now.getTime() + 24 * 60 * 60 * 1000)
endTime.value = toLocalISO(tomorrow)
const startTimeDisplay = computed(() => startTime.value || '')
const endTimeDisplay = computed(() => endTime.value || '')
const fromPickerValues = (val: any): string => {
  // Vant v4 DatePicker：confirm 返回数组 [yyyy, mm, dd, hh, mm]
  if (Array.isArray(val)) {
    const [y, m, d, h = 0, mi = 0] = val.map((v) => Number(v))
    const date = new Date(y, (m || 1) - 1, d || 1, h || 0, mi || 0)
    return toLocalISO(date)
  }
  if (val instanceof Date) return toLocalISO(val)
  if (Array.isArray((val as any)?.selectedValues)) return fromPickerValues((val as any).selectedValues)
  return ''
}
function onStartConfirm(val: any) { startTime.value = fromPickerValues(val); showStartPicker.value = false }
function onEndConfirm(val: any) { endTime.value = fromPickerValues(val); showEndPicker.value = false }

function currentChildId(): number | undefined {
  const cid = localStorage.getItem('wx_child_id')
  return cid ? Number(cid) : undefined
}

async function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const files = input.files
  if (!files || !files.length) return
  uploading.value = true
  error.value = ''
  try {
    for (const f of Array.from(files)) {
      const resp = await uploadAttachment(f)
      attachments.value.push(resp.url)
      attachmentItems.value.push({ url: resp.url, name: (resp as any).name })
    }
  } catch (err: any) {
    error.value = err?.message || '附件上传失败'
  } finally {
    uploading.value = false
    input.value = ''
  }
}

function validate(): string | null {
  if (!startTime.value || !endTime.value) return '请完善开始/结束时间'
  if (new Date(startTime.value) > new Date(endTime.value)) return '开始时间不能晚于结束时间'
  if (!reason.value || reason.value.trim().length < 5) return '请填写不少于5字的请假原因'
  return null
}

async function submit() {
  const msg = validate()
  if (msg) { error.value = msg; return }
  submitting.value = true
  error.value = ''
  successMsg.value = ''
  try {
    const resp = await applyLeave({ childId: currentChildId(), type: type.value, startTime: startTime.value, endTime: endTime.value, reason: reason.value.trim(), attachments: attachments.value })
    successMsg.value = `提交成功，单号：${resp.id}，状态：${resp.status}`
    // 跳转至记录页，便于查看审批状态
    setTimeout(() => router.push('/leave/records'), 800)
  } catch (err: any) {
    error.value = err?.message || '提交失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page">
    <h3 class="section-title"><van-icon name="todo-list-o" style="margin-right:6px;" />请假申请</h3>

    <!-- 填写须知（WeUI 面板） -->
    <div class="weui-panel weui-panel_access" style="margin-bottom:10px;">
      <div class="weui-panel__hd">填写须知</div>
      <div class="weui-panel__bd">
        <div class="weui-cells">
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <p class="tip">请准确填写开始与结束时间，避免与课程考试冲突。</p>
            </div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <p class="tip">支持上传病假条等附件，单次最多选择 5 个文件。</p>
            </div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <p class="tip">提交后可在“请假记录”中查看审批进度，必要时可撤销。</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 表单（WeUI 风格） -->
    <div class="weui-cells__title">请假信息</div>
    <div class="weui-cells weui-cells_form card">
      <div class="weui-cells__title">请假类型</div>
      <div class="weui-cells weui-cells_radio" style="margin:0;">
        <label class="weui-cell weui-check__label" for="type-sick">
          <div class="weui-cell__bd">病假</div>
          <div class="weui-cell__ft">
            <input id="type-sick" type="radio" class="weui-check" name="leave-type" value="SICK" v-model="type">
            <span class="weui-icon-checked"></span>
          </div>
        </label>
        <label class="weui-cell weui-check__label" for="type-personal">
          <div class="weui-cell__bd">事假</div>
          <div class="weui-cell__ft">
            <input id="type-personal" type="radio" class="weui-check" name="leave-type" value="PERSONAL" v-model="type">
            <span class="weui-icon-checked"></span>
          </div>
        </label>
        <label class="weui-cell weui-check__label" for="type-other">
          <div class="weui-cell__bd">其他</div>
          <div class="weui-cell__ft">
            <input id="type-other" type="radio" class="weui-check" name="leave-type" value="OTHER" v-model="type">
            <span class="weui-icon-checked"></span>
          </div>
        </label>
      </div>

      <div class="weui-cells__title">请假时段</div>
      <div class="weui-cell weui-cell_access" @click="showStartPicker = true">
        <div class="weui-cell__hd"><label class="weui-label">开始时间</label></div>
        <div class="weui-cell__bd">
          <input class="weui-input" type="text" :value="startTimeDisplay" placeholder="请选择开始时间" readonly />
        </div>
        <div class="weui-cell__ft"></div>
      </div>
      <div class="weui-cell weui-cell_access" @click="showEndPicker = true">
        <div class="weui-cell__hd"><label class="weui-label">结束时间</label></div>
        <div class="weui-cell__bd">
          <input class="weui-input" type="text" :value="endTimeDisplay" placeholder="请选择结束时间" readonly />
        </div>
        <div class="weui-cell__ft"></div>
      </div>
      <div class="weui-cell">
        <div class="weui-cell__hd"><label class="weui-label">请假原因</label></div>
        <div class="weui-cell__bd"><textarea class="weui-textarea" rows="3" v-model="reason" placeholder="请简要说明"></textarea></div>
      </div>

      <div class="weui-cell">
        <div class="weui-cell__hd"><label class="weui-label">附件</label></div>
        <div class="weui-cell__bd">
          <div class="weui-uploader">
            <div class="weui-uploader__bd">
              <input type="file" multiple @change="onFileChange" />
              <div v-if="attachmentItems.length" class="weui-cells" style="margin-top:6px;">
                <div class="weui-cell" v-for="(f, idx) in attachmentItems" :key="f.url + idx">
                  <div class="weui-cell__hd"><van-icon name="description" /></div>
                  <div class="weui-cell__bd">
                    <p class="file-name">{{ f.name || '附件' }}<span class="muted"> · {{ idx + 1 }}</span></p>
                    <p class="muted">{{ f.url }}</p>
                  </div>
                  <div class="weui-cell__ft"></div>
                </div>
              </div>
            </div>
            <div class="weui-uploader__ft weui-cells__tips">已选择 {{ attachmentItems.length }} 个文件</div>
          </div>
        </div>
      </div>

      <div class="weui-btn-area">
        <button type="button" class="weui-btn weui-btn_primary" :disabled="submitting || uploading" @click="submit">
          <van-icon name="success" style="margin-right:4px;" />{{ submitting ? '提交中...' : '提交申请' }}
        </button>
      </div>
    </div>

    <!-- 表单提示与操作区 -->
    <div class="weui-form__tips-area" style="padding:0 15px;">
      <p class="weui-cells__tips" v-if="!currentChildId()">未选择子女：请返回首页绑定并选择子女后再申请。</p>
      <p class="weui-cells__tips" v-else>审批通常在 24 小时内完成，如遇紧急情况请联系班主任。</p>
    </div>
    <div class="weui-form__opr-area" style="padding:12px;">
      <a href="javascript:;" class="weui-btn weui-btn_default" @click="router.push('/leave/records')">
        <van-icon name="orders-o" style="margin-right:4px;" />查看请假记录
      </a>
    </div>

    <!-- 状态提示 -->
    <div v-if="uploading || submitting" class="weui-loadmore" style="margin-top:12px;">
      <i class="weui-loading"></i>
      <span class="weui-loadmore__tips">{{ uploading ? '上传附件中' : '提交中' }}</span>
    </div>
    <div v-else-if="error" class="weui-toptips weui-toptips_warn" style="display: block; margin-top: 12px;">
      <i class="fa fa-exclamation-triangle"></i> {{ error }}
    </div>
    <div v-else-if="successMsg" class="weui-cells__tips" style="margin-top:12px;">{{ successMsg }}</div>

    <!-- 时间选择器弹层 -->
    <van-popup v-model:show="showStartPicker" round position="bottom">
      <van-date-picker
        type="datetime"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="onStartConfirm"
        @cancel="showStartPicker = false"
      />
    </van-popup>
    <van-popup v-model:show="showEndPicker" round position="bottom">
      <van-date-picker
        type="datetime"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="onEndConfirm"
        @cancel="showEndPicker = false"
      />
    </van-popup>
  </div>
</template>

<style scoped>
.radio-group { gap: 12px; }
.tip { color:#666; font-size:13px; }
.muted { color:#999; font-size:12px; }
.file-name { color:#333; }
</style>