<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { submitCardRequest, getCardRequests, type CardRequestItem, type CardRequestType } from '@/api/modules/card'
import { getProfile, type ProfileResp } from '@/api/modules/auth'

const childId = ref<number>(Number(localStorage.getItem('wx_child_id') || 2001))
const children = ref<ProfileResp['boundChildren']>([])
const listLoading = ref(false)
const type = ref<CardRequestType>('LOSS')
const reason = ref('')
const contact = ref('')
const loading = ref(false)
const list = ref<CardRequestItem[]>([])

async function load() {
  listLoading.value = true
  try {
    list.value = await getCardRequests(childId.value)
  } finally {
    listLoading.value = false
  }
}

async function initChildren() {
  try {
    const profile = await getProfile()
    children.value = profile.boundChildren || []
    // 如果本地没有选择，默认第一个绑定子女
    if (!localStorage.getItem('wx_child_id') && children.value.length > 0) {
      childId.value = children.value[0].id
      localStorage.setItem('wx_child_id', String(childId.value))
    }
  } catch {}
}

async function submit() {
  if (!type.value) { alert('请选择申请类型'); return }
  if (!childId.value || children.value.length === 0) { alert('请先绑定子女'); return }
  // 联系方式简单校验（选填但如填写需合法）
  if (contact.value && !/^1[3-9]\d{9}$/.test(contact.value)) {
    alert('手机号格式不正确')
    return
  }
  loading.value = true
  try {
    await submitCardRequest({ childId: childId.value, type: type.value, reason: reason.value, contact: contact.value })
    alert('提交成功，已进入审批')
    reason.value = ''
    contact.value = ''
    await load()
  } catch (e: any) {
    alert(e?.message || '提交失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await initChildren()
  await load()
})

watch(childId, async (v) => {
  localStorage.setItem('wx_child_id', String(v))
  await load()
})
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">挂失 / 解挂申请</div>
      <div class="weui-panel__bd">
        <div v-if="children.length===0" class="bind-warn">
          <i class="fa fa-exclamation-triangle"></i>
          <span>您尚未绑定孩子信息，请前往绑定。</span>
          <router-link to="/account/profile" class="weui-btn weui-btn_mini weui-btn_primary" style="margin-left:8px;">去绑定</router-link>
          <router-link to="/student/info" class="weui-btn weui-btn_mini weui-btn_default" style="margin-left:8px;">学生信息</router-link>
        </div>

        <div class="weui-cells__title">选择子女</div>
        <div class="weui-cells">
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <select class="weui-select" v-model.number="childId">
                <option v-for="c in children" :key="c.id" :value="c.id">{{ c.name }}（{{ c.className || c.classId }}）</option>
              </select>
            </div>
          </div>
        </div>

        <div class="weui-cells__title">申请类型</div>
        <div class="weui-cells weui-cells_radio">
          <label class="weui-cell weui-check__label">
            <div class="weui-cell__bd"><p>挂失</p></div>
            <div class="weui-cell__ft">
              <input type="radio" class="weui-check" value="LOSS" v-model="type">
              <span class="weui-icon-checked"></span>
            </div>
          </label>
          <label class="weui-cell weui-check__label">
            <div class="weui-cell__bd"><p>解挂</p></div>
            <div class="weui-cell__ft">
              <input type="radio" class="weui-check" value="UNLOSS" v-model="type">
              <span class="weui-icon-checked"></span>
            </div>
          </label>
        </div>

        <div class="weui-cells__title">原因说明</div>
        <div class="weui-cells">
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <textarea class="weui-textarea" placeholder="请输入原因（选填）" rows="3" v-model="reason"></textarea>
            </div>
          </div>
        </div>

        <div class="weui-cells__title">联系方式</div>
        <div class="weui-cells">
          <div class="weui-cell">
            <div class="weui-cell__bd">
              <input class="weui-input" placeholder="手机号或微信号（选填）" v-model="contact" />
            </div>
          </div>
        </div>

        <div class="weui-form__opr-area" style="padding:12px;">
          <a href="javascript:;" class="weui-btn weui-btn_primary" @click="submit"><span v-if="loading" class="weui-loading"></span> 提交申请</a>
        </div>

        <div class="weui-cells__title">最近申请</div>
        <div class="weui-cells">
          <div v-if="listLoading" class="weui-loadmore"><i class="weui-loading"></i><span class="weui-loadmore__tips">加载中</span></div>
          <div v-else-if="list.length===0" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">暂无申请</span></div>
          <a v-for="i in list" :key="i.id" class="weui-cell weui-cell_access" href="javascript:;">
            <div class="weui-cell__bd">
              <p><strong>{{ i.type==='LOSS'?'挂失':(i.type==='UNLOSS'?'解挂':'补卡') }}</strong> · {{ i.status==='PENDING'?'审批中':(i.status==='APPROVED'?'已通过':'已拒绝') }}</p>
              <p class="muted">{{ new Date(i.createdAt).toLocaleString() }}</p>
            </div>
            <div class="weui-cell__ft"></div>
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.muted { color:#888; font-size:12px; }
</style>