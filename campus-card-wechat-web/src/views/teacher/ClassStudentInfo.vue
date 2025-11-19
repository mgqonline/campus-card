<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { tGetStudentInfo } from '@/api/modules/teacher'

const route = useRoute()
const router = useRouter()
const childId = Number(route.params.childId)
const info = ref<{ id: number; name: string; classId: number; grade: string; cardNo: string; faceStatus: string } | null>(null)
const loadError = ref('')

onMounted(async () => {
  // 教师登录态守卫
  if (!localStorage.getItem('t_token')) { router.replace('/t/login'); return }
  try {
    info.value = await tGetStudentInfo(childId)
  } catch (e: any) {
    loadError.value = e?.message || '加载失败'
  }
})
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">学生信息</div>
      <div class="weui-panel__bd">
        <div v-if="loadError" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">{{ loadError }}</span></div>
        <div v-else-if="!info" class="weui-loadmore"><i class="weui-loading"></i><span class="weui-loadmore__tips">加载中</span></div>
        <template v-else>
          <div class="weui-cells__title">学生基本信息</div>
          <div class="weui-cells">
            <div class="weui-cell"><div class="weui-cell__bd">姓名</div><div class="weui-cell__ft">{{ info!.name }}</div></div>
            <div class="weui-cell"><div class="weui-cell__bd">学号</div><div class="weui-cell__ft">{{ info!.id }}</div></div>
            <div class="weui-cell"><div class="weui-cell__bd">班级</div><div class="weui-cell__ft">{{ info!.classId }}</div></div>
            <div class="weui-cell"><div class="weui-cell__bd">年级</div><div class="weui-cell__ft">{{ info!.grade }}</div></div>
            <div class="weui-cell"><div class="weui-cell__bd">卡号</div><div class="weui-cell__ft">{{ info!.cardNo }}</div></div>
            <div class="weui-cell"><div class="weui-cell__bd">人脸状态</div><div class="weui-cell__ft">{{ info!.faceStatus }}</div></div>
          </div>

          <div class="weui-cells__title">快捷操作</div>
          <div class="weui-cells">
            <router-link class="weui-cell weui-cell_access" :to="{ name: 'ClassAttendance', params: { id: info!.classId } }">
              <div class="weui-cell__bd">查看班级考勤</div>
              <div class="weui-cell__ft"></div>
            </router-link>
            <router-link class="weui-cell weui-cell_access" :to="{ name: 'StudentAttendance', params: { id: info!.classId, childId: info!.id } }">
              <div class="weui-cell__bd">查看学生考勤记录</div>
              <div class="weui-cell__ft"></div>
            </router-link>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { padding: 10px; }
</style>