<template>
  <div class="dashboard">
    <h3>专业管理员工作台</h3>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>批次管理</template>
          <p>查看和管理本专业毕业设计批次</p>
          <el-button type="primary" @click="$router.push('/batch')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>课题管理</template>
          <p>审核本专业教师申报的课题，管理课题库</p>
          <el-button type="primary" @click="$router.push('/topic')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>教师分配</template>
          <p>查看本专业指导教师和学生匹配情况</p>
          <el-button type="primary" @click="$router.push('/major/monitor')">进入</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>选题进度</template>
          <p>查看本专业学生选题状态和志愿分布</p>
          <el-button type="primary" @click="$router.push('/major/monitor')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>流程管理</template>
          <p>跟踪本专业毕业设计各阶段完成情况</p>
          <el-button type="primary" @click="$router.push('/major/monitor')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>成绩统计</template>
          <p>查看本专业毕业设计成绩分布</p>
          <el-button type="primary" @click="$router.push('/major/monitor')">进入</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
