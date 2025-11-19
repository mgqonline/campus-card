import http from '../request';

export interface Subject {
  id?: number;
  name: string;
  status?: number;
  schoolId?: number;
}

export function listSubjects(params: { page?: number; size?: number; name?: string; schoolId?: number }) {
  return http.get('/api/v1/subjects', { params });
}

export function getSubject(id: number) {
  return http.get(`/api/v1/subjects/${id}`);
}

export function createSubject(data: Subject) {
  return http.post('/api/v1/subjects', data);
}

export function updateSubject(id: number, data: Subject) {
  return http.put(`/api/v1/subjects/${id}`, data);
}

export function deleteSubject(id: number) {
  return http.delete(`/api/v1/subjects/${id}`);
}