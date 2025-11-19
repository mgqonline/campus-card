<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { getProfile, type ProfileResp } from '@/api/modules/auth'
import { getBalance, type BalanceResp } from '@/api/modules/account'
import { getStats, type StatsResp, getAlerts } from '@/api/modules/attendance'
import { getLeaveStatus, type LeaveStatus } from '@/api/modules/leave'
import { notifyApi } from '@/api/notify'

const profile = ref<ProfileResp | null>(null)
const loading = ref(false)
const error = ref('')
const selectedChildId = ref<number | null>(null)
const hasToken = ref<boolean>(!!localStorage.getItem('wx_token'))

// 关键指标数据
const balance = ref<BalanceResp | null>(null)
const stats = ref<StatsResp | null>(null)
const alertsCount = ref<number>(0)
const unreadNotifyCount = ref<number>(0)
// 请假数据摘要
const pendingLeaveCount = ref<number>(0)
const leaveSummaryLoading = ref<boolean>(false)
const leaveSummaryError = ref<string>('')

function maskPhone(p?: string) {
  if (!p) return ''
  return p.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

function onSelectChild(id: number) {
  selectedChildId.value = id
  localStorage.setItem('wx_child_id', String(id))
}

function formatClassName(classId?: number) {
  if (!classId) return '—'
  const mapping: Record<number, string> = {
    301: '三年级一班',
    302: '三年级二班',
    401: '四年级一班',
    402: '四年级二班',
  }
  return mapping[classId] || `班级 ${classId}`
}

async function loadIndicators() {
  if (!selectedChildId.value) {
    balance.value = null
    stats.value = null
    alertsCount.value = 0
    pendingLeaveCount.value = 0
    return
  }
  try {
    const [b, s, a] = await Promise.all([
      getBalance({ childId: selectedChildId.value }),
      getStats({ childId: selectedChildId.value, range: 'month' }),
      getAlerts(selectedChildId.value),
    ])
    balance.value = b
    stats.value = s
    alertsCount.value = Array.isArray(a) ? a.length : 0
    let totalUnread = 0
    try {
      const stat = await notifyApi.stats(selectedChildId.value!)
      totalUnread = Number(stat?.totalUnread || 0)
    } catch {
      const n = await notifyApi.unreadCount(selectedChildId.value!)
      totalUnread = Number(n) || 0
    }
    unreadNotifyCount.value = totalUnread
  } catch (e) {
    // 指标加载失败不影响整体页面
  }
}

async function loadLeaveSummary() {
  leaveSummaryLoading.value = true
  leaveSummaryError.value = ''
  try {
    const childId = selectedChildId.value ?? undefined
    const data = await getLeaveStatus(childId)
    pendingLeaveCount.value = Array.isArray(data)
      ? data.filter((i) => i.status === 'PENDING').length
      : 0
  } catch (e: any) {
    leaveSummaryError.value = e?.message || '请假状态加载失败'
    pendingLeaveCount.value = 0
  } finally {
    leaveSummaryLoading.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    if (!hasToken.value) {
      // 未登录时不请求 profile，仅结束加载状态
      profile.value = null
      return
    }
    profile.value = await getProfile()
    const saved = localStorage.getItem('wx_child_id')
    if (saved) {
      selectedChildId.value = Number(saved)
    } else if (profile.value?.boundChildren?.length) {
      selectedChildId.value = profile.value.boundChildren[0].id
      localStorage.setItem('wx_child_id', String(selectedChildId.value))
    }
  } catch (e: any) {
    error.value = e?.message || '获取个人信息失败'
  } finally {
    loading.value = false
  }
})

watch(selectedChildId, () => {
  loadIndicators()
  loadLeaveSummary()
})
</script>

<template>
  <div class="page">
    <div v-if="loading">加载中...</div>
    <div v-else-if="error" class="text-error">{{ error }}</div>
    <!-- 未登录提醒 -->
    <div v-else-if="!hasToken" class="login-warn">
      <i class="fa fa-info-circle"></i>
      <span>您还未登录，请先登录以使用完整功能。</span>
      <router-link to="/login" class="weui-btn weui-btn_mini weui-btn_primary" style="margin-left:8px;">去登录</router-link>
    </div>
    <div v-else-if="profile">
      <!-- 顶部家长详情（头像 + 姓名 + 手机） -->
      <div class="profile-card">
        <div class="avatar"><i class="fa fa-user-circle-o"></i></div>
        <div class="meta">
          <div class="name">{{ profile.name }}</div>
          <div class="sub">所属学校：{{ profile.schoolName || '—' }}</div>
          <div class="sub" v-if="profile.phone">手机：{{ maskPhone(profile.phone) }}</div>
        </div>
      </div>

      <!-- 未绑定提醒 -->
      <div v-if="!profile.boundChildren || profile.boundChildren.length === 0" class="bind-warn">
        <i class="fa fa-exclamation-triangle"></i>
        <span>您尚未绑定孩子信息，请前往绑定。</span>
        <router-link to="/account/profile" class="weui-btn weui-btn_mini weui-btn_primary" style="margin-left:8px;">去绑定</router-link>
      </div>

      <!-- 绑定子女（优化：显示班级名称，紧凑布局） -->
      <div class="weui-cells__title">绑定子女</div>
      <div class="weui-cells weui-cells_radio" v-if="profile.boundChildren && profile.boundChildren.length">
        <label v-for="child in profile.boundChildren" :key="child.id" class="weui-cell weui-check__label" :for="'child-'+child.id">
          <div class="weui-cell__bd">
            <p class="child-line">
              <span class="child-name">{{ child.name }}</span>
              <span class="child-class">{{ child.className || formatClassName(child.classId) }}</span>
            </p>
          </div>
          <div class="weui-cell__ft">
            <input type="radio" class="weui-check" name="child" :id="'child-'+child.id" :checked="child.id===selectedChildId" @change="onSelectChild(child.id)">
            <span class="weui-icon-checked"></span>
          </div>
        </label>
      </div>
      <div v-else class="weui-cells__tips" style="margin:8px 15px; color:#999;">暂无绑定的子女</div>

      <!-- 功能菜单（WeUI grids，参考 page.html） -->
      <div class="weui-cells__title">校园一卡通</div>
      <div class="weui-grids">
        <router-link to="/consume" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-credit-card"></i></div>
          <p class="weui-grid__label">消费查询</p>
        </router-link>
        <router-link to="/consume/stats" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-line-chart"></i></div>
          <p class="weui-grid__label">消费统计</p>
        </router-link>
        <router-link to="/consume/calendar" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-calendar"></i></div>
          <p class="weui-grid__label">消费日历</p>
        </router-link>
        <router-link to="/consume/trend" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-area-chart"></i></div>
          <p class="weui-grid__label">消费趋势</p>
        </router-link>
        <router-link to="/account/balance" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-money"></i></div>
          <p class="weui-grid__label">余额查询</p>
        </router-link>
        <router-link to="/recharge" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-credit-card"></i></div>
          <p class="weui-grid__label">在线充值</p>
        </router-link>
        <router-link to="/recharge/records" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-file-text-o"></i></div>
          <p class="weui-grid__label">充值记录</p>
        </router-link>
        <router-link to="/student/info" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-id-card"></i></div>
          <p class="weui-grid__label">学生信息</p>
        </router-link>
        <router-link to="/account/profile" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-user"></i></div>
          <p class="weui-grid__label">家长资料</p>
        </router-link>
        <!-- 消息通知入口 -->
        <router-link to="/notify" class="weui-grid notify-grid">
          <div class="weui-grid__icon">
            <i class="fa fa-bell"></i>
            <span v-if="unreadNotifyCount>0" class="weui-badge weui-badge_warn badge-dot">{{ unreadNotifyCount>99? '99+': unreadNotifyCount }}</span>
          </div>
          <p class="weui-grid__label">消息通知</p>
        </router-link>
        <router-link to="/face/collection" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-camera"></i></div>
          <p class="weui-grid__label">人像采集</p>
        </router-link>
        <!-- 考勤模块入口 -->
        <router-link to="/attendance/realtime" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-clock-o"></i></div>
          <p class="weui-grid__label">实时考勤</p>
        </router-link>
        <router-link to="/attendance/records" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-list-alt"></i></div>
          <p class="weui-grid__label">考勤记录</p>
        </router-link>
        <router-link to="/attendance/stats" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-bar-chart"></i></div>
          <p class="weui-grid__label">考勤统计</p>
        </router-link>
        <router-link to="/attendance/photos" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-picture-o"></i></div>
          <p class="weui-grid__label">考勤照片</p>
        </router-link>
        <router-link to="/attendance/alerts" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-bell"></i></div>
          <p class="weui-grid__label">考勤提醒</p>
        </router-link>
        <!-- 其他功能入口 -->
        <router-link to="/card/loss" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-ban"></i></div>
          <p class="weui-grid__label">挂失/解挂</p>
        </router-link>
        <router-link to="/card/replace" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-id-card"></i></div>
          <p class="weui-grid__label">补卡申请</p>
        </router-link>
        <router-link to="/feedback" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-commenting-o"></i></div>
          <p class="weui-grid__label">意见反馈</p>
        </router-link>
        <router-link to="/account/help" class="weui-grid">
          <div class="weui-grid__icon"><i class="fa fa-question-circle"></i></div>
          <p class="weui-grid__label">帮助中心</p>
        </router-link>
      </div>

      <!-- 请假管理（WeUI 列表区块，提升可发现性与点击面积） -->
      <div class="weui-cells__title">请假管理</div>
      <div class="weui-cells weui-cells_access">
        <router-link to="/leave/apply" class="weui-cell weui-cell_access">
          <div class="weui-cell__hd">
            <i class="fa fa-pencil-square-o leave-icon"></i>
          </div>
          <div class="weui-cell__bd">
            <p>请假申请</p>
            <p class="weui-cells__tips">
              <span v-if="!selectedChildId">请先在上方选择子女</span>
              <span v-else>支持病假、事假等类型，上传附件</span>
            </p>
          </div>
          <div class="weui-cell__ft"></div>
        </router-link>
        <router-link to="/leave/records" class="weui-cell weui-cell_access">
          <div class="weui-cell__hd">
            <i class="fa fa-file-text-o leave-icon"></i>
          </div>
          <div class="weui-cell__bd">
            <p>
              请假记录
              <span v-if="pendingLeaveCount > 0" class="weui-badge weui-badge_warn" style="margin-left:6px;">{{ pendingLeaveCount }}</span>
            </p>
            <p class="weui-cells__tips">
              <span v-if="leaveSummaryLoading"><i class="weui-loading" style="margin-right:4px;"></i>状态加载中</span>
              <span v-else-if="leaveSummaryError">状态加载失败，稍后重试</span>
              <span v-else-if="!selectedChildId">请先在上方选择子女</span>
              <span v-else-if="pendingLeaveCount > 0">有待审批 {{ pendingLeaveCount }} 条</span>
              <span v-else>查看历史申请与状态，支持撤销</span>
            </p>
          </div>
          <div class="weui-cell__ft"></div>
        </router-link>
      </div>

      <!-- 关键指标（替换通知公告）：余额、考勤出勤率、提醒数量 -->
      <div class="weui-cells__title">关键指标</div>
      <div class="metrics">
        <div class="metric">
          <div class="metric-label">账户余额</div>
          <div class="metric-value">
            <span v-if="balance">{{ balance.balance.toFixed(2) }}</span>
            <span v-else>—</span>
            <span class="metric-unit" v-if="balance">{{ balance.currency }}</span>
          </div>
        </div>
        <div class="metric">
          <div class="metric-label">本月出勤率</div>
          <div class="metric-value">
            <span v-if="stats">
              {{ ((stats.presentDays / Math.max(stats.totalDays, 1)) * 100).toFixed(0) }}%
            </span>
            <span v-else>—</span>
          </div>
          <div class="metric-sub" v-if="stats">迟到 {{ stats.lateCount }} 次 · 早退 {{ stats.earlyLeaveCount }} 次</div>
        </div>
        <div class="metric">
          <div class="metric-label">提醒</div>
          <div class="metric-value">
            <span>{{ alertsCount }}</span>
            <span class="metric-unit">条</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* WeUI grids 图标大小微调，与 page.html 接近 */
.weui-grid__icon { font-size: 20px; color: #2778F1; }
.weui-grid__label { font-size: 12px; color: #333; }
.notify-grid { position: relative; }
.badge-dot { position:absolute; right:-6px; top:-6px; }

/* 顶部家长信息卡片 */
.profile-card { display:flex; align-items:center; padding:12px 15px; background:#fff; border-radius:8px; box-shadow:0 1px 3px rgba(0,0,0,0.04); margin-bottom:12px; }
.profile-card .avatar { width:56px; height:56px; border-radius:50%; background:#e6f0ff; display:flex; align-items:center; justify-content:center; color:#2778F1; margin-right:12px; }
.profile-card .avatar .fa { font-size:28px; }
.profile-card .meta .name { font-size:16px; font-weight:600; color:#333; }
.profile-card .meta .sub { font-size:12px; color:#888; margin-top:2px; }

/* 未绑定提示条 */
.bind-warn { display:flex; align-items:center; gap:8px; padding:10px 12px; margin:0 15px 12px; background:#fffbe6; border:1px solid #ffe58f; color:#ad8b00; border-radius:6px; }
.bind-warn .fa { font-size:16px; }

/* 未登录提示条 */
.login-warn { display:flex; align-items:center; gap:8px; padding:10px 12px; margin:0 15px 12px; background:#e6f7ff; border:1px solid #91d5ff; color:#096dd9; border-radius:6px; }
.login-warn .fa { font-size:16px; }

/* 绑定子女优化样式 */
.child-line { display:flex; align-items:center; justify-content:space-between; }

/* 请假模块图标与布局优化 */
.leave-icon { font-size:20px; color:#2778F1; width:20px; text-align:center; margin-right:8px; }
.child-name { font-weight:600; color:#333; }
.child-class { font-size:12px; color:#666; }

/* 关键指标样式 */
.metrics { display:grid; grid-template-columns: repeat(3, 1fr); gap:8px; padding:0 15px 12px; }
.metric { background:#fff; border-radius:8px; padding:10px; box-shadow:0 1px 3px rgba(0,0,0,0.04); }
.metric-label { font-size:12px; color:#888; }
.metric-value { font-size:18px; font-weight:700; color:#222; display:flex; align-items:baseline; gap:4px; }
.metric-unit { font-size:12px; color:#888; }
.metric-sub { margin-top:4px; font-size:11px; color:#888; }
</style>