<script setup lang="ts">
import { onMounted, ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  listCardTypes,
  issueCard,
  replaceCard,
  reportLoss,
  unloss,
  refund,
  getBalance,
  type CardType,
  type HolderType,
  type RechargeMethod
} from '@/api/modules/card'

const loadingTypes = ref(false)
const cardTypes = ref<CardType[]>([])

const running = ref(false)
const logs = ref<string[]>([])
const issuedCardNo = ref<string | undefined>(undefined)
const replacedCardNo = ref<string | undefined>(undefined)
const currentCardNo = computed(() => replacedCardNo.value || issuedCardNo.value)

const form = reactive<{
  holderType: HolderType
  holderId: string
  cardTypeId?: number
  initialBalance?: number
  note?: string
  changeType: 'SUPPLEMENT' | 'TYPE_CHANGE'
  newCardTypeId?: number
  fee?: number
  refundAmount: number
  refundMethod: RechargeMethod
  lossReason?: string
}>({
  holderType: 'VISITOR',
  holderId: 'visitor-001',
  cardTypeId: undefined,
  initialBalance: 50,
  note: 'E2E 联调测试',
  changeType: 'SUPPLEMENT',
  newCardTypeId: undefined,
  fee: 10,
  refundAmount: 20,
  refundMethod: 'CASH',
  lossReason: '测试挂失'
})

const pushLog = (line: string) => {
  logs.value.push(`${new Date().toLocaleTimeString()} - ${line}`)
}

const fetchTypes = async () => {
  loadingTypes.value = true
  try {
    cardTypes.value = await listCardTypes()
  } catch (e: any) {
    message.error(e?.message || '获取卡种失败')
  } finally {
    loadingTypes.value = false
  }
}

onMounted(fetchTypes)

const runFlow = async () => {
  if (!form.cardTypeId) {
    return message.warning('请选择发卡的卡片类型')
  }
  if (form.changeType === 'TYPE_CHANGE' && !form.newCardTypeId) {
    return message.warning('换卡流程需选择新的卡片类型')
  }
  running.value = true
  logs.value = []
  issuedCardNo.value = undefined
  replacedCardNo.value = undefined
  try {
    // 1) 发卡
    pushLog('开始发卡...')
    const issueRes = await issueCard({
      holderType: form.holderType,
      holderId: form.holderId,
      cardTypeId: form.cardTypeId!,
      initialBalance: form.initialBalance,
      note: form.note
    })
    if (!issueRes.success || !issueRes.cardNo) throw new Error('发卡失败：未返回卡号')
    issuedCardNo.value = issueRes.cardNo
    pushLog(`发卡成功，卡号：${issueRes.cardNo}`)

    // 查询余额
    const bal1 = await getBalance(issueRes.cardNo)
    pushLog(`发卡后余额：${bal1.balance}`)

    // 2) 补卡/换卡
    pushLog(form.changeType === 'SUPPLEMENT' ? '开始补卡...' : '开始换卡...')
    const replaceRes = await replaceCard({
      oldCardNo: issueRes.cardNo,
      changeType: form.changeType,
      newCardTypeId: form.changeType === 'TYPE_CHANGE' ? form.newCardTypeId : undefined,
      fee: form.fee,
      note: form.note
    })
    if (!replaceRes.success || !replaceRes.newCardNo) throw new Error('补卡/换卡失败：未返回新卡号')
    replacedCardNo.value = replaceRes.newCardNo
    pushLog(`补卡/换卡成功，新卡号：${replaceRes.newCardNo}`)

    const bal2 = await getBalance(replaceRes.newCardNo)
    pushLog(`补卡/换卡后余额：${bal2.balance}`)

    // 3) 挂失
    pushLog('开始挂失...')
    const lossRes = await reportLoss({ cardNo: replaceRes.newCardNo, reason: form.lossReason })
    if (!lossRes.success) throw new Error('挂失失败')
    pushLog('挂失成功')

    // 4) 解挂
    pushLog('开始解挂...')
    const unlossRes = await unloss({ cardNo: replaceRes.newCardNo })
    if (!unlossRes.success) throw new Error('解挂失败')
    pushLog('解挂成功')

    // 5) 退款
    pushLog('开始退款...')
    const refundRes = await refund({ cardNo: replaceRes.newCardNo, amount: form.refundAmount, method: form.refundMethod, note: form.note })
    if (!refundRes.success) throw new Error('退款失败')
    pushLog('退款成功')

    const bal3 = await getBalance(replaceRes.newCardNo)
    pushLog(`退款后余额：${bal3.balance}`)

    message.success('完整联调流程执行成功')
  } catch (e: any) {
    message.error(e?.message || '流程执行失败')
    pushLog(`发生错误：${e?.message || e}`)
  } finally {
    running.value = false
  }
}
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="联调测试：发卡 → 补卡/换卡 → 挂失 → 解挂 → 退款" />
    <a-card :loading="loadingTypes" title="参数设置" style="margin-bottom: 16px;">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 14 }">
        <a-form-item label="持卡人类型">
          <a-select v-model:value="form.holderType" style="width: 240px">
            <a-select-option value="STUDENT">学生</a-select-option>
            <a-select-option value="TEACHER">教师</a-select-option>
            <a-select-option value="STAFF">职员</a-select-option>
            <a-select-option value="VISITOR">访客</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="持卡人标识">
          <a-input v-model:value="form.holderId" style="width: 360px" placeholder="例如学生ID或访客标识" />
        </a-form-item>
        <a-form-item label="发卡卡种">
          <a-select v-model:value="form.cardTypeId" style="width: 240px" placeholder="请选择卡片类型">
            <a-select-option v-for="t in cardTypes" :key="t.id" :value="t.id">{{ t.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="初始余额">
          <a-input-number v-model:value="form.initialBalance" :min="0" :step="1" style="width: 200px" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.note" style="width: 360px" />
        </a-form-item>

        <a-divider />
        <a-form-item label="变更类型">
          <a-radio-group v-model:value="form.changeType">
            <a-radio value="SUPPLEMENT">补卡</a-radio>
            <a-radio value="TYPE_CHANGE">换卡</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="新卡卡种" v-if="form.changeType === 'TYPE_CHANGE'">
          <a-select v-model:value="form.newCardTypeId" style="width: 240px" placeholder="请选择新卡片类型">
            <a-select-option v-for="t in cardTypes" :key="t.id" :value="t.id">{{ t.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="工本/换卡费">
          <a-input-number v-model:value="form.fee" :min="0" :step="1" style="width: 200px" />
        </a-form-item>

        <a-divider />
        <a-form-item label="退款金额">
          <a-input-number v-model:value="form.refundAmount" :min="0" :step="1" style="width: 200px" />
        </a-form-item>
        <a-form-item label="退款方式">
          <a-select v-model:value="form.refundMethod" style="width: 240px">
            <a-select-option value="CASH">现金</a-select-option>
            <a-select-option value="ONLINE">线上</a-select-option>
            <a-select-option value="ALIPAY">支付宝</a-select-option>
            <a-select-option value="WECHAT">微信</a-select-option>
            <a-select-option value="CARD_CENTER">卡务中心</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="挂失原因">
          <a-input v-model:value="form.lossReason" style="width: 360px" />
        </a-form-item>

        <a-form-item :wrapper-col="{ span: 14, offset: 6 }">
          <a-button type="primary" :loading="running" @click="runFlow">运行联调流程</a-button>
          <span style="margin-left: 16px;">测试卡号：<b>{{ currentCardNo || '-' }}</b></span>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card title="执行日志">
      <a-list :data-source="logs" bordered>
        <template #renderItem="{ item }">
          <a-list-item>{{ item }}</a-list-item>
        </template>
      </a-list>
    </a-card>
  </div>
</template>