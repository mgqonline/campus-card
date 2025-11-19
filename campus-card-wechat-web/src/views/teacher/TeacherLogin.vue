<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { tLogin } from '@/api/modules/teacher'

const router = useRouter()
const phone = ref('13900001234')
const code = ref('123456')
const loading = ref(false)
const errorMsg = ref('')

async function submit() {
  loading.value = true
  errorMsg.value = ''
  try {
    const resp = await tLogin({ phone: phone.value, code: code.value })
    localStorage.setItem('t_token', resp.token)
    router.replace('/t/home')
  } catch (e: any) {
    errorMsg.value = e?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">教师登录</div>
      <div class="weui-panel__bd">
        <div v-if="errorMsg" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">{{ errorMsg }}</span></div>
        <div class="weui-cells weui-cells_form">
          <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">手机号</label></div>
            <div class="weui-cell__bd"><input class="weui-input" v-model="phone" placeholder="请输入手机号" /></div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">验证码</label></div>
            <div class="weui-cell__bd"><input class="weui-input" v-model="code" placeholder="请输入验证码（测试环境为123456）" /></div>
          </div>
        </div>
        <div style="padding:16px;">
          <button class="weui-btn weui-btn_primary" :disabled="loading" @click="submit">{{ loading ? '登录中...' : '登录' }}</button>
        </div>
      </div>
    </div>
  </div>
  
</template>

<style scoped>
.page { padding-bottom: 40px; }
</style>