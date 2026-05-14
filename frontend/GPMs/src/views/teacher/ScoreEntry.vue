<template>
  <div class="page">
    <h3>成绩评定</h3>

    <el-form inline style="margin-bottom:16px">
      <el-form-item label="选择学生">
        <el-select v-model="selectedStudentId" placeholder="请选择学生" @change="onStudentChange" style="width:280px">
          <el-option v-for="s in students" :key="s.id" :label="`${s.studentName} — ${s.topicTitle}`" :value="s.id" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-empty v-if="!selectedStudentId" description="请先选择学生" />

    <div v-if="selectedStudentId">
      <!-- 已录入成绩 -->
      <el-card v-if="existingScore" style="margin-bottom:16px">
        <template #header>已有成绩</template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="总分">{{ existingScore.finalScore ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="等级">{{ existingScore.gradeLevel || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="existingScore.status === 'approved' ? 'success' : 'info'">{{ existingScore.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-for="d in existingScore.details" :key="d.type"
            :label="d.type === 'advisor' ? '指导教师' : d.type === 'reviewer' ? '评阅人' : '答辩'">
            {{ d.score }}（权重 {{ (d.weight * 100).toFixed(0) }}%）
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 录入新成绩 -->
      <el-card>
        <template #header>{{ existingScore ? '重新评定' : '录入成绩' }}</template>
        <el-form :model="form" label-width="120px">
          <el-form-item label="指导教师评分">
            <el-input-number v-model="form.advisorScore" :min="0" :max="100" :precision="1" />
            <span style="margin-left:8px;color:#909399">权重</span>
            <el-input-number v-model="form.advisorWeight" :min="0" :max="1" :step="0.1" :precision="1" style="margin-left:8px;width:100px" />
          </el-form-item>
          <el-form-item label="评阅人评分">
            <el-input-number v-model="form.reviewerScore" :min="0" :max="100" :precision="1" />
            <span style="margin-left:8px;color:#909399">权重</span>
            <el-input-number v-model="form.reviewerWeight" :min="0" :max="1" :step="0.1" :precision="1" style="margin-left:8px;width:100px" />
          </el-form-item>
          <el-form-item label="答辩评分">
            <el-input-number v-model="form.defenseScore" :min="0" :max="100" :precision="1" />
            <span style="margin-left:8px;color:#909399">权重</span>
            <el-input-number v-model="form.defenseWeight" :min="0" :max="1" :step="0.1" :precision="1" style="margin-left:8px;width:100px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleCalculate">计算成绩</el-button>
            <el-button v-if="existingScore && existingScore.status !== 'approved'" type="success" :loading="submitting" @click="handleSubmit">
              提交成绩
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getStudentTopicPage } from '@/api/studentTopic'
import { getScoreDetail, calculateScore, submitScore } from '@/api/score'

const authStore = useAuthStore()
const students = ref([])
const selectedStudentId = ref(null)
const existingScore = ref(null)
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  advisorScore: 85, advisorWeight: 0.3,
  reviewerScore: 80, reviewerWeight: 0.3,
  defenseScore: 82, defenseWeight: 0.4
})

const fetchStudents = async () => {
  const res = await getStudentTopicPage({ current: 1, size: 100, advisorId: authStore.user.userId })
  students.value = res.data?.records || res.data || []
}

const onStudentChange = async (id) => {
  try {
    const res = await getScoreDetail(id)
    existingScore.value = res.data
  } catch {
    existingScore.value = null
  }
}

const handleCalculate = async () => {
  loading.value = true
  try {
    const res = await calculateScore({
      studentTopicId: selectedStudentId.value,
      batchId: existingScore.value?.batchId || students.value.find(s => s.id === selectedStudentId.value)?.batchId,
      ...form
    })
    ElMessage.success('成绩计算完成')
    existingScore.value = res.data
  } catch {
    ElMessage.error('计算失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    await submitScore(existingScore.value.id)
    ElMessage.success('成绩已提交')
    await onStudentChange(selectedStudentId.value)
  } catch {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchStudents)
</script>

<style scoped>
.page h3 { margin-bottom: 20px; color: #303133; }
</style>
