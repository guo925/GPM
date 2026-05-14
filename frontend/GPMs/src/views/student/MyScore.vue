<template>
  <div class="page">
    <h3>我的成绩</h3>

    <el-empty v-if="!studentTopicId && !loading" description="请先完成选题" />

    <div v-if="studentTopicId">
      <el-alert type="info" :closable="false" style="margin-bottom:16px">
        课题：{{ myTopic?.topicTitle }} | 导师：{{ myTopic?.advisorName }}
      </el-alert>

      <el-empty v-if="!scoreData && !loading" description="暂无成绩数据" />

      <div v-if="scoreData">
        <el-card class="total-card">
          <div class="total-score">
            <span class="score-num">{{ scoreData.finalScore ?? '-' }}</span>
            <span class="score-label">总分</span>
          </div>
          <el-tag :type="gradeTag(scoreData.gradeLevel)" size="large" style="margin-left:20px">
            {{ scoreData.gradeLevel || '未评级' }}
          </el-tag>
          <el-tag :type="scoreData.status === 'approved' ? 'success' : 'info'" size="small" style="margin-left:10px">
            {{ scoreData.status === 'approved' ? '已确认' : scoreData.status }}
          </el-tag>
        </el-card>

        <el-card v-if="scoreData.details && scoreData.details.length > 0" style="margin-top:16px">
          <template #header>分项成绩</template>
          <el-table :data="scoreData.details" border stripe>
            <el-table-column label="评分类型" width="120">
              <template #default="{ row }">
                {{ row.type === 'advisor' ? '指导教师' : row.type === 'reviewer' ? '评阅人' : row.type === 'defense' ? '答辩' : row.type }}
              </template>
            </el-table-column>
            <el-table-column prop="score" label="分数" width="100" />
            <el-table-column label="权重" width="100">
              <template #default="{ row }">{{ (row.weight * 100).toFixed(0) }}%</template>
            </el-table-column>
            <el-table-column prop="comment" label="评语" min-width="200" show-overflow-tooltip />
            <el-table-column label="盲审" width="80">
              <template #default="{ row }">{{ row.isBlind ? '是' : '否' }}</template>
            </el-table-column>
          </el-table>
        </el-card>

        <p v-if="scoreData.reviewComment" style="margin-top:12px;color:#909399">
          审核意见：{{ scoreData.reviewComment }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMyTopic } from '@/api/studentTopic'
import { getScoreDetail } from '@/api/score'

const myTopic = ref(null)
const loading = ref(true)
const scoreData = ref(null)

const studentTopicId = computed(() => myTopic.value?.id)

const gradeTag = (level) => {
  const map = { '优': 'success', '优秀': 'success', '良': '', '良好': '', '中': 'warning', '中等': 'warning', '及格': 'warning', '不及格': 'danger' }
  return map[level] || 'info'
}

const fetchData = async () => {
  try {
    const res = await getMyTopic()
    if (res.data) {
      myTopic.value = res.data
      const sRes = await getScoreDetail(res.data.id)
      if (sRes.data) scoreData.value = sRes.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page h3 { margin-bottom: 20px; color: #303133; }
.total-card { display: flex; align-items: center; padding: 30px 20px; }
.total-score { display: flex; flex-direction: column; align-items: center; }
.score-num { font-size: 48px; font-weight: bold; color: #409EFF; }
.score-label { font-size: 14px; color: #909399; margin-top: 4px; }
</style>
