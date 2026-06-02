<template>
  <div class="dashboard">
    <div class="page-header">
      <div>
        <h1 class="page-title">系统工作台</h1>
        <p class="page-subtitle">当前账号可访问的核心管理入口</p>
      </div>
    </div>

    <WorkbenchOverview />

    <div class="action-grid">
      <div class="action-card" @click="$router.push('/system/user')">
        <div class="action-card__icon"><el-icon><User /></el-icon></div>
        <h3 class="action-card__title">用户管理</h3>
        <p class="action-card__meta">账号、角色、状态与密码维护</p>
        <el-button type="primary" text>进入</el-button>
      </div>
      <div class="action-card" @click="$router.push('/system/role')">
        <div class="action-card__icon"><el-icon><UserFilled /></el-icon></div>
        <h3 class="action-card__title">角色管理</h3>
        <p class="action-card__meta">角色定义与权限授权</p>
        <el-button type="primary" text>进入</el-button>
      </div>
      <div class="action-card" @click="$router.push(withSelectedBatchQuery('/statistics'))">
        <div class="action-card__icon"><el-icon><DataAnalysis /></el-icon></div>
        <h3 class="action-card__title">数据统计</h3>
        <p class="action-card__meta">整体进度、选题和成绩概览</p>
        <el-button type="primary" text>进入</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { DataAnalysis, User, UserFilled } from '@element-plus/icons-vue'
import WorkbenchOverview from '@/views/components/WorkbenchOverview.vue'
import { withSelectedBatchQuery } from '@/utils/batchContext'
</script>
