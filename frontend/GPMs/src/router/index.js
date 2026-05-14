import { createRouter, createWebHistory } from 'vue-router'
import { getDashboardPath } from '@/stores/auth'

function getRoles() {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  return user?.roles || []
}

function isTokenValid() {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  return token && user
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/',
      component: () => import('@/views/Layout.vue'),
      children: [
        // 通用 Dashboard（默认兜底页，不限制角色）
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '首页' }
        },
        // 校级管理员
        {
          path: 'university-admin',
          name: 'UniversityAdminDashboard',
          component: () => import('@/views/role/UniversityAdminDashboard.vue'),
          meta: { title: '校级管理', roles: ['UNIVERSITY_ADMIN'] }
        },
        // 院级管理员
        {
          path: 'college-admin',
          name: 'CollegeAdminDashboard',
          component: () => import('@/views/role/CollegeAdminDashboard.vue'),
          meta: { title: '院级管理', roles: ['COLLEGE_ADMIN'] }
        },
        // 年级管理员
        {
          path: 'grade-admin',
          name: 'GradeAdminDashboard',
          component: () => import('@/views/role/GradeAdminDashboard.vue'),
          meta: { title: '年级管理', roles: ['GRADE_ADMIN'] }
        },
        // 专业管理员
        {
          path: 'major-admin',
          name: 'MajorAdminDashboard',
          component: () => import('@/views/role/MajorAdminDashboard.vue'),
          meta: { title: '专业管理', roles: ['MAJOR_ADMIN'] }
        },
        // 教师
        {
          path: 'teacher',
          name: 'TeacherDashboard',
          component: () => import('@/views/role/TeacherDashboard.vue'),
          meta: { title: '教师工作台', roles: ['TEACHER'] }
        },
        // 学生
        {
          path: 'student',
          name: 'StudentDashboard',
          component: () => import('@/views/role/StudentDashboard.vue'),
          meta: { title: '学生工作台', roles: ['STUDENT'] }
        },
        // 系统管理页面
        {
          path: 'system/user',
          name: 'UserList',
          component: () => import('@/views/system/UserList.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'system/role',
          name: 'RoleList',
          component: () => import('@/views/system/RoleList.vue'),
          meta: { title: '角色管理' }
        },
        {
          path: 'system/permission',
          name: 'PermissionList',
          component: () => import('@/views/system/PermissionList.vue'),
          meta: { title: '权限管理' }
        },
        {
          path: 'system/college',
          name: 'CollegeList',
          component: () => import('@/views/system/CollegeList.vue'),
          meta: { title: '学院管理' }
        },
        {
          path: 'system/major',
          name: 'MajorList',
          component: () => import('@/views/system/MajorList.vue'),
          meta: { title: '专业管理' }
        },
        {
          path: 'batch',
          name: 'BatchList',
          component: () => import('@/views/batch/BatchList.vue'),
          meta: { title: '批次管理' }
        },
        {
          path: 'topic',
          name: 'TopicList',
          component: () => import('@/views/topic/TopicList.vue'),
          meta: { title: '课题管理' }
        },
        {
          path: 'statistics',
          name: 'Statistics',
          component: () => import('@/views/Statistics.vue'),
          meta: { title: '数据统计' }
        },
        // 专业管理监控
        {
          path: 'major/monitor',
          name: 'MajorMonitor',
          component: () => import('@/views/major/MajorMonitor.vue'),
          meta: { title: '专业管理' }
        },
        // 学生专区
        {
          path: 'student/selection',
          name: 'MySelection',
          component: () => import('@/views/student/MySelection.vue'),
          meta: { title: '我的选题' }
        },
        {
          path: 'student/process',
          name: 'MyProcess',
          component: () => import('@/views/student/MyProcess.vue'),
          meta: { title: '流程进度' }
        },
        {
          path: 'student/guidance',
          name: 'MyGuidance',
          component: () => import('@/views/student/MyGuidance.vue'),
          meta: { title: '指导记录' }
        },
        {
          path: 'student/score',
          name: 'MyScore',
          component: () => import('@/views/student/MyScore.vue'),
          meta: { title: '我的成绩' }
        },
        // 教师专区
        {
          path: 'teacher/selection-review',
          name: 'SelectionReview',
          component: () => import('@/views/teacher/SelectionReview.vue'),
          meta: { title: '选题审核' }
        },
        {
          path: 'teacher/guidance-review',
          name: 'GuidanceReview',
          component: () => import('@/views/teacher/GuidanceReview.vue'),
          meta: { title: '指导记录' }
        },
        {
          path: 'teacher/process-review',
          name: 'ProcessReview',
          component: () => import('@/views/teacher/ProcessReview.vue'),
          meta: { title: '流程审核' }
        },
        {
          path: 'teacher/score-entry',
          name: 'ScoreEntry',
          component: () => import('@/views/teacher/ScoreEntry.vue'),
          meta: { title: '成绩评定' }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const roles = getRoles()

  // 未登录 → 去登录页
  if (to.path !== '/login' && !token) {
    return next('/login')
  }
  // 已登录但访问登录页 → 跳转到对应首页
  if (to.path === '/login' && token) {
    // 如果 user 对象没有 roles，说明是旧 token，清除后重新登录
    if (!isTokenValid() || roles.length === 0) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      return next('/login')
    }
    return next(getDashboardPath(roles))
  }
  // 已登录访问根路径 → 跳转到对应首页
  if (to.path === '/' && token) {
    return next(getDashboardPath(roles))
  }
  // 角色页面保护：没有对应角色则跳回自己的首页
  if (to.meta?.roles && token && roles.length > 0) {
    const allowed = roles.some(r => to.meta.roles.includes(r))
    if (!allowed) {
      return next(getDashboardPath(roles))
    }
  }
  next()
})

export default router
