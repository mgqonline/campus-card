<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { tGetAttendanceRecords, tMarkAttendanceException } from '@/api/modules/teacher'

const route = useRoute()
const router = useRouter()
const classId = Number(route.params.id)
const childId = Number(route.params.childId)

const startDate = ref<string>('')
const endDate = ref<string>('')
const loading = ref(false)
const loadError = ref('')
const records = ref<Array<{ id: number; childId: number; childName?: string; time: string; type: string; gate: string; late?: boolean; earlyLeave?: boolean }>>([])

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    const params: any = { childId }
    if (startDate.value && endDate.value) { params.startDate = startDate.value; params.endDate = endDate.value }
    records.value = await tGetAttendanceRecords(params)
  } catch (e: any) {
    loadError.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function mark(id: number, status: 'normal'|'late'|'early') {
  try {
    await tMarkAttendanceException(id, { status })
    await load()
  } catch (e: any) {
    alert(e?.message || '操作失败')
  }
}

onMounted(async () => {
  // 教师登录态守卫
  if (!localStorage.getItem('t_token')) { router.replace('/t/login'); return }
  await load()
})
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">学生考勤记录</div>
      <div class="weui-panel__bd">
        <div class="weui-cells__title">筛选</div>
        <div class="weui-cells weui-cells_form">
          <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">开始日期</label></div>
            <div class="weui-cell__bd"><input type="date" class="weui-input" v-model="startDate" /></div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">结束日期</label></div>
            <div class="weui-cell__bd"><input type="date" class="weui-input" v-model="endDate" /></div>
          </div>
          <div class="weui-btn-area"><button class="weui-btn weui-btn_primary" @click="load">查询</button></div>
        </div>

        <div class="weui-cells__title">学生：{{ childId }}，班级：{{ classId }}</div>
        <div v-if="loadError" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">{{ loadError }}</span></div>
        <div v-else-if="loading" class="weui-loadmore"><i class="weui-loading"></i><span class="weui-loadmore__tips">加载中</span></div>

        <div v-else class="weui-cells">
          <div v-if="records.length===0" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">暂无记录</span></div>
          <div v-for="r in records" :key="r.id" class="weui-cell">
            <div class="weui-cell__bd">
              <div>{{ new Date(r.time).toLocaleString() }} · {{ r.type }} · {{ r.gate }}</div>
              <div class="tags">
                <span class="weui-badge" :class="{ warn: r.late }">迟到：{{ r.late ? '是' : '否' }}</span>
                <span class="weui-badge" :class="{ warn: r.earlyLeave }" style="margin-left:8px">早退：{{ r.earlyLeave ? '是' : '否' }}</span>
              </div>
            </div>
            <div class="weui-cell__ft">
              <button class="weui-btn weui-btn_mini weui-btn_warn" @click="mark(r.id, 'late')">标记迟到</button>
              <button class="weui-btn weui-btn_mini weui-btn_warn" style="margin-left:4px" @click="mark(r.id, 'early')">标记早退</button>
              <button class="weui-btn weui-btn_mini weui-btn_default" style="margin-left:4px" @click="mark(r.id, 'normal')">恢复正常</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { padding: 10px; }
.tags { margin-top: 4px; }
.weui-badge.warn { background-color: #fa5151; }
</style>