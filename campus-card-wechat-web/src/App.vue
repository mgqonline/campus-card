<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { notifyApi } from '@/api/notify'
import { tGetProfile } from '@/api/modules/teacher'

const router = useRouter()
const route = useRoute()
const title = computed(() => (route.meta?.title as string) || '校园卡')
function goBack() { router.back() }
function onRight() {
  const right = route.meta?.right as string | undefined
  switch (right) {
    case 'filter':
      alert('筛选：开发中')
      break
    case 'records':
      router.push('/recharge/records')
      break
    case 'notice':
      router.push('/notify')
      break
    default:
      break
  }
}
const rightIcon = computed(() => {
  const right = route.meta?.right as string | undefined
  if (right === 'filter') return 'filter-o'
  if (right === 'records') return 'bill-o'
  if (right === 'notice') return 'bell-o'
  return ''
})

// 顶部右侧铃铛未读角标与轮询
const unreadCount = ref<number>(0)
let timer: any = null
async function refreshUnread() {
  const right = route.meta?.right as string | undefined
  if (right !== 'notice') return
  const childId = Number(localStorage.getItem('wx_child_id') || 0)
  if (!childId) { unreadCount.value = 0; return }
  try {
    const stat = await notifyApi.stats(childId)
    unreadCount.value = Number(stat?.totalUnread || 0)
  } catch { /* 静默失败 */ }
}
onMounted(() => {
  refreshUnread()
  timer = setInterval(refreshUnread, 15000)
})
onUnmounted(() => { if (timer) clearInterval(timer) })

// 教师端：根据路由前缀切换底部导航，并准备考勤默认班级
const showTeacherTabbar = computed(() => route.path.startsWith('/t/') && route.path !== '/t/login')
const defaultClassId = ref<number | null>(null)
async function ensureTeacherDefaultClass() {
  if (!localStorage.getItem('t_token')) return
  try {
    const p = await tGetProfile()
    const first = (p as any)?.assignedClasses?.[0]
    defaultClassId.value = (first && (first.classId as any)) || 301
  } catch {
    // 静默失败，使用兜底班级ID
    defaultClassId.value = defaultClassId.value || 301
  }
}
onMounted(() => { if (showTeacherTabbar.value) ensureTeacherDefaultClass() })
watch(showTeacherTabbar, (val) => { if (val) ensureTeacherDefaultClass() })
</script>

<template>
  <div>
    <header class="header wechat-header">
      <div class="left" @click="goBack"><van-icon name="arrow-left" /></div>
      <div class="title">{{ title }}</div>
      <div class="right" @click="onRight">
        <div class="icon-bell">
          <van-icon v-if="rightIcon" :name="rightIcon" />
          <span v-if="rightIcon==='bell-o' && unreadCount>0 && $route.name!=='NotificationCenter'" class="weui-badge weui-badge_warn bell-badge">{{ unreadCount>99?'99+':unreadCount }}</span>
        </div>
      </div>
    </header>
    <main class="content">
      <router-view />
    </main>
    <footer v-if="!showTeacherTabbar" class="weui-tabbar">
      <router-link to="/" class="weui-tabbar__item" :class="{ 'weui-bar__item_on': $route.path === '/' }">
        <i class="fa fa-home weui-tabbar__icon"></i>
        <span class="weui-tabbar__label">首页</span>
      </router-link>
      <router-link to="/consume" class="weui-tabbar__item" :class="{ 'weui-bar__item_on': $route.path.startsWith('/consume') }">
        <i class="fa fa-credit-card weui-tabbar__icon"></i>
        <span class="weui-tabbar__label">消费</span>
      </router-link>
      <router-link to="/attendance" class="weui-tabbar__item" :class="{ 'weui-bar__item_on': $route.path.startsWith('/attendance') }">
        <i class="fa fa-clock-o weui-tabbar__icon"></i>
        <span class="weui-tabbar__label">考勤</span>
      </router-link>
      <router-link to="/account/profile" class="weui-tabbar__item" :class="{ 'weui-bar__item_on': $route.path.startsWith('/account') }">
        <i class="fa fa-user weui-tabbar__icon"></i>
        <span class="weui-tabbar__label">我</span>
      </router-link>
    </footer>
    <footer v-else class="weui-tabbar">
      <router-link to="/t/home" class="weui-tabbar__item" :class="{ 'weui-bar__item_on': $route.path.startsWith('/t/home') }">
        <i class="fa fa-home weui-tabbar__icon"></i>
        <span class="weui-tabbar__label">首页</span>
      </router-link>
      <router-link :to="`/t/class/${defaultClassId || 301}/attendance`" class="weui-tabbar__item" :class="{ 'weui-bar__item_on': $route.path.startsWith('/t/class') && $route.path.endsWith('/attendance') }">
        <i class="fa fa-clock-o weui-tabbar__icon"></i>
        <span class="weui-tabbar__label">考勤</span>
      </router-link>
      <router-link to="/t/account/profile" class="weui-tabbar__item" :class="{ 'weui-bar__item_on': $route.path.startsWith('/t/account') }">
        <i class="fa fa-user weui-tabbar__icon"></i>
        <span class="weui-tabbar__label">我</span>
      </router-link>
    </footer>
  </div>
</template>

<style scoped>
/* WeChat-style Tabbar fine-tuning */
:deep(.van-tabbar) {
  height: 50px; /* per spec */
  border-top: 1px solid #eee;
  padding: 0; /* 移除默认内边距 */
  margin: 0; /* 移除默认外边距 */
  box-sizing: border-box;
  /* 保持Vant默认布局，仅提升可点击与层级 */
  z-index: 1000;
  pointer-events: auto;
}
:deep(.van-tabbar-item) {
  /* 强制四项等宽，贴近WeUI视觉规范 */
  flex: 0 0 25%;
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 0; /* 防止内容溢出 */
  padding: 4px 0; /* 统一内边距 */
  margin: 0; /* 移除外边距 */
  box-sizing: border-box;
  pointer-events: auto;
}
:deep(.van-tabbar-item) { transition: transform .08s ease, color .2s ease; }
:deep(.van-tabbar-item:active) { transform: scale(0.96); }
:deep(.van-tabbar-item__icon) {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 26px; /* 固定图标区高度 */
  margin-bottom: 0;
}
:deep(.van-tabbar-item__icon .van-icon) {
  font-size: 24px;
}
:deep(.van-tabbar-item__text) {
  font-size: 12px;
  line-height: 14px; /* 固定文字区高度 */
  margin-top: 2px;
  text-align: center;
  white-space: nowrap; /* 防止文字换行 */
}
/* 选中态强调与动效（贴近WeUI规范） */
:deep(.van-tabbar-item--active .van-tabbar-item__text) { font-weight: 600; }
:deep(.van-tabbar-item--active .van-tabbar-item__icon .van-icon) { transform: scale(1.02); }
.wechat-header { display:flex; align-items:center; justify-content:space-between; }
.wechat-header .left, .wechat-header .right { width: 40px; display:flex; align-items:center; justify-content:center; color:#333; }
.wechat-header .title { font-size: 18px; font-weight: 500; color:#333; }

/* 固定底部 WeUI Tabbar，与 page.html 保持一致 */
.weui-tabbar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  border-top: 1px solid #eee;
  z-index: 50;
  height: 50px;
}
.weui-tabbar__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  color: #999;
  text-decoration: none;
  font-size: 10px;
  height: 100%;
}
.weui-bar__item_on { color: #1677ff; }
.weui-tabbar__icon { font-size: 20px; margin-bottom: 2px; }

/* 避免内容被底部导航遮挡 */
.content { padding-bottom: 60px; }
:deep(.van-icon) { vertical-align: middle; }
.icon-bell { position: relative; display: inline-block; }
.bell-badge { position: absolute; right: -8px; top: -6px; font-size: 10px; }
</style>
