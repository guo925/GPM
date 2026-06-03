<template>
  <div class="workspace-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">归档管理</h1>
        <p class="page-subtitle">历史批次冷热数据归档和归档记录</p>
      </div>
      <el-button type="warning" @click="archiveAll">归档所有历史批次</el-button>
    </div>

    <el-card class="work-card">
      <el-form inline class="filter-form">
        <el-form-item label="年级">
          <el-input v-model="grade" placeholder="指定年级" style="width:160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="archiveOne">归档指定批次</el-button>
          <el-button @click="fetchLogs">刷新日志</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logs" stripe>
        <el-table-column prop="created_at" label="时间" width="180" />
        <el-table-column prop="batch_id" label="批次ID" width="100" />
        <el-table-column prop="operator_id" label="操作人" width="100" />
        <el-table-column prop="topic_count" label="课题" width="80" />
        <el-table-column prop="selection_count" label="选题" width="80" />
        <el-table-column prop="score_count" label="成绩" width="80" />
        <el-table-column prop="weekly_log_count" label="周记" width="80" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="message" label="说明" min-width="200" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { archiveBatch, archiveHistoryBatches, getArchiveLogs } from '@/api/archive'

const grade = ref('')
const logs = ref([])

const fetchLogs = async () => {
  const res = await getArchiveLogs(50)
  logs.value = res.data || []
}

const archiveOne = async () => {
  if (!grade.value) {
    ElMessage.warning('请输入年级')
    return
  }
  await ElMessageBox.confirm(`确认归档年级 ${grade.value}？`, '归档确认', { type: 'warning' })
  await archiveBatch(grade.value)
  ElMessage.success('归档完成')
  await fetchLogs()
}

const archiveAll = async () => {
  await ElMessageBox.confirm('确认归档所有历史批次？', '归档确认', { type: 'warning' })
  await archiveHistoryBatches()
  ElMessage.success('归档任务完成')
  await fetchLogs()
}

onMounted(fetchLogs)
</script>

<style scoped>
.filter-form { margin-bottom: 8px; }
</style>
