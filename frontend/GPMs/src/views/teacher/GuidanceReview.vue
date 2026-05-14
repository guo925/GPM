<template>
  <div class="page">
    <h3>指导记录</h3>

    <el-form inline style="margin-bottom:16px">
      <el-form-item label="选择学生">
        <el-select v-model="selectedStudentId" placeholder="请选择学生" @change="onStudentChange" style="width:280px">
          <el-option v-for="s in students" :key="s.id" :label="`${s.studentName} — ${s.topicTitle}`" :value="s.id" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-empty v-if="!selectedStudentId" description="请先选择学生" />

    <div v-if="selectedStudentId">
      <el-table v-if="records.length" :data="records" border stripe>
        <el-table-column prop="weekNumber" label="周次" width="80" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="提交时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'reviewed' ? 'success' : 'info'">
              {{ row.status === 'reviewed' ? '已批阅' : '已提交' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.status !== 'reviewed'" type="primary" size="small" @click="openReview(row)">
              批阅
            </el-button>
            <span v-else style="color:#909399;font-size:13px">{{ row.advisorComment || '无评语' }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="该学生暂无指导记录" />
    </div>

    <!-- 批阅弹窗 -->
    <el-dialog v-model="dialog.visible" title="批阅指导记录" width="450px">
      <p style="color:#606266;margin-bottom:12px">{{ dialog.row?.content }}</p>
      <el-input v-model="dialog.comment" type="textarea" :rows="3" placeholder="请输入批阅意见" />
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.loading" @click="doReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getStudentTopicPage } from '@/api/studentTopic'
import { getGuidanceList, reviewGuidance } from '@/api/guidance'

const authStore = useAuthStore()
const students = ref([])
const selectedStudentId = ref(null)
const records = ref([])

const dialog = reactive({ visible: false, comment: '', loading: false, row: null })

const fetchStudents = async () => {
  const res = await getStudentTopicPage({ current: 1, size: 100, advisorId: authStore.user.userId })
  students.value = res.data?.records || res.data || []
}

const onStudentChange = async (id) => {
  const res = await getGuidanceList(id)
  records.value = res.data || []
}

const openReview = (row) => {
  dialog.row = row
  dialog.comment = ''
  dialog.visible = true
}

const doReview = async () => {
  if (!dialog.comment.trim()) {
    ElMessage.warning('请输入评语')
    return
  }
  dialog.loading = true
  try {
    await reviewGuidance(dialog.row.id, dialog.comment)
    ElMessage.success('批阅成功')
    dialog.visible = false
    await onStudentChange(selectedStudentId.value)
  } catch {
    ElMessage.error('批阅失败')
  } finally {
    dialog.loading = false
  }
}

onMounted(fetchStudents)
</script>

<style scoped>
.page h3 { margin-bottom: 20px; color: #303133; }
</style>
