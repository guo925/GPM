<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>专业管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增专业
          </el-button>
        </div>
      </template>
      <div style="margin-bottom:16px">
        <el-select v-model="filterCollegeId" placeholder="选择学院筛选" clearable @change="loadData" style="width:200px">
          <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="collegeName" label="所属学院" />
        <el-table-column prop="name" label="专业名称" />
        <el-table-column prop="code" label="专业代码" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" text @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" text @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑专业' : '新增专业'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="form.collegeId" placeholder="请选择学院">
            <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="专业代码" prop="code">
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
import { getMajorList, createMajor, updateMajor, deleteMajor } from '@/api/major'
import { getCollegeList } from '@/api/college'

const loading = ref(false)
const tableData = ref([])
const colleges = ref([])
const filterCollegeId = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const editId = ref(null)
const form = ref({ collegeId: null, name: '', code: '', sortOrder: 0 })
const rules = {
  collegeId: [{ required: true, message: '请选择学院', trigger: 'change' }],
  name: [{ required: true, message: '请输入专业名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入专业代码', trigger: 'blur' }]
}

const loadColleges = async () => {
  const res = await getCollegeList()
  colleges.value = res.data
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMajorList(filterCollegeId.value)
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  form.value = { collegeId: null, name: '', code: '', sortOrder: 0 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.value = { collegeId: row.collegeId, name: row.name, code: row.code, sortOrder: row.sortOrder }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该专业吗？', '提示', { type: 'warning' })
  await deleteMajor(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (isEdit.value) {
    await updateMajor(editId.value, form.value)
    ElMessage.success('修改成功')
  } else {
    await createMajor(form.value)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

onMounted(async () => {
  await loadColleges()
  loadData()
})
</script>
