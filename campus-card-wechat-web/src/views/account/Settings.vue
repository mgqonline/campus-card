<script setup lang="ts">
import { ref, watch } from 'vue'

// 设置项本地状态
const notifyEnabled = ref<boolean>(localStorage.getItem('wx_notify') !== '0')
const darkMode = ref<boolean>(localStorage.getItem('wx_dark') === '1')
const cellularEnabled = ref<boolean>(localStorage.getItem('wx_cellular') !== '0')
const lang = ref<string>(localStorage.getItem('wx_lang') || 'zh')

watch(notifyEnabled, (v) => localStorage.setItem('wx_notify', v ? '1' : '0'))
watch(darkMode, (v) => localStorage.setItem('wx_dark', v ? '1' : '0'))
watch(cellularEnabled, (v) => localStorage.setItem('wx_cellular', v ? '1' : '0'))
watch(lang, (v) => localStorage.setItem('wx_lang', v))

function clearCache() {
  try {
    localStorage.removeItem('wx_token')
    localStorage.removeItem('wx_child_id')
    // 保留设置项，清除会话类缓存即可
    alert('已清除会话缓存（登录状态/子女选择）')
  } catch (e) {
    alert('清除缓存失败')
  }
}
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">设置</div>
      <div class="weui-panel__bd">
        <!-- 功能开关 -->
        <div class="weui-cells__title">功能</div>
        <div class="weui-cells weui-cells_form">
          <div class="weui-cell">
            <div class="weui-cell__bd">消息通知</div>
            <div class="weui-cell__ft">
              <input class="weui-switch" type="checkbox" v-model="notifyEnabled" />
            </div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__bd">深色模式</div>
            <div class="weui-cell__ft">
              <input class="weui-switch" type="checkbox" v-model="darkMode" />
            </div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__bd">允许蜂窝数据</div>
            <div class="weui-cell__ft">
              <input class="weui-switch" type="checkbox" v-model="cellularEnabled" />
            </div>
          </div>
        </div>

        <!-- 语言选择 -->
        <div class="weui-cells__title">语言</div>
        <div class="weui-cells weui-cells_form">
          <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">界面语言</label></div>
            <div class="weui-cell__bd">
              <select class="weui-select" v-model="lang">
                <option value="zh">简体中文</option>
                <option value="en">English</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 数据与缓存 -->
        <div class="weui-cells__title">数据与缓存</div>
        <div class="weui-cells">
          <a class="weui-cell weui-cell_access" href="javascript:;" @click="clearCache">
            <div class="weui-cell__bd"><span class="danger">清除会话缓存</span></div>
            <div class="weui-cell__ft">仅清除登录状态与子女选择</div>
          </a>
        </div>

        <div class="weui-form__opr-area" style="padding:12px;">
          <a href="/account/profile" class="weui-btn weui-btn_default">返回个人中心</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.danger { color:#d9534f; font-weight:600; }
</style>