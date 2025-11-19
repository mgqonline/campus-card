<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login, type LoginReq } from '@/api/modules/auth'

const router = useRouter()
const form = ref<LoginReq>({ phone: '', code: '' })
const loading = ref(false)
const error = ref('')
const success = ref('')

// 手机号格式验证
const validatePhone = (phone: string) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

// 发送验证码（模拟）
const sendCode = () => {
  if (!form.value.phone) {
    error.value = '请先输入手机号'
    return
  }
  if (!validatePhone(form.value.phone)) {
    error.value = '请输入正确的手机号格式'
    return
  }
  success.value = '验证码已发送（测试环境请使用：123456）'
  error.value = ''
}

const onSubmit = async () => {
  error.value = ''
  success.value = ''
  
  if (!form.value.phone || !form.value.code) {
    error.value = '请输入手机号与验证码'
    return
  }
  
  if (!validatePhone(form.value.phone)) {
    error.value = '请输入正确的手机号格式'
    return
  }
  
  loading.value = true
  try {
    const resp = await login(form.value)
    localStorage.setItem('wx_token', resp.token)
    success.value = '登录成功，正在跳转...'
    setTimeout(() => {
      router.push('/')
    }, 1000)
  } catch (e: any) {
    error.value = e?.message || '登录失败，请检查手机号和验证码'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page">
    <!-- 头部标题区域 -->
    <div class="login-header">
      <div class="login-logo">
        <i class="fa fa-graduation-cap" style="font-size: 48px; color: #1aad19;"></i>
      </div>
      <h2 class="login-title">校园一卡通</h2>
      <p class="login-subtitle">家长端登录</p>
    </div>

    <!-- 登录表单 -->
    <div class="weui-cells weui-cells_form" style="margin-top: 20px;">
      <div class="weui-cell">
        <div class="weui-cell__hd">
          <label class="weui-label">手机号</label>
        </div>
        <div class="weui-cell__bd">
          <input 
            class="weui-input" 
            type="tel" 
            v-model="form.phone" 
            placeholder="请输入手机号"
            maxlength="11"
          />
        </div>
      </div>
      
      <div class="weui-cell weui-cell_vcode">
        <div class="weui-cell__hd">
          <label class="weui-label">验证码</label>
        </div>
        <div class="weui-cell__bd">
          <input 
            class="weui-input" 
            type="number" 
            v-model="form.code" 
            placeholder="请输入验证码"
            maxlength="6"
          />
        </div>
        <div class="weui-cell__ft">
          <button 
            type="button" 
            class="weui-vcode-btn"
            @click="sendCode"
            :disabled="!form.phone || !validatePhone(form.phone)"
          >
            获取验证码
          </button>
        </div>
      </div>
    </div>

    <!-- 错误和成功提示 -->
    <div v-if="error" class="weui-toptips weui-toptips_warn" style="display: block; position: relative; margin: 15px 0;">
      <i class="fa fa-exclamation-triangle"></i> {{ error }}
    </div>
    
    <div v-if="success" class="weui-toptips weui-toptips_success" style="display: block; position: relative; margin: 15px 0;">
      <i class="fa fa-check-circle"></i> {{ success }}
    </div>

    <!-- 登录按钮 -->
    <div class="weui-btn-area" style="margin-top: 30px;">
      <button 
        type="button" 
        class="weui-btn weui-btn_primary"
        :class="{ 'weui-btn_loading': loading }"
        :disabled="loading || !form.phone || !form.code"
        @click="onSubmit"
      >
        <span v-if="loading" class="weui-loading"></span>
        {{ loading ? '登录中...' : '登录' }}
      </button>
    </div>

    <!-- 帮助信息 -->
    <div class="login-help">
      <p class="weui-cells__tips">
        <i class="fa fa-info-circle"></i> 
        测试环境提示：验证码请使用 123456
      </p>
      <p class="weui-cells__tips">
        登录即表示同意 <a href="#" style="color: #1aad19;">用户协议</a> 和 <a href="#" style="color: #1aad19;">隐私政策</a>
      </p>
    </div>
  </div>
</template>

<style scoped>
.page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-header {
  text-align: center;
  padding: 40px 0 20px;
}

.login-logo {
  margin-bottom: 15px;
}

.login-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px;
}

.login-subtitle {
  font-size: 16px;
  color: #888;
  margin: 0;
}

.weui-cells {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.weui-vcode-btn {
  background: #1aad19;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.weui-vcode-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.weui-toptips {
  border-radius: 6px;
  padding: 12px 15px;
  font-size: 14px;
}

.weui-toptips_warn {
  background: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}

.weui-toptips_success {
  background: #f0f9ff;
  color: #1aad19;
  border: 1px solid #b3d8ff;
}

.weui-btn-area {
  padding: 0;
}

.weui-btn {
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  height: 48px;
  box-shadow: 0 2px 8px rgba(26, 173, 25, 0.3);
}

.weui-btn:disabled {
  background: #ccc !important;
  box-shadow: none;
}

.login-help {
  margin-top: 30px;
  text-align: center;
}

.login-help .weui-cells__tips {
  color: #888;
  font-size: 13px;
  margin: 8px 0;
}

.login-help .weui-cells__tips i {
  margin-right: 5px;
}

.weui-loading {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid #fff;
  border-top: 2px solid transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 8px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>