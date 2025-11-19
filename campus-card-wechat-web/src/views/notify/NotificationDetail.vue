<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { notifyApi, type NotificationItem } from '@/api/notify'

const route = useRoute()
const router = useRouter()
const loading = ref<boolean>(true)
const error = ref<string>('')
const item = ref<NotificationItem | null>(null)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const id = Number(route.params.id)
    const data = await notifyApi.item(id)
    item.value = data
    // 自动标记已读
    if (data && !data.readFlag) {
      try { await notifyApi.markRead(data.id) } catch {}
    }
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)

function goBack() { router.back() }
</script>

<template>
  <div class="page">
    <div v-if="loading" class="weui-loadmore"><i class="weui-loading"></i><span class="weui-loadmore__tips">加载中</span></div>
    <div v-else-if="error" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">{{ error }}</span></div>
    <div v-else-if="item" class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">
        <div class="title">{{ item.title }}</div>
        <div class="meta">
          <span class="badge">{{ item.category }}</span>
          <span class="time">{{ new Date(item.createdAt).toLocaleString() }}</span>
        </div>
      </div>
      <div class="weui-panel__bd">
        <div class="content">{{ item.content }}</div>
        <div v-if="item.extra" class="extra">
          <div class="extra-title">附加信息</div>
          <pre class="extra-json">{{ item.extra }}</pre>
        </div>
        <div class="weui-form__opr-area" style="padding:12px;">
          <button class="weui-btn weui-btn_primary" @click="goBack">返回</button>
        </div>
      </div>
    </div>
  </div>
  
</template>

<style scoped>
.page { min-height: 100vh; background:#fff; padding: 12px 16px; }
.weui-panel { border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.06); }
.weui-panel__hd { padding: 12px 16px; border-bottom: 1px solid #f2f3f5; }
.weui-panel__bd { padding: 12px 16px; }
.title { font-size:16px; font-weight:600; color:#333; }
.meta { margin-top:6px; font-size:12px; color:#888; display:flex; gap:8px; }
.badge { background:#f0f5ff; color:#1d39c4; padding:2px 6px; border-radius:12px; }
.time { color:#888; }
.content { white-space:pre-wrap; line-height:1.7; color:#333; padding:14px 0; }
.extra { margin-top:8px; background:#fafafa; border:1px solid #eee; border-radius:6px; padding:8px; }
.extra-title { font-size:12px; color:#666; margin-bottom:4px; }
.extra-json { font-size:12px; color:#555; }
</style>