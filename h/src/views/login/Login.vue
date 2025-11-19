<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { login, profile, sendForgotCode, resetPasswordWithCode } from '@/api/modules/auth'
import { usePermStore } from '@/stores/perm'
import { UserOutlined, LockOutlined, CreditCardOutlined, SmileOutlined, SafetyOutlined, BellOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const form = ref({ username: '', password: '' })
const rememberMe = ref(true)
const companyName = (import.meta as any)?.env?.VITE_COMPANY_NAME || '校园一卡通'
const icpRecord = (import.meta as any)?.env?.VITE_ICP || ''
const forgotOpen = ref(false)
const forgotLoading = ref(false)
const forgotUsername = ref('')
const forgotCode = ref('')
const forgotNewPwd = ref('')
const forgotConfirmPwd = ref('')
const sendLoading = ref(false)
const countdown = ref(0)
const loading = ref(false)

const onSubmit = async () => {
  loading.value = true
  try {
    if (!form.value.username || !form.value.password) {
      message.error('请输入用户名和密码')
      return
    }
    const resp = await login({ username: form.value.username, password: form.value.password })
    const token = (resp as any)?.token || resp
    if (token) {
      try {
        if (rememberMe.value) {
          localStorage.setItem('token', token)
        } else {
          sessionStorage.setItem('token', token)
        }
      } catch {}
      // 登录成功后加载权限，再进入首页，避免菜单空白
      const perm = usePermStore()
      await perm.load()
      message.success('登录成功')
      router.push('/dashboard')
    } else {
      message.error('登录失败：未获取到令牌')
    }
  } catch (e: any) {
    const errMsg = e?.message || '登录失败'
    message.error(errMsg)
  } finally {
    loading.value = false
  }
}

// 登录页不做任何自动动作
onMounted(() => {})

const parX = ref(0)
const parY = ref(0)
const onMouseMove = (e: MouseEvent) => {
  const el = e.currentTarget as HTMLElement
  const r = el.getBoundingClientRect()
  const x = (e.clientX - (r.left + r.width / 2)) / (r.width / 2)
  const y = (e.clientY - (r.top + r.height / 2)) / (r.height / 2)
  parX.value = Math.max(-1, Math.min(1, x))
  parY.value = Math.max(-1, Math.min(1, y))
}
const overlayLeftStyle = computed(() => ({ transform: `translate3d(${parX.value * 8}px, ${parY.value * 6}px, 0)` }))
const overlayRightStyle = computed(() => ({ transform: `translate3d(${parX.value * -8}px, ${parY.value * -6}px, 0)` }))

const onForgotOk = async () => {
  if (!forgotUsername.value || !forgotCode.value || !forgotNewPwd.value || !forgotConfirmPwd.value) { message.error('请完整填写信息'); return }
  if (!/^\d{6}$/.test(forgotCode.value)) { message.error('验证码为6位数字'); return }
  if (forgotNewPwd.value.length < 6) { message.error('新密码至少 6 位'); return }
  if (forgotNewPwd.value !== forgotConfirmPwd.value) { message.error('两次输入的密码不一致'); return }
  forgotLoading.value = true
  try {
    await resetPasswordWithCode(forgotUsername.value, forgotCode.value, forgotNewPwd.value)
    const resp: any = await login({ username: forgotUsername.value, password: forgotNewPwd.value })
    const tk = resp?.token || resp
    if (tk) {
      try { if (rememberMe.value) { localStorage.setItem('token', tk) } else { sessionStorage.setItem('token', tk) } } catch {}
      const perm = usePermStore()
      await perm.load()
      message.success('密码已重置并登录成功')
      forgotOpen.value = false
      router.push('/dashboard')
    } else {
      message.error('登录失败：未获取到令牌')
    }
  } catch (e: any) {
    const msg = e?.message || '找回密码失败，请联系管理员处理'
    message.error(msg)
  } finally {
    forgotLoading.value = false
  }
}

const onForgotCancel = () => {
  forgotUsername.value = ''
  forgotCode.value = ''
  forgotNewPwd.value = ''
  forgotConfirmPwd.value = ''
}

const onSendCode = async () => {
  if (!forgotUsername.value) { message.error('请先填写用户名'); return }
  if (countdown.value > 0) return
  sendLoading.value = true
  try {
    const info: any = await sendForgotCode(forgotUsername.value)
    message.success('验证码已发送至绑定手机号 ' + (info?.phoneMasked || ''))
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e: any) {
    message.error(e?.message || '验证码发送失败')
  } finally {
    sendLoading.value = false
  }
}
</script>

<template>
  <div class="login-page" @mousemove="onMouseMove">
    <div class="bg" aria-hidden="true"></div>
    <div class="illustrations" aria-hidden="true">
      <div class="card-illu left">
        <svg class="card-svg" width="480" height="300" viewBox="0 0 480 300" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="cc-grad" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stop-color="#7c3aed"/>
              <stop offset="50%" stop-color="#0ea5e9"/>
              <stop offset="100%" stop-color="#22c55e"/>
            </linearGradient>
          </defs>
          <rect x="30" y="30" width="420" height="240" rx="24" fill="url(#cc-grad)" opacity="0.85"/>
          <rect x="64" y="76" width="60" height="40" rx="6" fill="#f6d365" opacity="0.85" stroke="#f5c96b"/>
          <path d="M140 96 H420" stroke="rgba(255,255,255,0.6)" stroke-width="2" stroke-linecap="round"/>
          <path d="M140 110 H380" stroke="rgba(255,255,255,0.4)" stroke-width="2" stroke-linecap="round"/>
          <text x="64" y="190" font-size="26" font-weight="600" fill="#fff" opacity="0.95">校园一卡通</text>
          <text x="64" y="220" font-size="14" fill="#fff" opacity="0.85">Campus Card</text>
        </svg>
        <div class="card-overlay" :style="overlayLeftStyle">
          <div class="chip small consume">
            <div class="icon"><credit-card-outlined /></div>
            <div class="text">消费</div>
          </div>
          <div class="chip small face">
            <div class="icon"><smile-outlined /></div>
            <div class="text">人脸</div>
          </div>
          <div class="impacts">
            <span class="streak s1"></span>
            <span class="streak s2"></span>
            <span class="streak s3"></span>
            <span class="ripple"></span>
          </div>
          <div class="projectiles">
            <div class="proj p1"><credit-card-outlined /></div>
            <div class="proj p2"><smile-outlined /></div>
            <div class="proj p3"><safety-outlined /></div>
            <div class="proj p4"><bell-outlined /></div>
          </div>
          <div class="orbits">
            <div class="orbit o1"><span class="orb-dot"></span></div>
            <div class="orbit o2"><span class="orb-dot"></span></div>
            <div class="orbit o3"><span class="orb-dot"></span></div>
          </div>
          <div class="geo">
            <span class="geo-line g1"></span>
            <span class="geo-line g2"></span>
            <span class="geo-line g3"></span>
            <span class="geo-line g4"></span>
          </div>
        </div>
      </div>
      
    </div>
    <div class="beams" aria-hidden="true">
      <div class="beam b1"></div>
      <div class="beam b2"></div>
      <div class="beam b3"></div>
    </div>
    <div class="login-card">
      <div class="brand">
        <div class="logo-dot"></div>
        <div class="brand-text">
          <div class="title">校园一卡通后台</div>
          <div class="sub">Campus Card Admin</div>
        </div>
      </div>
      <a-form layout="horizontal" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item label="用户名">
          <a-input v-model:value="form.username" placeholder="请输入用户名">
            <template #prefix>
              <user-outlined />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item label="密码">
          <a-input-password v-model:value="form.password" placeholder="请输入密码">
            <template #prefix>
              <lock-outlined />
            </template>
          </a-input-password>
        </a-form-item>
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
          <a-checkbox v-model:checked="rememberMe">记住我</a-checkbox>
          <a-button type="link" @click="forgotOpen = true">忘记密码？</a-button>
        </div>
        <a-button type="primary" block :loading="loading" @click="onSubmit">登录</a-button>
      </a-form>
      <a-modal
        v-model:open="forgotOpen"
        title="找回密码"
        :confirmLoading="forgotLoading"
        @ok="onForgotOk"
        @cancel="onForgotCancel"
      >
        <a-form layout="horizontal" :label-col="{ span: 7 }" :wrapper-col="{ span: 17 }">
          <a-form-item label="用户名" required>
            <a-input v-model:value="forgotUsername" placeholder="请输入用户名" />
          </a-form-item>
          <a-form-item label="验证码" required>
            <div style="display:flex;gap:8px;align-items:center;">
              <a-input v-model:value="forgotCode" placeholder="请输入6位验证码" style="flex:1;" />
              <a-button :loading="sendLoading" :disabled="countdown>0" @click="onSendCode">
                {{ countdown>0 ? `${countdown}s` : '发送验证码' }}
              </a-button>
            </div>
            <div style="margin-top:4px;color:#888;font-size:12px;">若用户名已绑定手机号，将发送短信验证码；未绑定则无法找回</div>
          </a-form-item>
          <a-form-item label="新密码" required>
            <a-input-password v-model:value="forgotNewPwd" placeholder="请输入新密码" />
          </a-form-item>
          <a-form-item label="确认新密码" required>
            <a-input-password v-model:value="forgotConfirmPwd" placeholder="请再次输入新密码" />
          </a-form-item>
          <div style="margin-top:8px;color:#888;font-size:12px;">如遇无法重置，请联系管理员处理</div>
        </a-form>
      </a-modal>
    </div>
    <div class="footer">
      <div class="footer-line">© {{ companyName }}</div>
      <div class="footer-line" v-if="icpRecord">
        <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer">{{ icpRecord }}</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  display: grid;
  place-items: center;
  padding: 24px;
  background: radial-gradient(1200px 600px at 10% 10%, #0ea5e9 0%, transparent 60%),
              radial-gradient(1200px 800px at 90% 20%, #22c55e 0%, transparent 60%),
              radial-gradient(1200px 800px at 0% 100%, #a855f7 0%, transparent 60%),
              linear-gradient(135deg, #0f172a 0%, #111827 50%, #1f2937 100%);
}
.footer {
  position: absolute;
  bottom: 12px;
  left: 0;
  right: 0;
  text-align: center;
  color: rgba(255,255,255,0.85);
  font-size: 12px;
}
.footer a { color: inherit; }
.footer-line { line-height: 1.6; }
.bg::before,
.bg::after {
  content: '';
  position: absolute;
  inset: -20% -10% -10% -20%;
  pointer-events: none;
}
.illustrations {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.card-illu { position: absolute; }
.card-illu.left { left: 120px; top: 14%; transform: rotate(-8deg); }
.card-illu.right { right: 40px; bottom: 10%; transform: rotate(10deg); }
.card-svg {
  width: 460px;
  height: auto;
  opacity: 0.85;
  filter: drop-shadow(0 24px 60px rgba(2,6,23,0.45));
}
@keyframes floatSlow { 0% { transform: translateY(0); } 50% { transform: translateY(-6px); } 100% { transform: translateY(0); } }
.card-illu.left .card-svg, .card-illu.right .card-svg { animation: floatSlow 8s ease-in-out infinite; }
.bg::before {
  background:
    radial-gradient(320px 320px at 20% 30%, rgba(255,255,255,0.10) 0%, rgba(255,255,255,0) 60%),
    radial-gradient(360px 360px at 80% 60%, rgba(255,255,255,0.10) 0%, rgba(255,255,255,0) 60%);
  filter: blur(30px);
  animation: drift 16s ease-in-out infinite alternate;
}
.bg::after {
  background: linear-gradient(180deg, rgba(255,255,255,0.10), rgba(255,255,255,0.05));
}
@keyframes drift {
  0% { transform: translate3d(0,0,0) scale(1); }
  100% { transform: translate3d(20px,-10px,0) scale(1.05); }
}

.login-card {
  width: 100%;
  max-width: 420px;
  border-radius: 18px;
  padding: 28px;
  background: rgba(255,255,255,0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(148,163,184,0.35);
  box-shadow: 0 20px 50px rgba(2,6,23,0.35);
  color: #fff;
  position: relative;
}
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.logo-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: conic-gradient(from 180deg, #22c55e, #0ea5e9, #a855f7, #22c55e);
  box-shadow: 0 0 0 6px rgba(255,255,255,0.08), 0 6px 18px rgba(168,85,247,0.45);
}
.brand-text .title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.4px;
}
.brand-text .sub {
  font-size: 12px;
  opacity: 0.85;
}

/* 表单与按钮的高端视觉微调 */
:deep(.ant-form-item-label > label) { color: rgba(255,255,255,0.92); font-weight: 600; }
/* 仅在外层包装应用卡片样式，避免与内层 input 叠加 */
:deep(.ant-input-affix-wrapper) { height: 40px; border-radius: 10px !important; background: rgba(255,255,255,0.95); border: 1px solid rgba(148,163,184,0.35); box-shadow: none; }
:deep(.ant-input-affix-wrapper:hover) { border-color: rgba(99,102,241,0.55) !important; }
:deep(.ant-input-affix-wrapper-focused) { border-color: rgba(99,102,241,0.65) !important; box-shadow: none; background: #fff; }
/* 内层 input 仅作为文本层显示，禁用背景/边框/阴影避免出现双层卡片 */
:deep(.ant-input) {
  background: transparent;
  border: 0;
  box-shadow: none;
}
.field :deep(.ant-input-prefix) { color: #6b7280; display: inline-flex; align-items: center; }
/* 统一后缀图标的颜色 */
.field :deep(.ant-input-suffix svg) { color: #6b7280; }
.field :deep(.ant-input::placeholder), .field :deep(.ant-input-password .ant-input::placeholder) {
  color: #9aa0a6;
}
:deep(.ant-btn-primary) {
  height: 40px;
  border-radius: 10px;
  font-weight: 600;
  background: linear-gradient(135deg, #7c3aed, #0ea5e9);
  box-shadow: 0 12px 30px rgba(14,165,233,0.45);
}
:deep(.ant-btn-primary:hover) { filter: brightness(1.05); }

.card-illu { position: absolute; }
.card-illu .card-overlay { position: absolute; inset: 0; width: 460px; height: 100%; pointer-events: none; }
.card-illu .burst { position: absolute; width: 120px; height: 120px; display: grid; place-items: center; }
.card-illu .burst .dot { width: 6px; height: 6px; border-radius: 50%; background: rgba(255,255,255,.9); box-shadow: 0 0 20px rgba(255,255,255,.45); animation: burst 2.4s ease-out infinite; }
.card-illu .burst .d2 { animation-delay: .25s; transform: scale(1.3); }
.card-illu .burst .d3 { animation-delay: .5s; transform: scale(1.6); }
.card-illu.left .burst.b1 { left: 20%; top: 30%; }
@keyframes burst { 0% { transform: scale(.4); opacity: .0; } 40% { opacity: 1; } 100% { transform: scale(2.2); opacity: 0; } }
.card-illu .chip { position: absolute; display: flex; align-items: center; gap: 8px; padding: 8px 12px; border-radius: 999px; background: rgba(255,255,255,0.18); backdrop-filter: blur(8px); color: #fff; box-shadow: 0 14px 32px rgba(2,6,23,0.38); }
.card-illu .chip.small .icon { width: 22px; height: 22px; display: inline-flex; align-items: center; justify-content: center; }
.card-illu .chip.small .text { font-size: 13px; font-weight: 700; letter-spacing: .2px; }
.card-illu.left .chip.consume { left: 16px; top: 22px; transform: rotate(-6deg); }
.card-illu.left .chip.face { right: 24px; bottom: 18px; transform: rotate(8deg); }
.card-illu .impacts { position: absolute; inset: 0; }
.card-illu .impacts .streak { position: absolute; width: 120px; height: 3px; background: linear-gradient(90deg, rgba(255,255,255,0), rgba(255,255,255,.9)); filter: blur(1px); opacity: .9; }
.card-illu.left .impacts .s1 { left: -140px; top: 40%; animation: streakLeft 3.2s ease-in-out infinite; }
.card-illu.left .impacts .s2 { left: -180px; top: 55%; animation: streakLeft 3.6s ease-in-out infinite .3s; }
.card-illu.left .impacts .s3 { left: -160px; top: 32%; animation: streakLeft 3.8s ease-in-out infinite .6s; }
@keyframes streakLeft { 0% { transform: translateX(0); opacity: .0; } 10% { opacity: .7; } 70% { transform: translateX(220px); opacity: .9; } 100% { transform: translateX(260px); opacity: 0; } }
.card-illu .impacts .ripple { position: absolute; width: 120px; height: 120px; border-radius: 50%; border: 2px solid rgba(255,255,255,.35); filter: blur(1px); animation: ripple 2.8s ease-in-out infinite; }
.card-illu.left .impacts .ripple { left: 62%; top: 54%; }
@keyframes ripple { 0% { transform: scale(.6); opacity: .0; } 30% { opacity: .35; } 60% { transform: scale(1.15); opacity: .2; } 100% { transform: scale(1.35); opacity: 0; } }
.card-illu .projectiles { position: absolute; inset: 0; }
.card-illu .proj { position: absolute; width: 42px; height: 42px; border-radius: 50%; display: grid; place-items: center; background: radial-gradient(closest-side, rgba(255,255,255,.9), rgba(255,255,255,.2)); box-shadow: 0 10px 24px rgba(2,6,23,.35); filter: blur(.2px); }
.card-illu .proj svg { color: #111827; }
.card-illu.left .proj.p1 { left: -220px; top: 36%; animation: projInLeft 3.2s ease-in-out infinite; }
.card-illu.left .proj.p2 { left: -260px; top: 48%; animation: projInLeft 3.6s ease-in-out infinite .2s; }
.card-illu.left .proj.p3 { left: -240px; top: 58%; animation: projInLeft 3.8s ease-in-out infinite .4s; }
.card-illu.left .proj.p4 { left: -280px; top: 28%; animation: projInLeft 4.0s ease-in-out infinite .6s; }
@keyframes projInLeft { 0% { transform: translateX(0) scale(1.15); opacity: .0; } 20% { opacity: .9; } 70% { transform: translateX(240px) scale(.75); opacity: .95; } 100% { transform: translateX(280px) scale(.6); opacity: .0; } }
.orbits { position: absolute; inset: 0; }
.orbit { position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); border-radius: 50%; border: 2px solid rgba(255,255,255,.35); mix-blend-mode: screen; }
.orbit.o1 { width: 360px; height: 360px; transform: translate(-50%, -50%) rotateX(22deg) rotate(0deg); animation: orbSpin1 9s linear infinite; }
.orbit.o2 { width: 300px; height: 300px; transform: translate(-50%, -50%) rotateX(32deg) rotate(0deg); animation: orbSpin2 8s linear infinite reverse; }
.orbit.o3 { width: 240px; height: 240px; transform: translate(-50%, -50%) rotateX(14deg) rotate(0deg); animation: orbSpin3 7s linear infinite; }
.orbit .orb-dot { position: absolute; left: 50%; top: -6px; width: 10px; height: 10px; border-radius: 50%; background: radial-gradient(closest-side, #fff, rgba(255,255,255,.2)); box-shadow: 0 0 16px rgba(255,255,255,.65); }
@keyframes orbSpin1 { to { transform: translate(-50%, -50%) rotateX(22deg) rotate(360deg); } }
@keyframes orbSpin2 { to { transform: translate(-50%, -50%) rotateX(32deg) rotate(360deg); } }
@keyframes orbSpin3 { to { transform: translate(-50%, -50%) rotateX(14deg) rotate(360deg); } }
.geo { position: absolute; inset: 0; }
.geo-line { position: absolute; width: 160px; height: 2px; background: linear-gradient(90deg, rgba(255,255,255,.0), rgba(255,255,255,.95)); filter: blur(.6px); }
.geo .g1 { left: -180px; top: 34%; transform: rotate(18deg); animation: geoIn1 3.2s ease-in-out infinite; }
.geo .g2 { left: -220px; top: 52%; transform: rotate(-12deg); animation: geoIn2 3.6s ease-in-out infinite .2s; }
.geo .g3 { left: -200px; top: 44%; transform: rotate(6deg); animation: geoIn3 3.8s ease-in-out infinite .4s; }
.geo .g4 { left: -240px; top: 60%; transform: rotate(-22deg); animation: geoIn4 4.0s ease-in-out infinite .6s; }
@keyframes geoIn1 { 0% { transform: translateX(0) rotate(18deg) scale(1); opacity: .0; } 30% { opacity: .9; } 100% { transform: translateX(280px) rotate(18deg) scale(.6); opacity: .0; } }
@keyframes geoIn2 { 0% { transform: translateX(0) rotate(-12deg) scale(1); opacity: .0; } 30% { opacity: .9; } 100% { transform: translateX(280px) rotate(-12deg) scale(.6); opacity: .0; } }
@keyframes geoIn3 { 0% { transform: translateX(0) rotate(6deg) scale(1); opacity: .0; } 30% { opacity: .9; } 100% { transform: translateX(280px) rotate(6deg) scale(.6); opacity: .0; } }
@keyframes geoIn4 { 0% { transform: translateX(0) rotate(-22deg) scale(1); opacity: .0; } 30% { opacity: .9; } 100% { transform: translateX(280px) rotate(-22deg) scale(.6); opacity: .0; } }

.login-card { justify-self: end; margin-right: 80px; }

/* 响应式细化 */
@media (max-width: 480px) {
  .login-card { padding: 22px; border-radius: 16px; }
  .brand { margin-bottom: 12px; }
}

/* 深色方案适配 */
@media (prefers-color-scheme: light) {
  .login-page { background: radial-gradient(1200px 600px at 10% 10%, #bae6fd 0%, transparent 60%),
                            radial-gradient(1200px 800px at 90% 20%, #bbf7d0 0%, transparent 60%),
                            radial-gradient(1200px 800px at 0% 100%, #e9d5ff 0%, transparent 60%),
                            linear-gradient(135deg, #f8fafc 0%, #f1f5f9 50%, #e5e7eb 100%); }
  .login-card { color: #0f172a; background: rgba(255,255,255,0.92); border-color: rgba(148,163,184,0.35); }
  :deep(.ant-form-item-label > label) { color: #0f172a; }
}
</style>