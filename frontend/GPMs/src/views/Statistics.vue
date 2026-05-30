<template>
  <div class="workspace-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">数据统计</h1>
        <p class="page-subtitle">系统运行、选题进度、流程节点与成绩分布</p>
      </div>
    </div>

    <!-- 概览卡片 -->
    <div class="metric-grid">
      <div class="metric-card" v-for="c in overviewCards" :key="c.label">
        <div class="metric-card__value">{{ c.value }}</div>
        <div class="metric-card__label">{{ c.label }}</div>
      </div>
    </div>

    <!-- 选题统计 -->
    <el-row :gutter="16" class="stats-row" v-if="data">
      <el-col :span="12">
        <el-card header="课题与选题" class="work-card">
          <div class="summary-strip">
            <div>
              <strong>{{ data.totalTopics }}</strong>
              <span>课题总数</span>
            </div>
            <div>
              <strong class="success">{{ data.approvedTopics }}</strong>
              <span>已通过</span>
            </div>
            <div>
              <strong class="warning">{{ data.selectedStudents }}</strong>
              <span>已选题</span>
            </div>
            <div>
              <strong class="danger">{{ data.unselectedStudents }}</strong>
              <span>未选题</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 成绩分布 -->
      <el-col :span="12">
        <el-card header="成绩分布" v-if="data.scoreDistribution" class="work-card">
          <div class="score-bars">
            <div v-for="g in data.scoreDistribution" :key="g.grade" class="score-item">
              <div class="score-count" :style="{ color: gradeColor(g.grade) }">{{ g.count }}</div>
              <div :style="barStyle(g.count, maxScoreCount)" class="bar"></div>
              <div class="score-label">{{ g.grade }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 阶段进度 -->
    <el-card header="各阶段完成情况" v-if="data && data.stageStats" class="table-card">
      <el-table :data="data.stageStats" stripe>
        <el-table-column prop="label" label="阶段" width="120" />
        <el-table-column prop="submitted" label="已提交" width="100" />
        <el-table-column prop="approved" label="已通过" width="100" />
        <el-table-column prop="rejected" label="已驳回" width="100" />
        <el-table-column label="进度" min-width="200">
          <template #default="{ row }">
            <el-progress
              :percentage="stagePercent(row)"
              :color="stagePercent(row) >= 100 ? '#67C23A' : '#409EFF'"
              :format="() => stagePercent(row) + '%'" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getOverview } from '@/api/statistics'

const data = ref(null)

const overviewCards = computed(() => {
  if (!data.value) return []
  const d = data.value
  return [
    { label: '学院', value: d.totalColleges },
    { label: '专业', value: d.totalMajors },
    { label: '用户', value: d.totalUsers },
    { label: '教师', value: d.totalTeachers },
    { label: '学生', value: d.totalStudents },
    { label: '批次', value: d.activeBatches + '/' + d.totalBatches }
  ]
})

const maxScoreCount = computed(() => {
  if (!data.value?.scoreDistribution) return 1
  return Math.max(1, ...data.value.scoreDistribution.map(g => g.count))
})

const gradeColor = (g) => {
  const map = { '优': '#67C23A', '良': '#409EFF', '中': '#E6A23C', '及格': '#909399', '不及格': '#F56C6C' }
  return map[g] || '#999'
}

const barStyle = (count, max) => ({
  width: '30px',
  height: Math.max(4, (count / max) * 120) + 'px',
  backgroundColor: '#409EFF',
  margin: '8px auto 0',
  borderRadius: '4px'
})

const stagePercent = (row) => {
  const total = (row.submitted || 0) + (row.approved || 0) + (row.rejected || 0)
  if (total === 0) return 0
  return Math.round((row.approved / total) * 100)
}

onMounted(async () => {
  try {
    const res = await getOverview()
    data.value = res.data
  } catch { /* 无数据 */ }
})
</script>

<style scoped>
.stats-row {
  margin-bottom: 16px;
}

.summary-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 8px 0;
}

.summary-strip div {
  padding: 16px;
  text-align: center;
  background: var(--gp-surface-soft);
  border: 1px solid var(--gp-border);
  border-radius: 8px;
}

.summary-strip strong,
.summary-strip span {
  display: block;
}

.summary-strip strong {
  color: var(--gp-primary);
  font-size: 30px;
  line-height: 1;
}

.summary-strip span {
  margin-top: 9px;
  color: var(--gp-text-muted);
  font-size: 13px;
}

.summary-strip .success { color: var(--gp-success); }
.summary-strip .warning { color: var(--gp-warning); }
.summary-strip .danger { color: var(--gp-danger); }

.score-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 184px;
  padding: 10px 0;
}

.score-item {
  min-width: 52px;
  text-align: center;
}

.score-count {
  font-size: 22px;
  font-weight: 700;
}

.score-label {
  margin-top: 6px;
  color: var(--gp-text-muted);
  font-size: 13px;
}

.bar {
  transition: height 0.5s;
}
</style>
