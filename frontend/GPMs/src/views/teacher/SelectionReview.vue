<template>
  <div class="page">
    <h3>选题审核</h3>

    <el-form inline style="margin-bottom:16px">
      <el-form-item label="批次">
        <el-select v-model="batchId" placeholder="请选择批次" @change="onBatchChange" style="width:280px">
          <el-option v-for="b in batches" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-tabs v-model="tab" v-if="batchId">
      <el-tab-pane label="待审核志愿" name="pending">
        <el-table v-if="pendingList.length" :data="pendingList" border stripe>
          <el-table-column prop="studentName" label="学生" width="100" />
          <el-table-column prop="topicTitle" label="课题" />
          <el-table-column prop="priority" label="优先级" width="80" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="review(row, 'approve')">通过</el-button>
              <el-button type="danger" size="small" @click="openReject(row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无待审核志愿" />
      </el-tab-pane>

      <el-tab-pane label="已指导学生" name="assigned">
        <el-table v-if="assignedList.length" :data="assignedList" border stripe>
          <el-table-column prop="studentName" label="学生" width="100" />
          <el-table-column prop="topicTitle" label="课题" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="allocationTime" label="分配时间" width="160" />
        </el-table>
        <el-empty v-else description="暂无已指导的学生" />
      </el-tab-pane>
    </el-tabs>

    <el-empty v-else description="请先选择批次" />

    <!-- 拒绝弹窗 -->
    <el-dialog v-model="dialog.visible" title="拒绝申请" width="400px">
      <el-input v-model="dialog.comment" type="textarea" :rows="3" placeholder="请输入拒绝理由" />
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="danger" :loading="dialog.loading" @click="doReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getBatchPage } from '@/api/batch'
import { getReviewList, teacherReview } from '@/api/selection'
import { getStudentTopicPage } from '@/api/studentTopic'

const authStore = useAuthStore()
const batchId = ref(null)
const batches = ref([])
const pendingList = ref([])
const assignedList = ref([])
const tab = ref('pending')

const dialog = reactive({ visible: false, comment: '', loading: false, row: null })

const fetchBatches = async () => {
  const res = await getBatchPage({ current: 1, size: 50 })
  batches.value = res.data?.records || res.data || []
}

const onBatchChange = async (id) => {
  const [pRes, aRes] = await Promise.all([
    getReviewList(id),
    getStudentTopicPage({ current: 1, size: 100, advisorId: authStore.user.userId })
  ])
  pendingList.value = (pRes.data || []).filter(r => !r.teacherAction)
  assignedList.value = aRes.data?.records || aRes.data || []
}

const review = async (row, action) => {
  await teacherReview({ id: row.id, action })
  ElMessage.success(action === 'approve' ? '已通过' : '已拒绝')
  await onBatchChange(batchId.value)
}

const openReject = (row) => {
  dialog.row = row
  dialog.comment = ''
  dialog.visible = true
}

const doReject = async () => {
  dialog.loading = true
  try {
    await teacherReview({ id: dialog.row.id, action: 'reject', comment: dialog.comment })
    ElMessage.success('已拒绝')
    dialog.visible = false
    await onBatchChange(batchId.value)
  } catch {
    ElMessage.error('操作失败')
  } finally {
    dialog.loading = false
  }
}

onMounted(fetchBatches)
</script>

<style scoped>
.page h3 { margin-bottom: 20px; color: #303133; }
</style>
