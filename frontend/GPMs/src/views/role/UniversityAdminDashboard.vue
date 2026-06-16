<template>
  <div class="dashboard">
    <div class="page-header">
      <div>
        <h1 class="page-title">校级管理员工作台</h1>
        <p class="page-subtitle">维护全校基础数据、毕业设计批次和过程监管</p>
      </div>
    </div>

    <WorkbenchOverview />

    <div class="action-grid">
      <div v-for="item in actions" :key="item.path" class="action-card">
        <div class="action-card__icon"><el-icon><component :is="item.icon" /></el-icon></div>
        <h2 class="action-card__title">{{ item.title }}</h2>
        <p class="action-card__meta">{{ item.desc }}</p>
        <el-button type="primary" @click="router.push(withSelectedBatchQuery(item.path))">进入</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { OfficeBuilding, Grid, User, Tickets, Collection, DataAnalysis, Select } from '@element-plus/icons-vue'
import WorkbenchOverview from '@/views/components/WorkbenchOverview.vue'
import { withSelectedBatchQuery } from '@/utils/batchContext'

const router = useRouter()
const actions = [
  { title: '学院管理', desc: '维护学院编码、名称和排序，用于用户、专业和批次归属。', path: '/system/college', icon: OfficeBuilding },
  { title: '专业管理', desc: '维护全校专业信息，按学院组织专业基础数据。', path: '/system/major', icon: Grid },
  { title: '用户管理', desc: '创建并维护管理员、教师和学生账号。', path: '/system/user', icon: User },
  { title: '批次管理', desc: '创建毕业设计批次，配置选题规则并推进阶段。', path: '/batch', icon: Tickets },
  { title: '课题管理', desc: '查看课题库并审核各单位提交的课题。', path: '/topic', icon: Collection },
  { title: '选题审核', desc: '查看学生提交的选题志愿和导师匹配情况。', path: '/teacher/selection-review', icon: Select },
  { title: '数据统计', desc: '查看全校毕业设计选题、流程和成绩概览。', path: '/statistics', icon: DataAnalysis }
]
</script>
