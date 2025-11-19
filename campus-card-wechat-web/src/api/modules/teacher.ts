import request from '../request'

export interface TLoginReq { phone: string; code: string }
export interface TLoginResp { token: string; expireIn: number }

export interface TClassBrief { classId: number; className: string }

export interface TProfileResp {
  teacherId: number
  name: string
  phone?: string
  email?: string
  schoolName?: string
  subjects: string[]
  assignedClasses: TClassBrief[]
}

export const tLogin = (data: TLoginReq) => request.post<TLoginResp>('/api/v1/t/auth/login', data)
export const tBindWeChat = (data: { openId: string; phone?: string }) => request.post<string>('/api/v1/t/auth/bind', data)
export const tGetProfile = () => request.get<TProfileResp>('/api/v1/t/auth/profile')
export const tUpdatePassword = (data: { oldPassword?: string; newPassword: string }) => request.put<string>('/api/v1/t/auth/password', data)
export const tLogout = () => request.post<string>('/api/v1/t/auth/logout')

export const tGetClasses = () => request.get<TClassBrief[]>('/api/v1/t/class/list')
export const tGetClassStudents = (classId: number) => request.get<Array<{ id: number; name: string; classId: number; grade: string; cardNo: string; faceStatus: string }>>('/api/v1/t/class/students', { params: { classId } })
export const tGetStudentInfo = (childId: number) => request.get<{ id: number; name: string; classId: number; grade: string; cardNo: string; faceStatus: string }>('/api/v1/t/class/student/info', { params: { childId } })
export const tGetClassAttendanceStats = (params: { classId: number; range?: 'day' | 'month'; date?: string }) => request.get<{ classId: number; range: 'day' | 'month'; totalStudents: number; presentCount?: number | null; absentCount?: number | null; lateCount: number; earlyLeaveCount: number; dailyPresence: Record<string, number> }>('/api/v1/t/class/attendance/stats', { params })

// 教师端考勤管理
export const tGetAttendanceRecords = (params: { childId?: number; classId?: number; startDate?: string; endDate?: string; page?: number; size?: number }) => {
  return request.get<Array<{ id: number; childId: number; childName?: string; time: string; type: string; gate: string; late?: boolean; earlyLeave?: boolean }>>('/api/v1/t/attendance/records', { params })
}

export const tMarkAttendanceException = (id: number, data: { status: 'normal' | 'late' | 'early'; reason?: string }) => {
  return request.put<string>(`/api/v1/t/attendance/records/${id}/exception`, data)
}

export const tExportClassAttendance = (params: { classId: number; startDate?: string; endDate?: string }) => {
  // 返回 CSV Blob
  return request.get<Blob>('/api/v1/t/attendance/export', { params, responseType: 'blob' as any })
}

// 请假审批模块
export const tLeaveUpload = (file: File) => {
  const form = new FormData();
  form.append('file', file)
  return request.post<{ url: string; name: string }>('/api/v1/leave/upload', form)
}

export const tLeaveApply = (data: { childId: number; type: string; startTime: string; endTime: string; reason?: string; attachments?: string[] }) => {
  return request.post<{ id: number; status: string }>('/api/v1/leave/apply', data)
}

export const tTeacherLeavePending = (classId: number) => {
  return request.get<Array<{ id: number; childId: number; type: string; startTime: string; endTime: string; reason: string; attachments: string[]; status: string; applyTime: string }>>('/api/v1/leave/teacher/pending', { params: { classId } })
}

export const tTeacherLeaveRecords = (classId: number) => {
  return request.get<Array<{ id: number; childId: number; type: string; startTime: string; endTime: string; reason: string; attachments: string[]; status: string; applyTime: string }>>('/api/v1/leave/teacher/records', { params: { classId } })
}

export const tTeacherHelpApply = (data: { childId: number; type: string; startTime: string; endTime: string; reason?: string; attachments?: string[] }) => {
  return request.post<{ id: number; status: string }>('/api/v1/t/leave/help_apply', data)
}

export const tTeacherApproveLeave = (data: { childId: number; id: number; status: 'APPROVED' | 'REJECTED' }) => {
  return request.post<string>('/api/v1/leave/teacher/approve', data)
}

export const tTeacherBatchApproveLeave = (data: { items: Array<{ childId: number; id: number }>; status: 'APPROVED' | 'REJECTED' }) => {
  return request.post<number>('/api/v1/leave/teacher/batch_approve', data)
}

// 消息通知模块（教师）
export const tTeacherSchoolNotify = (params: { page?: number; size?: number }) => {
  return request.get<{ content: Array<{ id: number; title: string; content: string; createdAt: string }> }>('/api/v1/teacher/notify/school_list', { params })
}

export const tTeacherClassSendNotify = (data: { classId: number; title: string; content: string }) => {
  return request.post<number>('/api/v1/teacher/notify/class_send', data)
}

export const tTeacherListParentMessages = (params: { childId: number; page?: number; size?: number }) => {
  return request.get<{ content: Array<{ id: number; senderRole: string; content: string; createdAt: string }> }>('/api/v1/teacher/messages/list', { params })
}

export const tTeacherReplyParentMessage = (data: { childId: number; content: string }) => {
  return request.post<{ id: number; senderRole: string; content: string; createdAt: string }>('/api/v1/teacher/messages/reply', data)
}