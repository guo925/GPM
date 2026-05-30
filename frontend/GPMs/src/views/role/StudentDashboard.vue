<template>
  <div class="dashboard">
    <div class="page-header">
      <div>
        <h1 class="page-title">学生工作台</h1>
        <p class="page-subtitle">完成选题、阶段材料提交、指导记录和成绩查看</p>
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
import { Search, Select, Tickets, DocumentChecked, ChatLineRound, Medal } from '@element-plus/icons-vue'

const router = useRouter()
const actions = [
  { title: '课题浏览', desc: '查看可选课题、导师和容量信息。', path: '/topic', icon: Search },
  { title: '我的选题', desc: '提交志愿并查看最终选题结果。', path: '/student/selection', icon: Select },
  { title: '批次信息', desc: '查看毕业设计批次和阶段安排。', path: '/batch', icon: Tickets },
  { title: '流程进度', desc: '提交各阶段内容和附件，查看审核结果。', path: '/student/process', icon: DocumentChecked },
  { title: '指导记录', desc: '提交周记并查看导师批阅意见。', path: '/student/guidance', icon: ChatLineRound },
  { title: '我的成绩', desc: '查看分项评分、最终成绩和审核意见。', path: '/student/score', icon: Medal }
]
</script>
