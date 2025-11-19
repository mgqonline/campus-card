import request from '../request'

export interface StudentInfo {
  id: number
  name: string
  classId: number
  grade: string
  cardNo: string
  faceStatus: string
}

export const getStudentInfo = (childId: number) => {
  return request.get<StudentInfo>('/api/v1/student/info', { params: { childId } })
}