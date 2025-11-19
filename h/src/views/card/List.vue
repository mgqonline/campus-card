<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listCards, listCardTypes, reportLoss, unloss, freeze, unfreeze, cancelCard, issueCard, batchIssue, type Card, type HolderType, type CardType, type IssueReq } from '@/api/modules/card'

const router = useRouter()
const loading = ref(false)
const dataSource = ref<Card[]>([])
const types = ref<CardType[]>([])
const typeMap = ref<Record<number, string>>({})
const columns = [
  { title: '卡号', dataIndex: 'cardNo', key: 'cardNo' },
  { title: '卡种', dataIndex: 'typeName', key: 'typeName' },
  { title: '持卡人类型', dataIndex: 'holderType', key: 'holderType' },
  { title: '持卡人ID', dataIndex: 'holderId', key: 'holderId' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '余额', dataIndex: 'balance', key: 'balance' },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt' },
  { title: '操作', key: 'action' }
]

const query = reactive<{ cardNo?: string; holderType?: HolderType; status?: Card['status'] }>({ cardNo: undefined, holderType: undefined, status: undefined })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

// 新增：单卡开卡
const issueVisible = ref(false)
const issuing = ref(false)
const issueForm = reactive<IssueReq>({ holderType: 'STUDENT', holderId: '' as any, cardTypeId: 0, initialBalance: 0, note: '', expireAt: '' })

// 新增：批量发卡
const batchVisible = ref(false)
const batchLoading = ref(false)
const batchText = ref('')

const fetchCardTypes = async () => {
  try {
    const res = await listCardTypes()
    types.value = Array.isArray(res) ? res : []
    typeMap.value = Object.fromEntries(types.value.map((t: CardType) => [t.id, t.name]))
  } catch (e: any) {
    types.value = []
    typeMap.value = {}
    message.error(e?.message || '获取卡种失败')
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listCards({ page: pagination.current, size: pagination.pageSize, cardNo: query.cardNo, holderType: query.holderType, status: query.status })
    if (Array.isArray(res)) {
      dataSource.value = res as any
      pagination.total = (res as any).length || 0
    } else {
      dataSource.value = (res as any).records
      pagination.total = (res as any).total
    }
  } catch (e: any) {
    dataSource.value = []
    pagination.total = 0
    message.error(e?.message || '查询失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}
const handleReset = () => {
  query.cardNo = undefined
  query.holderType = undefined
  query.status = undefined
  pagination.current = 1
  fetchData()
}
const handlePageChange = (page: number, pageSize?: number) => {
  pagination.current = page
  if (pageSize) pagination.pageSize = pageSize
  fetchData()
}

const toRecords = (cardNo: string) => {
  router.push({ name: 'CardRecords', query: { cardNo } })
}

const onLoss = async (row: Card) => {
  try {
    await reportLoss({ cardNo: row.cardNo })
    message.success('挂失成功')
    fetchData()
  } catch (e: any) {
    message.error(e?.message || '挂失失败')
  }
}

const onUnloss = async (row: Card) => {
  try {
    await unloss({ cardNo: row.cardNo })
    message.success('解挂成功')
    fetchData()
  } catch (e: any) {
    message.error(e?.message || '解挂失败')
  }
}

const onFreeze = async (row: Card) => {
  try {
    const r = await freeze({ cardNo: row.cardNo })
    if (r?.success !== false) {
      message.success('冻结成功')
      fetchData()
    } else {
      message.error('冻结失败')
    }
  } catch (e: any) {
    message.error(e?.message || '冻结失败')
  }
}

const onUnfreeze = async (row: Card) => {
  try {
    const r = await unfreeze({ cardNo: row.cardNo })
    if (r?.success !== false) {
      message.success('解冻成功')
      fetchData()
    } else {
      message.error('解冻失败')
    }
  } catch (e: any) {
    message.error(e?.message || '解冻失败')
  }
}

const onCancel = async (row: Card) => {
  try {
    const r = await cancelCard({ cardNo: row.cardNo, refundAll: true })
    if (r?.success !== false) {
      message.success('注销成功')
      fetchData()
    } else {
      message.error('注销失败')
    }
  } catch (e: any) {
    message.error(e?.message || '注销失败')
  }
}

// 单卡开卡逻辑
const openIssue = () => {
  issueForm.holderType = 'STUDENT'
  ;(issueForm as any).holderId = ''
  issueForm.cardTypeId = types.value[0]?.id || 0
  issueForm.initialBalance = 0
  issueForm.note = ''
  ;(issueForm as any).expireAt = ''
  issueVisible.value = true
}
const submitIssue = async () => {
  if (!issueForm.holderType || !(issueForm as any).holderId || !issueForm.cardTypeId) {
    message.error('请填写持卡人类型、ID与卡种')
    return
  }
  if (issueForm.holderType === 'VISITOR' && !(issueForm as any).expireAt) {
    message.error('临时卡需填写过期时间')
    return
  }
  issuing.value = true
  try {
    const r = await issueCard(issueForm)
    if (r?.success) {
      message.success('开卡成功')
      issueVisible.value = false
      await fetchData()
    } else {
      message.error('开卡失败')
    }
  } catch (e: any) {
    message.error(e?.message || '开卡失败')
  } finally {
    issuing.value = false
  }
}

// 批量发卡逻辑
const openBatch = () => {
  batchText.value = 'VISITOR,guest-001,1,0,2025-12-31T23:59\nSTUDENT,S0001,1,50\nTEACHER,T0001,2,100'
  batchVisible.value = true
}
const submitBatch = async () => {
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
    const issuedNos = Array.isArray((res as any)?.issued) ? (res as any).issued : []
    const count = issuedNos.length || (res as any)?.count || 0
    if (res?.success !== false && count > 0) {
      message.success(`批量发卡成功（${count}）：${issuedNos.join('、')}`)
      // 聚焦到首个新卡号，确保列表中能立即看到数据
      query.cardNo = issuedNos[0]
      pagination.current = 1
      batchVisible.value = false
      await fetchData()
    } else if (res?.success !== false) {
      message.warning('批量发卡接口返回成功，但未返回新卡号；请检查后端返回结构')
      batchVisible.value = false
      await fetchData()
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
  await fetchCardTypes()
  await fetchData()
})
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="卡片列表" />

    <a-form layout="inline" style="margin-bottom: 16px;">
      <a-form-item label="卡号">
        <a-input v-model:value="query.cardNo" placeholder="请输入卡号" allow-clear />
      </a-form-item>
      <a-form-item label="持卡人类型">
        <a-select v-model:value="query.holderType" allow-clear style="width: 160px">
          <a-select-option value="STUDENT">学生</a-select-option>
          <a-select-option value="TEACHER">教师</a-select-option>
          <a-select-option value="STAFF">职工</a-select-option>
          <a-select-option value="VISITOR">临时卡</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 160px">
          <a-select-option value="ACTIVE">正常</a-select-option>
          <a-select-option value="LOST">挂失</a-select-option>
          <a-select-option value="FROZEN">冻结</a-select-option>
          <a-select-option value="CANCELLED">注销</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSearch">查询</a-button>
      </a-form-item>
      <a-form-item>
        <a-button @click="handleReset">重置</a-button>
      </a-form-item>
      <a-form-item style="margin-left: auto;">
        <a-space>
          <a-button type="primary" @click="openIssue">单卡开卡</a-button>
          <a-button @click="openBatch">批量发卡</a-button>
        </a-space>
      </a-form-item>
    </a-form>

    <a-table
      :columns="columns"
      :data-source="dataSource"
      :loading="loading"
      row-key="id"
      :pagination="{ current: pagination.current, pageSize: pagination.pageSize, total: pagination.total, showSizeChanger: true }"
      @change="(p:any) => handlePageChange(p.current, p.pageSize)"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'cardNo'">
          <a-button type="link" @click="toRecords(record.cardNo)">{{ record.cardNo }}</a-button>
        </template>
        <template v-else-if="column.key === 'typeName'">
          <span>{{ typeMap[record.typeId] || record.typeId }}</span>
        </template>
        <template v-else-if="column.key === 'holderType'">
          <span>{{ record.holderType === 'STUDENT' ? '学生' : record.holderType === 'TEACHER' ? '教师' : record.holderType === 'STAFF' ? '职工' : '临时' }}</span>
        </template>
        <template v-else-if="column.key === 'status'">
          <span>{{ record.status === 'ACTIVE' ? '正常' : record.status === 'LOST' ? '挂失' : record.status === 'FROZEN' ? '冻结' : '注销' }}</span>
        </template>
        <template v-else-if="column.key === 'balance'">
          <span>{{ (record.balance ?? 0).toFixed(2) }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="onLoss(record)" :disabled="record.status === 'LOST' || record.status === 'CANCELLED'">挂失</a-button>
            <a-button type="link" @click="onUnloss(record)" :disabled="record.status !== 'LOST'">解挂</a-button>
            <a-button type="link" @click="onFreeze(record)" :disabled="record.status === 'FROZEN' || record.status === 'CANCELLED'">冻结</a-button>
            <a-button type="link" @click="onUnfreeze(record)" :disabled="record.status !== 'FROZEN'">解冻</a-button>
            <a-popconfirm title="确认注销该卡片？余额将退回" @confirm="() => onCancel(record)">
              <a-button type="link" danger :disabled="record.status === 'CANCELLED'">注销</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 单卡开卡 -->
    <a-modal v-model:open="issueVisible" title="单卡开卡" :confirmLoading="issuing" @ok="submitIssue">
      <a-form layout="vertical">
        <a-form-item label="持卡人类型">
          <a-select v-model:value="issueForm.holderType" style="width: 200px">
            <a-select-option value="STUDENT">学生</a-select-option>
            <a-select-option value="TEACHER">教师</a-select-option>
            <a-select-option value="STAFF">职工</a-select-option>
            <a-select-option value="VISITOR">临时卡</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="持卡人ID">
          <a-input v-model:value="(issueForm as any).holderId" style="width: 200px" placeholder="输入学号/工号或标识" />
        </a-form-item>
        <a-form-item label="卡种">
          <a-select v-model:value="issueForm.cardTypeId" style="width: 200px" placeholder="选择卡种">
            <a-select-option v-for="t in types" :key="t.id" :value="t.id">{{ t.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="issueForm.holderType === 'VISITOR'" label="过期时间">
          <a-date-picker v-model:value="(issueForm as any).expireAt" show-time format="YYYY-MM-DD HH:mm" style="width: 240px" />
        </a-form-item>
        <a-form-item label="初始余额">
          <a-input-number v-model:value="issueForm.initialBalance" :min="0" :precision="2" style="width: 200px" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="issueForm.note" style="width: 320px" placeholder="可选备注" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 批量发卡 -->
    <a-modal v-model:open="batchVisible" title="批量发卡" :confirmLoading="batchLoading" @ok="submitBatch">
      <p>每行格式：holderType,holderId,cardTypeId,initialBalance[,expireAt]</p>
      <a-textarea v-model:value="batchText" :rows="6" placeholder="示例：\nVISITOR,guest-001,1,0,2025-12-31T23:59\nSTUDENT,S0001,1,50\nTEACHER,T0001,2,100" />
    </a-modal>
  </div>
</template>