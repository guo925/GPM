<template>
  <div>
    <h2>欢迎使用 GPMS 毕业设计全过程管理系统</h2>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>用户管理</template>
          <p>管理系统用户、分配角色、重置密码</p>
          <el-button type="primary" @click="$router.push('/system/user')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>角色管理</template>
          <p>管理角色定义、分配权限</p>
          <el-button type="primary" @click="$router.push('/system/role')">进入</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
