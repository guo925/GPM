<template>
  <el-container class="app-shell">
    <el-aside width="248px" class="app-aside">
      <div class="brand">
        <div class="brand-mark">G</div>
        <div>
          <div class="brand-title">GPMS</div>
          <div class="brand-subtitle">毕业设计管理</div>
        </div>
      </div>
      <el-menu
        :default-active="route.path"
        background-color="transparent"
        text-color="#b7c2d7"
        active-text-color="#ffffff"
        class="app-menu"
        router
      >
        <el-menu-item :index="homePath">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <!-- 系统管理：仅管理员可见 -->
        <el-sub-menu index="system" v-if="showSystemMenu">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN'])" index="/system/user">用户管理</el-menu-item>
          <el-menu-item v-if="isSuperAdmin" index="/system/role">角色管理</el-menu-item>
          <el-menu-item v-if="isSuperAdmin" index="/system/permission">权限管理</el-menu-item>
          <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN'])" index="/system/college">学院管理</el-menu-item>
          <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN'])" index="/system/major">专业管理</el-menu-item>
        </el-sub-menu>

        <!-- 批次管理：管理员 + 教师可见 -->
        <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER'])" index="/batch">
          <el-icon><Document /></el-icon>
          <span>批次管理</span>
        </el-menu-item>

        <!-- 课题管理：管理员 + 教师 + 学生可见 -->
        <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER','STUDENT'])" index="/topic">
          <el-icon><Collection /></el-icon>
          <span>课题管理</span>
        </el-menu-item>

        <el-sub-menu v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER','STUDENT'])" index="thesis">
          <template #title>
            <el-icon><DocumentChecked /></el-icon>
            <span>论文管理</span>
          </template>
          <el-menu-item index="/thesis/task-book">任务书</el-menu-item>
          <el-menu-item index="/thesis/opening-report">开题报告</el-menu-item>
          <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER'])" index="/thesis/opening-defense">开题答辩</el-menu-item>
          <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER'])" index="/thesis/opening-minutes">开题报告会议纪要</el-menu-item>
          <el-menu-item index="/thesis/weekly-log">指导记录周记</el-menu-item>
          <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER'])" index="/thesis/midterm">中期检查</el-menu-item>
          <el-menu-item index="/thesis/guidance">论文指导</el-menu-item>
          <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER'])" index="/thesis/post-defense-revision">答辩后论文修改审核</el-menu-item>
          <el-menu-item index="/thesis/final-thesis">查看论文终稿</el-menu-item>
          <el-menu-item index="/thesis/final-design">查看设计终稿</el-menu-item>
        </el-sub-menu>

        <!-- 数据统计：管理员可见 -->
        <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN'])" index="/statistics">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>

        <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN'])" index="/log/statistics">
          <el-icon><Histogram /></el-icon>
          <span>日志统计</span>
        </el-menu-item>

        <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN'])" index="/announcement">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>

        <el-menu-item index="/notification">
          <el-icon><Message /></el-icon>
          <span>通知中心</span>
        </el-menu-item>

        <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN'])" index="/log/audit">
          <el-icon><Tickets /></el-icon>
          <span>审核日志</span>
        </el-menu-item>

        <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN'])" index="/teacher/process-review">
          <el-icon><DocumentChecked /></el-icon>
          <span>流程审核</span>
        </el-menu-item>

        <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER'])" index="/defense">
          <el-icon><Calendar /></el-icon>
          <span>答辩管理</span>
        </el-menu-item>

        <el-sub-menu v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER'])" index="score-evaluation">
          <template #title>
            <el-icon><Tickets /></el-icon>
            <span>成绩评定</span>
          </template>
          <el-menu-item index="/score/defense-groups">答辩组信息</el-menu-item>
          <el-menu-item index="/score/advisor">指导导师评分</el-menu-item>
          <el-menu-item index="/score/reviewer">评阅导师评分</el-menu-item>
          <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN'])" index="/score/deputy-review">论文副院长审核</el-menu-item>
          <el-menu-item index="/score/history">历史评分</el-menu-item>
        </el-sub-menu>

        <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN'])" index="/archive">
          <el-icon><FolderChecked /></el-icon>
          <span>归档管理</span>
        </el-menu-item>

        <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER'])" index="/export">
          <el-icon><Download /></el-icon>
          <span>导出中心</span>
        </el-menu-item>

        <!-- 专业管理：专业管理员可见 -->
        <el-menu-item v-if="checkRole(['MAJOR_ADMIN'])" index="/major/monitor">
          <el-icon><Monitor /></el-icon>
          <span>专业管理</span>
        </el-menu-item>

        <el-sub-menu v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER','STUDENT'])" index="special">
          <template #title>
            <el-icon><Tickets /></el-icon>
            <span>特殊情况处理</span>
          </template>
          <el-menu-item index="/special/title-change">论文修改题目审核</el-menu-item>
          <el-menu-item index="/special/extension">申请延期</el-menu-item>
          <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER'])" index="/special/advisor-score">指导老师评分</el-menu-item>
          <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER'])" index="/special/attachment-review">附件修改审核</el-menu-item>
          <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER'])" index="/special/completion-edit-review">流程完成后修改审核</el-menu-item>
        </el-sub-menu>

        <!-- 学生专区：仅学生可见 -->
        <el-sub-menu index="student-area" v-if="checkRole(['STUDENT'])">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>学生专区</span>
          </template>
          <el-menu-item index="/student/selection">我的选题</el-menu-item>
          <el-menu-item index="/student/process">流程进度</el-menu-item>
          <el-menu-item index="/student/guidance">指导记录</el-menu-item>
          <el-menu-item index="/student/score">我的成绩</el-menu-item>
        </el-sub-menu>

        <!-- 教师专区：教师与管理员可见 -->
        <el-sub-menu index="teacher-area" v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER'])">
          <template #title>
            <el-icon><Avatar /></el-icon>
            <span>教师专区</span>
          </template>
          <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN','GRADE_ADMIN','MAJOR_ADMIN','TEACHER'])" index="/teacher/selection-review">选题审核</el-menu-item>
          <el-menu-item v-if="checkRole(['TEACHER'])" index="/teacher/guidance-review">指导记录</el-menu-item>
          <el-menu-item v-if="checkRole(['TEACHER'])" index="/teacher/process-review">流程审核</el-menu-item>
          <el-menu-item v-if="checkRole(['TEACHER'])" index="/teacher/score-entry">成绩评定</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="app-header">
        <div>
          <div class="header-title">{{ route.meta?.title || '工作台' }}</div>
          <div class="header-subtitle">Graduation Project Management System</div>
        </div>
        <div class="header-user">
          <div class="user-profile">
            <el-tag v-if="roleText" effect="plain">{{ roleText }}</el-tag>
            <span v-if="authStore.user?.studentNo">学号：{{ authStore.user.studentNo }}</span>
            <span v-if="authStore.user?.majorName">专业：{{ authStore.user.majorName }}</span>
            <span v-if="authStore.user?.grade">年级：{{ authStore.user.grade }}</span>
            <span v-if="authStore.user?.collegeName">学院：{{ authStore.user.collegeName }}</span>
          </div>
          <div class="user-name">
            <el-icon><User /></el-icon>
            <span>{{ authStore.user?.realName || authStore.user?.username }}</span>
          </div>
          <el-button type="danger" text @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore, hasRole, hasAnyRole, getDashboardPath } from '@/stores/auth'
import { HomeFilled, Setting, Document, Collection, UserFilled, Avatar, DataAnalysis, Monitor, SwitchButton, User, Histogram, Bell, Message, Tickets, Calendar, FolderChecked, Download, DocumentChecked } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isSuperAdmin = computed(() => hasRole(authStore.roles, 'SUPER_ADMIN'))
const showSystemMenu = computed(() =>
  hasAnyRole(authStore.roles, ['SUPER_ADMIN', 'UNIVERSITY_ADMIN', 'COLLEGE_ADMIN'])
)
const roleNameMap = {
  SUPER_ADMIN: '超级管理员',
  UNIVERSITY_ADMIN: '校级管理员',
  COLLEGE_ADMIN: '院级管理员',
  GRADE_ADMIN: '年级管理员',
  MAJOR_ADMIN: '专业管理员',
  TEACHER: '教师',
  STUDENT: '学生'
}
const roleText = computed(() =>
  (authStore.user?.roles || []).map(role => roleNameMap[role] || role).join(' / ')
)

const homePath = computed(() => getDashboardPath(authStore.roles))

function checkRole(codes) {
  return hasAnyRole(authStore.roles, codes)
}

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-shell {
  height: 100vh;
  background: var(--gp-bg);
  overflow: hidden;
}

.app-aside {
  background: var(--gp-sidebar);
  border-right: 1px solid rgba(255, 255, 255, .08);
  height: 100vh;
  overflow-y: auto;
  overflow-x: hidden;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 72px;
  padding: 0 20px;
  color: #fff;
}

.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: #2563eb;
  border-radius: 8px;
  font-weight: 750;
}

.brand-title {
  font-size: 18px;
  font-weight: 720;
  line-height: 1.1;
}

.brand-subtitle {
  margin-top: 4px;
  color: #8ea0bf;
  font-size: 12px;
}

.app-menu {
  border-right: 0;
  padding: 8px 12px 18px;
}

.app-menu :deep(.el-menu-item),
.app-menu :deep(.el-sub-menu__title) {
  height: 44px;
  margin: 4px 0;
  border-radius: 8px;
}

.app-menu :deep(.el-menu-item.is-active) {
  background: var(--gp-sidebar-active);
}

.app-menu :deep(.el-menu-item:hover),
.app-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, .06);
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid var(--gp-border);
}

.header-title {
  color: var(--gp-text);
  font-size: 17px;
  font-weight: 650;
}

.header-subtitle {
  margin-top: 3px;
  color: var(--gp-text-muted);
  font-size: 12px;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.user-profile {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  min-width: 0;
  max-width: 760px;
  color: var(--gp-text-secondary);
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
}

.user-name {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--gp-text-secondary);
  font-size: 14px;
  white-space: nowrap;
}

.app-main {
  height: calc(100vh - 64px);
  padding: 22px;
  background: var(--gp-bg);
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
