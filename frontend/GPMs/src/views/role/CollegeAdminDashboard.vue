<template>
  <div class="dashboard">
    <div class="page-header">
      <div>
        <h1 class="page-title">院级管理员工作台</h1>
        <p class="page-subtitle">管理学院范围内的专业、用户、批次和课题审核</p>
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
import { Grid, Tickets, User, Collection, DataAnalysis, Monitor } from '@element-plus/icons-vue'
import WorkbenchOverview from '@/views/components/WorkbenchOverview.vue'
import { withSelectedBatchQuery } from '@/utils/batchContext'

const router = useRouter()
const actions = [
  { title: '专业管理', desc: '维护本学院下属专业信息和编码。', path: '/system/major', icon: Grid },
  { title: '批次管理', desc: '创建并维护本学院毕业设计批次。', path: '/batch', icon: Tickets },
  { title: '用户管理', desc: '维护本学院教师、学生和管理账号。', path: '/system/user', icon: User },
  { title: '课题管理', desc: '审核本学院教师申报的课题。', path: '/topic', icon: Collection },
  { title: '数据统计', desc: '查看学院毕业设计整体进度。', path: '/statistics', icon: DataAnalysis },
  { title: '过程监控', desc: '通过批次与课题数据定位未完成事项。', path: '/batch', icon: Monitor }
]
</script>
