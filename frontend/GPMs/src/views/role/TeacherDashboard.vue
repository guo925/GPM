<template>
  <div class="dashboard">
    <div class="page-header">
      <div>
        <h1 class="page-title">教师工作台</h1>
        <p class="page-subtitle">管理课题、审核选题、批阅过程材料并录入成绩</p>
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
import { Collection, Select, Tickets, ChatLineRound, DocumentChecked, Medal } from '@element-plus/icons-vue'
import WorkbenchOverview from '@/views/components/WorkbenchOverview.vue'
import { withSelectedBatchQuery } from '@/utils/batchContext'

const router = useRouter()
const actions = [
  { title: '我的课题', desc: '申报和维护个人毕业设计课题。', path: '/topic', icon: Collection },
  { title: '选题审核', desc: '处理学生志愿申请并确认指导关系。', path: '/teacher/selection-review', icon: Select },
  { title: '批次信息', desc: '查看当前毕业设计批次和阶段安排。', path: '/batch', icon: Tickets },
  { title: '指导记录', desc: '查看并批阅学生提交的周记和反馈。', path: '/teacher/guidance-review', icon: ChatLineRound },
  { title: '流程审核', desc: '审核任务书、开题、论文等阶段材料。', path: '/teacher/process-review', icon: DocumentChecked },
  { title: '成绩评定', desc: '录入分项成绩并提交最终成绩。', path: '/teacher/score-entry', icon: Medal }
]
</script>
