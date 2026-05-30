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
        <el-table-column label="附件" width="110">
          <template #default="{ row }">
            <el-link v-if="row.filePath" type="primary" :href="getFileViewUrl(row.filePath)" target="_blank">查看</el-link>
            <span v-else style="color:#909399">-</span>
          </template>
        </el-table-column>
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
        <el-form-item label="附件">
          <el-upload
            class="upload-block"
            :show-file-list="false"
            :http-request="uploadGuidanceFile"
            :before-upload="beforeUpload"
          >
            <el-button :loading="dialog.uploading">
              <el-icon><Upload /></el-icon>
              选择文件
            </el-button>
          </el-upload>
          <el-link v-if="dialog.fileName" class="file-link" type="primary" :href="getFileViewUrl(dialog.form.filePath)" target="_blank">
            {{ dialog.fileName }}
          </el-link>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.loading" :disabled="dialog.uploading" @click="handleCreate">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getMyTopic } from '@/api/studentTopic'
import { getGuidanceList, createGuidance } from '@/api/guidance'
import { uploadFile, getFileViewUrl } from '@/api/file'

const myTopic = ref(null)
const loading = ref(true)
const records = ref([])

const studentTopicId = computed(() => myTopic.value?.id)

const dialog = reactive({
  visible: false,
  loading: false,
  uploading: false,
  fileName: '',
  form: { studentTopicId: null, weekNumber: 1, content: '', filePath: '' }
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
  dialog.fileName = ''
  dialog.form = { studentTopicId: studentTopicId.value, weekNumber: 1, content: '', filePath: '' }
  dialog.visible = true
}

const beforeUpload = (file) => {
  const allowed = ['pdf', 'doc', 'docx', 'wps', 'xls', 'xlsx', 'et', 'ppt', 'pptx', 'dps', 'txt', 'zip', 'rar', 'png', 'jpg', 'jpeg']
  const ext = file.name.split('.').pop()?.toLowerCase()
  if (!allowed.includes(ext)) {
    ElMessage.warning('不支持的文件类型')
    return false
  }
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过50MB')
    return false
  }
  return true
}

const uploadGuidanceFile = async ({ file }) => {
  dialog.uploading = true
  try {
    const res = await uploadFile(file, 'guidance')
    dialog.form.filePath = res.data.url
    dialog.fileName = res.data.originalName
    ElMessage.success('附件上传成功')
  } finally {
    dialog.uploading = false
  }
}

const handleCreate = async () => {
  if (dialog.uploading) {
    ElMessage.warning('附件正在上传，请稍后提交')
    return
  }
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
.upload-block { margin-right: 10px; }
.file-link { margin-left: 12px; vertical-align: middle; }
</style>
