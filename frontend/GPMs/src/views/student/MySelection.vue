<template>
  <div class="page">
    <h3>我的选题</h3>

    <!-- 已有选题结果 -->
    <el-card v-if="myTopic" class="result-card">
      <template #header>选题结果</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="课题名称">{{ myTopic.topicTitle }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ myTopic.advisorName }}</el-descriptions-item>
        <el-descriptions-item label="批次">{{ myTopic.batchName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="myTopic.status === 'active' ? 'success' : 'info'">{{ myTopic.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="分配时间" :span="2">{{ myTopic.allocationTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 未选题：提交志愿 -->
    <div v-if="!myTopic">
      <el-card style="margin-bottom:16px">
        <template #header>提交选题志愿</template>
        <el-form :model="form" label-width="80px">
          <el-form-item label="选择年级">
            <el-select v-model="form.grade" placeholder="请选择年级" @change="onGradeChange" style="width:300px">
              <el-option v-for="g in grades" :key="g" :label="g + ' 届'" :value="g" />
            </el-select>
          </el-form-item>
          <el-form-item label="选择课题">
            <el-checkbox-group v-model="form.topicIds" :max="3">
              <div v-for="t in topics" :key="t.id" style="margin-bottom:8px">
                <el-checkbox :value="t.id" :disabled="form.topicIds.length >= 3 && !form.topicIds.includes(t.id)">
                  {{ t.title }} — {{ t.creatorName }}（{{ t.currentCount }}/{{ t.maxCapacity }}人）
                </el-checkbox>
              </div>
            </el-checkbox-group>
            <div v-if="topics.length === 0 && form.grade" style="color:#909399">该年级暂无可选课题</div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :disabled="!canSubmit" :loading="submitting" @click="handleSubmit">
              {{ submitting ? '处理中' : `提交（已选 ${form.topicIds.length} 个志愿）` }}
            </el-button>
            <span style="margin-left:12px;color:#909399;font-size:13px">按勾选顺序作为优先级</span>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 已提交的志愿记录 -->
      <el-card v-if="mySelections.length > 0">
        <template #header>已提交的志愿</template>
        <el-table :data="mySelections" border stripe>
          <el-table-column prop="priority" label="优先级" width="80" />
          <el-table-column prop="topicTitle" label="课题名称" />
          <el-table-column prop="teacherAction" label="导师审核" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.teacherAction === 'approve'" type="success">已通过</el-tag>
              <el-tag v-else-if="row.teacherAction === 'reject'" type="danger">已拒绝</el-tag>
              <el-tag v-else type="info">待审核</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="teacherComment" label="导师意见" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyTopic } from '@/api/studentTopic'
import { getMySelections, submitPreferences } from '@/api/selection'
import { getDistinctGrades, getBatchesByGrade } from '@/api/batch'
import { getTopicPage } from '@/api/topic'

const myTopic = ref(null)
const mySelections = ref([])
const grades = ref([])
const topics = ref([])
const submitting = ref(false)

const form = reactive({
  grade: null,
  topicIds: []
})

const canSubmit = computed(() => form.grade && form.topicIds.length > 0)

const fetchData = async () => {
  const topicRes = await getMyTopic()
  if (topicRes.data) {
    myTopic.value = topicRes.data
    return
  }
  const gradeRes = await getDistinctGrades()
  grades.value = gradeRes.data || []
}

const onGradeChange = async (grade) => {
  form.topicIds = []
  const res = await getTopicPage({ current: 1, size: 100, grade, status: 'approved' })
  topics.value = res.data?.records || res.data || []
  const selRes = await getMySelections(grade)
  mySelections.value = selRes.data || []
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const firstTopic = topics.value.find(t => form.topicIds.includes(t.id))
    await submitPreferences({
      batchId: firstTopic?.batchId,
      topicIds: form.topicIds
    })
    ElMessage.success('选题请求已提交，正在处理中')
    form.topicIds = []
    await onGradeChange(form.grade)
    setTimeout(() => onGradeChange(form.grade), 1500)
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page h3 { margin-bottom: 20px; color: #303133; }
.result-card { margin-bottom: 16px; }
</style>
