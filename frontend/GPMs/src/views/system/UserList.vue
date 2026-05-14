<template>
  <div>
    <!-- 搜索区 -->
    <el-card style="margin-bottom:16px">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width:120px">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区 -->
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>用户列表</span>
          <el-button type="primary" @click="openCreate">新增用户</el-button>
        </div>
      </template>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="warning" text size="small" @click="openResetPwd(row)">重置密码</el-button>
            <el-button type="success" text size="small" @click="openAssignRole(row)">分配角色</el-button>
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
    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑用户' : '新增用户'" width="500px" @close="resetDialog">
      <el-form :model="dialog.form" :rules="dialog.rules" ref="dialogFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="dialog.form.username" :disabled="dialog.isEdit" />
        </el-form-item>
        <el-form-item v-if="!dialog.isEdit" label="密码" prop="password">
          <el-input v-model="dialog.form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="dialog.form.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="dialog.form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="dialog.form.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.loading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="pwdDialog.visible" title="重置密码" width="400px">
      <el-form :model="pwdDialog.form" :rules="pwdDialog.rules" ref="pwdFormRef" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdDialog.form.newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="pwdDialog.loading" @click="handleResetPwd">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色弹窗 -->
    <el-dialog v-model="roleDialog.visible" title="分配角色" width="450px">
      <el-checkbox-group v-model="roleDialog.checkedIds">
        <el-checkbox v-for="r in roleDialog.allRoles" :key="r.id" :value="r.id" :label="r.roleName" style="margin-bottom:12px" />
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="roleDialog.loading" @click="handleAssignRole">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserPage, createUser, updateUser, deleteUser,
  updateUserStatus, resetPassword, getUserRoles, assignUserRoles
} from '@/api/user'
import { getRoleList } from '@/api/role'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const searchForm = reactive({ current: 1, size: 10, username: '', realName: '', status: null })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserPage(searchForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { searchForm.current = 1; fetchData() }
const resetSearch = () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.status = null
  searchForm.current = 1
  fetchData()
}

// 新增/编辑
const dialogFormRef = ref()
const dialog = reactive({
  visible: false, isEdit: false, loading: false,
  form: { id: null, username: '', password: '', realName: '', phone: '', email: '' },
  rules: {
    username: [{ required: true, message: '必填', trigger: 'blur' }],
    password: [{ required: true, message: '必填', trigger: 'blur' }, { min: 6, message: '至少6位', trigger: 'blur' }],
    realName: [{ required: true, message: '必填', trigger: 'blur' }],
    phone: [{ pattern: /^1[3-9]\d{9}$/, message: '格式不正确', trigger: 'blur' }],
    email: [{ type: 'email', message: '格式不正确', trigger: 'blur' }]
  }
})

const openCreate = () => {
  dialog.isEdit = false
  dialog.visible = true
}

const openEdit = (row) => {
  dialog.isEdit = true
  dialog.form = { id: row.id, username: row.username, password: '', realName: row.realName, phone: row.phone || '', email: row.email || '' }
  dialog.visible = true
}

const resetDialog = () => {
  dialogFormRef.value?.resetFields()
  dialog.form = { id: null, username: '', password: '', realName: '', phone: '', email: '' }
}

const handleSubmit = async () => {
  const valid = await dialogFormRef.value.validate().catch(() => false)
  if (!valid) return
  dialog.loading = true
  try {
    if (dialog.isEdit) {
      await updateUser(dialog.form)
      ElMessage.success('修改成功')
    } else {
      await createUser(dialog.form)
      ElMessage.success('新增成功')
    }
    dialog.visible = false
    fetchData()
  } finally {
    dialog.loading = false
  }
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该用户？', '提示', { type: 'warning' }).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

// 状态切换
const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  await updateUserStatus({ id: row.id, status: newStatus })
  ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  fetchData()
}

// 重置密码
const pwdFormRef = ref()
const pwdDialog = reactive({
  visible: false, loading: false,
  userId: null,
  form: { newPassword: '' },
  rules: { newPassword: [
    { required: true, message: '必填', trigger: 'blur' },
    { min: 6, max: 20, message: '6-20位', trigger: 'blur' }
  ]}
})

const openResetPwd = (row) => {
  pwdDialog.userId = row.id
  pwdDialog.form.newPassword = ''
  pwdDialog.visible = true
}

const handleResetPwd = async () => {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  pwdDialog.loading = true
  try {
    await resetPassword({ id: pwdDialog.userId, newPassword: pwdDialog.form.newPassword })
    ElMessage.success('密码重置成功')
    pwdDialog.visible = false
  } finally {
    pwdDialog.loading = false
  }
}

// 分配角色
const roleDialog = reactive({
  visible: false, loading: false, userId: null,
  allRoles: [], checkedIds: []
})

const openAssignRole = async (row) => {
  roleDialog.userId = row.id
  roleDialog.checkedIds = []
  try {
    const [rolesRes, userRolesRes] = await Promise.all([
      getRoleList(),
      getUserRoles(row.id)
    ])
    roleDialog.allRoles = rolesRes.data
    roleDialog.checkedIds = userRolesRes.data.roles.map(r => r.id)
  } catch {}
  roleDialog.visible = true
}

const handleAssignRole = async () => {
  roleDialog.loading = true
  try {
    await assignUserRoles({
      userId: roleDialog.userId,
      roleIds: roleDialog.checkedIds
    })
    ElMessage.success('角色分配成功')
    roleDialog.visible = false
  } finally {
    roleDialog.loading = false
  }
}

onMounted(fetchData)
</script>
