<template>
  <div class="page">
    <h3>专业管理</h3>

    <el-form inline style="margin-bottom:16px">
      <el-form-item label="选择年级">
        <el-select v-model="grade" placeholder="请选择年级" @change="onGradeChange" style="width:280px">
          <el-option v-for="g in grades" :key="g" :label="g + ' 届'" :value="g" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-empty v-if="!grade" description="请选择年级" />

    <el-tabs v-model="tab" v-if="grade" type="border-card">
      <!-- 教师分配 -->
      <el-tab-pane label="教师分配" name="teacher">
        <el-table v-if="teacherList.length" :data="teacherList" border stripe>
          <el-table-column prop="teacherName" label="教师" width="120" />
          <el-table-column prop="studentName" label="指导学生" />
          <el-table-column prop="topicTitle" label="课题" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="allocationTime" label="分配时间" width="160" />
        </el-table>
        <el-empty v-else description="暂无分配数据" />
      </el-tab-pane>

      <!-- 选题进度 -->
      <el-tab-pane label="选题进度" name="selection">
        <el-row :gutter="16" style="margin-bottom:16px">
          <el-col :span="6"><el-card class="mini-card"><div class="mini-num">{{ selectionStats.selected }}</div><div>已选题</div></el-card></el-col>
          <el-col :span="6"><el-card class="mini-card"><div class="mini-num">{{ selectionStats.unselected }}</div><div>未选题</div></el-card></el-col>
          <el-col :span="6"><el-card class="mini-card"><div class="mini-num">{{ selectionStats.pendingReview }}</div><div>待审核</div></el-card></el-col>
          <el-col :span="6"><el-card class="mini-card"><div class="mini-num">{{ selectionStats.total }}</div><div>学生总数</div></el-card></el-col>
        </el-row>
        <el-table v-if="pendingList.length" :data="pendingList" border stripe>
          <el-table-column prop="studentName" label="学生" width="100" />
          <el-table-column prop="topicTitle" label="志愿课题" show-overflow-tooltip />
          <el-table-column prop="priority" label="优先级" width="80" />
          <el-table-column prop="teacherAction" label="导师审核" width="100">
            <template #default="{ r }">
              <el-tag v-if="r.teacherAction === 'approve'" type="success">已通过</el-tag>
              <el-tag v-else-if="r.teacherAction === 'reject'" type="danger">已拒绝</el-tag>
              <el-tag v-else type="info">待审核</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 流程管理 -->
      <el-tab-pane label="流程管理" name="process">
        <el-table v-if="studentTopicList.length" :data="processSummary" border stripe>
          <el-table-column prop="studentName" label="学生" width="100" />
          <el-table-column prop="topicTitle" label="课题" show-overflow-tooltip />
          <el-table-column v-for="s in stageCols" :key="s.key" :label="s.label" width="80">
            <template #default="{ row }">
              <el-tag :type="row[s.key]" size="small">{{ tagText(row[s.key]) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="完成率" width="90">
            <template #default="{ row }">{{ row.completed }}/{{ stageCols.length }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无选题数据" />
      </el-tab-pane>

      <!-- 成绩统计 -->
      <el-tab-pane label="成绩统计" name="score">
        <el-row :gutter="16" style="margin-bottom:16px">
          <el-col :span="12">
            <el-card header="成绩分布">
              <div v-if="scoreDist.length" style="display:flex;justify-content:space-around;align-items:flex-end;height:160px;padding:10px 0">
                <div v-for="g in scoreDist" :key="g.grade" style="text-align:center">
                  <div style="font-size:20px;font-weight:bold" :style="{color:gradeColor(g.grade)}">{{ g.count }}</div>
                  <div :style="barStyle(g.count, maxScore)" class="bar"></div>
                  <div style="color:#909399;margin-top:4px">{{ g.grade }}</div>
                </div>
              </div>
              <el-empty v-else description="暂无成绩数据" :image-size="60" />
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card header="汇总">
              <el-descriptions :column="1" border v-if="batchScores.length">
                <el-descriptions-item label="已评分">{{ batchScores.length }}</el-descriptions-item>
                <el-descriptions-item label="平均分">{{ avgScore }}</el-descriptions-item>
                <el-descriptions-item label="最高分">{{ maxScoreVal }}</el-descriptions-item>
                <el-descriptions-item label="最低分">{{ minScoreVal }}</el-descriptions-item>
              </el-descriptions>
              <el-empty v-else description="暂无成绩" :image-size="60" />
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { getDistinctGrades } from '@/api/batch'
import { getStudentTopicPage } from '@/api/studentTopic'
import { getReviewList } from '@/api/selection'
import { getProcessList } from '@/api/process'
import { getBatchScores } from '@/api/score'
import { getUserPage } from '@/api/user'

const tab = ref('teacher')
const grade = ref(null)
const grades = ref([])

// 教师分配
const teacherList = ref([])
// 选题进度
const pendingList = ref([])
const selectionStats = reactive({ total: 0, selected: 0, unselected: 0, pendingReview: 0 })
// 流程管理
const studentTopicList = ref([])
const processData = ref({})
const stageCols = [
  { key: 'task_book', label: '任务书' }, { key: 'opening_report', label: '开题' },
  { key: 'opening_defense', label: '答辩' }, { key: 'guidance_week', label: '周记' },
  { key: 'midterm_check', label: '中期' }, { key: 'thesis_draft', label: '初稿' },
  { key: 'thesis_final', label: '终稿' }, { key: 'post_defense_modify', label: '修改' }
]
// 成绩统计
const batchScores = ref([])

const STATUS_TAG = { not_started: 'info', submitted: 'warning', approved: 'success', rejected: 'danger' }
const tagText = (s) => ({ not_started: '未', submitted: '已交', approved: '通过', rejected: '驳回' }[s] || '-')

const processSummary = computed(() =>
  studentTopicList.value.map(st => {
    const stages = processData.value[st.id] || []
    const row = { studentName: st.studentName, topicTitle: st.topicTitle }
    let completed = 0
    stageCols.forEach(col => {
      const found = stages.find(s => s.stage === col.key)
      row[col.key] = found ? found.status : 'not_started'
      if (found && found.status === 'approved') completed++
    })
    row.completed = completed
    return row
  })
)

const scoreDist = computed(() => {
  const map = { '优': 0, '良': 0, '中': 0, '及格': 0, '不及格': 0 }
  batchScores.value.forEach(s => {
    const g = s.gradeLevel
    if (['优', '优秀'].includes(g)) map['优']++
    else if (['良', '良好'].includes(g)) map['良']++
    else if (['中', '中等'].includes(g)) map['中']++
    else if (g === '及格') map['及格']++
    else if (g === '不及格') map['不及格']++
  })
  return Object.entries(map).map(([grade, count]) => ({ grade, count }))
})

const maxScore = computed(() => Math.max(1, ...scoreDist.value.map(g => g.count)))
const avgScore = computed(() => {
  if (!batchScores.value.length) return '-'
  const sum = batchScores.value.reduce((a, b) => a + (b.finalScore || 0), 0)
  return (sum / batchScores.value.length).toFixed(1)
})
const maxScoreVal = computed(() => batchScores.value.length ? Math.max(...batchScores.value.map(s => s.finalScore || 0)) : '-')
const minScoreVal = computed(() => batchScores.value.length ? Math.min(...batchScores.value.map(s => s.finalScore || 0)) : '-')

const gradeColor = (g) => ({ '优': '#67C23A', '良': '#409EFF', '中': '#E6A23C', '及格': '#909399', '不及格': '#F56C6C' }[g])
const barStyle = (count, max) => ({
  width: '30px', height: Math.max(4, (count / max) * 100) + 'px',
  backgroundColor: '#409EFF', margin: '8px auto 0', borderRadius: '4px'
})

const fetchGrades = async () => {
  const res = await getDistinctGrades()
  grades.value = res.data || []
}

const onGradeChange = async () => {
  await Promise.all([loadTeachers(), loadSelections(), loadProcess(), loadScores()])
}

const loadTeachers = async () => {
  const res = await getStudentTopicPage({ current: 1, size: 200, grade: grade.value })
  teacherList.value = res.data?.records || res.data || []
}

const loadSelections = async () => {
  const allRes = await getUserPage({ current: 1, size: 200 })
  const selRes = await getReviewList(grade.value)
  const allSel = selRes.data || []
  pendingList.value = allSel.filter(r => !r.teacherAction)
  const assigned = teacherList.value.length
  selectionStats.selected = assigned
  selectionStats.pendingReview = pendingList.value.length
  selectionStats.total = Math.max(assigned + pendingList.value.length, assigned)
  selectionStats.unselected = Math.max(0, selectionStats.total - assigned - pendingList.value.length)
}

const loadProcess = async () => {
  const res = await getStudentTopicPage({ current: 1, size: 200, grade: grade.value })
  studentTopicList.value = res.data?.records || res.data || []
  const data = {}
  await Promise.all(studentTopicList.value.map(async st => {
    try {
      const pRes = await getProcessList(st.id)
      data[st.id] = pRes.data || []
    } catch { data[st.id] = [] }
  }))
  processData.value = data
}

const loadScores = async () => {
  try {
    const res = await getBatchScores(grade.value)
    batchScores.value = res.data || []
  } catch { batchScores.value = [] }
}

onMounted(fetchGrades)
</script>

<style scoped>
.page h3 { margin-bottom: 20px; color: #303133; }
.mini-card { text-align: center; padding: 10px 0; cursor: default; }
.mini-num { font-size: 28px; font-weight: bold; color: #409EFF; }
.bar { transition: height 0.5s; }
</style>
