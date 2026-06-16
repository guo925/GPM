<template>
  <div class="dashboard">
    <div class="page-header">
      <div>
        <h1 class="page-title">专业管理员工作台</h1>
        <p class="page-subtitle">聚焦本专业的课题、选题、流程和教师负载</p>
      </div>
    </div>

    <WorkbenchOverview />

    <div class="action-grid">
      <div v-for="item in actions" :key="item.path + item.title" class="action-card">
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
import { Tickets, Collection, Monitor, UserFilled, Finished, DataLine, Select } from '@element-plus/icons-vue'
import WorkbenchOverview from '@/views/components/WorkbenchOverview.vue'
import { withSelectedBatchQuery } from '@/utils/batchContext'

const router = useRouter()
const actions = [
  { title: '批次管理', desc: '查看和维护本专业毕业设计批次。', path: '/batch', icon: Tickets },
  { title: '课题管理', desc: '审核本专业课题并查看容量使用情况。', path: '/topic', icon: Collection },
  { title: '专业监控', desc: '查看教师负载、选题分配和流程异常。', path: '/major/monitor', icon: Monitor },
  { title: '选题审核', desc: '查看本专业学生提交的选题志愿。', path: '/teacher/selection-review', icon: Select },
  { title: '选题进度', desc: '跟踪本专业学生志愿和导师匹配情况。', path: '/major/monitor', icon: UserFilled },
  { title: '流程管理', desc: '定位任务书、开题、中期和论文阶段待处理事项。', path: '/major/monitor', icon: Finished },
  { title: '成绩统计', desc: '查看本专业毕业设计成绩分布和完成度。', path: '/major/monitor', icon: DataLine }
]
</script>
