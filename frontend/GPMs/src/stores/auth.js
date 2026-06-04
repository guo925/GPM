import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, me as meApi } from '@/api/auth'
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
    token: sessionStorage.getItem('token') || '',
    user: JSON.parse(sessionStorage.getItem('user') || 'null')
  }),
  getters: {
    roles: (state) => state.user?.roles || [],
    permissions: (state) => state.user?.permissions || [],
    isSuperAdmin: (state) => (state.user?.roles || []).includes('SUPER_ADMIN')
  },
  actions: {
    setAuthData(data) {
      const {
        token,
        userId,
        username,
        realName,
        studentNo,
        grade,
        collegeId,
        collegeName,
        majorId,
        majorName,
        roles,
        permissions
      } = data
      if (token) {
        this.token = token
        sessionStorage.setItem('token', token)
      }
      this.user = {
        userId,
        username,
        realName,
        studentNo,
        grade,
        collegeId,
        collegeName,
        majorId,
        majorName,
        roles: roles || [],
        permissions: permissions || []
      }
      sessionStorage.setItem('user', JSON.stringify(this.user))
    },
    async login(loginForm) {
      const res = await loginApi(loginForm)
      this.setAuthData(res.data)
      ElMessage.success('登录成功')
      return true
    },
    async refreshUser() {
      if (!this.token) return
      const res = await meApi()
      this.setAuthData({ ...res.data, token: this.token })
    },
    async logout() {
      try { await logoutApi() } catch {}
      this.token = ''
      this.user = null
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
    }
  }
})
