<template>
  <div class="dashboard">
    <h3>校级管理员工作台</h3>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>学院管理</template>
          <p>管理全校学院信息，添加/修改/删除学院</p>
          <el-button type="primary" @click="$router.push('/system/college')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>专业管理</template>
          <p>管理各学院下属专业信息</p>
          <el-button type="primary" @click="$router.push('/system/major')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>用户管理</template>
          <p>管理全校用户账号，分配院级管理员等角色</p>
          <el-button type="primary" @click="$router.push('/system/user')">进入</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>批次管理</template>
          <p>创建和管理毕业设计批次，设定各阶段参数</p>
          <el-button type="primary" @click="$router.push('/batch')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>课题管理</template>
          <p>查看全校课题库，审核课题申报</p>
          <el-button type="primary" @click="$router.push('/topic')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>数据统计</template>
          <p>查看全校毕业设计进度统计报表</p>
          <el-button type="primary" @click="$router.push('/statistics')">进入</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
</script>

<style scoped>
.dashboard h3 { margin-bottom: 20px; color: #303133; }
p { color: #909399; margin-bottom: 12px; }
</style>
