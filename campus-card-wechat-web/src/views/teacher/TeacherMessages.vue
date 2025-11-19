<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { tGetProfile, tTeacherSchoolNotify, tTeacherClassSendNotify, tTeacherListParentMessages, tTeacherReplyParentMessage, tGetClassStudents, type TProfileResp } from '@/api/modules/teacher'

const router = useRouter()
const profile = ref<TProfileResp | null>(null)
const classId = ref<number | null>(null)
const notifyPage = ref({ page: 0, size: 10 })
const schoolNotices = ref<Array<any>>([])
const noticeTitle = ref('')
const noticeContent = ref('')
const students = ref<Array<{ id: number; name: string }>>([])
const childId = ref<number | null>(null)
const messages = ref<Array<any>>([])
const replyContent = ref('')
const loading = ref(false)
const err = ref('')

async function loadSchoolNotices() {
  try { const res = await tTeacherSchoolNotify({ page: notifyPage.value.page, size: notifyPage.value.size }); schoolNotices.value = res.content || [] } catch (e: any) { err.value = e?.message || '通知加载失败' }
}

async function sendClassNotice() {
  if (!classId.value) return
  if (!noticeTitle.value || !noticeContent.value) { alert('请填写标题和内容'); return }
  try { await tTeacherClassSendNotify({ classId: classId.value, title: noticeTitle.value, content: noticeContent.value }); noticeTitle.value=''; noticeContent.value=''; alert('已发送'); } catch (e: any) { alert(e?.message || '发送失败') }
}

async function loadMessages() {
  if (!childId.value) { messages.value = []; return }
  try { const res = await tTeacherListParentMessages({ childId: childId.value, page: 0, size: 20 }); messages.value = res.content || [] } catch (e: any) { err.value = e?.message || '消息加载失败' }
}

async function replyParent() {
  if (!childId.value || !replyContent.value) { alert('请选择学生并填写回复内容'); return }
  try { await tTeacherReplyParentMessage({ childId: childId.value, content: replyContent.value }); replyContent.value=''; await loadMessages(); } catch (e: any) { alert(e?.message || '回复失败') }
}

onMounted(async () => {
  if (!localStorage.getItem('t_token')) { router.replace('/t/login'); return }
  loading.value = true
  try {
    profile.value = await tGetProfile();
    classId.value = (profile.value!.assignedClasses[0]||{}).classId || 301
    // 拉取班级学生列表，并选取第一个学生作为默认会话
    const list = await tGetClassStudents(classId.value)
    students.value = (list || []).map(s => ({ id: s.id, name: s.name }))
    childId.value = students.value[0]?.id ?? null
    await loadSchoolNotices()
    await loadMessages()
  } catch (e: any) { err.value = e?.message || '加载失败' }
  finally { loading.value=false }
})
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">学校通知</div>
      <div class="weui-panel__bd">
        <div v-if="loading" class="weui-loadmore"><i class="weui-loading"></i><span class="weui-loadmore__tips">加载中</span></div>
        <div v-else>
          <div v-for="n in schoolNotices" :key="n.id" class="weui-media-box weui-media-box_text">
            <h4 class="weui-media-box__title">{{ n.title }}</h4>
            <p class="weui-media-box__desc">{{ n.content }}</p>
            <ul class="weui-media-box__info"><li class="weui-media-box__info__meta">{{ n.createdAt }}</li></ul>
          </div>
        </div>
      </div>
    </div>

    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">班级通知发送</div>
      <div class="weui-panel__bd">
        <div class="weui-cells">
          <div class="weui-cell">
            <div class="weui-cell__bd"><input class="weui-input" placeholder="标题" v-model="noticeTitle" /></div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__bd"><textarea class="weui-textarea" rows="3" placeholder="内容" v-model="noticeContent"></textarea></div>
          </div>
        </div>
        <div style="padding:12px;">
          <button class="weui-btn weui-btn_primary" @click="sendClassNotice">发送</button>
        </div>
      </div>
    </div>

    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">家长消息回复</div>
      <div class="weui-panel__bd">
        <div class="weui-cells__title">选择学生会话</div>
        <div class="weui-cells">
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <select class="weui-select" v-model.number="childId" @change="loadMessages">
                <option v-if="students.length===0" value="">暂无学生</option>
                <option v-for="s in students" :key="s.id" :value="s.id">{{ s.name }}（ID: {{ s.id }}）</option>
              </select>
            </div>
          </div>
        </div>
        <div class="weui-cells__title">近期消息</div>
        <div class="weui-cells">
          <div v-for="m in messages" :key="m.id" class="weui-cell">
            <div class="weui-cell__bd">
              <p>{{ m.senderRole }}：{{ m.content }}</p>
              <p style="color:#999;">{{ m.createdAt }}</p>
            </div>
          </div>
        </div>
        <div class="weui-cells__title">回复家长</div>
        <div class="weui-cells">
          <div class="weui-cell">
            <div class="weui-cell__bd"><textarea class="weui-textarea" rows="3" placeholder="回复内容" v-model="replyContent"></textarea></div>
          </div>
        </div>
        <div style="padding:12px;">
          <button class="weui-btn weui-btn_primary" @click="replyParent">发送回复</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { padding-bottom: 16px; }
</style>