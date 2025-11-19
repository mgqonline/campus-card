import axios, { type AxiosRequestConfig } from 'axios'
import { message } from 'ant-design-vue'
import router from '@/router'

let last401ShownAt = 0

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 10000
})

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  if (token) {
    config.headers = (config.headers || {}) as any
    ;(config.headers as any).Authorization = `Bearer ${token}`
  }
  // Dev 调试输出请求详情
  if (import.meta && (import.meta as any).env && (import.meta as any).env.DEV) {
    const method = (config.method || 'GET').toUpperCase()
    console.log('[HTTP]', method, config.url, { params: config.params, data: config.data })
  }
  return config
})

instance.interceptors.response.use(
  (resp) => {
    const body = resp.data
    // 统一后端返回结构：{ code, message, data }
    if (body && typeof body === 'object' && 'code' in body) {
      const code = (body as any).code
      if (code === 0 || code === 200) {
        // 常见：data 为实际数据
        return (body as any).data !== undefined ? (body as any).data : body
      }
      const msg = (body as any).message || (body as any).msg || '请求失败'
      // 业务无权限码透出
      if (code === 401) {
        const isOnLogin = router.currentRoute?.value?.path === '/login'
        // 2秒内只提示一次，且不在登录页重复提示
        const now = Date.now()
        if (!isOnLogin && now - last401ShownAt > 2000) {
          message.error('登录已过期，请重新登录')
          last401ShownAt = now
        }
        localStorage.removeItem('token')
        try { sessionStorage.removeItem('token') } catch {}
        try { sessionStorage.setItem('redirected_due_to_401', '1') } catch {}
        router.replace('/login')
      } else if (code === 403) {
        message.warning('无权限访问该资源')
      }
      const err: any = new Error(msg)
      err.code = code
      return Promise.reject(err)
    }
    // 兼容：{ success, data }
    if (body && typeof body === 'object' && 'success' in body) {
      const ok = !!(body as any).success
      if (ok) {
        return (body as any).data !== undefined ? (body as any).data : body
      }
      const msg = (body as any).message || '请求失败'
      return Promise.reject(new Error(msg))
    }
    // 兼容分页直接返回：{ records, total }
    if (body && typeof body === 'object' && 'records' in body && 'total' in body) {
      return body
    }
    // 非统一结构，直接返回
    return body
  },
  (error) => {
    const status = error.response?.status
    const messageText = error.response?.data?.message || error.message
    if (status === 401) {
      const isOnLogin = router.currentRoute?.value?.path === '/login'
      const now = Date.now()
      if (!isOnLogin && now - last401ShownAt > 2000) {
        message.error('登录已过期，请重新登录')
        last401ShownAt = now
      }
      localStorage.removeItem('token')
      try { sessionStorage.removeItem('token') } catch {}
      try { sessionStorage.setItem('redirected_due_to_401', '1') } catch {}
      router.replace('/login')
    } else if (status === 403) {
      message.warning('无权限操作或访问受限')
    } else {
      console.error('[API Error]', messageText)
    }
    // 将状态码和响应体附加到错误对象，便于业务层按状态做友好提示
    const err: any = new Error(messageText)
    err.status = status
    err.data = error.response?.data
    return Promise.reject(err)
  }
)

const http = {
  get<T = any>(url: string, config?: AxiosRequestConfig) {
    return instance.get(url, config) as any as Promise<T>
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
    return instance.post(url, data, config) as any as Promise<T>
  },
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
    return instance.put(url, data, config) as any as Promise<T>
  },
  delete<T = any>(url: string, config?: AxiosRequestConfig) {
    return instance.delete(url, config) as any as Promise<T>
  }
}

export default http