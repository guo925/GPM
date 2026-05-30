<template>
  <div class="workspace-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">答辩管理</h1>
        <p class="page-subtitle">答辩批次、分组、安排和结果录入</p>
      </div>
      <el-button type="primary" @click="batchDialog.visible = true">新建答辩批次</el-button>
    </div>

    <el-row :gutter="16">
      <el-col :span="8">
        <el-card header="答辩批次" class="work-card">
          <el-form inline>
            <el-form-item label="毕设批次">
              <el-input v-model="batchId" clearable placeholder="批次ID" style="width:140px" />
            </el-form-item>
            <el-form-item><el-button @click="fetchBatches">查询</el-button></el-form-item>
          </el-form>
          <div v-for="item in batches" :key="item.id" :class="['list-item', selectedBatch?.id === item.id && 'active']" @click="selectBatch(item)">
            <div>
              <strong>{{ item.name }}</strong>
              <span>{{ item.type }}</span>
            </div>
            <el-button type="danger" link @click.stop="removeBatch(item)">删除</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card header="答辩组" class="work-card">
          <el-button :disabled="!selectedBatch" type="primary" plain @click="openGroupDialog">新建答辩组</el-button>
          <div v-for="item in groups" :key="item.id" :class="['list-item', selectedGroup?.id === item.id && 'active']" @click="selectGroup(item)">
            <div>
              <strong>{{ item.name }}</strong>
              <span>组长ID {{ item.leaderId }}</span>
            </div>
            <el-button type="danger" link @click.stop="removeGroup(item)">删除</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card header="答辩安排" class="work-card">
          <el-button :disabled="!selectedGroup" type="primary" plain @click="openArrangeDialog">添加安排</el-button>
          <div v-for="item in arrangements" :key="item.id" class="arrange-item">
            <div>
              <strong>学生ID {{ item.studentId }}</strong>
              <span>{{ item.defenseTime || '-' }} ｜ {{ item.location || '-' }}</span>
            </div>
            <el-button type="primary" link @click="openResultDialog(item)">结果</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="batchDialog.visible" title="新建答辩批次" width="520px">
      <el-form :model="batchDialog.form" label-width="92px">
        <el-form-item label="毕设批次ID"><el-input v-model="batchDialog.form.batchId" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="batchDialog.form.name" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="batchDialog.form.type" style="width:100%">
            <el-option label="开题答辩" value="opening_defense" />
            <el-option label="毕业答辩" value="final_defense" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="batchDialog.form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="batchDialog.form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="地点模板"><el-input v-model="batchDialog.form.locationTemplate" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveBatch">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="groupDialog.visible" title="新建答辩组" width="480px">
      <el-form :model="groupDialog.form" label-width="80px">
        <el-form-item label="组名"><el-input v-model="groupDialog.form.name" /></el-form-item>
        <el-form-item label="组长ID"><el-input v-model="groupDialog.form.leaderId" /></el-form-item>
        <el-form-item label="成员ID"><el-input v-model="groupDialog.memberText" placeholder="多个 ID 用逗号分隔" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="groupDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveGroup">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="arrangeDialog.visible" title="添加答辩安排" width="480px">
      <el-form :model="arrangeDialog.form" label-width="80px">
        <el-form-item label="学生ID"><el-input v-model="arrangeDialog.form.studentId" /></el-form-item>
        <el-form-item label="时间"><el-date-picker v-model="arrangeDialog.form.defenseTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="地点"><el-input v-model="arrangeDialog.form.location" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="arrangeDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveArrange">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resultDialog.visible" title="答辩结果" width="500px">
      <el-form :model="resultDialog.form" label-width="80px">
        <el-form-item label="总分"><el-input-number v-model="resultDialog.form.totalScore" :min="0" :max="100" /></el-form-item>
        <el-form-item label="结论">
          <el-select v-model="resultDialog.form.decision" style="width:100%">
            <el-option label="通过" value="pass" />
            <el-option label="修改后通过" value="revise_pass" />
            <el-option label="不通过" value="fail" />
          </el-select>
        </el-form-item>
        <el-form-item label="评分项"><el-input v-model="resultDialog.form.scoreItems" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="评语"><el-input v-model="resultDialog.form.comment" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resultDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveResult">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addDefenseArrangement,
  createDefenseBatch,
  createDefenseGroup,
  deleteDefenseBatch,
  deleteDefenseGroup,
  getDefenseArrangements,
  getDefenseBatches,
  getDefenseGroups,
  getDefenseResult,
  saveDefenseResult
} from '@/api/defense'

const batchId = ref('')
const batches = ref([])
const groups = ref([])
const arrangements = ref([])
const selectedBatch = ref(null)
const selectedGroup = ref(null)

const batchDialog = reactive({ visible: false, form: { batchId: '', type: 'final_defense', name: '', startTime: '', endTime: '', locationTemplate: '' } })
const groupDialog = reactive({ visible: false, memberText: '', form: { defenseBatchId: null, name: '', leaderId: '' } })
const arrangeDialog = reactive({ visible: false, form: { groupId: null, studentId: '', defenseTime: '', location: '' } })
const resultDialog = reactive({ visible: false, form: { arrangementId: null, totalScore: 0, decision: 'pass', scoreItems: '', comment: '' } })

const fetchBatches = async () => {
  const res = await getDefenseBatches(batchId.value || undefined)
  batches.value = res.data || []
}

const selectBatch = async (item) => {
  selectedBatch.value = item
  selectedGroup.value = null
  arrangements.value = []
  const res = await getDefenseGroups(item.id)
  groups.value = res.data || []
}

const selectGroup = async (item) => {
  selectedGroup.value = item
  const res = await getDefenseArrangements(item.id)
  arrangements.value = res.data || []
}

const saveBatch = async () => {
  await createDefenseBatch({ ...batchDialog.form, batchId: Number(batchDialog.form.batchId) })
  ElMessage.success('创建成功')
  batchDialog.visible = false
  await fetchBatches()
}

const removeBatch = async (item) => {
  await ElMessageBox.confirm(`确认删除 ${item.name}？`, '删除确认', { type: 'warning' })
  await deleteDefenseBatch(item.id)
  await fetchBatches()
}

const openGroupDialog = () => {
  groupDialog.form = { defenseBatchId: selectedBatch.value.id, name: '', leaderId: '' }
  groupDialog.memberText = ''
  groupDialog.visible = true
}

const saveGroup = async () => {
  const memberIds = groupDialog.memberText.split(',').map(i => Number(i.trim())).filter(Boolean)
  await createDefenseGroup({ ...groupDialog.form, leaderId: Number(groupDialog.form.leaderId), memberIds })
  ElMessage.success('创建成功')
  groupDialog.visible = false
  await selectBatch(selectedBatch.value)
}

const removeGroup = async (item) => {
  await ElMessageBox.confirm(`确认删除 ${item.name}？`, '删除确认', { type: 'warning' })
  await deleteDefenseGroup(item.id)
  await selectBatch(selectedBatch.value)
}

const openArrangeDialog = () => {
  arrangeDialog.form = { groupId: selectedGroup.value.id, studentId: '', defenseTime: '', location: selectedBatch.value.locationTemplate || '' }
  arrangeDialog.visible = true
}

const saveArrange = async () => {
  await addDefenseArrangement({ ...arrangeDialog.form, studentId: Number(arrangeDialog.form.studentId) })
  ElMessage.success('添加成功')
  arrangeDialog.visible = false
  await selectGroup(selectedGroup.value)
}

const openResultDialog = async (item) => {
  const res = await getDefenseResult(item.id)
  resultDialog.form = res.data || { arrangementId: item.id, totalScore: 0, decision: 'pass', scoreItems: '', comment: '' }
  resultDialog.form.arrangementId = item.id
  resultDialog.visible = true
}

const saveResult = async () => {
  await saveDefenseResult(resultDialog.form)
  ElMessage.success('保存成功')
  resultDialog.visible = false
}

onMounted(fetchBatches)
</script>

<style scoped>
.list-item,
.arrange-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  margin-top: 10px;
  border: 1px solid var(--gp-border);
  border-radius: 8px;
  cursor: pointer;
}
.list-item.active { border-color: var(--gp-primary); background: #eef5ff; }
.list-item strong,
.arrange-item strong,
.list-item span,
.arrange-item span { display: block; }
.list-item span,
.arrange-item span { margin-top: 5px; color: var(--gp-text-muted); font-size: 12px; }
</style>
