<template>
  <div class="workspace-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">批次管理</h1>
        <p class="page-subtitle">毕业设计批次、阶段推进和选题规则维护</p>
      </div>
      <div v-if="canManageBatch" class="page-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增批次
        </el-button>
      </div>
    </div>

    <el-card class="table-card">
      <div class="toolbar-form">
        <el-form :inline="true" :model="query">
          <el-form-item label="批次名称">
            <el-input v-model="query.name" placeholder="输入批次名称" clearable style="width:220px" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部状态" clearable style="width:140px">
              <el-option label="进行中" :value="1" />
              <el-option label="已结束" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="批次名称" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="collegeName" label="学院" />
        <el-table-column prop="majorName" label="专业" />
        <el-table-column prop="currentStage" label="当前阶段" width="120">
          <template #default="{ row }">
            <el-tag>{{ stageMap[row.currentStage] || row.currentStage }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canManageBatch || canAdvanceBatch" label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canManageBatch" type="primary" text @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="canAdvanceBatch" type="success" text @click="handleStage(row)">推进阶段</el-button>
            <el-button v-if="canManageBatch" type="danger" text @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          layout="total, prev, pager, next, sizes"
          @change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑批次' : '新增批次'" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="批次名称" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年级" prop="grade" v-if="!isEdit">
              <el-input v-model="form.grade" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="!isEdit">
          <el-col :span="12">
            <el-form-item label="所属学院" prop="collegeId">
              <el-select v-model="form.collegeId" placeholder="请选择" @change="onCollegeChange">
                <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属专业" prop="majorId">
              <el-select v-model="form.majorId" placeholder="请选择">
                <el-option v-for="m in filteredMajors" :key="m.id" :label="m.name" :value="m.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="每导师最多学生数">
              <el-input-number v-model="form.maxStudentPerTeacher" :min="1" :max="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学生可选志愿数">
              <el-input-number v-model="form.studentMaxChoices" :min="1" :max="5" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="双选模式">
              <el-select v-model="form.selectionMode">
                <el-option label="志愿制" value="voluntary" />
                <el-option label="先到先得" value="first_come" />
                <el-option label="教师选择" value="teacher_choose" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="被拒后策略">
              <el-select v-model="form.rejectStrategy">
                <el-option label="回池重新分配" value="pool" />
                <el-option label="手动处理" value="manual" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="时间节点配置">
          <div class="stage-time-list">
            <div v-for="s in stageOptionsForTime" :key="s.value" class="stage-time-row">
              <span class="stage-time-label">{{ s.label }}</span>
              <el-date-picker
                v-model="form.stageTimes[s.value].start"
                type="datetime"
                placeholder="开始时间"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
              <el-date-picker
                v-model="form.stageTimes[s.value].end"
                type="datetime"
                placeholder="结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stageVisible" title="推进阶段" width="400px">
      <el-form label-width="100px">
        <el-form-item label="当前阶段">
          <el-tag>{{ stageMap[currentBatch?.currentStage] || currentBatch?.currentStage }}</el-tag>
        </el-form-item>
        <el-form-item label="推进至">
          <el-select v-model="nextStage" placeholder="请选择下一阶段">
            <el-option v-for="s in stageOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stageVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdvanceStage">确定推进</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getBatchPage, createBatch, updateBatch, deleteBatch, advanceStage } from '@/api/batch'
import { getCollegeList } from '@/api/college'
import { getMajorList } from '@/api/major'
import { useAuthStore, hasAnyRole } from '@/stores/auth'

const authStore = useAuthStore()
const canManageBatch = computed(() => hasAnyRole(authStore.roles, ['SUPER_ADMIN', 'UNIVERSITY_ADMIN', 'COLLEGE_ADMIN', 'GRADE_ADMIN', 'MAJOR_ADMIN']))
const canAdvanceBatch = computed(() => hasAnyRole(authStore.roles, ['SUPER_ADMIN', 'UNIVERSITY_ADMIN', 'GRADE_ADMIN']))

const stageMap = {
  topic_selection: '选题阶段',
  task_book: '任务书',
  opening_report: '开题报告',
  opening_defense: '开题答辩',
  guidance: '指导记录',
  midterm: '中期检查',
  thesis_guidance: '论文指导',
  defense: '答辩',
  thesis_revision: '答辩后修改',
  thesis_final: '论文终稿',
  scoring: '成绩评定',
  score_review: '成绩审核',
  completed: '已完成'
}

const stageOptions = [
  { label: '选题阶段', value: 'topic_selection' },
  { label: '任务书', value: 'task_book' },
  { label: '开题报告', value: 'opening_report' },
  { label: '开题答辩', value: 'opening_defense' },
  { label: '指导记录', value: 'guidance' },
  { label: '中期检查', value: 'midterm' },
  { label: '论文指导', value: 'thesis_guidance' },
  { label: '答辩', value: 'defense' },
  { label: '答辩后修改', value: 'thesis_revision' },
  { label: '论文终稿', value: 'thesis_final' },
  { label: '成绩评定', value: 'scoring' },
  { label: '成绩审核', value: 'score_review' },
  { label: '已完成', value: 'completed' }
]
const stageOptionsForTime = stageOptions.filter(item => item.value !== 'completed')

const createStageTimes = () => Object.fromEntries(
  stageOptionsForTime.map(item => [item.value, { start: '', end: '' }])
)

const parseStageTimes = (configText) => {
  const stageTimes = createStageTimes()
  if (!configText) return stageTimes
  try {
    const parsed = JSON.parse(configText)
    const config = parsed.stages || parsed
    stageOptionsForTime.forEach(item => {
      stageTimes[item.value].start = config[`${item.value}_start`] || config[`${item.value}Start`] || ''
      stageTimes[item.value].end = config[`${item.value}_end`] || config[`${item.value}End`] || ''
    })
  } catch {}
  return stageTimes
}

const stringifyStageTimes = (stageTimes) => {
  const config = {}
  stageOptionsForTime.forEach(item => {
    const time = stageTimes[item.value] || {}
    if (time.start) config[`${item.value}_start`] = time.start
    if (time.end) config[`${item.value}_end`] = time.end
  })
  return JSON.stringify(config)
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = ref({ current: 1, size: 10, name: '', status: null })
const dialogVisible = ref(false)
const stageVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const editId = ref(null)
const currentBatch = ref(null)
const nextStage = ref('')
const colleges = ref([])
const allMajors = ref([])
const form = ref({
  name: '', grade: '', collegeId: null, majorId: null,
  maxStudentPerTeacher: 5, studentMaxChoices: 3,
  selectionMode: 'voluntary', allowTeacherReject: 1, rejectStrategy: 'pool', stageTimes: createStageTimes()
})
const rules = {
  name: [{ required: true, message: '请输入批次名称', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入年级', trigger: 'blur' }],
  collegeId: [{ required: true, message: '请选择学院', trigger: 'change' }],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }]
}

const filteredMajors = computed(() =>
  allMajors.value.filter(m => m.collegeId === form.value.collegeId)
)

const onCollegeChange = () => {
  form.value.majorId = null
}

const loadCollegesAndMajors = async () => {
  const [cRes, mRes] = await Promise.all([getCollegeList(), getMajorList()])
  colleges.value = cRes.data
  allMajors.value = mRes.data
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getBatchPage(query.value)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  if (!canManageBatch.value) return
  isEdit.value = false
  editId.value = null
  form.value = {
    name: '', grade: '', collegeId: null, majorId: null,
    maxStudentPerTeacher: 5, studentMaxChoices: 3,
    selectionMode: 'voluntary', allowTeacherReject: 1, rejectStrategy: 'pool', stageTimes: createStageTimes()
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  if (!canManageBatch.value) return
  isEdit.value = true
  editId.value = row.id
  form.value = {
    name: row.name,
    maxStudentPerTeacher: row.maxStudentPerTeacher,
    studentMaxChoices: row.studentMaxChoices,
    selectionMode: row.selectionMode,
    allowTeacherReject: row.allowTeacherReject,
    rejectStrategy: row.rejectStrategy,
    stageTimes: parseStageTimes(row.config)
  }
  dialogVisible.value = true
}

const handleStage = (row) => {
  if (!canAdvanceBatch.value) return
  currentBatch.value = row
  nextStage.value = ''
  stageVisible.value = true
}

const handleAdvanceStage = async () => {
  if (!nextStage.value) {
    ElMessage.warning('请选择目标阶段')
    return
  }
  await advanceStage(currentBatch.value.id, nextStage.value)
  ElMessage.success('阶段推进成功')
  stageVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  if (!canManageBatch.value) return
  await ElMessageBox.confirm('确定删除该批次吗？', '提示', { type: 'warning' })
  await deleteBatch(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  const payload = {
    ...form.value,
    config: stringifyStageTimes(form.value.stageTimes)
  }
  delete payload.stageTimes
  if (isEdit.value) {
    await updateBatch({ id: editId.value, ...payload })
    ElMessage.success('修改成功')
  } else {
    await createBatch(payload)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

onMounted(async () => {
  if (canManageBatch.value) {
    await loadCollegesAndMajors()
  }
  loadData()
})
</script>

<style scoped>
.stage-time-list {
  display: grid;
  gap: 10px;
  width: 100%;
}

.stage-time-row {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr) minmax(0, 1fr);
  gap: 10px;
  align-items: center;
}

.stage-time-label {
  color: #606266;
  font-size: 13px;
}
</style>
