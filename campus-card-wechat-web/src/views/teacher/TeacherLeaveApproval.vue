<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { tGetProfile, tTeacherLeavePending, tTeacherLeaveRecords, tTeacherApproveLeave, tTeacherBatchApproveLeave, type TProfileResp } from '@/api/modules/teacher'

const router = useRouter()
const profile = ref<TProfileResp | null>(null)
const classId = ref<number | null>(null)
const loading = ref(false)
const loadError = ref('')
const pendingList = ref<Array<any>>([])
const recordList = ref<Array<any>>([])
const selected = ref<Set<string>>(new Set())

function keyOf(item: any) { return `${item.childId}-${item.id}` }

async function load() {
  if (!classId.value) return
  loading.value = true
  loadError.value = ''
  try {
    // 先加载待审批列表；记录列表若接口未就绪则优雅降级为空
    pendingList.value = await tTeacherLeavePending(classId.value)
    try {
      recordList.value = await tTeacherLeaveRecords(classId.value)
    } catch (err) {
      recordList.value = []
    }
  } catch (e: any) {
    loadError.value = e?.message || '加载失败'
  } finally { loading.value = false }
}

async function approveOne(item: any, status: 'APPROVED'|'REJECTED') {
  try { await tTeacherApproveLeave({ childId: item.childId, id: item.id, status }); await load() } catch (e: any) { alert(e?.message || '操作失败') }
}

async function batchApprove(status: 'APPROVED'|'REJECTED') {
  const items = Array.from(selected.value).map(k => { const [childId,id] = k.split('-'); return { childId: Number(childId), id: Number(id) } })
  if (items.length === 0) { alert('请先勾选待审批记录'); return }
  try { await tTeacherBatchApproveLeave({ items, status }); selected.value.clear(); await load() } catch (e: any) { alert(e?.message || '操作失败') }
}

onMounted(async () => {
  if (!localStorage.getItem('t_token')) { router.replace('/t/login'); return }
  try { profile.value = await tGetProfile(); classId.value = (profile.value!.assignedClasses[0]||{}).classId || 301; await load() } catch (e: any) { loadError.value = e?.message || '加载失败' }
})
</script>

<template>
  <div class="page">
    <div class="weui-panel weui-panel_access">
      <div class="weui-panel__hd">请假审批</div>
      <div class="weui-panel__bd">
        <div v-if="loadError" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">{{ loadError }}</span></div>
        <div v-else-if="loading" class="weui-loadmore"><i class="weui-loading"></i><span class="weui-loadmore__tips">加载中</span></div>

        <template v-else>
          <div class="weui-cells__title">待审批</div>
          <div class="weui-cells">
            <div v-if="pendingList.length===0" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">暂无待审批</span></div>
            <div v-for="item in pendingList" :key="keyOf(item)" class="weui-cell">
              <div class="weui-cell__hd"><input type="checkbox" :checked="selected.has(keyOf(item))" @change="(e:any)=>{if(e.target.checked)selected.add(keyOf(item));else selected.delete(keyOf(item))}" /></div>
              <div class="weui-cell__bd">
                <p>学生：{{ item.childId }}</p>
                <p>类型：{{ item.type }} ｜ 时间：{{ item.startTime }} 至 {{ item.endTime }}</p>
                <p>原因：{{ item.reason }}</p>
              </div>
              <div class="weui-cell__ft">
                <button class="weui-btn weui-btn_mini weui-btn_primary" @click="approveOne(item, 'APPROVED')">通过</button>
                <button class="weui-btn weui-btn_mini weui-btn_warn" style="margin-left:8px;" @click="approveOne(item, 'REJECTED')">驳回</button>
              </div>
            </div>
          </div>

          <div style="padding:12px;">
            <button class="weui-btn weui-btn_default" @click="batchApprove('APPROVED')">批量通过</button>
            <button class="weui-btn weui-btn_warn" style="margin-left:8px;" @click="batchApprove('REJECTED')">批量驳回</button>
          </div>

          <div class="weui-cells__title">请假记录</div>
          <div class="weui-cells">
            <div v-if="recordList.length===0" class="weui-loadmore weui-loadmore_line"><span class="weui-loadmore__tips">暂无记录</span></div>
            <div v-for="item in recordList" :key="item.id" class="weui-cell">
              <div class="weui-cell__bd">
                <p>学生：{{ item.childId }} ｜ 类型：{{ item.type }}</p>
                <p>时间：{{ item.startTime }} 至 {{ item.endTime }}</p>
                <p>状态：{{ item.status }} ｜ 申请时间：{{ item.applyTime }}</p>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { padding-bottom: 16px; }
</style>