<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPhotos, type PhotoItem } from '@/api/modules/attendance'
import { showToast } from 'vant'

const date = ref<string>('')
const photos = ref<PhotoItem[]>([])
const loading = ref(false)

const fetchPhotos = async () => {
  loading.value = true
  try {
    photos.value = await getPhotos({ date: date.value })
  } catch (e: any) {
    showToast(e?.message || '获取考勤照片失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchPhotos)
</script>

<template>
  <div class="page">
    <div class="weui-cells__title">考勤照片</div>
    <div class="weui-cells weui-cells_form">
      <div class="weui-cell">
        <div class="weui-cell__hd"><label class="weui-label">日期</label></div>
        <div class="weui-cell__bd">
          <input class="weui-input" type="date" v-model="date" />
        </div>
      </div>
    </div>
    <div class="weui-form__opr-area" style="padding:12px;">
      <a href="javascript:;" class="weui-btn weui-btn_primary" @click="fetchPhotos" :class="{ 'weui-btn_loading': loading }">
        <span v-if="loading" class="weui-loading"></span> 查询
      </a>
    </div>

    <div class="weui-cells__title">照片列表</div>
    <div class="grid">
      <div v-for="p in photos" :key="p.url + p.time" class="img-card">
        <img :src="p.url" :alt="p.type" />
        <div class="meta">
          <span>{{ p.type }} · {{ p.gate }}</span>
          <span class="time">{{ new Date(p.time).toLocaleString() }}</span>
        </div>
      </div>
      <div v-if="photos.length === 0" class="weui-loadmore weui-loadmore_line" style="grid-column: 1 / -1;">
        <span class="weui-loadmore__tips">暂无照片</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.grid { display:grid; grid-template-columns: repeat(2, 1fr); gap:12px; padding:12px; }
.img-card { background:#fff; border:1px solid #f2f2f2; border-radius:8px; overflow:hidden; box-shadow: 0 1px 2px rgba(0,0,0,0.03); }
.img-card img { width:100%; display:block; }
.meta { display:flex; align-items:center; justify-content:space-between; padding:8px; font-size:12px; color:#666; }
.meta .time { color:#999; }
</style>