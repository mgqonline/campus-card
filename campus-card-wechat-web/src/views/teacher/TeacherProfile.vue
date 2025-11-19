<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { tGetProfile, tBindWeChat, tLogout, type TProfileResp } from '@/api/modules/teacher'
import { useRouter } from 'vue-router'
const router = useRouter()
const profile = ref<TProfileResp | null>(null)
const openId = ref('')
const loadError = ref('')
async function bind() {
  try { await tBindWeChat({ openId: openId.value }); alert('绑定成功') } catch (e: any) { alert(e?.message || '绑定失败') }
}
onMounted(async () => {
  // 若未登录教师端，跳转登录
  if (!localStorage.getItem('t_token')) { router.replace('/t/login'); return }
  try { profile.value = await tGetProfile() } catch (e: any) { loadError.value = e?.message || '加载失败' }
})

async function logout() {
  try { await tLogout() } catch {}
  localStorage.removeItem('t_token')
  router.replace('/t/login')
}
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">教师个人信息</div>
      <div class="weui-panel__bd">
        <div v-if="loadError" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">{{ loadError }}</span></div>
        <div v-else-if="!profile" class="weui-loadmore"><i class="weui-loading"></i><span class="weui-loadmore__tips">加载中</span></div>

        <template v-else>
          <div class="weui-cells weui-cells_form">
            <div class="weui-cell">
              <div class="weui-cell__hd"><label class="weui-label">姓名</label></div>
              <div class="weui-cell__bd"><input class="weui-input" :value="profile!.name" readonly /></div>
            </div>
            <div class="weui-cell">
              <div class="weui-cell__hd"><label class="weui-label">手机号</label></div>
              <div class="weui-cell__bd"><input class="weui-input" :value="profile!.phone" readonly /></div>
            </div>
            <div class="weui-cell">
              <div class="weui-cell__hd"><label class="weui-label">邮箱</label></div>
              <div class="weui-cell__bd"><input class="weui-input" :value="profile!.email" readonly /></div>
            </div>
            <div class="weui-cell">
              <div class="weui-cell__hd"><label class="weui-label">学校</label></div>
              <div class="weui-cell__bd"><input class="weui-input" :value="profile!.schoolName" readonly /></div>
            </div>
            <div class="weui-cell">
              <div class="weui-cell__hd"><label class="weui-label">科目</label></div>
              <div class="weui-cell__bd"><input class="weui-input" :value="profile!.subjects.join('、')" readonly /></div>
            </div>
            <div class="weui-cell">
              <div class="weui-cell__hd"><label class="weui-label">微信OpenId</label></div>
              <div class="weui-cell__bd"><input class="weui-input" v-model="openId" placeholder="模拟或填写真实OpenId" /></div>
              <div class="weui-cell__ft"><button class="weui-btn weui-btn_mini weui-btn_default" @click="bind">绑定</button></div>
            </div>
          </div>

          <div class="weui-cells__title">快捷入口</div>
          <div class="weui-cells">
            <router-link class="weui-cell weui-cell_access" to="/t/account/password">
              <div class="weui-cell__bd">密码修改</div>
              <div class="weui-cell__ft"></div>
            </router-link>
            <router-link class="weui-cell weui-cell_access" to="/t/classes">
              <div class="weui-cell__bd">班级管理</div>
              <div class="weui-cell__ft"></div>
            </router-link>
            <div class="weui-cell">
              <div class="weui-cell__bd">退出登录</div>
              <div class="weui-cell__ft"><button class="weui-btn weui-btn_mini weui-btn_warn" @click="logout">退出</button></div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>