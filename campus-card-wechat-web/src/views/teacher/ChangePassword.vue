<script setup lang="ts">
import { ref } from 'vue'
import { tUpdatePassword } from '@/api/modules/teacher'
const oldPwd = ref('')
const newPwd = ref('')
const loading = ref(false)
async function submit() {
  if (!newPwd.value || newPwd.value.length < 6) { alert('新密码至少6位'); return }
  loading.value = true
  try { await tUpdatePassword({ oldPassword: oldPwd.value, newPassword: newPwd.value }); alert('密码修改成功'); oldPwd.value=''; newPwd.value='' } catch (e: any) { alert(e?.message || '修改失败') } finally { loading.value = false }
}
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">密码修改</div>
      <div class="weui-panel__bd">
        <div class="weui-cells weui-cells_form">
          <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">旧密码</label></div>
            <div class="weui-cell__bd"><input type="password" class="weui-input" v-model="oldPwd" placeholder="请输入旧密码" /></div>
          </div>
          <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">新密码</label></div>
            <div class="weui-cell__bd"><input type="password" class="weui-input" v-model="newPwd" placeholder="至少6位" /></div>
          </div>
        </div>
        <div class="weui-form__opr-area" style="padding:12px;">
          <button :disabled="loading" class="weui-btn weui-btn_primary" @click="submit">提交</button>
        </div>
      </div>
    </div>
  </div>
</template>