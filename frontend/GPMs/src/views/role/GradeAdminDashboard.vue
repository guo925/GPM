<template>
  <div class="dashboard">
    <div class="page-header">
      <div>
        <h1 class="page-title">年级管理员工作台</h1>
        <p class="page-subtitle">负责年级批次推进、课题审核和选题过程跟踪</p>
      </div>
    </div>

    <div class="action-grid">
      <div v-for="item in actions" :key="item.path" class="action-card">
        <div class="action-card__icon"><el-icon><component :is="item.icon" /></el-icon></div>
        <h2 class="action-card__title">{{ item.title }}</h2>
        <p class="action-card__meta">{{ item.desc }}</p>
        <el-button type="primary" @click="router.push(item.path)">进入</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Tickets, Collection, UserFilled, Monitor, DataLine, OfficeBuilding } from '@element-plus/icons-vue'

const router = useRouter()
const actions = [
  { title: '批次管理', desc: '维护本年级毕业设计批次并推进阶段。', path: '/batch', icon: Tickets },
  { title: '课题管理', desc: '审核和查看本年级可选课题。', path: '/topic', icon: Collection },
  { title: '选题结果', desc: '查看学生选题和指导教师匹配情况。', path: '/teacher/selection-review', icon: UserFilled },
  { title: '流程监控', desc: '按阶段检查任务书、开题、论文等完成情况。', path: '/batch', icon: Monitor },
  { title: '成绩汇总', desc: '进入批次与成绩相关页面核对年级进展。', path: '/batch', icon: DataLine },
  { title: '基础数据', desc: '查看学院和专业基础数据。', path: '/system/major', icon: OfficeBuilding }
]
</script>
