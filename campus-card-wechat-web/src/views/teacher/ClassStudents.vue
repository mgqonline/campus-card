<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { tGetClassStudents } from '@/api/modules/teacher'
const route = useRoute()
const router = useRouter()
const classId = Number(route.params.id)
const students = ref<Array<{ id: number; name: string; classId: number; grade: string; cardNo: string; faceStatus: string }> | null>(null)
const loadError = ref('')
onMounted(async () => {
  // 教师登录态守卫
  if (!localStorage.getItem('t_token')) { router.replace('/t/login'); return }
  try { students.value = await tGetClassStudents(classId) } catch (e: any) { loadError.value = e?.message || '加载失败' }
})
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">班级学生名单</div>
      <div class="weui-panel__bd">
        <div class="weui-cells__title">班级：{{ classId }}</div>
        <div v-if="loadError" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">{{ loadError }}</span></div>
        <div v-else-if="!students" class="weui-loadmore"><i class="weui-loading"></i><span class="weui-loadmore__tips">加载中</span></div>
        <div v-else class="weui-cells">
          <div v-if="students.length===0" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">暂无学生</span></div>
          <router-link v-for="s in students" :key="s.id" class="weui-cell weui-cell_access" :to="{ name: 'ClassStudentInfo', params: { id: classId, childId: s.id } }">
            <div class="weui-cell__bd">
              <p><span class="em">{{ s.name }}</span>（ID: {{ s.id }}）</p>
              <p class="sub">年级：{{ s.grade }}｜卡号：{{ s.cardNo }}</p>
            </div>
            <div class="weui-cell__ft"><span class="weui-badge" :class="{ warn: (s.faceStatus||'').includes('未') }">{{ s.faceStatus || '未知' }}</span></div>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.em { font-weight:600; color:#333; }
.sub { color:#666; font-size:12px; }
.warn { background:#f85; }
</style>