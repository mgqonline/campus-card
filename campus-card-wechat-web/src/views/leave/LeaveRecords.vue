<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getLeaveRecords, cancelLeave, type LeaveRecord } from '@/api/modules/leave'

const records = ref<LeaveRecord[]>([])
const loading = ref(false)
const error = ref('')
const statusFilter = ref<'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELED'>('ALL')

function currentChildId(): number | undefined {
  const cid = localStorage.getItem('wx_child_id')
  return cid ? Number(cid) : undefined
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await getLeaveRecords({ childId: currentChildId(), page: 1, size: 50 })
    records.value = data
  } catch (err: any) {
    error.value = err?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function revoke(id: number) {
  try {
    await cancelLeave({ childId: currentChildId(), id })
    await load()
  } catch (err: any) {
    error.value = err?.message || '撤销失败'
  }
}

onMounted(load)

const filteredRecords = computed(() => {
  if (statusFilter.value === 'ALL') return records.value
  return records.value.filter(r => r.status === statusFilter.value)
})

function typeText(t: LeaveRecord['type']) {
  return t === 'SICK' ? '病假' : t === 'PERSONAL' ? '事假' : '其他'
}
function statusText(s: LeaveRecord['status']) {
  const map: Record<LeaveRecord['status'], string> = {
    PENDING: '审批中',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    CANCELED: '已撤销',
  }
  return map[s]
}
function statusClass(s: LeaveRecord['status']) {
  switch (s) {
    case 'PENDING': return 'badge-warn'
    case 'APPROVED': return 'badge-ok'
    case 'REJECTED': return 'badge-danger'
    case 'CANCELED': return 'badge-muted'
    default: return ''
  }
}
</script>

<template>
  <div class="page">
    <h3 class="section-title"><van-icon name="orders-o" style="margin-right:6px;" />请假记录</h3>

    <!-- 筛选与操作（WeUI 按钮区域） -->
    <div class="weui-form__opr-area" style="padding:0 15px 10px;">
      <div class="weui-flex filters">
        <a href="javascript:;" class="weui-btn weui-btn_mini" :class="{ 'weui-btn_primary': statusFilter==='ALL', 'weui-btn_default': statusFilter!=='ALL' }" @click="statusFilter='ALL'">全部</a>
        <a href="javascript:;" class="weui-btn weui-btn_mini" :class="{ 'weui-btn_primary': statusFilter==='PENDING', 'weui-btn_default': statusFilter!=='PENDING' }" @click="statusFilter='PENDING'">审批中</a>
        <a href="javascript:;" class="weui-btn weui-btn_mini" :class="{ 'weui-btn_primary': statusFilter==='APPROVED', 'weui-btn_default': statusFilter!=='APPROVED' }" @click="statusFilter='APPROVED'">已通过</a>
        <a href="javascript:;" class="weui-btn weui-btn_mini" :class="{ 'weui-btn_primary': statusFilter==='REJECTED', 'weui-btn_default': statusFilter!=='REJECTED' }" @click="statusFilter='REJECTED'">已拒绝</a>
        <a href="javascript:;" class="weui-btn weui-btn_mini" :class="{ 'weui-btn_primary': statusFilter==='CANCELED', 'weui-btn_default': statusFilter!=='CANCELED' }" @click="statusFilter='CANCELED'">已撤销</a>
        <a href="javascript:;" class="weui-btn weui-btn_mini weui-btn_default" style="margin-left:auto;" @click="load">刷新</a>
      </div>
    </div>

    <div v-if="loading" class="weui-loadmore"><i class="weui-loading"></i><span class="weui-loadmore__tips">加载中</span></div>
    <div v-else-if="error" class="weui-toptips weui-toptips_warn" style="display:block">{{ error }}</div>
    <div v-else-if="!filteredRecords.length" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">暂无记录</span></div>
    <div v-else class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">最近记录</div>
      <div class="weui-panel__bd">
        <div class="weui-media-box weui-media-box_text" v-for="item in filteredRecords" :key="item.id">
          <h4 class="weui-media-box__title">
            {{ typeText(item.type) }}
            <span class="weui-badge" :class="statusClass(item.status)" style="margin-left:6px;">{{ statusText(item.status) }}</span>
          </h4>
          <p class="weui-media-box__desc">{{ new Date(item.startTime).toLocaleString() }} 至 {{ new Date(item.endTime).toLocaleString() }}</p>
          <p class="weui-media-box__desc">原因：{{ item.reason }}</p>
          <p class="weui-media-box__desc" v-if="item.applyTime">申请时间：{{ new Date(item.applyTime).toLocaleString() }}</p>

          <div v-if="item.attachments && item.attachments.length" class="attachments">
            <span class="muted">附件：</span>
            <span v-for="(att, idx) in item.attachments" :key="att + idx" class="chip">附件{{ idx + 1 }}</span>
          </div>

          <div class="weui-flex ops">
            <button v-if="item.status === 'PENDING'" class="weui-btn weui-btn_mini weui-btn_warn" @click="revoke(item.id)">撤销</button>
            <span class="weui-cells__tips" style="margin-left:auto;">单号：{{ item.id }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 使用提示 -->
    <div class="weui-form__tips-area" style="padding:0 15px;">
      <p class="weui-cells__tips">如需补充说明或更正信息，请在“审批中”状态下使用撤销后重新提交。</p>
    </div>
  </div>
</template>

<style scoped>
.filters { gap: 6px; align-items: center; }
.muted { color:#999; font-size:12px; }
.chip { display:inline-block; background:#f5f5f5; color:#666; font-size:12px; padding:2px 6px; border-radius:10px; margin-right:6px; }
.ops { margin-top:8px; }
.badge-warn { background-color:#f56c6c; color:#fff; }
.badge-ok { background-color:#19be6b; color:#fff; }
.badge-danger { background-color:#909399; color:#fff; }
.badge-muted { background-color:#ccc; color:#fff; }
</style>