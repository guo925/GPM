<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>学院管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增学院
          </el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="学院名称" />
        <el-table-column prop="code" label="学院代码" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" text @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" text @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑学院' : '新增学院'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="学院名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="学院代码" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCollegeList, createCollege, updateCollege, deleteCollege } from '@/api/college'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const editId = ref(null)
const form = ref({ name: '', code: '', sortOrder: 0 })
const rules = {
  name: [{ required: true, message: '请输入学院名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入学院代码', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCollegeList()
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  form.value = { name: '', code: '', sortOrder: 0 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.value = { name: row.name, code: row.code, sortOrder: row.sortOrder }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该学院吗？', '提示', { type: 'warning' })
  await deleteCollege(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (isEdit.value) {
    await updateCollege(editId.value, form.value)
    ElMessage.success('修改成功')
  } else {
    await createCollege(form.value)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

onMounted(() => loadData())
</script>
