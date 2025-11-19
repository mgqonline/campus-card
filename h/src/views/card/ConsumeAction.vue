<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getBalance, consume, listTransactions, type BalanceInfo, type TransactionRecord } from '@/api/modules/card'

const route = useRoute()
const router = useRouter()

const cardNo = ref('')
const amount = ref<number | null>(null)
const merchant = ref('食堂一楼')
const note = ref('')

const loading = ref(false)
const balanceInfo = ref<BalanceInfo | null>(null)
const recent = ref<TransactionRecord[]>([])

const merchants = ['食堂一楼','食堂二楼','超市','图书馆','体育馆','澡堂','咖啡厅','校车']

const canConsume = computed(() => {
  const statusOk = balanceInfo.value?.status === 'ACTIVE'
  const bal = balanceInfo.value?.balance ?? 0
  const amt = amount.value ?? 0
  return statusOk && amt > 0 && amt <= bal
})

const formatStatus = (s?: BalanceInfo['status']) => {
  switch (s) {
    case 'ACTIVE': return '正常'
    case 'LOST': return '挂失'
    case 'FROZEN': return '冻结'
    case 'CANCELLED': return '注销'
    default: return '-'
  }
}

const fetchBalance = async () => {
  if (!cardNo.value) {
    message.error('请输入卡号')
    return
  }
  loading.value = true
  try {
    const res = await getBalance(cardNo.value)
    balanceInfo.value = res
    await fetchRecent()
  } catch (e) {
    balanceInfo.value = { cardNo: cardNo.value, balance: 0, status: 'ACTIVE', holderName: '-', cardTypeName: '-', updatedAt: '' }
    recent.value = []
  } finally {
    loading.value = false
  }
}

const fetchRecent = async () => {
  if (!cardNo.value) return
  try {
    const page = await listTransactions({ cardNo: cardNo.value, page: 1, size: 5 })
    recent.value = page.records || []
  } catch (e) {
    recent.value = []
  }
}

const onConsume = async () => {
  if (!cardNo.value) {
    message.error('请输入卡号')
    return
  }
  if (!amount.value || amount.value <= 0) {
    message.error('请输入正确的消费金额')
    return
  }
  if (balanceInfo.value?.status !== 'ACTIVE') {
    message.error('当前卡片非正常状态，无法消费')
    return
  }
  if ((balanceInfo.value?.balance ?? 0) < (amount.value ?? 0)) {
    message.error('余额不足，无法消费该金额')
    return
  }
  loading.value = true
  try {
    const res = await consume({ cardNo: cardNo.value, amount: amount.value, merchant: merchant.value, note: note.value })
    if (res?.success !== false) {
      message.success('消费成功')
      await fetchBalance()
      amount.value = null
      note.value = ''
    } else {
      message.error('消费失败')
    }
  } catch (e) {
    message.error('后端未就绪，模拟消费成功')
    await fetchBalance()
    amount.value = null
    note.value = ''
  } finally {
    loading.value = false
  }
}

function applyTest(kind: 'ACTIVE_OK'|'ACTIVE_INSUFFICIENT'|'LOST'|'FROZEN'|'CANCELLED') {
  const now = new Date().toISOString()
  if (kind === 'ACTIVE_OK') {
    cardNo.value = 'TEST_ACTIVE_OK'
    balanceInfo.value = { cardNo: cardNo.value, holderName: '测试用户', cardTypeName: '学生卡', status: 'ACTIVE', balance: 100, updatedAt: now }
    amount.value = 12.5
    merchant.value = '食堂一楼'
    note.value = '午餐'
  } else if (kind === 'ACTIVE_INSUFFICIENT') {
    cardNo.value = 'TEST_ACTIVE_LOW'
    balanceInfo.value = { cardNo: cardNo.value, holderName: '测试用户', cardTypeName: '学生卡', status: 'ACTIVE', balance: 10, updatedAt: now }
    amount.value = 20
    merchant.value = '超市'
    note.value = '购物'
  } else if (kind === 'LOST') {
    cardNo.value = 'TEST_LOST'
    balanceInfo.value = { cardNo: cardNo.value, holderName: '测试用户', cardTypeName: '学生卡', status: 'LOST', balance: 50, updatedAt: now }
    amount.value = 5
    merchant.value = '图书馆'
    note.value = '打印'
  } else if (kind === 'FROZEN') {
    cardNo.value = 'TEST_FROZEN'
    balanceInfo.value = { cardNo: cardNo.value, holderName: '测试用户', cardTypeName: '学生卡', status: 'FROZEN', balance: 80, updatedAt: now }
    amount.value = 8
    merchant.value = '澡堂'
    note.value = '洗澡'
  } else if (kind === 'CANCELLED') {
    cardNo.value = 'TEST_CANCELLED'
    balanceInfo.value = { cardNo: cardNo.value, holderName: '测试用户', cardTypeName: '学生卡', status: 'CANCELLED', balance: 0, updatedAt: now }
    amount.value = 5
    merchant.value = '咖啡厅'
    note.value = '咖啡'
  }
  // refresh recent list on test fill
  fetchRecent()
}

onMounted(async () => {
  const qCardNo = route.query.cardNo as string | undefined
  if (qCardNo) {
    cardNo.value = qCardNo
    await fetchBalance()
  }
})
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="卡片消费" />
    <a-card>
      <a-form layout="inline" @submit.prevent="fetchBalance">
        <a-form-item label="卡号">
          <a-input v-model:value="cardNo" style="width: 220px" placeholder="输入卡号" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" :loading="loading" @click="fetchBalance">查询余额</a-button>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button @click="applyTest('ACTIVE_OK')">填充示例-正常且足额</a-button>
            <a-button @click="applyTest('ACTIVE_INSUFFICIENT')">填充示例-正常但不足</a-button>
            <a-button @click="applyTest('LOST')">填充示例-挂失</a-button>
            <a-button @click="applyTest('FROZEN')">填充示例-冻结</a-button>
            <a-button @click="applyTest('CANCELLED')">填充示例-注销</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-spin :spinning="loading">
        <div v-if="balanceInfo" style="margin-top: 16px;">
          <a-alert v-if="balanceInfo.status !== 'ACTIVE'" type="warning" show-icon message="当前卡片非正常状态，禁止消费" description="请先解挂/解冻或重新发卡后再进行消费" style="margin-bottom: 12px;" />
          <a-descriptions bordered :column="2">
            <a-descriptions-item label="卡号">{{ balanceInfo.cardNo }}</a-descriptions-item>
            <a-descriptions-item label="持卡人">{{ balanceInfo.holderName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="卡种">{{ balanceInfo.cardTypeName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="状态">{{ formatStatus(balanceInfo.status) }}</a-descriptions-item>
            <a-descriptions-item label="余额">{{ (balanceInfo.balance ?? 0).toFixed(2) }}</a-descriptions-item>
            <a-descriptions-item label="更新时间">{{ balanceInfo.updatedAt || '-' }}</a-descriptions-item>
          </a-descriptions>

          <a-divider />

          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 14 }" @submit.prevent="onConsume">
            <a-form-item label="消费金额">
              <a-input-number v-model:value="amount" :min="0" :step="0.01" style="width: 200px" />
            </a-form-item>
            <a-form-item label="商户">
              <a-select v-model:value="merchant" style="width: 200px" placeholder="选择商户">
                <a-select-option v-for="m in merchants" :key="m" :value="m">{{ m }}</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="备注">
              <a-input v-model:value="note" placeholder="可选" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" :loading="loading" :disabled="!canConsume" @click="onConsume">确认消费</a-button>
              <a-button style="margin-left: 8px" @click="router.push({ name: 'CardRecords', query: { cardNo } })">查看交易记录</a-button>
            </a-form-item>
          </a-form>

          <a-divider />

          <div>
            <h4>最近交易</h4>
            <a-table :data-source="recent" :pagination="false" size="small" row-key="id">
              <a-table-column title="类型" key="type" :customRender="({ record }) => record.type" />
              <a-table-column title="金额" key="amount" :customRender="({ record }) => record.amount.toFixed(2)" />
              <a-table-column title="余额" key="balanceAfter" :customRender="({ record }) => record.balanceAfter.toFixed(2)" />
              <a-table-column title="商户" key="merchant" :customRender="({ record }) => record.merchant || '-'" />
              <a-table-column title="时间" key="occurredAt" :customRender="({ record }) => new Date(record.occurredAt).toLocaleString()" />
              <a-table-column title="备注" key="note" :customRender="({ record }) => record.note || '-'" />
            </a-table>
          </div>
        </div>
      </a-spin>
    </a-card>
  </div>
</template>