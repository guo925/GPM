<template>
  <div class="page">
    <h3>数据统计</h3>

    <!-- 概览卡片 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="4" v-for="c in overviewCards" :key="c.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-num">{{ c.value }}</div>
          <div class="stat-label">{{ c.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 选题统计 -->
    <el-row :gutter="16" style="margin-bottom:16px" v-if="data">
      <el-col :span="12">
        <el-card header="课题与选题">
          <div style="display:flex;justify-content:space-around;padding:20px 0">
            <div style="text-align:center">
              <div style="font-size:36px;color:#409EFF;font-weight:bold">{{ data.totalTopics }}</div>
              <div style="color:#909399;margin-top:4px">课题总数</div>
            </div>
            <div style="text-align:center">
              <div style="font-size:36px;color:#67C23A;font-weight:bold">{{ data.approvedTopics }}</div>
              <div style="color:#909399;margin-top:4px">已通过</div>
            </div>
            <div style="text-align:center">
              <div style="font-size:36px;color:#E6A23C;font-weight:bold">{{ data.selectedStudents }}</div>
              <div style="color:#909399;margin-top:4px">已选题</div>
            </div>
            <div style="text-align:center">
              <div style="font-size:36px;color:#F56C6C;font-weight:bold">{{ data.unselectedStudents }}</div>
              <div style="color:#909399;margin-top:4px">未选题</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 成绩分布 -->
      <el-col :span="12">
        <el-card header="成绩分布" v-if="data.scoreDistribution">
          <div style="display:flex;justify-content:space-around;align-items:flex-end;height:180px;padding:10px 0">
            <div v-for="g in data.scoreDistribution" :key="g.grade" style="text-align:center">
              <div style="font-size:24px;font-weight:bold" :style="{ color: gradeColor(g.grade) }">{{ g.count }}</div>
              <div :style="barStyle(g.count, maxScoreCount)" class="bar"></div>
              <div style="color:#909399;margin-top:4px">{{ g.grade }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 阶段进度 -->
    <el-card header="各阶段完成情况" v-if="data && data.stageStats">
      <el-table :data="data.stageStats" border stripe>
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
.page h3 { margin-bottom: 20px; color: #303133; }
.stat-card { text-align: center; padding: 10px 0; }
.stat-num { font-size: 32px; font-weight: bold; color: #409EFF; }
.stat-label { color: #909399; margin-top: 6px; font-size: 14px; }
.bar { transition: height 0.5s; }
</style>
