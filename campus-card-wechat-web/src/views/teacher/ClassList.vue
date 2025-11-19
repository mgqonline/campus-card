<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { tGetClasses, type TClassBrief } from '@/api/modules/teacher'
const classes = ref<TClassBrief[] | null>(null)
const loadError = ref('')
onMounted(async () => {
  try { classes.value = await tGetClasses() } catch (e: any) { loadError.value = e?.message || '加载失败' }
})
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">班级管理</div>
      <div class="weui-panel__bd">
        <div v-if="loadError" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">{{ loadError }}</span></div>
        <div v-else-if="!classes" class="weui-loadmore"><i class="weui-loading"></i><span class="weui-loadmore__tips">加载中</span></div>
        <div v-else class="weui-cells">
          <div v-if="classes.length===0" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">暂无班级</span></div>
          <router-link v-for="c in classes" :key="c.classId" class="weui-cell weui-cell_access" :to="{ name: 'ClassStudents', params: { id: c.classId } }">
            <div class="weui-cell__bd">{{ c.className }}（{{ c.classId }}）</div>
            <div class="weui-cell__ft"></div>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>