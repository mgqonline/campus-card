import { defineStore } from 'pinia'
import { ref } from 'vue'
import { me, type MeInfo } from '@/api/modules/auth'

export const usePermStore = defineStore('perm', () => {
  const loaded = ref(false)
  const user = ref<Pick<MeInfo, 'id' | 'username'>>()
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])
  const menus = ref<string[]>([])

  const load = async () => {
    try {
      const token = localStorage.getItem('token')
      if (!token) {
        loaded.value = true
        roles.value = []
        permissions.value = []
        menus.value = []
        return
      }
      const info = await me()
      user.value = { id: info.id, username: info.username }
      roles.value = info.roles || []
      permissions.value = info.permissions || []
      menus.value = info.menus || []
    } catch (e) {
      // 兜底：后端未就绪时允许所有（避免开发期阻断）
      roles.value = []
      permissions.value = []
      menus.value = []
    } finally {
      loaded.value = true
    }
  }

  const hasPerm = (code?: string): boolean => {
    if (!code) return true
    // 未加载或为空时默认放行，避免阻断页面渲染
    if (!loaded.value || permissions.value.length === 0) return true
    // 支持通配符匹配（后端拦截器也支持类似逻辑）
    return permissions.value.some(p => {
      if (p === code) return true
      if (p.endsWith(':*')) {
        const prefix = p.slice(0, -2)
        return code.startsWith(prefix)
      }
      return false
    })
  }

  const allowMenu = (path?: string): boolean => {
    if (!path) return true
    // 超级管理员直接放行所有菜单
    if (roles.value.some(r => r === 'ADMIN' || r === '超级管理员')) return true
    if (!loaded.value || menus.value.length === 0) return true
    return menus.value.includes(path)
  }

  return { loaded, user, roles, permissions, menus, load, hasPerm, allowMenu }
})