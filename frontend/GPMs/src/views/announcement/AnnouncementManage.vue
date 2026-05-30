<template>
  <div class="workspace-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">公告管理</h1>
        <p class="page-subtitle">发布系统公告，供全体用户查看</p>
      </div>
      <el-button type="primary" @click="openCreate">发布公告</el-button>
    </div>

    <el-card class="work-card">
      <el-form inline class="filter-form">
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" clearable placeholder="标题或内容" style="width:220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" stripe>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="发布时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="fetchList"
      />
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.form.id ? '编辑公告' : '发布公告'" width="560px">
      <el-form :model="dialog.form" label-width="72px">
        <el-form-item label="标题">
          <el-input v-model="dialog.form.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="dialog.form.content" type="textarea" :rows="7" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.loading" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createAnnouncement, deleteAnnouncement, getAnnouncementPage, updateAnnouncement } from '@/api/announcement'

const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, keyword: '' })
const dialog = reactive({
  visible: false,
  loading: false,
  form: { id: null, title: '', content: '' }
})

const fetchList = async () => {
  const params = { ...query }
  if (!params.keyword) delete params.keyword
  const res = await getAnnouncementPage(params)
  list.value = res.data?.records || []
  total.value = res.data?.total || 0
}

const openCreate = () => {
  dialog.form = { id: null, title: '', content: '' }
  dialog.visible = true
}

const openEdit = (row) => {
  dialog.form = { id: row.id, title: row.title, content: row.content }
  dialog.visible = true
}

const save = async () => {
  if (!dialog.form.title || !dialog.form.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  dialog.loading = true
  try {
    const payload = { title: dialog.form.title, content: dialog.form.content }
    if (dialog.form.id) {
      await updateAnnouncement(dialog.form.id, payload)
    } else {
      await createAnnouncement(payload)
    }
    ElMessage.success('保存成功')
    dialog.visible = false
    await fetchList()
  } finally {
    dialog.loading = false
  }
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除公告「${row.title}」？`, '删除确认', { type: 'warning' })
  await deleteAnnouncement(row.id)
  ElMessage.success('删除成功')
  await fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.filter-form { margin-bottom: 8px; }
</style>
