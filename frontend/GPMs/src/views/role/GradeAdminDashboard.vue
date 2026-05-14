<template>
  <div class="dashboard">
    <h3>年级管理员工作台</h3>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>批次管理</template>
          <p>管理本年级毕业设计批次，推进各阶段流程</p>
          <el-button type="primary" @click="$router.push('/batch')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>课题管理</template>
          <p>审核和管理本年级毕业设计课题</p>
          <el-button type="primary" @click="$router.push('/topic')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>选题管理</template>
          <p>查看学生选题进度，处理选题调剂</p>
          <el-button type="info" disabled>即将上线</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>流程监控</template>
          <p>查看各阶段完成情况，督促未完成学生</p>
          <el-button type="info" disabled>即将上线</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>成绩管理</template>
          <p>汇总和导出本年级毕业设计成绩</p>
          <el-button type="info" disabled>即将上线</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>通知公告</template>
          <p>向本年级师生发布通知</p>
          <el-button type="info" disabled>即将上线</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
