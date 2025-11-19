<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { tGetProfile, type TProfileResp } from '@/api/modules/teacher'

const router = useRouter()
const profile = ref<TProfileResp | null>(null)
const loadError = ref('')

onMounted(async () => {
  if (!localStorage.getItem('t_token')) { router.replace('/t/login'); return }
  try {
    profile.value = await tGetProfile()
  } catch (e: any) {
    loadError.value = e?.message || '加载失败'
  }
})
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">教师端首页</div>
      <div class="weui-panel__bd">
        <div v-if="loadError" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">{{ loadError }}</span></div>
        <div v-else-if="!profile" class="weui-loadmore"><i class="weui-loading"></i><span class="weui-loadmore__tips">加载中</span></div>
        <template v-else>
          <div class="weui-cells__title">教师信息</div>
          <div class="weui-cells">
            <div class="weui-cell"><div class="weui-cell__bd">姓名</div><div class="weui-cell__ft">{{ profile!.name }}</div></div>
            <div class="weui-cell"><div class="weui-cell__bd">手机号</div><div class="weui-cell__ft">{{ profile!.phone }}</div></div>
          </div>

          <div class="weui-grids teacher-grids">
            <router-link to="/t/leave" class="weui-grid">
              <div class="weui-grid__icon"><i class="fa fa-check-square-o"></i></div>
              <p class="weui-grid__label">请假审批</p>
            </router-link>
            <router-link to="/t/leave/help" class="weui-grid">
              <div class="weui-grid__icon"><i class="fa fa-hand-o-right"></i></div>
              <p class="weui-grid__label">老师帮请</p>
            </router-link>
            <router-link to="/t/classes" class="weui-grid">
              <div class="weui-grid__icon"><i class="fa fa-users"></i></div>
              <p class="weui-grid__label">班级管理</p>
            </router-link>
            <router-link to="/t/messages" class="weui-grid">
              <div class="weui-grid__icon"><i class="fa fa-bell"></i></div>
              <p class="weui-grid__label">消息通知</p>
            </router-link>
            <router-link to="/t/account/profile" class="weui-grid">
              <div class="weui-grid__icon"><i class="fa fa-user"></i></div>
              <p class="weui-grid__label">个人信息</p>
            </router-link>
          </div>
        </template>
      </div>
    </div>
  </div>
 </template>

<style scoped>
.teacher-grids { display: flex; flex-wrap: wrap; }
.weui-grid { width: 33.33%; text-align: center; padding: 16px 0; }
.weui-grid__icon { font-size: 24px; margin-bottom: 6px; }
.weui-grid__label { font-size: 14px; }
</style>