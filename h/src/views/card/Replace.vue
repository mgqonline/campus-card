<script setup lang="ts">
import { onMounted, ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import { listCardTypes, getBalance, replaceCard, type CardType } from '@/api/modules/card'

const types = ref<CardType[]>([])
const loading = ref(false)
const replacing = ref(false)

const form = reactive({
  oldCardNo: '',
  changeType: 'SUPPLEMENT' as 'SUPPLEMENT' | 'TYPE_CHANGE',
  newCardTypeId: undefined as number | undefined,
  fee: undefined as number | undefined,
  note: ''
})

const balanceInfo = ref<{ balance: number; status?: 'ACTIVE' | 'LOST' | 'FROZEN' | 'CANCELLED' | string; holderName?: string } | null>(null)
const newCardNoResult = ref<string | null>(null)

const canTypeChange = computed(() => balanceInfo.value?.status === 'ACTIVE')

const fetchTypes = async () => {
  try {
    const res = await listCardTypes()
    types.value = Array.isArray(res) ? res : []
  } catch (e: any) {
    types.value = []
    message.error(e?.message || '获取卡种失败')
  }
}

const fetchBalance = async () => {
  if (!form.oldCardNo) {
    message.error('请输入旧卡号')
    return
  }
  loading.value = true
  try {
    const res = await getBalance(form.oldCardNo)
    balanceInfo.value = {
      balance: res.balance ?? 0,
      status: res.status,
      holderName: res.holderName
    }
  } catch (e: any) {
    balanceInfo.value = null
    message.error(e?.message || '查询余额失败')
  } finally {
    loading.value = false
  }
}

const onReplace = async () => {
  if (!form.oldCardNo) {
    message.error('请输入旧卡号')
    return
  }
  if (form.changeType === 'TYPE_CHANGE') {
    if (!form.newCardTypeId) {
      message.error('请选择新的卡种')
      return
    }
    if (!canTypeChange.value) {
      message.error('当前状态不可进行换卡（需为正常状态）')
      return
    }
  }
  if ((form.fee ?? 0) < 0) {
    message.error('工本费不能为负数')
    return
  }
  replacing.value = true
  try {
    const res = await replaceCard({
      oldCardNo: form.oldCardNo,
      changeType: form.changeType,
      newCardTypeId: form.changeType === 'TYPE_CHANGE' ? form.newCardTypeId : undefined,
      fee: form.fee,
      note: form.note
    })
    newCardNoResult.value = res?.newCardNo || null
    message.success('补卡/换卡成功')
    await fetchBalance()
  } catch (e: any) {
    message.error(e?.message || '补卡/换卡失败')
  } finally {
    replacing.value = false
  }
}

onMounted(async () => {
  await fetchTypes()
})
</script>

<template>
  <div style="padding: 24px;">
    <a-page-header title="补卡 / 换卡" />

    <a-card title="卡片信息" style="margin-bottom: 16px;">
      <a-form layout="inline">
        <a-form-item label="旧卡号">
          <a-input v-model:value="form.oldCardNo" style="width: 180px" placeholder="输入旧卡号" />
        </a-form-item>
        <a-form-item>
          <a-button type="default" :loading="loading" @click="fetchBalance">查询余额与状态</a-button>
        </a-form-item>
      </a-form>
      <div v-if="balanceInfo" style="margin-top: 8px;">
        <a-alert type="info" :message="`持卡人：${balanceInfo?.holderName || '-' }，状态：${balanceInfo?.status || '-' }，余额：${(balanceInfo?.balance ?? 0).toFixed(2)} 元`" show-icon />
      </div>
    </a-card>

    <a-card title="补卡 / 换卡" style="margin-bottom: 16px;">
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 12 }">
        <a-form-item label="变更类型">
          <a-radio-group v-model:value="form.changeType">
            <a-radio value="SUPPLEMENT">补卡（生成新卡）</a-radio>
            <a-radio value="TYPE_CHANGE" :disabled="!canTypeChange">换卡（变更卡种）</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item v-if="form.changeType === 'TYPE_CHANGE'" label="新的卡种">
          <a-select v-model:value="form.newCardTypeId" style="width: 220px" placeholder="选择新的卡种">
            <a-select-option v-for="t in types" :key="t.id" :value="t.id">{{ t.name }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="工本费">
          <a-input-number v-model:value="form.fee" :min="0" :step="0.01" style="width: 220px" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.note" style="width: 320px" placeholder="可选备注" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" :loading="replacing" @click="onReplace">提交补卡 / 换卡</a-button>
        </a-form-item>
        <a-form-item v-if="newCardNoResult" label="新卡卡号">
          <b>{{ newCardNoResult }}</b>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>