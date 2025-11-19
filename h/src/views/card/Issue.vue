<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { listCards, listCardTypes, issueCard, batchIssue, type Card, type CardType, type IssueReq } from '@/api/modules/card'

const loading = ref(false)
const issuing = ref(false)
const types = ref<CardType[]>([])
const dataSource = ref<Card[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const form = reactive<IssueReq>({ holderType: 'STUDENT', holderId: 0 as any, cardTypeId: 0, initialBalance: 0, note: '', expireAt: '' })

// 批量发卡
const batchVisible = ref(false)
const batchLoading = ref(false)
const batchText = ref('')

const fetchTypes = async () => {
  try {
    const res = await listCardTypes()
    types.value = Array.isArray(res) ? res : []
  } catch (e: any) {
    types.value = []
    message.error(e?.message || '获取卡种失败')
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await listCards({ page: pagination.current, size: pagination.pageSize })
    dataSource.value = res.records
    pagination.total = res.total
  } catch (e: any) {
    dataSource.value = []
    pagination.total = 0
    message.error(e?.message || '查询失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const onIssue = async () => {
  if (!form.holderType || !(form as any).holderId || !form.cardTypeId) {
    message.error('请填写持卡人类型、ID与卡种')
    return
  }
  if (form.holderType === 'VISITOR' && !(form as any).expireAt) {
    message.error('临时卡需填写过期时间')
    return
  }
  issuing.value = true
  try {
    await issueCard(form)
    message.success('发卡成功')
    form.initialBalance = 0
    form.note = ''
    fetchList()
  } catch (e: any) {
    message.error(e?.message || '发卡失败，请稍后重试')
  } finally {
    issuing.value = false
  }
}

const openBatch = () => {
  batchText.value = 'VISITOR,guest-001,1,0,2025-12-31T23:59\nSTUDENT,S0001,1,50\nTEACHER,T0001,2,100'
  batchVisible.value = true
}

const doBatchIssue = async () => {
  const lines = batchText.value.split(/\n+/).map(l => l.trim()).filter(Boolean)
  if (!lines.length) {
    message.error('请输入批量发卡内容')
    return
  }
  const list: IssueReq[] = []
  for (const l of lines) {
    const parts = l.split(',').map(p => p.trim())
    const holderType = parts[0] as IssueReq['holderType']
    const holderId = parts[1]
    const cardTypeId = Number(parts[2])
    const initialBalance = parts[3] !== undefined ? Number(parts[3]) : undefined
    const expireAt = parts[4]
    if (!holderType || !holderId || !cardTypeId) {
      message.error(`格式错误: ${l}`)
      return
    }
    list.push({ holderType, holderId, cardTypeId, initialBalance, expireAt })
  }
  batchLoading.value = true
  try {
    const res = await batchIssue(list)
    if (res?.success !== false) {
      message.success(`批量发卡成功（${res.count || list.length}）`)
      batchVisible.value = false
      await fetchList()
    } else {
      message.error('批量发卡失败')
    }
  } catch (e: any) {
    message.error(e?.message || '批量发卡失败')
  } finally {
    batchLoading.value = false
  }
}

onMounted(async () => {
  await fetchTypes()
  await fetchList()
})
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="卡片发放" />

    <a-card title="发卡信息" style="margin-bottom: 16px;">
      <a-form layout="inline">
        <a-form-item label="持卡人类型">
          <a-select v-model:value="form.holderType" style="width: 180px">
            <a-select-option value="STUDENT">学生</a-select-option>
            <a-select-option value="TEACHER">教师</a-select-option>
            <a-select-option value="STAFF">职工</a-select-option>
            <a-select-option value="VISITOR">临时卡</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="持卡人ID">
          <a-input v-model:value="(form as any).holderId" style="width: 160px" placeholder="输入学号/工号或标识" />
        </a-form-item>
        <a-form-item label="卡种">
          <a-select v-model:value="form.cardTypeId" style="width: 160px" placeholder="选择卡种">
            <a-select-option v-for="t in types" :key="t.id" :value="t.id">{{ t.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="form.holderType === 'VISITOR'" label="过期时间">
          <a-date-picker v-model:value="(form as any).expireAt" show-time format="YYYY-MM-DD HH:mm" style="width: 220px" />
        </a-form-item>
        <a-form-item label="初始余额">
          <a-input-number v-model:value="form.initialBalance" :min="0" :precision="2" style="width: 160px" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.note" style="width: 220px" placeholder="可选备注" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" :loading="issuing" @click="onIssue">发放</a-button>
            <a-button @click="openBatch">批量发卡</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card title="发卡记录">
      <a-table
        :data-source="dataSource"
        :loading="loading"
        row-key="id"
        :pagination="{ current: pagination.current, pageSize: pagination.pageSize, total: pagination.total, showSizeChanger: true }"
        @change="(p:any) => { pagination.current = p.current; pagination.pageSize = p.pageSize; fetchList() }"
      >
        <a-table-column title="卡号" dataIndex="cardNo" key="cardNo" />
        <a-table-column title="持卡人" key="holderName" :customRender="({ record }) => record.holderName || '-'" />
        <a-table-column title="类型" key="typeName" :customRender="({ record }) => record.typeName || '-'" />
        <a-table-column title="余额" key="balance" :customRender="({ record }) => (record.balance ?? 0).toFixed(2)" />
        <a-table-column title="状态" key="status" :customRender="({ record }) => (record.status === 'ACTIVE' ? '正常' : record.status === 'LOST' ? '挂失' : record.status === 'FROZEN' ? '冻结' : '注销')" />
        <a-table-column title="发放时间" key="createdAt" :customRender="({ record }) => record.createdAt || '-'" />
      </a-table>
    </a-card>

    <a-modal v-model:open="batchVisible" title="批量发卡" :confirmLoading="batchLoading" @ok="doBatchIssue">
      <p>每行格式：holderType,holderId,cardTypeId,initialBalance[,expireAt]</p>
      <a-textarea v-model:value="batchText" :rows="6" placeholder="示例：\nVISITOR,guest-001,1,0,2025-12-31T23:59\nSTUDENT,S0001,1,50\nTEACHER,T0001,2,100" />
    </a-modal>
  </div>
</template>