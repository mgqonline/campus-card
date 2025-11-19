import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { message } from 'ant-design-vue'
import { usePermStore } from '@/stores/perm'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/Login.vue') },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/Dashboard.vue') },
      { path: 'school/list', name: 'SchoolList', component: () => import('@/views/school/List.vue'), meta: { requiresAuth: true } },
      { path: 'school/campus', name: 'SchoolCampus', component: () => import('@/views/school/Campus.vue'), meta: { requiresAuth: true } },
      { path: 'school/config', name: 'SchoolConfig', component: () => import('@/views/school/Config.vue'), meta: { requiresAuth: true } },
      { path: 'school/logo', name: 'SchoolLogo', component: () => import('@/views/school/Logo.vue'), meta: { requiresAuth: true } },
      { path: 'student/list', name: 'StudentList', component: () => import('@/views/student/List.vue'), meta: { requiresAuth: true } },
      { path: 'grade/list', name: 'GradeList', component: () => import('@/views/grade/List.vue'), meta: { requiresAuth: true } },
      { path: 'subject/list', name: 'SubjectList', component: () => import('@/views/subject/List.vue'), meta: { requiresAuth: true } },
      { path: 'department/list', name: 'DepartmentList', component: () => import('@/views/department/List.vue'), meta: { requiresAuth: true } },
      { path: 'semester/list', name: 'SemesterList', component: () => import('@/views/semester/List.vue'), meta: { requiresAuth: true } },
      { path: 'class/list', name: 'ClassList', component: () => import('@/views/class/List.vue'), meta: { requiresAuth: true } },
      { path: 'class/organization', name: 'ClassOrganization', component: () => import('@/views/class/Organization.vue'), meta: { requiresAuth: true } },
      { path: 'teacher/list', name: 'TeacherList', component: () => import('@/views/teacher/List.vue'), meta: { requiresAuth: true } },
      { path: 'parent/list', name: 'ParentList', component: () => import('@/views/parent/List.vue'), meta: { requiresAuth: true } },
      { path: 'parent/account', name: 'ParentAccount', component: () => import('@/views/parent/Account.vue'), meta: { requiresAuth: true } },
      { path: 'user/list', name: 'UserList', component: () => import('@/views/user/List.vue'), meta: { requiresAuth: true } },
      { path: 'role/list', name: 'RoleList', component: () => import('@/views/role/List.vue'), meta: { requiresAuth: true } },
      { path: 'permission/list', name: 'PermissionList', component: () => import('@/views/permission/List.vue'), meta: { requiresAuth: true } },
      // 系统配置
      { path: 'system/config', name: 'SystemConfig', component: () => import('@/views/system/Config.vue'), meta: { requiresAuth: true } },
      { path: 'attendance/stat', name: 'AttendanceStat', component: () => import('@/views/attendance/Stat.vue'), meta: { requiresAuth: true } },
      { path: 'attendance/analytics', name: 'AttendanceAnalytics', component: () => import('@/views/attendance/Analytics.vue'), meta: { requiresAuth: true } },
      { path: 'attendance/rules', name: 'AttendanceRules', component: () => import('@/views/attendance/Rules.vue'), meta: { requiresAuth: true } },
      { path: 'attendance/exceptions', name: 'AttendanceExceptions', component: () => import('@/views/attendance/Exceptions.vue'), meta: { requiresAuth: true } },
      { path: 'attendance/devices', name: 'AttendanceDevices', component: () => import('@/views/attendance/Devices.vue'), meta: { requiresAuth: true } },
      { path: 'attendance/hikvision-config', name: 'HikvisionConfig', component: () => import('@/views/attendance/HikvisionConfig.vue'), meta: { requiresAuth: true } },
      { path: 'card/list', name: 'CardList', component: () => import('@/views/card/List.vue'), meta: { requiresAuth: true } },
      { path: 'card/replace', name: 'CardReplace', component: () => import('@/views/card/Replace.vue'), meta: { requiresAuth: true } },
      { path: 'card/recharge', name: 'CardRecharge', component: () => import('@/views/card/Recharge.vue'), meta: { requiresAuth: true } },
      { path: 'card/consume', name: 'CardConsume', component: () => import('@/views/card/ConsumeAction.vue'), meta: { requiresAuth: true } },
      { path: 'card/records', name: 'CardRecords', component: () => import('@/views/card/Consume.vue'), meta: { requiresAuth: true } },
      { path: 'card/types', name: 'CardTypes', component: () => import('@/views/card/Type.vue'), meta: { requiresAuth: true } },
      { path: 'card/type-config/student', name: 'CardTypeStudent', component: () => import('@/views/card/type/StudentConfig.vue'), meta: { requiresAuth: true } },
      { path: 'card/type-config/teacher', name: 'CardTypeTeacher', component: () => import('@/views/card/type/TeacherConfig.vue'), meta: { requiresAuth: true } },
      { path: 'card/type-config/temp', name: 'CardTypeTemp', component: () => import('@/views/card/type/TempConfig.vue'), meta: { requiresAuth: true } },
      { path: 'card/type-config/visitor', name: 'CardTypeVisitor', component: () => import('@/views/card/type/VisitorConfig.vue'), meta: { requiresAuth: true } },
      { path: 'card/type-config/permissions', name: 'CardTypePermissions', component: () => import('@/views/card/type/PermissionsConfig.vue'), meta: { requiresAuth: true } },
      { path: 'account/balance', name: 'AccountBalance', component: () => import('@/views/account/Balance.vue'), meta: { requiresAuth: true } },
      { path: 'account/recharge', name: 'AccountRecharge', component: () => import('@/views/account/Recharge.vue'), meta: { requiresAuth: true } },
      { path: 'account/refund', name: 'AccountRefund', component: () => import('@/views/account/Refund.vue'), meta: { requiresAuth: true } },
      { path: 'account/freeze', name: 'AccountFreeze', component: () => import('@/views/account/Freeze.vue'), meta: { requiresAuth: true } },
      { path: 'account/transactions', name: 'AccountTransactions', component: () => import('@/views/account/Transactions.vue'), meta: { requiresAuth: true } },
      { path: 'account/reconcile', name: 'AccountReconcile', component: () => import('@/views/account/Reconcile.vue'), meta: { requiresAuth: true } },
      { path: 'face/manage', name: 'FaceManage', component: () => import('@/views/face/Manage.vue'), meta: { requiresAuth: true } },
      { path: 'face/config', name: 'FaceConfig', component: () => import('@/views/face/Config.vue'), meta: { requiresAuth: true } },
      { path: 'face/dispatch', name: 'FaceDispatch', component: () => import('@/views/face/Dispatch.vue'), meta: { requiresAuth: true } },
      { path: 'sync/dispatch', name: 'SyncDispatch', component: () => import('@/views/sync/Dispatch.vue'), meta: { requiresAuth: true } },
      { path: 'logs/op', name: 'OpLogs', component: () => import('@/views/logs/OpLogs.vue'), meta: { requiresAuth: true } },
      { path: 'logs/device', name: 'DeviceLogs', component: () => import('@/views/logs/DeviceLogs.vue'), meta: { requiresAuth: true } },
      { path: 'logs/data-change', name: 'DataChangeLogs', component: () => import('@/views/logs/DataChangeLogs.vue'), meta: { requiresAuth: true } },
      // 新增：消费报表
      { path: 'reports/consume', name: 'ReportsConsume', component: () => import('@/views/reports/Consume.vue'), meta: { requiresAuth: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  if (to.path === '/login') {
    // 始终允许进入登录页（避免无效token阻塞登录）
    return next()
  }
  // 需要登录
  if (!token) return next('/login')
  // 权限路由校验：仅对 requiresAuth 路由进行菜单权限验证
  if (to.meta && (to.meta as any).requiresAuth) {
    const perm = usePermStore()
    // 未加载时先放行，初次挂载 main.ts 会等待 load
    if (perm.loaded && !perm.allowMenu(to.path)) {
      message.warning('无权访问该页面')
      return next('/dashboard')
    }
  }
  next()
})

export default router