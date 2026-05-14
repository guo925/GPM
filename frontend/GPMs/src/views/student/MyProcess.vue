<template>
  <div class="page">
    <h3>流程进度</h3>

    <el-empty v-if="!studentTopicId && !loading" description="请先完成选题" />

    <div v-if="studentTopicId">
      <el-alert type="info" :closable="false" style="margin-bottom:16px">
        课题：{{ myTopic?.topicTitle }} | 导师：{{ myTopic?.advisorName }}
      </el-alert>

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
              提交时间：{{ s.data.submittedAt || '-' }}
              <span v-if="s.data.version > 1"> | 版本：{{ s.data.version }}</span>
            </p>
            <p v-if="s.data.reviewComment" style="color:#E6A23C;font-size:12px">评语：{{ s.data.reviewComment }}</p>
          </div>
          <div v-else style="color:#909399;font-size:13px">尚未提交</div>

          <el-button v-if="['not_started','rejected'].includes(s.status)" type="primary" size="small"
            style="margin-top:10px" @click="openSubmit(s)">
            {{ s.status === 'rejected' ? '重新提交' : '提交' }}
          </el-button>
        </el-card>
      </div>
    </div>

    <!-- 提交弹窗 -->
    <el-dialog v-model="dialog.visible" :title="'提交 - ' + dialog.stageLabel" width="500px">
      <el-form :model="dialog.form">
        <el-form-item label="内容">
          <el-input v-model="dialog.form.content" type="textarea" :rows="5" placeholder="请输入提交内容" />
        </el-form-item>
        <el-form-item label="附件链接（可选）">
          <el-input v-model="dialog.form.filePath" placeholder="文件路径" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.loading" @click="handleSubmitStage">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyTopic } from '@/api/studentTopic'
import { getProcessList, submitProcess } from '@/api/process'

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

const myTopic = ref(null)
const loading = ref(true)
const stages = ref(STAGE_MAP.map(s => ({ ...s, status: 'not_started', data: null })))

const studentTopicId = computed(() => myTopic.value?.id)

const dialog = reactive({
  visible: false,
  stageLabel: '',
  loading: false,
  form: { studentTopicId: null, stage: '', content: '', filePath: '' }
})

const statusTag = (s) => ({ not_started: 'info', submitted: 'warning', approved: 'success', rejected: 'danger' }[s])
const statusText = (s) => ({ not_started: '未开始', submitted: '已提交', approved: '已通过', rejected: '已驳回' }[s])
const truncate = (text, n) => text && text.length > n ? text.slice(0, n) + '...' : text

const fetchData = async () => {
  try {
    const res = await getMyTopic()
    if (res.data) {
      myTopic.value = res.data
      const processRes = await getProcessList(res.data.id)
      const dataList = processRes.data || []
      stages.value = STAGE_MAP.map(s => {
        const found = dataList.find(d => d.stage === s.stage)
        return { ...s, status: found ? found.status : 'not_started', data: found || null }
      })
    }
  } finally {
    loading.value = false
  }
}

const openSubmit = (s) => {
  dialog.stageLabel = s.label
  dialog.form = { studentTopicId: studentTopicId.value, stage: s.stage, content: '', filePath: '' }
  dialog.visible = true
}

const handleSubmitStage = async () => {
  dialog.loading = true
  try {
    await submitProcess(dialog.form)
    ElMessage.success('提交成功')
    dialog.visible = false
    await fetchData()
  } catch {
    ElMessage.error('提交失败')
  } finally {
    dialog.loading = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page h3 { margin-bottom: 20px; color: #303133; }
.stage-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stage-card { min-height: 160px; }
.stage-card .stage-header { display: flex; justify-content: space-between; align-items: center; }
.stage-content { color: #606266; font-size: 13px; margin: 0 0 8px 0; }
@media (max-width: 1200px) { .stage-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
