<template>
  <div>
    <el-card style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.roleName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>角色列表</span>
          <el-button type="primary" @click="openCreate">新增角色</el-button>
        </div>
      </template>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="180" />
        <el-table-column label="权限" min-width="300">
          <template #default="{ row }">
            <el-tag v-for="p in row.permissions" :key="p.id" style="margin-right:6px;margin-bottom:4px" size="small">
              {{ p.permissionName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:16px;display:flex;justify-content:flex-end">
        <el-pagination
          v-model:current-page="searchForm.current"
          v-model:page-size="searchForm.size"
          :total="total"
          :page-sizes="[5,10,20,50]"
          layout="total,sizes,prev,pager,next"
          @change="fetchData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑角色' : '新增角色'" width="550px" @close="resetDialog">
      <el-form :model="dialog.form" :rules="dialog.rules" ref="dialogFormRef" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="dialog.form.roleName" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="dialog.form.roleCode" :disabled="dialog.isEdit" />
        </el-form-item>
        <el-form-item label="权限">
          <el-checkbox-group v-model="dialog.form.permissionIds">
            <div v-for="p in dialog.allPermissions" :key="p.id" style="margin-bottom:8px">
              <el-checkbox :value="p.id" :label="p.permissionCode">
                {{ p.permissionName }} ({{ p.permissionCode }})
              </el-checkbox>
            </div>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.loading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRolePage, createRole, updateRole, deleteRole, getPermissionList } from '@/api/role'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const searchForm = reactive({ current: 1, size: 10, roleName: '' })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRolePage(searchForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { searchForm.current = 1; fetchData() }
const resetSearch = () => {
  searchForm.roleName = ''
  searchForm.current = 1
  fetchData()
}

const dialogFormRef = ref()
const dialog = reactive({
  visible: false, isEdit: false, loading: false,
  allPermissions: [],
  form: { id: null, roleName: '', roleCode: '', permissionIds: [] },
  rules: {
    roleName: [{ required: true, message: '必填', trigger: 'blur' }],
    roleCode: [{ required: true, message: '必填', trigger: 'blur' }]
  }
})

const openCreate = async () => {
  dialog.isEdit = false
  try {
    const res = await getPermissionList()
    dialog.allPermissions = res.data
  } catch {}
  dialog.visible = true
}

const openEdit = async (row) => {
  dialog.isEdit = true
  try {
    const res = await getPermissionList()
    dialog.allPermissions = res.data
  } catch {}
  dialog.form = {
    id: row.id,
    roleName: row.roleName,
    roleCode: row.roleCode,
    permissionIds: (row.permissions || []).map(p => p.id)
  }
  dialog.visible = true
}

const resetDialog = () => {
  dialogFormRef.value?.resetFields()
  dialog.form = { id: null, roleName: '', roleCode: '', permissionIds: [] }
  dialog.allPermissions = []
}

const handleSubmit = async () => {
  const valid = await dialogFormRef.value.validate().catch(() => false)
  if (!valid) return
  dialog.loading = true
  try {
    if (dialog.isEdit) {
      await updateRole(dialog.form)
      ElMessage.success('修改成功')
    } else {
      await createRole(dialog.form)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    fetchData()
  } finally {
    dialog.loading = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该角色？', '提示', { type: 'warning' }).then(async () => {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(fetchData)
</script>
