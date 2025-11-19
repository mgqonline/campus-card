<template>
  <a-layout :style="{ minHeight: '100vh' }">
    <a-layout-sider v-model:collapsed="collapsed" collapsible :trigger="null" class="sider-card" :style="{ background: '#fff' }">
      <div class="logo">
        <router-link to="/">
          <h3>校园卡管理平台</h3>
        </router-link>
      </div>
      <a-menu theme="light" mode="inline" v-model:selectedKeys="selectedKeys" v-model:openKeys="openKeys" :inline-collapsed="collapsed" :items="filteredMenuItems" @click="onMenuClick" />
    </a-layout-sider>
    <a-layout>
      <a-layout-header style="background: #fff; padding: 0">
        <div style="padding: 0 16px; display: flex; align-items: center; justify-content: space-between;">
          <div style="display: flex; align-items: center;">
            <a-button type="text" @click="collapsed = !collapsed">
              <template #icon>
                <menu-unfold-outlined v-if="collapsed" />
                <menu-fold-outlined v-else />
              </template>
            </a-button>
            <span style="margin-left: 8px; font-weight: 600;">欢迎使用</span>
          </div>
          <div style="display: flex; align-items: center; gap: 12px; padding-right: 12px;">
            <span v-if="perm.user">{{ perm.user.username }}</span>
            <a-dropdown>
              <a-button>
                账户
              </a-button>
              <template #overlay>
                <a-menu>
                  <a-menu-item key="profile" @click="openProfile">个人信息</a-menu-item>
                  <a-menu-item key="change-pwd" @click="openChangePwd">修改密码</a-menu-item>
                  <a-menu-item key="logout" @click="onLogout">退出登录</a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
        </div>
      </a-layout-header>
      <!-- 个人信息弹窗 -->
      <a-modal
        v-model:open="infoVisible"
        title="个人信息"
        :footer="null"
        @cancel="onInfoCancel"
      >
        <a-spin :spinning="infoLoading">
          <a-descriptions bordered size="small" :column="1">
            <template #extra>
              <a-button size="small" :loading="infoLoading" @click="loadUserInfo">刷新</a-button>
            </template>
            <a-descriptions-item label="用户ID">{{ userInfo?.id ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="用户名">{{ userInfo?.username ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="手机">{{ userInfo?.phone ?? '-' }}</a-descriptions-item>
            <a-descriptions-item label="状态">{{ formatStatus(userInfo?.status) }}</a-descriptions-item>
            <a-descriptions-item label="角色">{{ (userInfo?.roles || []).map(r => r.name).join('，') || '-' }}</a-descriptions-item>
          </a-descriptions>
          <div style="margin-top:8px;color:#888;font-size:12px;">数据来源：后端接口 GET /api/v1/users/{id}</div>
        </a-spin>
      </a-modal>
      <!-- 修改密码弹窗 -->
      <a-modal
        v-model:open="pwdVisible"
        title="修改密码"
        :confirmLoading="pwdLoading"
        @ok="onPwdOk"
        @cancel="onPwdCancel"
      >
        <a-form layout="vertical">
          <a-form-item label="新密码" required>
            <a-input-password v-model:value="newPwd" placeholder="请输入新密码" />
          </a-form-item>
          <a-form-item label="确认新密码" required>
            <a-input-password v-model:value="confirmPwd" placeholder="请再次输入新密码" />
          </a-form-item>
        </a-form>
      </a-modal>
      <a-layout-content style="margin: 16px">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MenuUnfoldOutlined, MenuFoldOutlined } from '@ant-design/icons-vue'
import { usePermStore } from '@/stores/perm'
import { message } from 'ant-design-vue'
import { logout as apiLogout, login as apiLogin, resetPasswordById } from '@/api/modules/auth'
import { getUserDetail, type UserInfo } from '@/api/modules/user'

const collapsed = ref(false)
const route = useRoute()
const router = useRouter()
const selectedKeys = ref<string[]>([route.path])
const openKeys = ref<string[]>([])
const perm = usePermStore()

const onLogout = async () => {
  try {
    // 调用后端注销（后端可能未严格校验，失败也继续清理本地）
    try { await apiLogout() } catch {}
    localStorage.removeItem('token')
    try { sessionStorage.removeItem('token') } catch {}
    // 清空权限状态，避免残留菜单
    perm.roles = []
    perm.permissions = []
    perm.menus = []
    perm.user = undefined as any
    perm.loaded = false
    router.replace('/login')
  } catch {}
}

// 个人信息弹窗状态
const infoVisible = ref(false)
const infoLoading = ref(false)
const userInfo = ref<UserInfo>()

const formatStatus = (s?: number) => (s === 1 ? '启用' : s === 0 ? '禁用' : (s ?? '-'))

const openProfile = () => {
  infoVisible.value = true
  loadUserInfo()
}

const onInfoCancel = () => {
  infoVisible.value = false
}

const loadUserInfo = async () => {
  if (!perm.user?.id) {
    message.error('用户信息未加载')
    return
  }
  infoLoading.value = true
  try {
    const res = await getUserDetail(perm.user.id)
    userInfo.value = res
  } catch (e: any) {
    message.error(e?.message || '获取个人信息失败')
  } finally {
    infoLoading.value = false
  }
}

// 修改密码弹窗状态
const pwdVisible = ref(false)
const pwdLoading = ref(false)
const newPwd = ref('')
const confirmPwd = ref('')

const openChangePwd = () => {
  newPwd.value = ''
  confirmPwd.value = ''
  pwdVisible.value = true
}

const onPwdCancel = () => {
  pwdVisible.value = false
}

const onPwdOk = async () => {
  if (!newPwd.value || !confirmPwd.value) {
    message.error('请填写新密码并确认')
    return
  }
  if (newPwd.value.length < 6) {
    message.error('密码长度至少为 6 位')
    return
  }
  if (newPwd.value !== confirmPwd.value) {
    message.error('两次输入的密码不一致')
    return
  }
  if (!perm.user?.id || !perm.user?.username) {
    message.error('用户信息未加载，请稍后再试')
    return
  }
  pwdLoading.value = true
  try {
    // 调用后端重置密码并验证持久化：用新密码重新登录
    await resetPasswordById(perm.user.id, newPwd.value)
    const res = await apiLogin({ username: perm.user.username, password: newPwd.value })
    localStorage.setItem('token', res.token)
    await perm.load()
    message.success('密码修改成功，已使用新密码重新登录')
    pwdVisible.value = false
  } catch (e: any) {
    message.error(e?.message || '密码修改失败，请稍后重试')
  } finally {
    pwdLoading.value = false
  }
}

const menuItems = [
  { key: '/dashboard', label: '仪表盘' },
  {
    key: 'attendance',
    label: '考勤管理',
    children: [
      { key: '/attendance/stat', label: '考勤统计' },
      { key: '/attendance/analytics', label: '统计分析' },
      { key: '/attendance/exceptions', label: '异常处理' },
      { key: '/attendance/rules', label: '考勤规则' },
      { key: '/attendance/devices', label: '设备管理' },
      { key: '/attendance/hikvision-config', label: '海康设备配置' }
    ]
  },
  {
    key: 'school',
    label: '学校管理',
    children: [
      { key: '/school/list', label: '学校管理' },
      { key: '/school/campus', label: '校区管理' },
      { key: '/grade/list', label: '年级管理' },
      { key: '/subject/list', label: '学科管理' },
      { key: '/department/list', label: '部门管理' },
      { key: '/semester/list', label: '学期管理' },
      { key: '/class/list', label: '班级管理' },
      { key: '/class/organization', label: '班级组织架构' },
      { key: '/teacher/list', label: '教师管理' },
      { key: '/school/config', label: '参数配置' },
      { key: '/school/logo', label: 'Logo设置' }
    ]
  },
  { key: 'student', label: '学生管理', children: [{ key: '/student/list', label: '学生列表' }] },
  { key: 'parent', label: '家长管理', children: [{ key: '/parent/list', label: '家长列表' }, { key: '/parent/account', label: '家长账号管理' }] },
  { key: 'user', label: '用户管理', children: [{ key: '/user/list', label: '用户列表' }, { key: '/role/list', label: '角色管理' }, { key: '/permission/list', label: '权限管理' }] },
  {
    key: 'card',
    label: '校园卡管理',
    children: [
      { key: '/card/list', label: '校园卡列表' },
      { key: '/card/replace', label: '补卡/换卡' },
      { key: '/card/recharge', label: '卡片充值' },
      { key: '/card/consume', label: '卡片消费' },
      { key: '/card/types', label: '卡种管理' },
      { key: '/card/type-config/student', label: '学生卡配置' },
      { key: '/card/type-config/teacher', label: '教师卡配置' },
      { key: '/card/type-config/temp', label: '临时卡配置' },
      { key: '/card/type-config/visitor', label: '访客卡配置' },
      { key: '/card/type-config/permissions', label: '卡片权限配置' }
    ]
  },
  {
    key: 'account',
    label: '账户管理',
    children: [
      { key: '/account/balance', label: '账户余额查询' },
      { key: '/account/recharge', label: '账户充值（现金/线上）' },
      { key: '/account/refund', label: '账户退款' },
      { key: '/account/freeze', label: '账户冻结/解冻' },
      { key: '/account/transactions', label: '账户流水查询' },
      { key: '/account/reconcile', label: '账户对账管理' }
    ]
  },
  { key: 'face', label: '人脸管理', children: [{ key: '/face/manage', label: '人脸信息管理' }, { key: '/face/config', label: '人脸识别配置' }, { key: '/face/dispatch', label: '人脸数据下发' }] },
  { key: 'sync', label: '数据下发', children: [{ key: '/sync/dispatch', label: '基础数据下发' }] },
  { key: 'logs', label: '日志审计', children: [
    { key: '/logs/op', label: '操作日志' },
    { key: '/logs/device', label: '设备操作日志' },
    { key: '/logs/data-change', label: '数据变更日志' }
  ] },
  { key: 'system', label: '系统设置', children: [
    { key: '/system/config', label: '系统配置' }
  ] },
  // 新增：报表统计
  { key: 'reports', label: '报表统计', children: [
    { key: '/reports/consume', label: '消费报表' }
  ] }
]

// 计算：按权限过滤菜单
const filteredMenuItems = computed(() => {
  // 未加载或无菜单权限时，默认不做过滤（开发期兜底）
  if (!perm.loaded || (perm.menus?.length || 0) === 0) return menuItems
  const keep = (item: any) => {
    if (item.children && item.children.length) {
      const children = item.children.filter((c: any) => perm.allowMenu(c.key))
      if (children.length) return { ...item, children }
      return null
    }
    // 顶级路由项（如 /dashboard）
    return perm.allowMenu(item.key) ? item : null
  }
  const res: any[] = []
  for (const it of menuItems) {
    const k = keep(it)
    if (k) res.push(k)
  }
  // 若过滤后无任何菜单，则保留仪表盘以确保可导航
  if (!res.length) res.push({ key: '/dashboard', label: '仪表盘' })
  return res
})

const onMenuClick = (info: { key: string }) => {
  const key = info.key
  if (key && key.startsWith('/')) router.push(key)
}
watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]
    if (path.startsWith('/attendance/')) {
      if (!openKeys.value.includes('attendance')) openKeys.value.push('attendance')
    }
    if (path.startsWith('/school/') || path.startsWith('/grade/') || path.startsWith('/class/') || path.startsWith('/teacher/') || path.startsWith('/subject/')) {
      if (!openKeys.value.includes('school')) openKeys.value.push('school')
    }
    if (path.startsWith('/student/')) {
      if (!openKeys.value.includes('student')) openKeys.value.push('student')
    }
    if (path.startsWith('/parent/')) {
      if (!openKeys.value.includes('parent')) openKeys.value.push('parent')
    }
    if (path.startsWith('/user/') || path.startsWith('/role/') || path.startsWith('/permission/')) {
      if (!openKeys.value.includes('user')) openKeys.value.push('user')
    }
    if (path.startsWith('/card/')) {
      if (!openKeys.value.includes('card')) openKeys.value.push('card')
    }
    if (path.startsWith('/account/')) {
      if (!openKeys.value.includes('account')) openKeys.value.push('account')
    }
    if (path.startsWith('/logs/')) {
      if (!openKeys.value.includes('logs')) openKeys.value.push('logs')
    }
    if (path.startsWith('/system/')) {
      if (!openKeys.value.includes('system')) openKeys.value.push('system')
    }
    // 新增：报表菜单展开联动
    if (path.startsWith('/reports/')) {
      if (!openKeys.value.includes('reports')) openKeys.value.push('reports')
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.logo { height: 48px; margin: 16px; color: #1f1f1f; font-weight: 600; }
.sider-card {
  background: #fff !important;
  border-right: 1px solid #f0f0f0;
  padding: 12px 8px;
}
/* 让菜单呈现卡片视觉：圆角、阴影、分隔 */
:deep(.ant-menu-light) {
  background: #fff !important;
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.06);
  padding: 8px;
}
:deep(.ant-menu-light .ant-menu-item) {
  border-radius: 8px;
  margin-inline: 6px;
}
:deep(.ant-menu-light .ant-menu-item-selected) {
  background: rgba(24, 144, 255, 0.12) !important;
  color: #1677ff !important;
}
:deep(.ant-menu-light .ant-menu-submenu-title) {
  border-radius: 8px;
  margin-inline: 6px;
}
/* 折叠态时保留卡片圆角与阴影 */
:deep(.ant-layout-sider-collapsed .ant-menu-light) {
  border-radius: 12px;
}
/* 隐藏默认折叠触发器的黑色条 */
:deep(.ant-layout-sider-trigger) { display: none !important; }
</style>