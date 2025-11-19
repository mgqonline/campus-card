<template>
  <div class="attendance-page">
    <!-- 页面标题 -->
    <van-nav-bar title="考勤管理" left-arrow @click-left="$router.go(-1)" />
    
    <!-- Tab切换 -->
    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <!-- 实时考勤 -->
      <van-tab title="实时考勤" name="realtime">
        <div class="tab-content">
          <div class="realtime-section">
            <div class="status-card">
              <div class="status-icon" :class="attendanceStatus.status">
                <van-icon :name="getStatusIcon()" />
              </div>
              <div class="status-info">
                <h3>{{ attendanceStatus.message }}</h3>
                <p>{{ attendanceStatus.time }}</p>
              </div>
            </div>
            
            <van-button 
              type="primary" 
              size="large" 
              class="attendance-btn"
              @click="doAttendance"
              :loading="attendanceLoading"
            >
              {{ attendanceLoading ? '打卡中...' : '立即打卡' }}
            </van-button>
          </div>
        </div>
      </van-tab>
      
      <!-- 考勤记录 -->
      <van-tab title="考勤记录" name="records">
        <div class="tab-content">
          <div class="filter-section">
            <van-field
              v-model="recordsFilter.date"
              label="日期"
              placeholder="选择日期"
              readonly
              @click="showDatePicker = true"
            />
            <van-button type="primary" size="small" @click="loadAttendanceRecords">查询</van-button>
          </div>
          
          <div class="records-list">
            <van-empty v-if="attendanceRecords.length === 0" description="暂无考勤记录" />
            <div v-else>
              <div 
                v-for="record in attendanceRecords" 
                :key="record.id"
                class="record-item"
              >
                <div class="record-info">
                  <div class="record-time">{{ record.checkTime }}</div>
                  <div class="record-type" :class="record.type">{{ getRecordTypeText(record.type) }}</div>
                </div>
                <div class="record-status" :class="record.status">
                  {{ getRecordStatusText(record.status) }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </van-tab>
      
      <!-- 考勤统计 -->
      <van-tab title="考勤统计" name="stats">
        <div class="tab-content">
          <div class="stats-cards">
            <div class="stats-card">
              <div class="stats-number">{{ attendanceStats.totalDays }}</div>
              <div class="stats-label">总天数</div>
            </div>
            <div class="stats-card">
              <div class="stats-number">{{ attendanceStats.presentDays }}</div>
              <div class="stats-label">出勤天数</div>
            </div>
            <div class="stats-card">
              <div class="stats-number">{{ attendanceStats.lateDays }}</div>
              <div class="stats-label">迟到天数</div>
            </div>
            <div class="stats-card">
              <div class="stats-number">{{ attendanceStats.absentDays }}</div>
              <div class="stats-label">缺勤天数</div>
            </div>
          </div>
          
          <div class="attendance-rate">
            <van-circle
              v-model:current-rate="attendanceStats.attendanceRate"
              :rate="attendanceStats.attendanceRate"
              :speed="100"
              :text="attendanceStats.attendanceRate + '%'"
              stroke-width="6"
              color="#1989fa"
            />
            <div class="rate-label">出勤率</div>
          </div>
        </div>
      </van-tab>
      
      <!-- 考勤提醒 -->
      <van-tab title="考勤提醒" name="alerts">
        <div class="tab-content">
          <van-empty v-if="attendanceAlerts.length === 0" description="暂无考勤提醒" />
          <div v-else>
            <div 
              v-for="alert in attendanceAlerts" 
              :key="alert.title + alert.time"
              class="alert-item"
            >
                <van-icon :name="getAlertIconByText(alert.title, alert.txtdesc)" />
              <div class="alert-content">
                <div class="alert-title">{{ alert.title }}</div>
                <div class="alert-message">{{ alert.txtdesc }}</div>
                <div class="alert-time">{{ new Date(alert.time).toLocaleString() }}</div>
              </div>
            </div>
          </div>
        </div>
      </van-tab>
    </van-tabs>
    
    <!-- 日期选择器 -->
    <van-popup v-model:show="showDatePicker" position="bottom">
      <van-date-picker
        v-model="selectedDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { getRealtime, getRecords, getStats, getAlerts, checkIn } from '@/api/modules/attendance'

// 响应式数据
const activeTab = ref('realtime')
const attendanceLoading = ref(false)
const showDatePicker = ref(false)
const selectedDate = ref([new Date().getFullYear().toString(), (new Date().getMonth() + 1).toString().padStart(2, '0'), new Date().getDate().toString().padStart(2, '0')])

// 实时考勤状态
const attendanceStatus = reactive({
  childId: 0,
  hasNew: false,
  event: null as any,
  status: 'normal', // normal, late, absent
  message: '今日未打卡',
  time: ''
})

// 考勤记录筛选
const recordsFilter = reactive({
  date: new Date().toISOString().split('T')[0]
})

// 考勤记录列表
const attendanceRecords = ref([])

// 考勤统计
const attendanceStats = reactive({
  totalDays: 0,
  presentDays: 0,
  lateDays: 0,
  absentDays: 0,
  attendanceRate: 0
})

// 考勤提醒
const attendanceAlerts = ref([])

// 方法
const onTabChange = (name: string) => {
  switch (name) {
    case 'realtime':
      loadRealtimeAttendance()
      break
    case 'records':
      loadAttendanceRecords()
      break
    case 'stats':
      loadAttendanceStats()
      break
    case 'alerts':
      loadAttendanceAlerts()
      break
  }
}

const loadRealtimeAttendance = async () => {
  try {
    const data = await getRealtime()
    Object.assign(attendanceStatus, data)
    // 根据事件状态设置显示信息
    if (data.hasNew && data.event) {
      attendanceStatus.status = data.event.late ? 'late' : 'present'
      attendanceStatus.message = data.event.late ? '今日已打卡（迟到）' : '今日已打卡'
      attendanceStatus.time = new Date(data.event.time).toLocaleTimeString()
    } else {
      attendanceStatus.status = 'normal'
      attendanceStatus.message = '今日未打卡'
      attendanceStatus.time = ''
    }
  } catch (error) {
    console.error('加载实时考勤失败:', error)
  }
}

const doAttendance = async () => {
  attendanceLoading.value = true
  const loadingToast = showLoadingToast({
    message: '打卡中...',
    forbidClick: true,
  })
  
  try {
    const data = await checkIn()
    showToast(data.message || (data.success ? '打卡成功' : '打卡失败'))
    await loadRealtimeAttendance()
  } catch (error) {
    console.error('打卡失败:', error)
    showToast('打卡失败，请重试')
  } finally {
    attendanceLoading.value = false
    closeToast()
  }
}

const loadAttendanceRecords = async () => {
  try {
    const data = await getRecords({
      startDate: recordsFilter.date,
      endDate: recordsFilter.date,
      page: 1,
      size: 50
    })
    attendanceRecords.value = data
  } catch (error) {
    console.error('加载考勤记录失败:', error)
  }
}

const loadAttendanceStats = async () => {
  try {
    const data = await getStats({ range: 'month' })
    Object.assign(attendanceStats, data)
  } catch (error) {
    console.error('加载考勤统计失败:', error)
  }
}

const loadAttendanceAlerts = async () => {
  try {
    const data = await getAlerts()
    attendanceAlerts.value = data
  } catch (error) {
    console.error('加载考勤提醒失败:', error)
  }
}

const onDateConfirm = (value: string[]) => {
  recordsFilter.date = `${value[0]}-${value[1]}-${value[2]}`
  showDatePicker.value = false
  loadAttendanceRecords()
}

const getStatusIcon = () => {
  switch (attendanceStatus.status) {
    case 'present': return 'success'
    case 'late': return 'warning-o'
    case 'absent': return 'close'
    default: return 'clock-o'
  }
}

const getRecordTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'check_in': '签到',
    'check_out': '签退'
  }
  return typeMap[type] || type
}

const getRecordStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'normal': '正常',
    'late': '迟到',
    'early': '早退',
    'absent': '缺勤'
  }
  return statusMap[status] || status
}

const getAlertIconByText = (title?: string, txtdesc?: string) => {
  const text = (title || '') + ' ' + (txtdesc || '')
  if (text.includes('迟到')) return 'warning-o'
  if (text.includes('缺勤')) return 'close'
  if (text.includes('早退')) return 'clock-o'
  return 'info-o'
}

// 生命周期
onMounted(() => {
  loadRealtimeAttendance()
})
</script>

<style scoped>
.attendance-page {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.tab-content {
  padding: 16px;
}

/* 实时考勤样式 */
.realtime-section {
  text-align: center;
}

.status-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.status-icon.normal {
  background-color: #969799;
}

.status-icon.present {
  background-color: #07c160;
}

.status-icon.late {
  background-color: #ff976a;
}

.status-icon.absent {
  background-color: #ee0a24;
}

.status-info {
  flex: 1;
  text-align: left;
}

.status-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
}

.status-info p {
  margin: 0;
  color: #969799;
  font-size: 14px;
}

.attendance-btn {
  width: 100%;
  height: 48px;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
}

/* 考勤记录样式 */
.filter-section {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-section .van-field {
  flex: 1;
}

.records-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.record-item {
  padding: 16px;
  border-bottom: 1px solid #ebedf0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-item:last-child {
  border-bottom: none;
}

.record-info {
  flex: 1;
}

.record-time {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.record-type {
  font-size: 14px;
  color: #969799;
}

.record-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.record-status.normal {
  background-color: #f0f9ff;
  color: #1989fa;
}

.record-status.late {
  background-color: #fff7ed;
  color: #ff976a;
}

.record-status.early {
  background-color: #fff7ed;
  color: #ff976a;
}

.record-status.absent {
  background-color: #fef2f2;
  color: #ee0a24;
}

/* 考勤统计样式 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stats-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stats-number {
  font-size: 24px;
  font-weight: 600;
  color: #1989fa;
  margin-bottom: 8px;
}

.stats-label {
  font-size: 14px;
  color: #969799;
}

.attendance-rate {
  background: white;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.rate-label {
  margin-top: 16px;
  font-size: 16px;
  font-weight: 500;
  color: #323233;
}

/* 考勤提醒样式 */
.alert-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.alert-item.late {
  border-left: 4px solid #ff976a;
}

.alert-item.absent {
  border-left: 4px solid #ee0a24;
}

.alert-item.early {
  border-left: 4px solid #ff976a;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.alert-message {
  font-size: 14px;
  color: #646566;
  margin-bottom: 8px;
}

.alert-time {
  font-size: 12px;
  color: #969799;
}
</style>