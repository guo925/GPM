<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>课题管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增课题
          </el-button>
        </div>
      </template>
      <el-form :inline="true" :model="query" style="margin-bottom:16px">
        <el-form-item label="批次">
          <el-select v-model="query.batchId" placeholder="全部" clearable style="width:200px" @change="loadData">
            <el-option v-for="b in batches" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
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
            提交志愿（已选 {{ selectedIds.length }}/3）
          </el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" border @selection-change="onSelectionChange" ref="tableRef">
        <el-table-column v-if="isStudent" type="selection" width="50" :selectable="canSelect" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="batchName" label="批次" />
        <el-table-column prop="title" label="题目名称" />
        <el-table-column prop="creatorName" label="出题人" />
        <el-table-column prop="source" label="来源" width="100">
          <template #default="{ row }">
            <el-tag :type="row.source === 'preset' ? '' : 'warning'">
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
        <el-table-column label="操作" :width="isStudent ? 300 : 220">
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
            <template v-if="!isStudent || authStore.isSuperAdmin">
              <el-button type="primary" text @click="handleEdit(row)" v-if="row.status !== 'approved'">编辑</el-button>
              <el-button type="success" text @click="handleReview(row, 'approved')" v-if="row.status === 'pending'">通过</el-button>
              <el-button type="danger" text @click="handleReview(row, 'rejected')" v-if="row.status === 'pending'">拒绝</el-button>
              <el-button type="danger" text @click="handleDelete(row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top:16px;justify-content:flex-end"
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next, sizes"
        @change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑课题' : '新增课题'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属批次" prop="batchId">
          <el-select v-model="form.batchId" placeholder="请选择批次" :disabled="isEdit">
            <el-option v-for="b in batches" :key="b.id" :label="b.name" :value="b.id" />
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
import { Plus } from '@element-plus/icons-vue'
import { getTopicPage, createTopic, updateTopic, deleteTopic, reviewTopic } from '@/api/topic'
import { getBatchPage } from '@/api/batch'
import { submitPreferences, getMySelections } from '@/api/selection'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const isStudent = computed(() => authStore.roles.includes('STUDENT'))

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const batches = ref([])
const selectedIds = ref([])
const submittedIds = ref([])
const submitting = ref(false)
const tableRef = ref(null)
const query = ref({ current: 1, size: 10, batchId: null, status: null })
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const editId = ref(null)
const form = ref({ batchId: null, title: '', description: '', source: 'preset', maxCapacity: 1 })
const rules = {
  batchId: [{ required: true, message: '请选择批次', trigger: 'change' }],
  title: [{ required: true, message: '请输入题目名称', trigger: 'blur' }],
  source: [{ required: true, message: '请选择来源', trigger: 'change' }]
}

const canSelect = (row) => {
  if (selectedIds.value.length >= 3 && !selectedIds.value.includes(row.id)) return false
  return row.status === 'approved' && !submittedIds.value.includes(row.id)
}

const onSelectionChange = (rows) => {
  selectedIds.value = rows.map(r => r.id)
}

const selectSingle = async (row) => {
  if (!query.value.batchId) {
    ElMessage.warning('请先筛选批次')
    return
  }
  if (submittedIds.value.length >= 3) {
    ElMessage.warning('最多选择3个志愿')
    return
  }
  try {
    const newIds = [...submittedIds.value, row.id]
    await submitPreferences({ batchId: query.value.batchId, topicIds: newIds })
    ElMessage.success('已选择：' + row.title)
    await loadSubmittedTopics()
  } catch {
    ElMessage.error('操作失败')
  }
}

const submitSelections = async () => {
  if (!query.value.batchId) {
    ElMessage.warning('请先选择批次')
    return
  }
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请至少选择一个课题')
    return
  }
  submitting.value = true
  try {
    await submitPreferences({ batchId: query.value.batchId, topicIds: selectedIds.value })
    ElMessage.success('志愿提交成功')
    selectedIds.value = []
    tableRef.value?.clearSelection()
    await loadSubmittedTopics()
  } catch {
    ElMessage.error('提交失败，请前往「我的选题」查看已提交志愿')
  } finally {
    submitting.value = false
  }
}

const loadSubmittedTopics = async () => {
  if (!isStudent.value || !query.value.batchId) return
  try {
    const res = await getMySelections(query.value.batchId)
    const list = res.data || []
    submittedIds.value = list.map(r => r.topicId)
  } catch { submittedIds.value = [] }
}

const loadBatches = async () => {
  const res = await getBatchPage({ current: 1, size: 100 })
  batches.value = res.data.records
}

const loadData = async () => {
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
  isEdit.value = false
  editId.value = null
  form.value = { batchId: null, title: '', description: '', source: 'preset', maxCapacity: 1 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.value = { batchId: row.batchId, title: row.title, description: row.description, source: row.source, maxCapacity: row.maxCapacity }
  dialogVisible.value = true
}

const handleReview = async (row, status) => {
  await reviewTopic({ id: row.id, status, reviewComment: status === 'approved' ? '审核通过' : '审核不通过' })
  ElMessage.success('操作成功')
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该课题吗？', '提示', { type: 'warning' })
  await deleteTopic(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (isEdit.value) {
    await updateTopic(editId.value, form.value)
    ElMessage.success('修改成功')
  } else {
    await createTopic(form.value)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

onMounted(async () => {
  await loadBatches()
  loadData()
})
</script>
