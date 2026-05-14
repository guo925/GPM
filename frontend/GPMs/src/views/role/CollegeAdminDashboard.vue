<template>
  <div class="dashboard">
    <h3>院级管理员工作台</h3>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>专业管理</template>
          <p>管理学院下属专业信息</p>
          <el-button type="primary" @click="$router.push('/system/major')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>批次管理</template>
          <p>创建和管理本院毕业设计批次</p>
          <el-button type="primary" @click="$router.push('/batch')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>用户管理</template>
          <p>管理本院教师和学生账号</p>
          <el-button type="primary" @click="$router.push('/system/user')">进入</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>课题管理</template>
          <p>审核本院教师申报的课题</p>
          <el-button type="primary" @click="$router.push('/topic')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>选题进度</template>
          <p>查看本院学生选题情况和志愿统计</p>
          <el-button type="info" disabled>即将上线</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>答辩管理</template>
          <p>安排和管理本院答辩批次与分组</p>
          <el-button type="info" disabled>即将上线</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
