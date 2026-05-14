<template>
  <div class="page">
    <h3>流程审核</h3>

    <el-form inline style="margin-bottom:16px">
      <el-form-item label="选择学生">
        <el-select v-model="selectedStudentId" placeholder="请选择学生" @change="onStudentChange" style="width:280px">
          <el-option v-for="s in students" :key="s.id" :label="`${s.studentName} — ${s.topicTitle}`" :value="s.id" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-empty v-if="!selectedStudentId" description="请先选择学生" />

    <div v-if="selectedStudentId">
      <div class="stage-grid">
        <el-card v-for="s in stages" :key="s.stage" :class="['stage-card', s.status]">
          <template #header>
            <div class="stage-header">
              <span>{{ s.label }}</span>
              <el-tag :type="statusTag(s.status)" size="small">{{ statusText(s.status) }}</el-tag>
            </div>
          </template>
          <div v-if="s.data">
            <p v-if="s.data.content" class="stage-content">{{ truncate(s.data.content, 100) }}</p>
            <p style="color:#909399;font-size:12px">
              提交时间：{{ s.data.submittedAt || '-' }} | 版本：{{ s.data.version || 1 }}
            </p>
          </div>
          <div v-else style="color:#909399;font-size:13px">学生尚未提交</div>

          <el-button v-if="s.status === 'submitted'" type="success" size="small" style="margin-top:10px" @click="review(s, 'approved')">
            通过
          </el-button>
          <el-button v-if="s.status === 'submitted'" type="danger" size="small" style="margin-top:10px" @click="openReject(s)">
            驳回
          </el-button>
        </el-card>
      </div>
    </div>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="dialog.visible" title="驳回意见" width="400px">
      <el-input v-model="dialog.comment" type="textarea" :rows="3" placeholder="请输入驳回理由" />
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="danger" :loading="dialog.loading" @click="doReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getStudentTopicPage } from '@/api/studentTopic'
import { getProcessList, reviewProcess } from '@/api/process'

const STAGE_MAP = [
  { stage: 'task_book', label: '任务书' },
  { stage: 'opening_report', label: '开题报告' },
  { stage: 'opening_defense', label: '开题答辩' },
  { stage: 'guidance_week', label: '指导周记' },
  { stage: 'midterm_check', label: '中期检查' },
  { stage: 'thesis_draft', label: '论文初稿' },
  { stage: 'thesis_final', label: '论文终稿' },
  { stage: 'post_defense_modify', label: '答辩后修改' }
]

const authStore = useAuthStore()
const students = ref([])
const selectedStudentId = ref(null)
const stages = ref([])

const dialog = reactive({ visible: false, comment: '', loading: false, stage: null })

const statusTag = (s) => ({ not_started: 'info', submitted: 'warning', approved: 'success', rejected: 'danger' }[s])
const statusText = (s) => ({ not_started: '未开始', submitted: '待审核', approved: '已通过', rejected: '已驳回' }[s])
const truncate = (text, n) => text && text.length > n ? text.slice(0, n) + '...' : text

const fetchStudents = async () => {
  const res = await getStudentTopicPage({ current: 1, size: 100, advisorId: authStore.user.userId })
  students.value = res.data?.records || res.data || []
}

const onStudentChange = async (id) => {
  const res = await getProcessList(id)
  const dataList = res.data || []
  stages.value = STAGE_MAP.map(s => {
    const found = dataList.find(d => d.stage === s.stage)
    return { ...s, status: found ? found.status : 'not_started', data: found || null, id: found?.id }
  })
}

const review = async (s, status) => {
  try {
    await reviewProcess({ id: s.data.id, status })
    ElMessage.success(status === 'approved' ? '已通过' : '已驳回')
    await onStudentChange(selectedStudentId.value)
  } catch {
    ElMessage.error('操作失败')
  }
}

const openReject = (s) => {
  dialog.stage = s
  dialog.comment = ''
  dialog.visible = true
}

const doReject = async () => {
  dialog.loading = true
  try {
    await reviewProcess({ id: dialog.stage.data.id, status: 'rejected', reviewComment: dialog.comment })
    ElMessage.success('已驳回')
    dialog.visible = false
    await onStudentChange(selectedStudentId.value)
  } catch {
    ElMessage.error('操作失败')
  } finally {
    dialog.loading = false
  }
}

onMounted(fetchStudents)
</script>

<style scoped>
.page h3 { margin-bottom: 20px; color: #303133; }
.stage-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stage-card { min-height: 160px; }
.stage-card .stage-header { display: flex; justify-content: space-between; align-items: center; }
.stage-content { color: #606266; font-size: 13px; margin: 0 0 8px 0; }
@media (max-width: 1200px) { .stage-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
