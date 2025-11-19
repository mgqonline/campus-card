import axios, { type AxiosRequestConfig } from 'axios'

// 在开发模式下强制使用 Vite 代理（/api），避免浏览器直接访问本地 8082 端口导致连接问题
const API_BASE = import.meta.env.DEV ? '/api' : (import.meta.env.VITE_API_BASE || '')

// 兼容性处理：若 baseURL 含有 '/api'，而传入的 url 也以 '/api/' 开头，
// 则移除 url 的前导 '/api'，避免形成 '/api/api/...'
const normalizeUrl = (url: string) => {
  if ((API_BASE === '/api' || API_BASE.endsWith('/api')) && url.startsWith('/api/')) {
    return url.replace(/^\/api/, '')
  }
  return url
}

const instance = axios.create({
  baseURL: API_BASE,
  timeout: 10000
})

instance.interceptors.request.use((config) => {
  const url = (config.url || '') as string
  const isTeacherApi = url.startsWith('/api/v1/t/')
  const tokenKey = isTeacherApi ? 't_token' : 'wx_token'
  const token = localStorage.getItem(tokenKey)
  if (token) {
    config.headers = (config.headers || {}) as any
    ;(config.headers as any).Authorization = `Bearer ${token}`
  }
  return config
})

instance.interceptors.response.use(
  (resp) => {
    const body = resp.data
    if (body && typeof body === 'object' && 'code' in body && 'data' in body) {
      const code = (body as any).code
      if (code === 0 || code === 200) {
        return (body as any).data
      }
      const msg = (body as any).message || '请求失败'
      return Promise.reject(new Error(msg))
    }
    return body
  },
  (error) => {
    const message = error.response?.data?.message || error.message
    console.error('[API Error]', message)
    return Promise.reject(new Error(message))
  }
)

const http = {
  get<T = any>(url: string, config?: AxiosRequestConfig) {
    return instance.get(normalizeUrl(url), config).then((data) => data as T)
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
    return instance.post(normalizeUrl(url), data, config).then((data) => data as T)
  },
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
    return instance.put(normalizeUrl(url), data, config).then((data) => data as T)
  },
  delete<T = any>(url: string, config?: AxiosRequestConfig) {
    return instance.delete(normalizeUrl(url), config).then((data) => data as T)
  }
}

export default http