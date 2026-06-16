<template>
  <div class="workspace-page workflow-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ config.title }}</h1>
        <p class="page-subtitle">{{ config.subtitle }}</p>
      </div>
      <div class="page-actions">
        <el-button v-if="config.allowCreate" type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon>{{ config.createText || '新增' }}
        </el-button>
        <el-button v-if="config.allowExport" @click="exportRows">
          <el-icon><Download /></el-icon>导出
        </el-button>
      </div>
    </div>

    <div class="summary-grid">
      <el-card v-for="item in summary" :key="item.label" class="metric-card">
        <div class="summary-value">{{ item.value }}</div>
        <div class="summary-label">{{ item.label }}</div>
      </el-card>
    </div>

    <el-card class="table-card">
      <div class="toolbar-form">
        <el-form :inline="true" :model="query">
          <el-form-item label="关键词">
            <el-input v-model="query.keyword" clearable placeholder="学生、题目、导师" style="width:220px" />
          </el-form-item>
          <el-form-item label="年级">
            <el-select v-if="!isStudent" v-model="query.grade" placeholder="全部年级" clearable style="width:240px" @change="onGradeChange">
              <el-option v-for="g in grades" :key="g" :label="g + ' 届'" :value="g" />
            </el-select>
            <el-input v-else :model-value="query.grade ? `${query.grade} 届` : '未配置年级'" disabled style="width:240px" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" clearable placeholder="全部状态" style="width:140px">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadRows">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="filteredRows" stripe>
        <el-table-column label="业务类型" width="130">
          <template #default>
            <el-tag effect="plain">{{ config.categoryLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="studentNo" label="学号" width="130" />
        <el-table-column prop="title" :label="config.itemLabel || '论文/事项'" min-width="220" />
        <el-table-column v-if="config.extraField" prop="extra" :label="config.extraField.label" min-width="150">
          <template #default="{ row }">
            <el-link v-if="config.extraField.upload && row.extra" type="primary" :href="getFileViewUrl(row.extra)" target="_blank">查看</el-link>
            <span v-else>{{ row.extra || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="advisorName" label="指导教师" width="120" />
        <el-table-column prop="updatedAt" label="更新时间" width="170" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="config.showScore" prop="score" :label="config.scoreLabel || '分数'" width="100" />
        <el-table-column label="操作" :width="config.actionWidth || 300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text @click="openDetail(row)">查看</el-button>
            <el-button v-if="config.allowScore" type="success" text @click="openScore(row)">评分</el-button>
            <el-button v-if="canApprove" type="success" text @click="approve(row)">{{ config.approveText || '通过' }}</el-button>
            <el-button v-if="canApprove" type="warning" text @click="openReject(row)">退回</el-button>
            <el-button v-if="config.allowEdit" type="primary" text @click="openEdit(row)">修改</el-button>
            <el-button type="danger" text @click="removeRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <span>共 {{ filteredRows.length }} 条</span>
      </div>
    </el-card>

    <el-dialog v-model="formDialog.visible" :title="formDialog.title" width="640px">
      <el-form :model="formDialog.form" label-width="96px">
        <el-form-item label="学生姓名">
          <el-input v-model="formDialog.form.studentName" />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="formDialog.form.studentNo" />
        </el-form-item>
        <el-form-item label="指导教师">
          <el-input v-model="formDialog.form.advisorName" />
        </el-form-item>
        <el-form-item :label="config.itemLabel || '事项标题'">
          <el-input v-model="formDialog.form.title" />
        </el-form-item>
        <el-form-item v-if="config.showScore" :label="config.scoreLabel || '分数'">
          <el-input-number v-model="formDialog.form.score" :min="0" :max="100" :precision="1" />
        </el-form-item>
        <el-form-item v-if="config.extraField" :label="config.extraField.label">
          <template v-if="config.extraField.upload">
            <div class="attachment-row">
              <el-upload
                :show-file-list="false"
                :http-request="uploadWorkflowFile"
                :before-upload="beforeUpload"
              >
                <el-button :loading="formDialog.uploading">
                  <el-icon><Upload /></el-icon>上传附件
                </el-button>
              </el-upload>
              <el-link v-if="formDialog.form.extra" type="primary" :href="getFileViewUrl(formDialog.form.extra)" target="_blank">
                查看附件
              </el-link>
            </div>
          </template>
          <el-input v-else v-model="formDialog.form.extra" :type="config.extraField.type || 'text'" :rows="3" />
        </el-form-item>
        <el-form-item :label="config.remarkLabel || '说明'">
          <el-input v-model="formDialog.form.remark" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialog.visible" :title="reviewDialog.title" width="560px">
      <el-form :model="reviewDialog.form" label-width="88px">
        <el-form-item v-if="reviewDialog.mode === 'score'" label="分数">
          <el-input-number v-model="reviewDialog.form.score" :min="0" :max="100" :precision="1" />
        </el-form-item>
        <el-form-item label="意见">
          <el-input v-model="reviewDialog.form.comment" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveReview">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialog.visible" title="详情" width="680px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生">{{ detailDialog.row.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailDialog.row.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ detailDialog.row.advisorName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(detailDialog.row.status) }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ detailDialog.row.title }}</el-descriptions-item>
        <el-descriptions-item v-if="config.extraField" :label="config.extraField.label" :span="2">
          <el-link v-if="config.extraField.upload && detailDialog.row.extra" type="primary" :href="getFileViewUrl(detailDialog.row.extra)" target="_blank">
            查看附件
          </el-link>
          <span v-else>{{ detailDialog.row.extra || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailDialog.row.score !== null" label="分数">{{ detailDialog.row.score }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailDialog.row.updatedAt }}</el-descriptions-item>
        <el-descriptions-item label="说明" :span="2">{{ detailDialog.row.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ detailDialog.row.comment || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Plus, Upload } from '@element-plus/icons-vue'
import { getBatchPage, getDistinctGrades } from '@/api/batch'
import { getFileViewUrl, uploadFile } from '@/api/file'
import { deleteFeatureItem, getFeatureItems, reviewFeatureItem, saveFeatureItem } from '@/api/workflow'
import { getSelectedGrade, setSelectedGrade } from '@/utils/batchContext'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()
const isStudent = computed(() => authStore.roles.includes('STUDENT'))
const canApprove = computed(() => config.value.allowApprove && !isStudent.value)

const baseStatuses = [
  { label: '待处理', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已退回', value: 'rejected' },
  { label: '草稿', value: 'draft' }
]

const scoreStatuses = [
  { label: '待评分', value: 'pending' },
  { label: '已评分', value: 'approved' },
  { label: '退回调整', value: 'rejected' },
  { label: '草稿', value: 'draft' }
]

const specialStatuses = [
  { label: '待审批', value: 'pending' },
  { label: '已同意', value: 'approved' },
  { label: '已驳回', value: 'rejected' },
  { label: '草稿', value: 'draft' }
]

const workflowConfigs = {
  taskBook: { apiBase: '/thesis/task-book', category: 'thesis', categoryLabel: '论文流程', title: '任务书', subtitle: '任务书提交、审核与退回处理', allowCreate: true, allowApprove: true, allowEdit: true, allowExport: true, itemLabel: '任务书题目', extraField: { label: '附件路径', upload: true }, remarkLabel: '任务要求' },
  openingReport: { apiBase: '/thesis/opening-report', category: 'thesis', categoryLabel: '论文流程', title: '开题报告', subtitle: '开题报告材料提交、查看和审核', allowCreate: true, allowApprove: true, allowEdit: true, allowExport: true, itemLabel: '开题报告', extraField: { label: '报告附件', upload: true }, remarkLabel: '研究内容' },
  openingDefense: { apiBase: '/thesis/opening-defense', category: 'thesis', categoryLabel: '答辩流程', title: '开题答辩', subtitle: '开题答辩安排、记录和结论维护', allowCreate: true, allowApprove: true, allowExport: true, itemLabel: '答辩题目', extraField: { label: '答辩地点' }, remarkLabel: '答辩记录' },
  openingMinutes: { apiBase: '/thesis/opening-minutes', category: 'thesis', categoryLabel: '会议纪要', title: '开题报告会议纪要', subtitle: '开题报告会议纪要上传、确认和归档', allowCreate: true, allowApprove: true, allowEdit: true, itemLabel: '会议主题', extraField: { label: '会议纪要', type: 'textarea' }, remarkLabel: '参会与决议' },
  weeklyLog: { apiBase: '/thesis/weekly-log', category: 'thesis', categoryLabel: '指导记录', title: '指导记录周记', subtitle: '学生周记查看、指导教师批阅和反馈', allowCreate: true, allowApprove: true, allowEdit: true, itemLabel: '周记标题', extraField: { label: '周记内容', type: 'textarea' }, remarkLabel: '导师反馈' },
  midterm: { apiBase: '/thesis/midterm', category: 'thesis', categoryLabel: '过程检查', title: '中期检查', subtitle: '中期检查材料审核、整改意见和结果维护', allowCreate: true, allowApprove: true, allowEdit: true, allowExport: true, itemLabel: '检查事项', extraField: { label: '检查材料', upload: true }, remarkLabel: '整改意见' },
  thesisGuidance: { apiBase: '/thesis/guidance', category: 'thesis', categoryLabel: '论文指导', title: '论文指导', subtitle: '论文指导记录、修改意见和阶段反馈', allowCreate: true, allowApprove: true, allowEdit: true, itemLabel: '指导主题', extraField: { label: '指导内容', type: 'textarea' }, remarkLabel: '修改建议' },
  postDefenseRevision: { apiBase: '/thesis/post-defense-revision', category: 'thesis', categoryLabel: '修改审核', title: '答辩后论文修改审核', subtitle: '答辩后修改稿提交、审核和意见退回', allowCreate: true, allowApprove: true, allowEdit: true, allowExport: true, itemLabel: '修改稿标题', extraField: { label: '修改稿附件', upload: true }, remarkLabel: '修改说明' },
  finalThesis: { apiBase: '/thesis/final-thesis', category: 'thesis', categoryLabel: '终稿查看', title: '查看论文终稿', subtitle: '论文终稿查看、确认和导出', allowApprove: true, allowExport: true, itemLabel: '论文终稿', extraField: { label: '终稿附件', upload: true }, remarkLabel: '归档说明', approveText: '确认' },
  finalDesign: { apiBase: '/thesis/final-design', category: 'thesis', categoryLabel: '终稿查看', title: '查看设计终稿', subtitle: '设计类终稿查看、确认和导出', allowApprove: true, allowExport: true, itemLabel: '设计终稿', extraField: { label: '设计附件', upload: true }, remarkLabel: '归档说明', approveText: '确认' },
  advisorScore: { apiBase: '/score-workflow/advisor', category: 'score', categoryLabel: '成绩评定', title: '指导导师评分', subtitle: '指导教师评分录入、修改和提交', allowScore: true, allowExport: true, showScore: true, itemLabel: '论文题目', extraField: { label: '评分项' }, scoreLabel: '指导分', remarkLabel: '评分说明' },
  reviewerScore: { apiBase: '/score-workflow/reviewer', category: 'score', categoryLabel: '成绩评定', title: '评阅导师评分', subtitle: '评阅教师评分录入、复核和提交', allowScore: true, allowExport: true, showScore: true, itemLabel: '论文题目', extraField: { label: '评阅意见' }, scoreLabel: '评阅分', remarkLabel: '评分说明' },
  deputyReview: { apiBase: '/score-workflow/deputy-review', category: 'score', categoryLabel: '成绩审核', title: '论文副院长审核', subtitle: '学院层面论文成绩和材料审核', allowApprove: true, allowExport: true, showScore: true, itemLabel: '论文题目', extraField: { label: '审核批次' }, scoreLabel: '综合分', remarkLabel: '审核意见', approveText: '审核通过' },
  scoreHistory: { apiBase: '/score-workflow/history', category: 'score', categoryLabel: '历史成绩', title: '历史评分', subtitle: '历次评分记录、意见和状态追踪', allowExport: true, showScore: true, itemLabel: '评分记录', extraField: { label: '评分人' }, scoreLabel: '历史分', remarkLabel: '历史意见' },
  titleChange: { apiBase: '/special/title-change', category: 'special', categoryLabel: '特殊申请', title: '论文修改题目审核', subtitle: '论文题目变更申请、审核和退回', allowCreate: true, allowApprove: true, allowEdit: true, itemLabel: '新题目', extraField: { label: '原题目' }, remarkLabel: '修改原因', approveText: '同意' },
  extension: { apiBase: '/special/extension', category: 'special', categoryLabel: '特殊申请', title: '申请延期', subtitle: '延期申请提交、审批和处理意见维护', allowCreate: true, allowApprove: true, allowEdit: true, createText: '发起延期申请', itemLabel: '延期事项', extraField: { label: '延期至' }, remarkLabel: '延期原因', approveText: '同意' },
  specialAdvisorScore: { apiBase: '/special/advisor-score', category: 'score', categoryLabel: '补充评分', title: '指导老师评分', subtitle: '特殊情况下指导教师补评分和调整', allowScore: true, allowApprove: true, allowExport: true, showScore: true, itemLabel: '论文题目', extraField: { label: '调整原因' }, scoreLabel: '调整分', remarkLabel: '处理说明' },
  attachmentReview: { apiBase: '/special/attachment-review', category: 'special', categoryLabel: '附件审核', title: '附件修改审核', subtitle: '附件补传、替换和修改审核', allowCreate: true, allowApprove: true, allowEdit: true, itemLabel: '附件名称', extraField: { label: '附件类型' }, remarkLabel: '修改说明', approveText: '同意' },
  completionEditReview: { apiBase: '/special/completion-edit-review', category: 'special', categoryLabel: '完成后修改', title: '流程完成后修改审核', subtitle: '流程完成后材料修改申请和审批', allowCreate: true, allowApprove: true, allowEdit: true, itemLabel: '修改事项', extraField: { label: '所属流程' }, remarkLabel: '申请说明', approveText: '同意' }
}

const config = computed(() => workflowConfigs[route.meta.workflowType] || workflowConfigs.taskBook)
const workflowType = computed(() => route.meta.workflowType || 'taskBook')
const statusOptions = computed(() => {
  if (config.value.category === 'score') return scoreStatuses
  if (config.value.category === 'special') return specialStatuses
  return baseStatuses
})

const batches = ref([])
const grades = ref([])
const routeGrade = () => isStudent.value ? (authStore.user?.grade || '') : (route.query.grade || getSelectedGrade())
const query = reactive({ keyword: '', status: '', grade: routeGrade() })
const state = reactive({ rows: [] })
const formDialog = reactive({ visible: false, mode: 'create', title: '新增', uploading: false, form: {} })
const reviewDialog = reactive({ visible: false, mode: 'approve', title: '审核', row: null, form: { score: 80, comment: '' } })
const detailDialog = reactive({ visible: false, row: {} })

const formatTime = (value) => value ? String(value).replace('T', ' ').slice(0, 19) : ''

const loadRows = async () => {
  const res = await getFeatureItems(config.value.apiBase, {
    grade: query.grade || undefined,
    workflowType: workflowType.value,
    keyword: query.keyword || undefined,
    status: query.status || undefined
  })
  state.rows = (res.data || []).map(row => ({
    ...row,
    updatedAt: formatTime(row.updatedAt || row.createdAt)
  }))
}

watch(() => route.meta.workflowType, () => {
  query.keyword = ''
  query.status = ''
  query.grade = routeGrade()
  loadRows()
}, { immediate: true })

watch(() => route.query.grade, () => {
  query.grade = routeGrade()
  loadRows()
})

const filteredRows = computed(() => state.rows.filter(row => {
  const keyword = query.keyword.trim()
  const matchesKeyword = !keyword || [row.studentName, row.studentNo, row.title, row.advisorName].some(value => String(value || '').includes(keyword))
  const matchesStatus = !query.status || row.status === query.status
  return matchesKeyword && matchesStatus
}))

const summary = computed(() => [
  { label: '全部', value: state.rows.length },
  { label: statusOptions.value[0].label, value: state.rows.filter(row => row.status === 'pending').length },
  { label: statusOptions.value[1].label, value: state.rows.filter(row => row.status === 'approved').length },
  { label: statusOptions.value[2].label, value: state.rows.filter(row => row.status === 'rejected').length }
])

const statusLabel = (value) => statusOptions.value.find(item => item.value === value)?.label || value
const statusType = (value) => ({ pending: 'warning', approved: 'success', rejected: 'danger', draft: 'info' }[value] || 'info')
const resetQuery = () => {
  query.keyword = ''
  query.status = ''
  loadRows()
}

const onGradeChange = () => {
  if (isStudent.value) {
    query.grade = authStore.user?.grade || ''
  }
  setSelectedGrade(query.grade)
  loadRows()
}

const emptyForm = () => ({
  batchId: null,
  studentName: '',
  studentNo: '',
  advisorName: '',
  title: '',
  extra: '',
  remark: '',
  status: 'pending',
  score: config.value.showScore ? 0 : null
})

const openCreate = () => {
  formDialog.mode = 'create'
  formDialog.title = config.value.createText || `新增${config.value.title}`
  formDialog.uploading = false
  formDialog.form = emptyForm()
  formDialog.visible = true
}

const openEdit = (row) => {
  formDialog.mode = 'edit'
  formDialog.title = `修改${config.value.title}`
  formDialog.uploading = false
  formDialog.form = { ...row }
  formDialog.visible = true
}

const saveForm = async () => {
  if (formDialog.uploading) {
    ElMessage.warning('附件正在上传，请稍候')
    return
  }
  const form = { ...formDialog.form }
  if (!form.title || (!isStudent.value && (!form.studentName || !form.studentNo))) {
    ElMessage.warning(isStudent.value ? '请填写标题' : '请填写学生、学号和标题')
    return
  }
  await saveFeatureItem(config.value.apiBase, {
    ...form,
    batchId: form.batchId || undefined,
    grade: query.grade || undefined,
    workflowType: workflowType.value,
    status: form.status || 'pending'
  })
  formDialog.visible = false
  ElMessage.success('保存成功')
  await loadRows()
}

const beforeUpload = (file) => {
  const maxSize = 200 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('文件大小不能超过200MB')
    return false
  }
  return true
}

const uploadWorkflowFile = async ({ file }) => {
  formDialog.uploading = true
  try {
    const res = await uploadFile(file, 'thesis')
    formDialog.form.extra = res.data.url
    ElMessage.success('上传成功')
  } finally {
    formDialog.uploading = false
  }
}

const openDetail = (row) => {
  detailDialog.row = { ...row }
  detailDialog.visible = true
}

const approve = async (row) => {
  await reviewFeatureItem(config.value.apiBase, { id: row.id, status: 'approved', score: row.score, comment: row.comment || '审核通过' })
  ElMessage.success('已通过')
  await loadRows()
}

const openReject = (row) => {
  reviewDialog.mode = 'reject'
  reviewDialog.title = '退回意见'
  reviewDialog.row = row
  reviewDialog.form = { score: row.score || 0, comment: row.comment || '' }
  reviewDialog.visible = true
}

const openScore = (row) => {
  reviewDialog.mode = 'score'
  reviewDialog.title = '评分'
  reviewDialog.row = row
  reviewDialog.form = { score: row.score || 80, comment: row.comment || '' }
  reviewDialog.visible = true
}

const saveReview = async () => {
  const row = reviewDialog.row
  if (!row) return
  const payload = {
    id: row.id,
    status: reviewDialog.mode === 'score' ? 'approved' : 'rejected',
    score: reviewDialog.mode === 'score' ? reviewDialog.form.score : row.score,
    comment: reviewDialog.form.comment || (reviewDialog.mode === 'score' ? '评分已提交' : '退回修改')
  }
  await reviewFeatureItem(config.value.apiBase, payload)
  reviewDialog.visible = false
  ElMessage.success('处理成功')
  await loadRows()
}

const removeRow = async (row) => {
  await ElMessageBox.confirm(`确认删除 ${row.studentName} 的记录？`, '删除确认', { type: 'warning' })
  await deleteFeatureItem(config.value.apiBase, row.id)
  ElMessage.success('删除成功')
  await loadRows()
}

const exportRows = () => {
  if (!filteredRows.value.length) {
    ElMessage.warning('暂无可导出的数据')
    return
  }
  const escapeCsv = (value) => `"${String(value ?? '').replaceAll('"', '""')}"`
  const header = ['学号', '学生', '标题', '状态', '分数']
  const body = filteredRows.value.map(row => [
    row.studentNo,
    row.studentName,
    row.title,
    statusLabel(row.status),
    row.score ?? ''
  ].map(escapeCsv).join(','))
  const csv = [header.map(escapeCsv).join(','), ...body].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${config.value.title}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

onMounted(async () => {
  const [gradeRes, batchRes] = await Promise.all([
    getDistinctGrades(),
    getBatchPage({ current: 1, size: 100 })
  ])
  grades.value = isStudent.value && authStore.user?.grade ? [authStore.user.grade] : (gradeRes.data || [])
  batches.value = batchRes.data?.records || []
  if (isStudent.value && query.grade) {
    setSelectedGrade(query.grade)
  }
  if (!query.grade && grades.value.length > 0) {
    query.grade = grades.value[0]
    setSelectedGrade(query.grade)
    await loadRows()
  }
})
</script>

<style scoped>
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.summary-value {
  color: var(--gp-text);
  font-size: 24px;
  font-weight: 700;
}

.summary-label {
  margin-top: 6px;
  color: var(--gp-text-muted);
  font-size: 13px;
}

.attachment-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

@media (max-width: 900px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
