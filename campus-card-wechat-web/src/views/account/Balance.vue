<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getBalance, type BalanceResp } from '@/api/modules/account'

const balance = ref<BalanceResp | null>(null)
const loading = ref(false)
const error = ref('')

function currentChildId(): number | undefined {
  const cid = localStorage.getItem('wx_child_id')
  return cid ? Number(cid) : undefined
}

async function load() {
  loading.value = true
  try {
    balance.value = await getBalance({ childId: currentChildId() })
    error.value = ''
  } catch (e: any) {
    error.value = e?.message || '获取余额失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
<template>
  <div class="page">
    <h3 class="section-title"><van-icon name="balance-list-o" style="margin-right:6px;" />余额查询</h3>

    <!-- 加载/错误状态（WeUI） -->
    <div v-if="loading" class="weui-loadmore" style="margin: 12px 0;">
      <i class="weui-loading"></i>
      <span class="weui-loadmore__tips">加载中</span>
    </div>
    <div v-else-if="error" class="weui-toptips weui-toptips_warn" style="display: block; margin-bottom: 12px;">
      <i class="fa fa-exclamation-triangle"></i> {{ error }}
    </div>

    <!-- 余额概览（WeUI Panel + Hero） -->
    <div v-else-if="balance" class="card">
      <div class="weui-panel weui-panel_access">
        <div class="weui-panel__hd">账户余额</div>
        <div class="weui-panel__bd">
          <div class="balance-hero">
            <div class="icon"><i class="fa fa-money"></i></div>
            <div class="amount">
              {{ Number(balance.balance).toFixed(2) }}
              <span class="currency">{{ balance.currency }}</span>
            </div>
            <div class="tips">子女ID：{{ balance.childId }}</div>
          </div>

          <!-- 详情（WeUI cells） -->
          <div class="weui-cells" style="margin-top:12px;">
            <div class="weui-cell">
              <div class="weui-cell__bd"><p>当前余额</p></div>
              <div class="weui-cell__ft">{{ Number(balance.balance).toFixed(2) }} {{ balance.currency }}</div>
            </div>
            <div class="weui-cell">
              <div class="weui-cell__bd"><p>子女ID</p></div>
              <div class="weui-cell__ft">{{ balance.childId }}</div>
            </div>
          </div>
        </div>
        <div class="weui-panel__ft weui-cells__tips">数据来源：校园一卡通账户</div>
      </div>

      <!-- 操作区（WeUI 按钮） -->
      <div class="weui-btn-area inline">
        <button type="button" class="weui-btn weui-btn_primary" @click="load">
          <van-icon name="replay" style="margin-right:4px;" />刷新
        </button>
        <router-link to="/recharge" class="weui-btn weui-btn_default">
          <i class="fa fa-credit-card" style="margin-right:4px;"></i>去充值
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.balance-hero { display:flex; flex-direction:column; align-items:center; justify-content:center; padding:16px; background:#fff; border-radius:12px; box-shadow:0 1px 3px rgba(0,0,0,0.06); }
.balance-hero .icon { color:#2778F1; font-size:24px; margin-bottom:8px; }
.balance-hero .amount { font-size:28px; font-weight:700; color:#111; }
.balance-hero .currency { font-size:14px; color:#666; margin-left:6px; }
.balance-hero .tips { margin-top:6px; font-size:12px; color:#888; }
.weui-btn-area.inline { display:flex; gap:8px; align-items:center; margin-top:12px; }
</style>