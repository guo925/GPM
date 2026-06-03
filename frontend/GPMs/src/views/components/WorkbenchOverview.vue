<template>
  <div class="workbench-overview">
    <div class="batch-switch">
      <div>
        <div class="batch-switch__label">当前年级</div>
        <div class="batch-switch__name">{{ selectedGrade || '未选择年级' }}</div>
      </div>
      <el-select
        v-model="selectedGrade"
        placeholder="请选择年级"
        style="width: 300px"
        @change="onGradeChange"
      >
        <el-option
          v-for="grade in grades"
          :key="grade"
          :label="grade + ' 届'"
          :value="grade"
        />
      </el-select>
    </div>

    <el-card class="table-card" header="通知公告" v-loading="noticeLoading">
      <div v-if="notices.length">
        <div v-for="notice in notices" :key="notice.id" class="notice-row" :class="{ important: notice.important }">
          <div>
            <strong>{{ notice.title }}</strong>
            <span>{{ notice.content }}</span>
          </div>
          <span>{{ notice.time }}</span>
        </div>
      </div>
      <div v-else class="compact-empty">暂无公告</div>
    </el-card>

    <el-card class="table-card todo-card" header="待办事项" v-loading="todoLoading">
      <div v-if="todos.length" class="todo-grid">
        <div v-for="todo in todos" :key="todo.key" class="todo-item" @click="router.push(todo.to)">
          <el-tag :type="todo.type">{{ todo.status }}</el-tag>
          <strong>{{ todo.title }}</strong>
          <span>{{ todo.desc }}</span>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="empty-mark">?</div>
        <span>暂时没有相关的信息哦~</span>
      </div>
    </el-card>

    <el-card class="table-card" header="进度概况" v-loading="progressLoading">
      <div class="progress-line">
        <div v-for="stage in stages" :key="stage.label" class="stage">
          <div class="stage-icon" :class="{ done: stage.done, current: stage.current }">
            <el-icon><component :is="stage.icon" /></el-icon>
          </div>
          <div class="stage-label">{{ stage.label }}</div>
          <div class="stage-time">起：{{ stage.start }}</div>
          <div class="stage-time">止：{{ stage.end }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Checked, Document, EditPen, Files, Finished, Notebook, Reading } from '@element-plus/icons-vue'
import { getLatestAnnouncements } from '@/api/announcement'
import { getBatchDetail, getBatchPage, getCurrentBatch, getDistinctGrades } from '@/api/batch'
import { getFeatureItems } from '@/api/workflow'
import { getSelectedGrade, setSelectedGrade, withSelectedGradeQuery } from '@/utils/batchContext'

const router = useRouter()

const noticeLoading = ref(false)
const todoLoading = ref(false)
const progressLoading = ref(false)
const notices = ref([])
const todos = ref([])
const stages = ref([])
const grades = ref([])
const selectedGrade = ref(getSelectedGrade())

const workflowTasks = [
  { key: 'taskBook', apiBase: '/thesis/task-book', title: '任务书审核', path: '/thesis/task-book' },
  { key: 'openingReport', apiBase: '/thesis/opening-report', title: '开题报告审核', path: '/thesis/opening-report' },
  { key: 'openingDefense', apiBase: '/thesis/opening-defense', title: '开题答辩处理', path: '/thesis/opening-defense' },
  { key: 'weeklyLog', apiBase: '/thesis/weekly-log', title: '指导记录批阅', path: '/thesis/weekly-log' },
  { key: 'midterm', apiBase: '/thesis/midterm', title: '中期检查审核', path: '/thesis/midterm' },
  { key: 'postDefenseRevision', apiBase: '/thesis/post-defense-revision', title: '答辩后修改审核', path: '/thesis/post-defense-revision' },
  { key: 'advisorScore', apiBase: '/score-workflow/advisor', title: '指导导师评分', path: '/score/advisor' },
  { key: 'reviewerScore', apiBase: '/score-workflow/reviewer', title: '评阅导师评分', path: '/score/reviewer' },
  { key: 'titleChange', apiBase: '/special/title-change', title: '题目修改审核', path: '/special/title-change' },
  { key: 'extension', apiBase: '/special/extension', title: '延期申请审核', path: '/special/extension' },
  { key: 'attachmentReview', apiBase: '/special/attachment-review', title: '附件修改审核', path: '/special/attachment-review' },
  { key: 'completionEditReview', apiBase: '/special/completion-edit-review', title: '完成后修改审核', path: '/special/completion-edit-review' }
]

const stageTemplates = [
  { key: 'topic_selection', label: '选题', icon: Document, start: '2025-06-14 00:00', end: '2025-06-26 17:00' },
  { key: 'task_book', label: '任务书', icon: Checked, start: '2025-07-28 00:00', end: '2025-08-03 17:00' },
  { key: 'opening_report', label: '开题报告', icon: EditPen, start: '2025-08-04 00:00', end: '2026-06-07 17:00' },
  { key: 'midterm', label: '中期检查', icon: Files, start: '2025-12-02 00:00', end: '2026-06-07 17:00' },
  { key: 'thesis_guidance', label: '论文阶段', icon: Notebook, start: '2025-09-01 00:00', end: '2026-06-18 17:00' },
  { key: 'format_check', label: '格式检测', icon: Reading, start: '2025-07-29 00:00', end: '2026-06-21 17:00' },
  { key: 'plagiarism_check', label: '查重检测', icon: Finished, start: '2025-07-29 00:00', end: '2026-06-21 17:00' },
  { key: 'defense', label: '答辩阶段', icon: Finished, start: '2026-03-15 00:00', end: '2026-04-16 17:00' }
]

const formatTime = (value) => value ? String(value).replace('T', ' ').slice(0, 16) : ''

const loadNotices = async () => {
  noticeLoading.value = true
  try {
    const res = await getLatestAnnouncements({ size: 5 })
    notices.value = (res.data?.records || []).map((item, index) => ({
      id: item.id,
      title: item.title,
      content: item.content || '所有',
      time: formatTime(item.createdAt),
      important: index === 0
    }))
  } finally {
    noticeLoading.value = false
  }
}

const loadTodos = async () => {
  todoLoading.value = true
  try {
    const results = await Promise.all(workflowTasks.map(async task => {
      const [pendingRes, rejectedRes] = await Promise.all([
        getFeatureItems(task.apiBase, { grade: selectedGrade.value || undefined, status: 'pending' }),
        getFeatureItems(task.apiBase, { grade: selectedGrade.value || undefined, status: 'rejected' })
      ])
      const pending = pendingRes.data?.length || 0
      const rejected = rejectedRes.data?.length || 0
      return { ...task, pending, rejected }
    }))
    todos.value = results
      .filter(item => item.pending > 0 || item.rejected > 0)
      .slice(0, 6)
      .map(item => ({
        key: item.key,
        title: item.title,
        desc: `${item.pending} 条待处理，${item.rejected} 条退回待修改`,
        status: item.pending > 0 ? '待处理' : '需修改',
        type: item.pending > 0 ? 'warning' : 'danger',
        to: withSelectedGradeQuery(item.path, selectedGrade.value)
      }))
  } finally {
    todoLoading.value = false
  }
}

const parseStageConfig = (configText) => {
  if (!configText) return {}
  try {
    const parsed = JSON.parse(configText)
    return parsed.stages || parsed
  } catch {
    return {}
  }
}

const loadProgress = async () => {
  progressLoading.value = true
  try {
    const res = await getCurrentBatch()
    const currentStage = res.data?.currentStage || 'topic_selection'
    const config = parseStageConfig(res.data?.config)
    const currentIndex = stageTemplates.findIndex(item => item.key === currentStage)
    stages.value = stageTemplates.map((item, index) => ({
      ...item,
      start: config[`${item.key}_start`] || config[`${item.key}Start`] || item.start,
      end: config[`${item.key}_end`] || config[`${item.key}End`] || item.end,
      done: currentIndex >= 0 && index < currentIndex,
      current: item.key === currentStage
    }))
  } finally {
    progressLoading.value = false
  }
}

const loadGrades = async () => {
  const res = await getDistinctGrades()
  grades.value = res.data || []
  if (!selectedGrade.value && grades.value.length > 0) {
    selectedGrade.value = grades.value[0]
    setSelectedGrade(selectedGrade.value)
  }
}

const onGradeChange = async () => {
  setSelectedGrade(selectedGrade.value)
  await Promise.all([loadTodos(), loadProgress()])
}

onMounted(async () => {
  await loadGrades()
  loadNotices()
  loadTodos()
  loadProgress()
})
</script>

<style scoped>
.workbench-overview {
  display: grid;
  gap: 16px;
  margin-bottom: 16px;
}

.batch-switch {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border: 1px solid var(--gp-border);
  border-radius: 8px;
  background: #fff;
}

.batch-switch__label {
  color: var(--gp-text-muted);
  font-size: 12px;
}

.batch-switch__name {
  margin-top: 4px;
  color: var(--gp-text);
  font-size: 16px;
  font-weight: 700;
}

.notice-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 11px 12px;
  border-bottom: 1px solid var(--gp-border);
}

.notice-row.important {
  background: #fffbe6;
}

.notice-row strong,
.notice-row span {
  font-size: 13px;
}

.notice-row div span {
  margin-left: 16px;
  color: var(--gp-text-muted);
}

.todo-card :deep(.el-card__body) {
  min-height: 190px;
}

.todo-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.todo-item {
  display: grid;
  gap: 8px;
  padding: 14px;
  border: 1px solid var(--gp-border);
  border-radius: 8px;
  cursor: pointer;
  transition: border-color .18s ease, box-shadow .18s ease;
}

.todo-item:hover {
  border-color: var(--gp-primary);
  box-shadow: 0 8px 22px rgba(37, 99, 235, .12);
}

.todo-item span:last-child {
  color: var(--gp-text-muted);
  font-size: 13px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  min-height: 150px;
  color: var(--gp-text-muted);
}

.empty-mark {
  color: #9bbcf6;
  font-size: 58px;
  font-weight: 700;
}

.compact-empty {
  padding: 24px;
  color: var(--gp-text-muted);
  text-align: center;
}

.progress-line {
  display: grid;
  grid-template-columns: repeat(7, minmax(110px, 1fr));
  gap: 10px;
  overflow-x: auto;
  padding: 10px 0;
}

.stage {
  position: relative;
  min-width: 120px;
  text-align: center;
}

.stage::before {
  content: "";
  position: absolute;
  top: 24px;
  left: 0;
  right: 0;
  height: 3px;
  background: #4f86f7;
}

.stage-icon {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  color: #4f86f7;
  background: #fff;
  border: 2px solid #9bbcf6;
  border-radius: 999px;
  font-size: 24px;
}

.stage-icon.done {
  color: #fff;
  background: #4f86f7;
}

.stage-icon.current {
  color: #fff;
  background: var(--gp-warning);
  border-color: var(--gp-warning);
}

.stage-label {
  margin-top: 8px;
  color: var(--gp-text);
  font-weight: 650;
}

.stage-time {
  margin-top: 4px;
  color: var(--gp-text-muted);
  font-size: 12px;
}
</style>
