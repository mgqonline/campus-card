<template>
  <div class="notify-page">
    <!-- 无子女选择时的友好提示 -->
    <div v-if="!hasChild" class="no-child">
      <i class="fa fa-info-circle"></i>
      <span>未检测到已选择的子女，请先返回首页选择。</span>
      <router-link to="/" class="weui-btn weui-btn_mini weui-btn_primary" style="margin-left:8px;">去首页</router-link>
      <van-button size="small" type="success" plain style="margin-left:8px;" @click="useSampleChild">一键使用示例子女(2001)</van-button>
    </div>

    <template v-else>
      <van-notice-bar v-if="unreadTotal > 0" :text="`当前未读 ${unreadTotal} 条消息`" mode="closeable" />
      <!-- 操作区：刷新、仅看未读与生成示例数据 -->
      <div class="ops">
        <van-button size="small" type="primary" plain @click="refreshActive">刷新</van-button>
        <div class="op-item">
          <span class="op-label">仅看未读</span>
          <van-switch v-model="showUnreadOnly" size="20px" />
        </div>
        
      </div>
      <van-tabs v-model:active="active" animated sticky>
        <van-tab v-for="t in tabs" :key="t.key" :title="t.title">
          <van-list
            v-model:loading="t.loading"
            :finished="t.finished"
            finished-text="没有更多了"
            @load="() => loadMore(t.key)"
          >
            <div
              v-for="item in visibleList(t.key)"
              :key="item.id"
              class="card-item"
              @click="openDetail(item)"
            >
              <div class="card-header">
                <div class="left">
                  <van-icon :name="getIcon(item.category)" />
                  <span class="title">{{ item.title }}</span>
                  <span v-if="!item.readFlag" class="badge">未读</span>
                </div>
                <div class="right">
                  <van-icon name="ellipsis" />
                </div>
              </div>
              <div class="card-body">
                <div class="row"><span class="label">通知类型</span><span class="value">{{ categoryText(item.category) }}</span></div>
                <div class="row"><span class="label">告警对象</span><span class="value">{{ targetText(item) }}</span></div>
                <div class="row"><span class="label">告警时间</span><span class="value">{{ formatTime(item.createdAt) }}</span></div>
                <div class="row"><span class="label">报警内容</span><span class="value">{{ item.content }}</span></div>
              </div>
              <div class="card-footer">
                <span class="from">校园一卡通</span>
                <span class="entry">小程序</span>
              </div>
            </div>
          </van-list>
          <van-empty v-if="!t.loading && visibleList(t.key).length === 0" description="暂无消息" />
        </van-tab>
      </van-tabs>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { notifyApi, type NotificationItem, type NotifyCategory } from '@/api/notify'
import { showToast } from 'vant'

type TabKey = NotifyCategory | 'ALL'
const tabs = reactive<{
  key: TabKey
  title: string
  loading: boolean
  finished: boolean
}[]>([
  { key: 'ALL', title: '全部', loading: false, finished: false },
  { key: 'ATTENDANCE', title: '考勤消息', loading: false, finished: false },
  { key: 'CONSUME', title: '消费消息', loading: false, finished: false },
  { key: 'LOW_BALANCE', title: '余额不足', loading: false, finished: false },
  { key: 'LEAVE_APPROVAL', title: '请假审批', loading: false, finished: false },
  { key: 'ANNOUNCEMENT', title: '学校公告', loading: false, finished: false }
  ,{ key: 'CLASS_NOTICE', title: '班级通知', loading: false, finished: false }
])

const lists: Record<TabKey, NotificationItem[]> = reactive({
  ALL: [],
  ATTENDANCE: [],
  CONSUME: [],
  LOW_BALANCE: [],
  LEAVE_APPROVAL: [],
  ANNOUNCEMENT: []
  ,CLASS_NOTICE: []
})

const pageState: Record<TabKey, { page: number; size: number }> = reactive({
  ALL: { page: 0, size: 10 },
  ATTENDANCE: { page: 0, size: 10 },
  CONSUME: { page: 0, size: 10 },
  LOW_BALANCE: { page: 0, size: 10 },
  LEAVE_APPROVAL: { page: 0, size: 10 },
  ANNOUNCEMENT: { page: 0, size: 10 }
  ,CLASS_NOTICE: { page: 0, size: 10 }
})

const active = ref(0)
const unreadTotal = ref(0)
const showUnreadOnly = ref(false)
const childIdStr = localStorage.getItem('wx_child_id')
const childId = childIdStr ? Number(childIdStr) : null
const hasChild = computed(() => childId !== null && !Number.isNaN(childId))
const router = useRouter()
const route = useRoute()

function loadMore(cat: TabKey) {
  if (!hasChild.value) return
  const st = pageState[cat]
  const tab = tabs.find((x) => x.key === cat)!
  tab.loading = true
  notifyApi
    .list(childId!, cat === 'ALL' ? undefined : (cat as NotifyCategory), st.page, st.size, showUnreadOnly.value)
    .then((resp) => {
      lists[cat].push(...resp.content)
      st.page += 1
      tab.finished = resp.content.length < st.size
    })
    .catch((e) => showToast(e.message || '加载失败'))
    .finally(() => (tab.loading = false))
}

function formatLabel(n: NotificationItem) {
  const time = new Date(n.createdAt).toLocaleString()
  return `${n.content} · ${time}`
}

function openDetail(n: NotificationItem) {
  router.push(`/notify/${n.id}`)
}

function refreshActive() {
  if (!hasChild.value) return
  const cat = tabs[active.value].key
  pageState[cat].page = 0
  tabs[active.value].finished = false
  lists[cat].length = 0
  loadMore(cat)
}



function useSampleChild() {
  try {
    localStorage.setItem('wx_child_id', String(2001))
    showToast('已绑定示例子女：2001，正在刷新页面')
    setTimeout(() => { location.replace('/notify') }, 300)
  } catch {}
}

function visibleList(cat: TabKey) {
  const arr = lists[cat]
  if (!showUnreadOnly.value) return arr
  return arr.filter((n) => !n.readFlag)
}

function getIcon(cat: NotifyCategory) {
  const map: Record<NotifyCategory, string> = {
    ATTENDANCE: 'clock-o',
    CONSUME: 'paid',
    LOW_BALANCE: 'info-o',
    LEAVE_APPROVAL: 'passed',
    ANNOUNCEMENT: 'volume',
    CLASS_NOTICE: 'friends'
  }
  return map[cat] || 'info-o'
}

function categoryText(cat: NotifyCategory) {
  const map: Record<NotifyCategory, string> = {
    ATTENDANCE: '考勤通知',
    CONSUME: '消费通知',
    LOW_BALANCE: '余额不足',
    LEAVE_APPROVAL: '请假审批',
    ANNOUNCEMENT: '学校公告',
    CLASS_NOTICE: '班级通知'
  }
  return map[cat] || cat
}

function targetText(n: NotificationItem) {
  try {
    const ex = n.extra ? JSON.parse(n.extra) : null
    if (ex?.place) return ex.place
    if (ex?.target) return ex.target
  } catch {}
  return `学生ID ${n.childId}`
}

function formatTime(s: string) {
  try { return new Date(s).toLocaleString() } catch { return s }
}

// 首次进入加载当前激活tab数据 & 轮询未读总数
onMounted(async () => {
  if (!hasChild.value) {
    showToast('请先在首页选择子女')
    return
  }
  // 通过查询参数设置默认分类
  const qcat = String(route.query.category || '') as TabKey
  const idx = tabs.findIndex((t) => t.key === qcat)
  if (idx >= 0) active.value = idx
  loadMore(tabs[active.value].key)
  // 轮询实时提醒与未读总数
  const tick = async () => {
    try {
      const latest = await notifyApi.realtime(childId!)
      if (latest && latest.length) {
        showToast(`收到 ${latest.length} 条新消息`)
        // 检测到新消息时自动刷新当前标签，确保列表及时展现
        refreshActive()
      }
      const cat = tabs[active.value].key
      const currentCategory = cat === 'ALL' ? undefined : (cat as NotifyCategory)
      unreadTotal.value = await notifyApi.unreadCount(childId!, currentCategory)
    } catch {}
  }
  tick()
  setInterval(tick, 15000)
})

// 切换标签时若尚未加载过，自动拉取第一页
watch(active, (idx) => {
  const cat = tabs[idx].key
  if (!lists[cat].length && !tabs[idx].loading) {
    pageState[cat].page = 0
    tabs[idx].finished = false
    loadMore(cat)
  }
  // 更新当前标签未读数量
  if (hasChild.value) {
    const currentCategory = cat === 'ALL' ? undefined : (cat as NotifyCategory)
    notifyApi.unreadCount(childId!, currentCategory).then((n) => (unreadTotal.value = Number(n) || 0)).catch(() => {})
  }
})

// 仅看未读切换时，重置当前标签分页并重新拉取数据
watch(showUnreadOnly, () => {
  if (!hasChild.value) return
  const cat = tabs[active.value].key
  pageState[cat].page = 0
  tabs[active.value].finished = false
  lists[cat].length = 0
  loadMore(cat)
})
</script>

<style scoped>
.notify-page {
  background: #fff;
  min-height: 100vh;
}
.no-child {
  padding: 12px 16px;
  color: #666;
  display: flex;
  align-items: center;
}
.no-child i {
  margin-right: 8px;
  color: #fa9c1b;
}
.ops {
  padding: 8px 12px;
  display: flex;
  gap: 8px;
}
.ops .op-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.ops .op-label {
  font-size: 12px;
  color: #666;
}
.notify-item.unread .van-cell__title {
  font-weight: 600;
}

.card-item {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 12px 14px;
  margin: 10px 12px;
}
.card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.card-header .left { display: inline-flex; align-items: center; gap: 8px; }
.card-header .title { font-size: 16px; font-weight: 600; }
.card-header .badge { margin-left: 8px; background: #ee0a24; color: #fff; border-radius: 10px; padding: 2px 8px; font-size: 12px; }
.card-body { display: grid; grid-template-columns: 1fr; gap: 6px; }
.card-body .row { display: flex; gap: 8px; }
.card-body .label { color: #646566; width: 80px; flex-shrink: 0; }
.card-body .value { color: #323233; flex: 1; }
.card-footer { display: flex; justify-content: space-between; margin-top: 12px; color: #969799; font-size: 12px; }
</style>