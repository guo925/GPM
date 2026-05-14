<template>
  <div class="dashboard">
    <h3>学生工作台</h3>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>课题浏览</template>
          <p>浏览可选课题列表，了解导师和研究方向</p>
          <el-button type="primary" @click="$router.push('/topic')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>我的选题</template>
          <p>提交选题志愿，查看选题结果和指导教师</p>
          <el-button type="primary" @click="$router.push('/student/selection')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>批次信息</template>
          <p>查看当前毕业设计批次各阶段时间安排</p>
          <el-button type="primary" @click="$router.push('/batch')">进入</el-button>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>流程进度</template>
          <p>查看毕业设计各阶段状态，提交阶段文档</p>
          <el-button type="primary" @click="$router.push('/student/process')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>指导记录</template>
          <p>查看与指导教师的沟通记录和反馈</p>
          <el-button type="primary" @click="$router.push('/student/guidance')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>我的成绩</template>
          <p>查看毕业设计各阶段评分和最终成绩</p>
          <el-button type="primary" @click="$router.push('/student/score')">进入</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
