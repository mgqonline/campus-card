<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">学生信息</div>
      <div class="weui-panel__bd">
        <!-- 子女选择（WeUI 表单样式） -->
        <div class="weui-cells weui-cells_form">
          <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">选择子女</label></div>
            <div class="weui-cell__bd">
              <select class="weui-select" v-model="currentChildId" @change="onChildChange">
                <option v-for="c in children" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 加载状态/空状态 -->
        <div v-if="loading" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">加载中</span></div>
        <div v-else-if="!info" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">暂无数据</span></div>

        <!-- 资料卡片（WeUI风格） -->
        <div v-else class="profile-card">
          <div class="profile">
            <div class="avatar">{{ (info!.name || '学').slice(0,1) }}</div>
            <div class="meta">
              <div class="name">{{ info!.name }}</div>
              <div class="tags">
                <span class="weui-badge" :class="{ warn: (info!.faceStatus || '').includes('未') }">{{ info!.faceStatus || '未知' }}</span>
              </div>
            </div>
          </div>

          <div class="weui-cells">
            <div class="weui-cell">
              <div class="weui-cell__bd">班级ID</div>
              <div class="weui-cell__ft">{{ info!.classId }}</div>
            </div>
            <div class="weui-cell">
              <div class="weui-cell__bd">年级</div>
              <div class="weui-cell__ft">{{ info!.grade }}</div>
            </div>
            <div class="weui-cell">
              <div class="weui-cell__bd">卡号</div>
              <div class="weui-cell__ft">{{ info!.cardNo }}</div>
            </div>
            <div class="weui-cell">
              <div class="weui-cell__bd">人脸状态</div>
              <div class="weui-cell__ft"><span class="status" :class="{ danger: (info!.faceStatus || '').includes('未') }">{{ info!.faceStatus }}</span></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getProfile, type ProfileResp } from '@/api/modules/auth'
import { getStudentInfo, type StudentInfo } from '@/api/modules/student'

const loading = ref(false)
const children = ref<ProfileResp['boundChildren']>([])
const currentChildId = ref<number | null>(null)
const info = ref<StudentInfo | null>(null)

onMounted(async () => {
  const p = await getProfile()
  children.value = p.boundChildren
  const cached = localStorage.getItem('wx_child_id')
  currentChildId.value = cached ? Number(cached) : (children.value[0]?.id ?? null)
  if (currentChildId.value) {
    await loadInfo(currentChildId.value)
  }
})

async function onChildChange() {
  if (currentChildId.value) {
    localStorage.setItem('wx_child_id', String(currentChildId.value))
    await loadInfo(currentChildId.value)
  }
}

async function loadInfo(childId: number) {
  loading.value = true
  info.value = await getStudentInfo(childId)
  loading.value = false
}
</script>

<style scoped>
.profile-card { background:#fff; border:1px solid #eee; border-radius:12px; padding:12px; }
.profile { display:flex; align-items:center; gap:12px; margin-bottom:8px; }
.avatar { width:48px; height:48px; border-radius:50%; background:#f2f2f2; color:#666; display:flex; align-items:center; justify-content:center; font-weight:600; }
.meta .name { font-size:16px; font-weight:600; }
.meta .tags { margin-top:4px; }
.weui-badge.warn { background-color:#f0ad4e; }
.status { font-size:13px; }
.status.danger { color:#d9534f; font-weight:600; }
</style>