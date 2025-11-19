<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getRecords, type EventItem } from '@/api/modules/attendance'
import { showToast } from 'vant'

const list = ref<EventItem[]>([])
const startDate = ref<string>('')
const endDate = ref<string>('')
const loading = ref(false)

const fetchList = async () => {
  loading.value = true
  try {
    const params: any = { page: 1, size: 50 }
    if (startDate.value) params.startDate = startDate.value
    if (endDate.value) params.endDate = endDate.value
    list.value = await getRecords(params)
  } catch (e: any) {
    showToast(e?.message || '获取考勤记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="page">
    <div class="weui-cells__title">考勤记录</div>
    <div class="weui-cells weui-cells_form">
      <div class="weui-cell">
        <div class="weui-cell__hd"><label class="weui-label">开始日期</label></div>
        <div class="weui-cell__bd">
          <input class="weui-input" type="date" v-model="startDate" />
        </div>
      </div>
      <div class="weui-cell">
        <div class="weui-cell__hd"><label class="weui-label">结束日期</label></div>
        <div class="weui-cell__bd">
          <input class="weui-input" type="date" v-model="endDate" />
        </div>
      </div>
    </div>
    <div class="weui-form__opr-area" style="padding:12px;">
      <a href="javascript:;" class="weui-btn weui-btn_primary" @click="fetchList" :class="{ 'weui-btn_loading': loading }">
        <span v-if="loading" class="weui-loading"></span> 查询
      </a>
    </div>

    <div class="weui-cells__title">记录列表</div>
    <div class="weui-cells">
      <div v-if="list.length === 0" class="weui-loadmore weui-loadmore_line">
        <span class="weui-loadmore__tips">暂无记录</span>
      </div>
      <a v-for="(i, idx) in list" :key="idx" class="weui-cell weui-cell_access" href="javascript:;">
        <div class="weui-cell__bd">
          <p><strong>{{ i.type }}</strong>
            <span v-if="i.late" class="weui-badge weui-badge_warn" style="margin-left:6px;">迟到</span>
            <span v-if="i.earlyLeave" class="weui-badge" style="margin-left:6px;">早退</span>
          </p>
          <p class="muted">{{ i.gate }} · {{ new Date(i.time).toLocaleString() }}</p>
        </div>
        <div class="weui-cell__ft"></div>
      </a>
    </div>
  </div>
</template>

<style scoped>
.muted { color:#888; font-size:12px; }
</style>