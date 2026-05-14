<template>
  <div class="page">
    <h3>指导记录</h3>

    <el-empty v-if="!studentTopicId && !loading" description="请先完成选题" />

    <div v-if="studentTopicId">
      <el-alert type="info" :closable="false" style="margin-bottom:16px">
        课题：{{ myTopic?.topicTitle }} | 导师：{{ myTopic?.advisorName }}
      </el-alert>

      <div style="margin-bottom:16px">
        <el-button type="primary" @click="openCreate">新增指导记录</el-button>
      </div>

      <el-table v-if="records.length > 0" :data="records" border stripe>
        <el-table-column prop="weekNumber" label="周次" width="80" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'reviewed' ? 'success' : 'info'">
              {{ row.status === 'reviewed' ? '已批阅' : '已提交' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="advisorComment" label="导师评语" min-width="150" show-overflow-tooltip />
        <el-table-column prop="reviewedAt" label="批阅时间" width="160" />
        <el-table-column prop="createdAt" label="提交时间" width="160" />
      </el-table>
      <el-empty v-else description="暂无指导记录" />
    </div>

    <!-- 新增弹窗 -->
    <el-dialog v-model="dialog.visible" title="新增指导记录" width="500px">
      <el-form :model="dialog.form">
        <el-form-item label="周次">
          <el-input-number v-model="dialog.form.weekNumber" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="dialog.form.content" type="textarea" :rows="5" placeholder="请输入本周工作内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.loading" @click="handleCreate">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyTopic } from '@/api/studentTopic'
import { getGuidanceList, createGuidance } from '@/api/guidance'

const myTopic = ref(null)
const loading = ref(true)
const records = ref([])

const studentTopicId = computed(() => myTopic.value?.id)

const dialog = reactive({
  visible: false,
  loading: false,
  form: { studentTopicId: null, weekNumber: 1, content: '' }
})

const fetchData = async () => {
  try {
    const res = await getMyTopic()
    if (res.data) {
      myTopic.value = res.data
      const gRes = await getGuidanceList(res.data.id)
      records.value = gRes.data || []
    }
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  dialog.form = { studentTopicId: studentTopicId.value, weekNumber: 1, content: '' }
  dialog.visible = true
}

const handleCreate = async () => {
  if (!dialog.form.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  dialog.loading = true
  try {
    await createGuidance(dialog.form)
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
</style>
