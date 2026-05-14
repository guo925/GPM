<template>
  <div class="dashboard">
    <h3>教师工作台</h3>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>我的课题</template>
          <p>申报和管理个人毕业设计课题，设置容量和方向</p>
          <el-button type="primary" @click="$router.push('/topic')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>选题审核</template>
          <p>审核学生志愿申请，接受或拒绝选题请求</p>
          <el-button type="primary" @click="$router.push('/teacher/selection-review')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>批次信息</template>
          <p>查看当前毕业设计批次安排和时间节点</p>
          <el-button type="primary" @click="$router.push('/batch')">进入</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>指导记录</template>
          <p>查看和批阅学生提交的指导记录</p>
          <el-button type="primary" @click="$router.push('/teacher/guidance-review')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>流程审核</template>
          <p>审核学生提交的各阶段文档（开题报告、论文等）</p>
          <el-button type="primary" @click="$router.push('/teacher/process-review')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>成绩评定</template>
          <p>对学生毕业设计进行评分和评语</p>
          <el-button type="primary" @click="$router.push('/teacher/score-entry')">进入</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
