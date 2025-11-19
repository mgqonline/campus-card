<template>
  <div style="padding: 24px;">
    <a-page-header title="交易记录查询" />
    <a-card>
      <a-form layout="inline">
        <a-form-item label="卡号">
          <a-input v-model:value="form.cardNo" placeholder="请输入卡号" style="width: 220px" />
        </a-form-item>
        <a-form-item label="类型">
          <a-select v-model:value="form.type" allow-clear style="width: 160px">
            <a-select-option value="CONSUME">消费</a-select-option>
            <a-select-option value="RECHARGE">充值</a-select-option>
            <a-select-option value="REFUND">退款</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="开始时间">
          <a-date-picker show-time format="YYYY-MM-DD HH:mm" style="width: 240px" @change="onStartChange" />
        </a-form-item>
        <a-form-item label="结束时间">
          <a-date-picker show-time format="YYYY-MM-DD HH:mm" style="width: 240px" @change="onEndChange" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" :loading="loading" @click="onQuery">查询</a-button>
            <a-button @click="onReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-divider />

      <div>
        <div style="margin-bottom: 8px; color: #555;">总数：{{ page.total }}</div>
        <a-table :data-source="page.records" :loading="loading" row-key="id" :pagination="false">
          <a-table-column title="ID" key="id" :customRender="({ record }) => record.id" />
          <a-table-column title="卡号" key="cardNo" :customRender="({ record }) => record.cardNo" />
          <a-table-column title="类型" key="type" :customRender="({ record }) => typeText(record.type)" />
          <a-table-column title="金额" key="amount" :customRender="({ record }) => (record.amount ?? 0).toFixed(2)" />
          <a-table-column title="交易后余额" key="balanceAfter" :customRender="({ record }) => (record.balanceAfter ?? 0).toFixed(2)" />
          <a-table-column title="商户" key="merchant" :customRender="({ record }) => record.merchant || '-'" />
          <a-table-column title="时间" key="occurredAt" :customRender="({ record }) => formatTime(record.occurredAt)" />
          <a-table-column title="备注" key="note" :customRender="({ record }) => record.note || '-'" />
          <template #emptyText>
            <a-empty description="暂无数据" />
          </template>
        </a-table>
        <div style="display: flex; justify-content: flex-end; margin-top: 8px;">
          <a-pagination :current="form.page" :pageSize="form.size" :total="page.total" showSizeChanger @change="onPageChange" @showSizeChange="onPageChange" />
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { listTransactions, type TransQuery, type PageResult, type TransactionRecord, type TransactionType } from '@/api/modules/card';

const loading = ref(false);

const form = reactive<TransQuery>({
  cardNo: '',
  type: undefined,
  start: '',
  end: '',
  page: 1,
  size: 10,
});

const page = reactive<PageResult<TransactionRecord>>({ total: 0, records: [] });

function typeText(t?: TransactionType) {
  switch (t) {
    case 'CONSUME': return '消费';
    case 'RECHARGE': return '充值';
    case 'REFUND': return '退款';
    default: return '-';
  }
}

function formatTime(s: string) {
  try {
    const d = new Date(s);
    return d.toLocaleString();
  } catch {
    return s;
  }
}

async function onQuery() {
  loading.value = true;
  try {
    const res = await listTransactions({ ...form });
    page.total = res.total;
    page.records = res.records;
  } finally {
    loading.value = false;
  }
}

function onReset() {
  form.cardNo = '';
  form.type = undefined;
  form.start = '';
  form.end = '';
  form.page = 1;
  form.size = 10;
  page.total = 0;
  page.records = [];
}

function onPageChange(p: number, s?: number) {
  form.page = p;
  if (s) form.size = s;
  onQuery();
}

function onStartChange(_: any, dateString: string) {
  form.start = dateString || ''
}

function onEndChange(_: any, dateString: string) {
  form.end = dateString || ''
}

const route = useRoute();
onMounted(() => {
  const q = route.query?.cardNo;
  if (typeof q === 'string' && q) {
    form.cardNo = q;
  }
  onQuery();
});
</script>

<style scoped>
/* 使用 Ant Design Vue 的组件样式，页面保持统一的留白与层次 */
</style>