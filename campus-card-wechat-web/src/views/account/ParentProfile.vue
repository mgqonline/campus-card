<template>
  <div class="page">
    <div class="profile-card">
      <div class="avatar"><i class="fa fa-user-circle-o"></i></div>
      <div class="meta">
        <div class="name">{{ profile.name }}</div>
        <div class="sub">所属学校：{{ profile.schoolName || '—' }}</div>
      </div>
    </div>

    <div class="weui-cells__title">资料设置</div>
  <div class="weui-cells weui-cells_form">
    <div class="weui-cell">
      <div class="weui-cell__hd"><label class="weui-label">姓名</label></div>
      <div class="weui-cell__bd"><input class="weui-input" v-model="name" placeholder="请输入姓名" /></div>
      <div class="weui-cell__ft"><button class="weui-btn weui-btn_mini weui-btn_primary" @click="saveProfile">保存</button></div>
    </div>
    <div class="weui-cell">
      <div class="weui-cell__hd"><label class="weui-label">手机号</label></div>
      <div class="weui-cell__bd"><input class="weui-input" v-model="phone" placeholder="请输入手机号" /></div>
      <div class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell">
      <div class="weui-cell__hd"><label class="weui-label">邮箱</label></div>
      <div class="weui-cell__bd"><input class="weui-input" v-model="email" placeholder="请输入邮箱（可选）" /></div>
      <div class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell">
      <div class="weui-cell__hd"><label class="weui-label">联系地址</label></div>
      <div class="weui-cell__bd"><input class="weui-input" v-model="address" placeholder="请输入联系地址（可选）" /></div>
      <div class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell">
      <div class="weui-cell__hd"><label class="weui-label">微信OpenId</label></div>
      <div class="weui-cell__bd"><input class="weui-input" v-model="openId" placeholder="模拟或填写真实OpenId" /></div>
      <div class="weui-cell__ft"><button class="weui-btn weui-btn_mini weui-btn_default" @click="bind">绑定</button></div>
    </div>
  </div>

    <div class="weui-cells__title">快捷入口</div>
    <div class="weui-cells">
      <router-link class="weui-cell weui-cell_access" to="/student/info">
        <div class="weui-cell__bd">绑定子女管理</div>
        <div class="weui-cell__ft"></div>
      </router-link>
      <router-link class="weui-cell weui-cell_access" to="/account/settings">
        <div class="weui-cell__bd">设置</div>
        <div class="weui-cell__ft"></div>
      </router-link>
      <router-link class="weui-cell weui-cell_access" to="/account/help">
        <div class="weui-cell__bd">帮助中心</div>
        <div class="weui-cell__ft"></div>
      </router-link>
      <router-link class="weui-cell weui-cell_access" to="/account/about">
        <div class="weui-cell__bd">关于我们</div>
        <div class="weui-cell__ft"></div>
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getProfile, updateProfile, bindWeChat, type ProfileResp } from '@/api/modules/auth'

const loading = ref(true)
const profile = ref<ProfileResp>({ parentId: 0, name: '', boundChildren: [] })
const name = ref('')
const phone = ref('')
const email = ref('')
const address = ref('')
const openId = ref('mock-openid-123')

onMounted(async () => {
  const p = await getProfile()
  profile.value = p
  name.value = p.name
  phone.value = p.phone || ''
  email.value = p.email || ''
  address.value = p.address || ''
  loading.value = false
})

async function saveProfile() {
  // 简单校验
  if (phone.value && !/^1[3-9]\d{9}$/.test(phone.value)) {
    alert('手机号格式不正确')
    return
  }
  if (email.value && !/^\S+@\S+\.\S+$/.test(email.value)) {
    alert('邮箱格式不正确')
    return
  }
  const p = await updateProfile({ name: name.value, phone: phone.value || undefined, email: email.value || undefined, address: address.value || undefined })
  profile.value = p
  alert('资料已更新')
}

async function bind() {
  await bindWeChat({ openId: openId.value })
  alert('绑定成功')
}
</script>

<style scoped>
/* 个人中心头部卡片（与首页风格一致） */
.profile-card { display:flex; align-items:center; padding:12px 15px; background:#fff; border-radius:8px; box-shadow:0 1px 3px rgba(0,0,0,0.04); margin-bottom:12px; }
.profile-card .avatar { width:56px; height:56px; border-radius:50%; background:#e6f0ff; display:flex; align-items:center; justify-content:center; color:#2778F1; margin-right:12px; }
.profile-card .avatar .fa { font-size:28px; }
.profile-card .meta .name { font-size:16px; font-weight:600; color:#333; }
.profile-card .meta .sub { font-size:12px; color:#888; margin-top:2px; }
</style>