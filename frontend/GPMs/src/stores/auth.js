import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

const ROLE_DASHBOARD_MAP = {
  SUPER_ADMIN: '/dashboard',
  UNIVERSITY_ADMIN: '/university-admin',
  COLLEGE_ADMIN: '/college-admin',
  GRADE_ADMIN: '/grade-admin',
  MAJOR_ADMIN: '/major-admin',
  TEACHER: '/teacher',
  STUDENT: '/student'
}

export function getDashboardPath(roles) {
  if (!roles || roles.length === 0) return '/dashboard'
  for (const [code, path] of Object.entries(ROLE_DASHBOARD_MAP)) {
    if (roles.includes(code)) return path
  }
  return '/dashboard'
}

export function hasRole(roles, code) {
  return roles && roles.includes(code)
}

export function hasAnyRole(roles, codes) {
  return roles && codes.some(c => roles.includes(c))
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    roles: (state) => state.user?.roles || [],
    permissions: (state) => state.user?.permissions || [],
    isSuperAdmin: (state) => (state.user?.roles || []).includes('SUPER_ADMIN')
  },
  actions: {
    async login(loginForm) {
      const res = await loginApi(loginForm)
      const { token, userId, username, roles, permissions } = res.data
      this.token = token
      this.user = { userId, username, roles: roles || [], permissions: permissions || [] }
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(this.user))
      ElMessage.success('登录成功')
      return true
    },
    async logout() {
      try { await logoutApi() } catch {}
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
