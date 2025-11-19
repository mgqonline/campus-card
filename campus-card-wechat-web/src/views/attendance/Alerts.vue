<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAlerts, type AlertItem } from '@/api/modules/attendance'
import { showToast } from 'vant'

const alerts = ref<AlertItem[]>([])
const loading = ref(false)

const fetchAlerts = async () => {
  loading.value = true
  try {
    alerts.value = await getAlerts()
  } catch (e: any) {
    showToast(e?.message || '获取考勤提醒失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchAlerts)
</script>

<template>
  <div class="page">
    <div class="weui-cells__title">考勤提醒（迟到/早退/缺勤）</div>
    <div class="weui-form__opr-area" style="padding:12px;">
      <a href="javascript:;" class="weui-btn weui-btn_default" @click="fetchAlerts" :class="{ 'weui-btn_loading': loading }">
        <span v-if="loading" class="weui-loading"></span> 刷新
      </a>
    </div>
    <div class="weui-cells">
      <a v-for="a in alerts" :key="a.title + a.time" class="weui-cell weui-cell_access" href="javascript:;">
        <div class="weui-cell__bd">
          <p class="title">{{ a.title }}</p>
          <p class="desc">{{ a.txtdesc }}</p>
          <p class="time">{{ new Date(a.time).toLocaleString() }}</p>
        </div>
        <div class="weui-cell__ft"></div>
      </a>
      <div v-if="alerts.length === 0" class="weui-loadmore weui-loadmore_line">
        <span class="weui-loadmore__tips">暂无提醒</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.title { color:#333; font-weight:600; }
.time { color:#999; font-size:12px; }
.desc { color:#666; font-size:13px; margin-top:4px; }
</style>