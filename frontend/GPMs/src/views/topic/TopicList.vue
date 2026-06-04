<template>
  <div class="workspace-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">课题管理</h1>
        <p class="page-subtitle">课题申报、审核、容量与学生志愿选择</p>
      </div>
      <div v-if="canCreateTopic" class="page-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增课题
        </el-button>
      </div>
    </div>

    <el-card class="table-card">
      <div class="toolbar-form">
        <el-form :inline="true" :model="query">
          <el-form-item label="年级">
            <el-select v-if="!isStudent" v-model="query.grade" placeholder="全部年级" clearable style="width:240px" @change="loadData">
              <el-option v-for="g in grades" :key="g" :label="g + ' 届'" :value="g" />
            </el-select>
            <el-input v-else :model-value="query.grade ? `${query.grade} 届` : '未配置年级'" disabled style="width:240px" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部状态" clearable style="width:140px">
              <el-option label="待审核" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
          </el-form-item>
          <el-form-item v-if="isStudent && selectedIds.length > 0">
            <el-button type="success" @click="submitSelections" :loading="submitting">
              提交志愿（{{ selectedIds.length }}/3）
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe @selection-change="onSelectionChange" ref="tableRef">
        <el-table-column v-if="isStudent" type="selection" width="50" :selectable="canSelect" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="batchName" label="批次" />
        <el-table-column prop="title" label="题目名称" />
        <el-table-column prop="creatorName" label="出题人" />
        <el-table-column label="附件" width="90">
          <template #default="{ row }">
            <el-link v-if="row.filePath" type="primary" :href="getFileViewUrl(row.filePath)" target="_blank">查看</el-link>
            <span v-else class="muted-text">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="100">
          <template #default="{ row }">
            <el-tag :type="row.source === 'preset' ? 'info' : 'warning'">
              {{ row.source === 'preset' ? '预设' : '自拟' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'approved' ? 'success' : row.status === 'rejected' ? 'danger' : 'warning'">
              {{ row.status === 'approved' ? '已通过' : row.status === 'rejected' ? '已拒绝' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentCount" label="已选/容量" width="100">
          <template #default="{ row }">{{ row.currentCount }}/{{ row.maxCapacity }}</template>
        </el-table-column>
        <el-table-column label="操作" :width="isStudent ? 280 : 220" fixed="right">
          <template #default="{ row }">
            <!-- 学生：选择课题按钮 -->
            <template v-if="isStudent && row.status === 'approved'">
              <el-button type="success" text size="small"
                v-if="!submittedIds.includes(row.id) && submittedIds.length < 3"
                :disabled="row.currentCount >= row.maxCapacity"
                @click="selectSingle(row)">
                {{ row.currentCount >= row.maxCapacity ? '已满' : '选择课题' }}
              </el-button>
              <el-tag v-else-if="submittedIds.includes(row.id)" type="success" size="small">已选</el-tag>
            </template>
            <!-- 管理员/教师：管理按钮 -->
            <template v-if="!isStudent">
              <el-button v-if="canEditTopic && row.status !== 'approved'" type="primary" text @click="handleEdit(row)">编辑</el-button>
              <el-button v-if="canReviewTopic && row.status === 'pending'" type="success" text @click="handleReview(row, 'approved')">通过</el-button>
              <el-button v-if="canReviewTopic && row.status === 'pending'" type="danger" text @click="handleReview(row, 'rejected')">拒绝</el-button>
              <el-button v-if="canDeleteTopic" type="danger" text @click="handleDelete(row)">删除</el-button>
            </template>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑课题' : '新增课题'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item v-if="!isEdit" label="年级" prop="grade">
          <el-select v-model="form.grade" placeholder="请选择年级" style="width:100%" @change="handleTopicGradeChange">
            <el-option v-for="g in grades" :key="g" :label="g + ' 届'" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="学院" prop="collegeId">
          <el-select v-model="form.collegeId" placeholder="请选择学院" filterable style="width:100%" @change="handleTopicCollegeChange">
            <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="专业" prop="majorId">
          <el-select v-model="form.majorId" placeholder="请选择专业" filterable style="width:100%" @change="handleTopicMajorChange">
            <el-option v-for="m in topicMajors" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属批次" prop="batchId">
          <el-select v-model="form.batchId" placeholder="请选择批次" :disabled="isEdit" style="width:100%">
            <el-option v-for="b in topicBatches" :key="b.id" :label="batchLabel(b)" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目名称" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="题目描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="题目来源" prop="source">
          <el-radio-group v-model="form.source" :disabled="isEdit">
            <el-radio value="preset">教师预设</el-radio>
            <el-radio value="student_propose">学生自拟</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="可容纳学生数">
          <el-input-number v-model="form.maxCapacity" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="课题附件">
          <div class="attachment-row">
            <el-upload
              :show-file-list="false"
              :http-request="uploadTopicFile"
              :before-upload="beforeUpload"
            >
              <el-button :loading="uploading">
                <el-icon><Upload /></el-icon>上传附件
              </el-button>
            </el-upload>
            <el-link v-if="form.filePath" type="primary" :href="getFileViewUrl(form.filePath)" target="_blank">
              查看附件
            </el-link>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import { getTopicPage, createTopic, updateTopic, deleteTopic, reviewTopic } from '@/api/topic'
import { getBatchPage, getDistinctGrades } from '@/api/batch'
import { submitPreferences, getMySelections } from '@/api/selection'
import { useAuthStore, hasAnyRole } from '@/stores/auth'
import { getSelectedGrade, setSelectedGrade } from '@/utils/batchContext'
import { getFileViewUrl, uploadFile } from '@/api/file'

const authStore = useAuthStore()
const isStudent = computed(() => authStore.roles.includes('STUDENT'))
const canCreateTopic = computed(() => hasAnyRole(authStore.roles, ['SUPER_ADMIN', 'TEACHER']))
const canEditTopic = computed(() => hasAnyRole(authStore.roles, ['SUPER_ADMIN', 'TEACHER']))
const canDeleteTopic = computed(() => hasAnyRole(authStore.roles, ['SUPER_ADMIN', 'UNIVERSITY_ADMIN', 'COLLEGE_ADMIN', 'GRADE_ADMIN', 'MAJOR_ADMIN']))
const canReviewTopic = computed(() => hasAnyRole(authStore.roles, ['SUPER_ADMIN', 'UNIVERSITY_ADMIN', 'COLLEGE_ADMIN', 'GRADE_ADMIN', 'MAJOR_ADMIN']))

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const batches = ref([])
const grades = ref([])
const selectedIds = ref([])
const submittedIds = ref([])
const submitting = ref(false)
const uploading = ref(false)
const tableRef = ref(null)
const query = ref({ current: 1, size: 10, grade: isStudent.value ? (authStore.user?.grade || '') : getSelectedGrade(), status: null })
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const editId = ref(null)
const form = ref({ grade: '', collegeId: null, majorId: null, batchId: null, title: '', description: '', source: 'preset', maxCapacity: 1, filePath: '' })
const colleges = computed(() => {
  const map = new Map()
  batches.value.forEach(batch => {
    if (batch.collegeId && !map.has(batch.collegeId)) {
      map.set(batch.collegeId, { id: batch.collegeId, name: batch.collegeName || String(batch.collegeId) })
    }
  })
  return [...map.values()]
})
const majors = computed(() => {
  const map = new Map()
  batches.value.forEach(batch => {
    if (batch.majorId && !map.has(batch.majorId)) {
      map.set(batch.majorId, { id: batch.majorId, collegeId: batch.collegeId, name: batch.majorName || String(batch.majorId) })
    }
  })
  return [...map.values()]
})
const topicMajors = computed(() => {
  if (!form.value.collegeId) return majors.value
  return majors.value.filter(m => m.collegeId === form.value.collegeId)
})
const topicBatches = computed(() => batches.value.filter(batch =>
  (!form.value.grade || batch.grade === form.value.grade)
  && (!form.value.collegeId || batch.collegeId === form.value.collegeId)
  && (!form.value.majorId || batch.majorId === form.value.majorId)
))
const rules = {
  grade: [{ required: true, message: '请选择年级', trigger: 'change' }],
  collegeId: [{ required: true, message: '请选择学院', trigger: 'change' }],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  batchId: [{ required: true, message: '请选择批次', trigger: 'change' }],
  title: [{ required: true, message: '请输入题目名称', trigger: 'blur' }],
  source: [{ required: true, message: '请选择来源', trigger: 'change' }]
}

const batchLabel = (batch) => `${batch.name}（${batch.grade} 届 / ${batch.collegeName || '-'} / ${batch.majorName || '-'}）`

const handleTopicGradeChange = () => {
  if (form.value.batchId && !topicBatches.value.some(batch => batch.id === form.value.batchId)) {
    form.value.batchId = null
  }
}

const handleTopicCollegeChange = () => {
  if (form.value.majorId && !topicMajors.value.some(major => major.id === form.value.majorId)) {
    form.value.majorId = null
  }
  form.value.batchId = null
}

const handleTopicMajorChange = () => {
  if (form.value.batchId && !topicBatches.value.some(batch => batch.id === form.value.batchId)) {
    form.value.batchId = null
  }
}

const canSelect = (row) => {
  if (selectedIds.value.length >= 3 && !selectedIds.value.includes(row.id)) return false
  return row.status === 'approved' && !submittedIds.value.includes(row.id)
}

const onSelectionChange = (rows) => {
  selectedIds.value = rows.map(r => r.id)
}

const selectSingle = async (row) => {
  if (!query.value.grade) {
    ElMessage.warning('请先筛选年级')
    return
  }
  if (submittedIds.value.length >= 3) {
    ElMessage.warning('最多选择3个志愿')
    return
  }
  try {
    const newIds = [...submittedIds.value, row.id]
    await submitPreferences({ batchId: row.batchId, topicIds: newIds })
    ElMessage.success('已选择：' + row.title)
    await loadSubmittedTopics()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const submitSelections = async () => {
  if (!query.value.grade) {
    ElMessage.warning('请先选择年级')
    return
  }
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请至少选择一个课题')
    return
  }
  submitting.value = true
  try {
    const firstSelected = tableData.value.find(r => selectedIds.value.includes(r.id))
    await submitPreferences({ batchId: firstSelected?.batchId, topicIds: selectedIds.value })
    ElMessage.success('志愿提交成功')
    selectedIds.value = []
    tableRef.value?.clearSelection()
    await loadSubmittedTopics()
  } catch (error) {
    ElMessage.error(error.message || '提交失败，请前往「我的选题」查看已提交志愿')
  } finally {
    submitting.value = false
  }
}

const loadSubmittedTopics = async () => {
  if (!isStudent.value || !query.value.grade) return
  try {
    const res = await getMySelections(query.value.grade)
    const list = res.data || []
    submittedIds.value = list.map(r => r.topicId)
  } catch { submittedIds.value = [] }
}

const loadBatches = async () => {
  const res = await getBatchPage({ current: 1, size: 100 })
  batches.value = res.data.records
}

const loadGrades = async () => {
  const res = await getDistinctGrades()
  grades.value = isStudent.value && authStore.user?.grade ? [authStore.user.grade] : (res.data || [])
  if (isStudent.value) {
    query.value.grade = authStore.user?.grade || ''
    setSelectedGrade(query.value.grade)
  }
}

const loadData = async () => {
  if (isStudent.value) {
    query.value.grade = authStore.user?.grade || ''
  }
  setSelectedGrade(query.value.grade)
  loading.value = true
  try {
    const res = await getTopicPage(query.value)
    tableData.value = res.data.records
    total.value = res.data.total
    if (isStudent.value) await loadSubmittedTopics()
  } finally {
    loading.value = false
    selectedIds.value = []
    tableRef.value?.clearSelection()
  }
}

const handleAdd = () => {
  if (!canCreateTopic.value) return
  isEdit.value = false
  editId.value = null
  form.value = { grade: query.value.grade || '', collegeId: null, majorId: null, batchId: null, title: '', description: '', source: 'preset', maxCapacity: 1, filePath: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  if (!canEditTopic.value) return
  isEdit.value = true
  editId.value = row.id
  const batch = batches.value.find(item => item.id === row.batchId)
  form.value = { grade: batch?.grade || '', collegeId: batch?.collegeId || null, majorId: batch?.majorId || null, batchId: row.batchId, title: row.title, description: row.description, source: row.source, maxCapacity: row.maxCapacity, filePath: row.filePath || '' }
  dialogVisible.value = true
}

const beforeUpload = (file) => {
  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('文件大小不能超过50MB')
    return false
  }
  return true
}

const uploadTopicFile = async ({ file }) => {
  uploading.value = true
  try {
    const res = await uploadFile(file, 'topic')
    form.value.filePath = res.data.url
    ElMessage.success('上传成功')
  } finally {
    uploading.value = false
  }
}

const handleReview = async (row, status) => {
  if (!canReviewTopic.value) return
  await reviewTopic({ id: row.id, status, reviewComment: status === 'approved' ? '审核通过' : '审核不通过' })
  ElMessage.success('操作成功')
  loadData()
}

const handleDelete = async (row) => {
  if (!canDeleteTopic.value) return
  await ElMessageBox.confirm('确定删除该课题吗？', '提示', { type: 'warning' })
  await deleteTopic(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  const payload = {
    batchId: form.value.batchId,
    title: form.value.title,
    description: form.value.description,
    source: form.value.source,
    maxCapacity: form.value.maxCapacity,
    filePath: form.value.filePath
  }
  if (isEdit.value) {
    await updateTopic(editId.value, payload)
    ElMessage.success('修改成功')
  } else {
    await createTopic(payload)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

onMounted(async () => {
  await loadGrades()
  await loadBatches()
  loadData()
})
</script>

<style scoped>
.attachment-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.muted-text {
  color: #909399;
}
</style>
