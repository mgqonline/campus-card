<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { getRealtime, getAlerts, checkIn, type RealtimeResp, type AlertItem } from '@/api/modules/attendance'
import { showToast } from 'vant'

const realtime = ref<RealtimeResp | null>(null)
const alerts = ref<AlertItem[]>([])
let timer: number | null = null
const busy = ref(false)

const fetchData = async () => {
  try {
    const r = await getRealtime()
    realtime.value = r
    alerts.value = await getAlerts()
  } catch (e: any) {
    showToast(e?.message || '获取实时考勤失败')
  }
}

const doCheckIn = async () => {
  if (busy.value) return
  busy.value = true
  try {
    const resp = await checkIn()
    showToast(resp.message || (resp.success ? '打卡成功' : '打卡失败'))
    await fetchData()
  } catch (e: any) {
    showToast(e?.message || '打卡失败')
  } finally {
    busy.value = false
  }
}

onMounted(() => {
  fetchData()
  timer = window.setInterval(fetchData, 15000)
})
onUnmounted(() => { if (timer) { clearInterval(timer); timer = null } })
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">实时考勤</div>
      <div class="weui-panel__bd">
        <div v-if="realtime?.event" class="weui-media-box weui-media-box_appmsg">
          <div class="weui-media-box__hd">
            <img v-if="realtime!.event.photoUrl" class="weui-media-box__thumb" :src="realtime!.event.photoUrl" alt="" />
            <div v-else class="thumb-placeholder">KQ</div>
          </div>
          <div class="weui-media-box__bd">
            <h4 class="weui-media-box__title">
              {{ realtime!.event.type }}
              <span v-if="realtime!.event.late" class="weui-badge weui-badge_warn">迟到</span>
              <span v-if="realtime!.event.earlyLeave" class="weui-badge">早退</span>
            </h4>
            <p class="weui-media-box__desc">
              {{ realtime!.event.gate }} · {{ new Date(realtime!.event.time).toLocaleString() }}
            </p>
          </div>
        </div>
        <div v-else class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">暂无打卡</span></div>
      </div>
      <div class="weui-panel__ft">
        <a href="javascript:;" class="weui-btn weui-btn_primary" @click="doCheckIn" :class="{ 'weui-btn_loading': busy }">
          <span v-if="busy" class="weui-loading"></span> 立即打卡
        </a>
      </div>
    </div>

    <div class="weui-cells__title">提醒中心</div>
    <div class="weui-cells">
      <div v-if="alerts.length === 0" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">暂无提醒</span></div>
      <a v-for="a in alerts" :key="a.title + a.time" class="weui-cell weui-cell_access" href="javascript:;">
        <div class="weui-cell__bd">
          <p class="title">{{ a.title }}</p>
          <p class="desc">{{ a.txtdesc }}</p>
          <p class="time">{{ new Date(a.time).toLocaleString() }}</p>
        </div>
        <div class="weui-cell__ft"></div>
      </a>
    </div>
  </div>
</template>

<style scoped>
.thumb-placeholder { width:40px; height:40px; border-radius:4px; background:#f2f2f2; color:#999; display:flex; align-items:center; justify-content:center; font-size:12px; }
.title { font-weight:600; }
.desc { color:#666; font-size:13px; }
.time { color:#999; font-size:12px; }
</style>