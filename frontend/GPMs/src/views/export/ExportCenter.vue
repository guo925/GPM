<template>
  <div class="workspace-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">导出中心</h1>
        <p class="page-subtitle">导出成绩单等业务数据</p>
      </div>
    </div>

    <el-card header="成绩单导出" class="work-card">
      <el-form inline>
        <el-form-item label="年级">
          <el-select v-model="grade" placeholder="请选择年级" style="width:180px">
            <el-option v-for="g in grades" :key="g" :label="g + ' 届'" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadScores">预览</el-button>
          <el-button :disabled="!rows.length" @click="downloadCsv">下载 CSV</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rows" stripe>
        <el-table-column prop="studentName" label="学生" />
        <el-table-column prop="advisorName" label="导师" />
        <el-table-column prop="finalScore" label="总分" />
        <el-table-column prop="gradeLevel" label="等级" />
        <el-table-column prop="status" label="状态" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { exportScores } from '@/api/export'
import { getDistinctGrades } from '@/api/batch'

const grade = ref('')
const grades = ref([])
const rows = ref([])

const loadScores = async () => {
  if (!grade.value) {
    ElMessage.warning('请输入年级')
    return
  }
  const res = await exportScores(grade.value)
  rows.value = res.data || []
}

const escapeCsv = (value) => `"${String(value ?? '').replaceAll('"', '""')}"`

const downloadCsv = () => {
  const header = ['学生', '导师', '总分', '等级', '状态']
  const body = rows.value.map(row => [row.studentName, row.advisorName, row.finalScore, row.gradeLevel, row.status].map(escapeCsv).join(','))
  const csv = [header.map(escapeCsv).join(','), ...body].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `scores-grade-${grade.value}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

onMounted(async () => {
  const res = await getDistinctGrades()
  grades.value = res.data || []
  if (grades.value.length > 0) {
    grade.value = grades.value[0]
  }
})
</script>
