import http from '../request';

export interface ClassSubjectTeacher {
  id?: number;
  classId: number;
  subjectId: number;
  teacherId: number;
}

export function listClassSubjectTeachers(classId: number) {
  return http.get(`/api/v1/classes/${classId}/subject-teachers`);
}

export function assignSubjectTeacher(classId: number, data: { subjectId: number; teacherId: number }) {
  return http.post(`/api/v1/classes/${classId}/subject-teachers`, data);
}

export function removeSubjectTeacherById(classId: number, id: number) {
  return http.delete(`/api/v1/classes/${classId}/subject-teachers/${id}`);
}

export function removeSubjectTeacherBySubject(classId: number, subjectId: number) {
  return http.delete(`/api/v1/classes/${classId}/subject-teachers`, { params: { subjectId } });
}