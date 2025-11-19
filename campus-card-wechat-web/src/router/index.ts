import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '家长端首页', right: 'notice' } },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { title: '登录' } },
  { path: '/consume', name: 'ConsumeList', component: () => import('@/views/consume/ConsumeList.vue'), meta: { title: '消费记录', right: 'filter' } },
  { path: '/consume/stats', name: 'ConsumeStats', component: () => import('@/views/consume/ConsumeStats.vue'), meta: { title: '消费统计' } },
  { path: '/consume/calendar', name: 'ConsumeCalendar', component: () => import('@/views/consume/ConsumeCalendar.vue'), meta: { title: '消费日历' } },
  { path: '/consume/trend', name: 'ConsumeTrend', component: () => import('@/views/consume/ConsumeTrend.vue'), meta: { title: '消费趋势' } },
  { path: '/account/balance', name: 'Balance', component: () => import('@/views/account/Balance.vue'), meta: { title: '余额查询' } },
  { path: '/account/profile', name: 'ParentProfile', component: () => import('@/views/account/ParentProfile.vue'), meta: { title: '个人中心' } },
  { path: '/account/help', name: 'Help', component: () => import('@/views/account/Help.vue'), meta: { title: '帮助中心' } },
  { path: '/account/settings', name: 'Settings', component: () => import('@/views/account/Settings.vue'), meta: { title: '设置' } },
  { path: '/account/about', name: 'About', component: () => import('@/views/account/About.vue'), meta: { title: '关于我们' } },
  { path: '/student/info', name: 'StudentInfo', component: () => import('@/views/student/StudentInfo.vue'), meta: { title: '学生信息' } },
  { path: '/recharge', name: 'Recharge', component: () => import('@/views/recharge/Recharge.vue'), meta: { title: '校园卡充值', right: 'records' } },
  { path: '/recharge/records', name: 'RechargeRecords', component: () => import('@/views/recharge/Records.vue'), meta: { title: '充值记录' } },
  { path: '/attendance', name: 'Attendance', component: () => import('@/views/Attendance.vue'), meta: { title: '考勤管理' } },
  { path: '/attendance/realtime', name: 'AttendanceRealtime', component: () => import('@/views/attendance/Realtime.vue'), meta: { title: '实时考勤', right: 'notice' } },
  { path: '/attendance/records', name: 'AttendanceRecords', component: () => import('@/views/attendance/Records.vue'), meta: { title: '考勤记录', right: 'filter' } },
  { path: '/attendance/stats', name: 'AttendanceStats', component: () => import('@/views/attendance/Stats.vue'), meta: { title: '考勤统计' } },
  { path: '/attendance/photos', name: 'AttendancePhotos', component: () => import('@/views/attendance/Photos.vue'), meta: { title: '考勤照片' } },
  { path: '/attendance/alerts', name: 'AttendanceAlerts', component: () => import('@/views/attendance/Alerts.vue'), meta: { title: '考勤提醒' } },
  { path: '/notify', name: 'NotificationCenter', component: () => import('@/views/notify/NotificationCenter.vue'), meta: { title: '消息通知', right: 'notice' } },
  { path: '/notify/:id', name: 'NotificationDetail', component: () => import('@/views/notify/NotificationDetail.vue'), meta: { title: '通知详情' } },
  { path: '/leave/apply', name: 'LeaveApply', component: () => import('@/views/leave/LeaveApply.vue'), meta: { title: '请假申请' } },
  { path: '/leave/records', name: 'LeaveRecords', component: () => import('@/views/leave/LeaveRecords.vue'), meta: { title: '请假记录' } },
  { path: '/face/collection', name: 'FaceCollection', component: () => import('@/views/face/FaceCollection.vue'), meta: { title: '人像采集' } },
  { path: '/card/loss', name: 'CardLossUnloss', component: () => import('@/views/card/LossUnloss.vue'), meta: { title: '挂失/解挂申请' } },
  { path: '/card/replace', name: 'CardReplace', component: () => import('@/views/card/ReplaceCard.vue'), meta: { title: '补卡申请' } },
  { path: '/feedback', name: 'Feedback', component: () => import('@/views/feedback/Feedback.vue'), meta: { title: '意见反馈' } },
  // 教师端（独立入口与页面）
  { path: '/t/home', name: 'TeacherHome', component: () => import('@/views/teacher/TeacherHome.vue'), meta: { title: '教师端首页' } },
  { path: '/t/login', name: 'TeacherLogin', component: () => import('@/views/teacher/TeacherLogin.vue'), meta: { title: '教师登录' } },
  { path: '/t/account/profile', name: 'TeacherProfile', component: () => import('@/views/teacher/TeacherProfile.vue'), meta: { title: '教师个人信息' } },
  { path: '/t/account/password', name: 'TeacherPassword', component: () => import('@/views/teacher/ChangePassword.vue'), meta: { title: '密码修改' } },
  { path: '/t/classes', name: 'TeacherClasses', component: () => import('@/views/teacher/ClassList.vue'), meta: { title: '班级管理' } },
  { path: '/t/class/:id/students', name: 'ClassStudents', component: () => import('@/views/teacher/ClassStudents.vue'), meta: { title: '班级学生名单' } },
  { path: '/t/class/:id/student/:childId/info', name: 'ClassStudentInfo', component: () => import('@/views/teacher/ClassStudentInfo.vue'), meta: { title: '学生信息' } },
  { path: '/t/class/:id/student/:childId/attendance', name: 'StudentAttendance', component: () => import('@/views/teacher/StudentAttendance.vue'), meta: { title: '学生考勤记录' } },
  { path: '/t/class/:id/attendance/records', name: 'ClassAttendanceRecords', component: () => import('@/views/teacher/ClassAttendanceRecords.vue'), meta: { title: '班级考勤记录' } },
  { path: '/t/class/:id/attendance', name: 'ClassAttendance', component: () => import('@/views/teacher/ClassAttendance.vue'), meta: { title: '班级考勤统计' } },
  { path: '/t/leave', name: 'TeacherLeave', component: () => import('@/views/teacher/TeacherLeaveApproval.vue'), meta: { title: '请假审批' } },
  { path: '/t/leave/help', name: 'TeacherHelpLeave', component: () => import('@/views/teacher/TeacherHelpLeave.vue'), meta: { title: '老师帮请' } },
  { path: '/t/messages', name: 'TeacherMessages', component: () => import('@/views/teacher/TeacherMessages.vue'), meta: { title: '消息通知' } },
  // 默认重定向到首页，确保首次打开落在首页
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router