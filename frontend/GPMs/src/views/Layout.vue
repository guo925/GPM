<template>
  <el-container style="height:100vh">
    <el-aside width="220px" style="background:#304156">
      <div class="logo">GPMS</div>
      <el-menu
        :default-active="route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
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

        <!-- 数据统计：管理员可见 -->
        <el-menu-item v-if="checkRole(['SUPER_ADMIN','UNIVERSITY_ADMIN','COLLEGE_ADMIN'])" index="/statistics">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>

        <!-- 专业管理：专业管理员可见 -->
        <el-menu-item v-if="checkRole(['MAJOR_ADMIN'])" index="/major/monitor">
          <el-icon><Monitor /></el-icon>
          <span>专业管理</span>
        </el-menu-item>

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

        <!-- 教师专区：仅教师可见 -->
        <el-sub-menu index="teacher-area" v-if="checkRole(['TEACHER'])">
          <template #title>
            <el-icon><Avatar /></el-icon>
            <span>教师专区</span>
          </template>
          <el-menu-item index="/teacher/selection-review">选题审核</el-menu-item>
          <el-menu-item index="/teacher/guidance-review">指导记录</el-menu-item>
          <el-menu-item index="/teacher/process-review">流程审核</el-menu-item>
          <el-menu-item index="/teacher/score-entry">成绩评定</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background:#fff;border-bottom:1px solid #e6e6e6;display:flex;align-items:center;justify-content:flex-end;padding:0 20px">
        <span style="margin-right:16px">{{ authStore.user?.username }}</span>
        <span v-if="authStore.user?.roles?.length" style="margin-right:16px;color:#909399;font-size:13px">
          {{ authStore.user.roles.join(' / ') }}
        </span>
        <el-button type="danger" text @click="handleLogout">退出</el-button>
      </el-header>
      <el-main style="background:#f0f2f5">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore, hasRole, hasAnyRole, getDashboardPath } from '@/stores/auth'
import { HomeFilled, Setting, Document, Collection, UserFilled, Avatar, DataAnalysis, Monitor } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isSuperAdmin = computed(() => hasRole(authStore.roles, 'SUPER_ADMIN'))
const showSystemMenu = computed(() =>
  hasAnyRole(authStore.roles, ['SUPER_ADMIN', 'UNIVERSITY_ADMIN', 'COLLEGE_ADMIN'])
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
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  letter-spacing: 2px;
}
</style>
