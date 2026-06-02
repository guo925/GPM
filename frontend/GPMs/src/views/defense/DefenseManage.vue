<template>
  <div class="workspace-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">答辩管理</h1>
        <p class="page-subtitle">答辩批次、分组、安排和结果录入</p>
      </div>
      <el-button type="primary" @click="openBatchDialog">新建答辩批次</el-button>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col :span="8">
        <el-card header="答辩批次" class="work-card">
          <el-form inline>
            <el-form-item label="毕设批次">
              <el-select v-model="batchId" clearable placeholder="全部批次" style="width:180px">
                <el-option v-for="item in batchOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
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
            <div class="item-actions">
              <el-button type="primary" link @click="openResultDialog(item)">结果</el-button>
              <el-button type="danger" link @click="removeArrangement(item)">删除</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="batchDialog.visible" title="新建答辩批次" width="520px">
      <el-form ref="batchFormRef" :model="batchDialog.form" :rules="batchRules" label-width="92px">
        <el-form-item label="毕设批次" prop="batchId">
          <el-select v-model="batchDialog.form.batchId" placeholder="请选择" style="width:100%">
            <el-option v-for="item in batchOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name"><el-input v-model="batchDialog.form.name" /></el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="batchDialog.form.type" style="width:100%">
            <el-option label="开题答辩" value="opening_defense" />
            <el-option label="毕业答辩" value="final_defense" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="batchDialog.form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="batchDialog.form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></el-form-item>
        <el-form-item label="地点模板"><el-input v-model="batchDialog.form.locationTemplate" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="saveBatch">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="groupDialog.visible" title="新建答辩组" width="480px">
      <el-form ref="groupFormRef" :model="groupDialog.form" :rules="groupRules" label-width="80px">
        <el-form-item label="组名" prop="name"><el-input v-model="groupDialog.form.name" /></el-form-item>
        <el-form-item label="组长ID" prop="leaderId"><el-input v-model="groupDialog.form.leaderId" /></el-form-item>
        <el-form-item label="成员ID"><el-input v-model="groupDialog.memberText" placeholder="多个 ID 用逗号分隔" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="groupDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="saveGroup">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="arrangeDialog.visible" title="添加答辩安排" width="480px">
      <el-form ref="arrangeFormRef" :model="arrangeDialog.form" :rules="arrangeRules" label-width="80px">
        <el-form-item label="学生ID" prop="studentId"><el-input v-model="arrangeDialog.form.studentId" /></el-form-item>
        <el-form-item label="时间"><el-date-picker v-model="arrangeDialog.form.defenseTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></el-form-item>
        <el-form-item label="地点"><el-input v-model="arrangeDialog.form.location" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="arrangeDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="saveArrange">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resultDialog.visible" title="答辩结果" width="500px">
      <el-form ref="resultFormRef" :model="resultDialog.form" :rules="resultRules" label-width="80px">
        <el-form-item label="总分"><el-input-number v-model="resultDialog.form.totalScore" :min="0" :max="100" /></el-form-item>
        <el-form-item label="结论" prop="decision">
          <el-select v-model="resultDialog.form.decision" style="width:100%">
            <el-option label="通过" value="pass" />
            <el-option label="小修" value="minor_revision" />
            <el-option label="大修" value="major_revision" />
            <el-option label="不通过" value="fail" />
          </el-select>
        </el-form-item>
        <el-form-item label="评分项"><el-input v-model="resultDialog.form.scoreItems" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="评语"><el-input v-model="resultDialog.form.comment" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resultDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="saveResult">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBatchPage } from '@/api/batch'
import {
  addDefenseArrangement,
  createDefenseBatch,
  createDefenseGroup,
  deleteDefenseBatch,
  deleteDefenseArrangement,
  deleteDefenseGroup,
  getDefenseArrangements,
  getDefenseBatches,
  getDefenseGroups,
  getDefenseResult,
  saveDefenseResult
} from '@/api/defense'

const batchId = ref('')
const loading = ref(false)
const submitting = ref(false)
const batchOptions = ref([])
const batches = ref([])
const groups = ref([])
const arrangements = ref([])
const selectedBatch = ref(null)
const selectedGroup = ref(null)

const batchFormRef = ref(null)
const groupFormRef = ref(null)
const arrangeFormRef = ref(null)
const resultFormRef = ref(null)

const batchDialog = reactive({ visible: false, form: { batchId: null, type: 'final_defense', name: '', startTime: '', endTime: '', locationTemplate: '' } })
const groupDialog = reactive({ visible: false, memberText: '', form: { defenseBatchId: null, name: '', leaderId: '' } })
const arrangeDialog = reactive({ visible: false, form: { groupId: null, studentId: '', defenseTime: '', location: '' } })
const resultDialog = reactive({ visible: false, form: { arrangementId: null, totalScore: 0, decision: 'pass', scoreItems: '', comment: '' } })

const positiveNumberRule = (message) => ({
  validator: (_rule, value, callback) => {
    const numberValue = Number(value)
    if (!Number.isInteger(numberValue) || numberValue <= 0) {
      callback(new Error(message))
      return
    }
    callback()
  },
  trigger: 'blur'
})

const batchRules = {
  batchId: [{ required: true, message: '请选择毕设批次', trigger: 'change' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const groupRules = {
  name: [{ required: true, message: '请输入组名', trigger: 'blur' }],
  leaderId: [positiveNumberRule('请输入有效组长ID')]
}

const arrangeRules = {
  studentId: [positiveNumberRule('请输入有效学生ID')]
}

const resultRules = {
  decision: [{ required: true, message: '请选择结论', trigger: 'change' }]
}

const loadBatchOptions = async () => {
  const res = await getBatchPage({ current: 1, size: 100, status: 1 })
  batchOptions.value = res.data?.records || []
}

const fetchBatches = async () => {
  loading.value = true
  try {
    const res = await getDefenseBatches(batchId.value || undefined)
    batches.value = res.data || []
  } finally {
    loading.value = false
  }
}

const selectNewBatch = async (form) => {
  await fetchBatches()
  const created = batches.value.find(item =>
    item.batchId === Number(form.batchId) &&
    item.type === form.type &&
    item.name === form.name
  ) || batches.value[0]
  if (created) {
    await selectBatch(created)
  }
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
  await batchFormRef.value.validate()
  submitting.value = true
  const savedForm = { ...batchDialog.form }
  try {
    await createDefenseBatch({ ...batchDialog.form, batchId: Number(batchDialog.form.batchId) })
    ElMessage.success('创建成功')
    batchDialog.visible = false
    await selectNewBatch(savedForm)
  } finally {
    submitting.value = false
  }
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
  await groupFormRef.value.validate()
  submitting.value = true
  const savedName = groupDialog.form.name
  const savedLeaderId = Number(groupDialog.form.leaderId)
  try {
    const memberIds = groupDialog.memberText.split(',').map(i => Number(i.trim())).filter(Boolean)
    await createDefenseGroup({ ...groupDialog.form, leaderId: Number(groupDialog.form.leaderId), memberIds })
    ElMessage.success('创建成功')
    groupDialog.visible = false
    await selectBatch(selectedBatch.value)
    const created = groups.value.find(item => item.name === savedName && item.leaderId === savedLeaderId) || groups.value[0]
    if (created) {
      await selectGroup(created)
    }
  } finally {
    submitting.value = false
  }
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
  await arrangeFormRef.value.validate()
  submitting.value = true
  try {
    await addDefenseArrangement({ ...arrangeDialog.form, studentId: Number(arrangeDialog.form.studentId) })
    ElMessage.success('添加成功')
    arrangeDialog.visible = false
    await selectGroup(selectedGroup.value)
  } finally {
    submitting.value = false
  }
}

const removeArrangement = async (item) => {
  await ElMessageBox.confirm(`确认删除学生 ${item.studentId} 的答辩安排？`, '删除确认', { type: 'warning' })
  await deleteDefenseArrangement(item.id)
  ElMessage.success('删除成功')
  await selectGroup(selectedGroup.value)
}

const openResultDialog = async (item) => {
  const res = await getDefenseResult(item.id)
  resultDialog.form = res.data || { arrangementId: item.id, totalScore: 0, decision: 'pass', scoreItems: '', comment: '' }
  resultDialog.form.arrangementId = item.id
  resultDialog.visible = true
}

const saveResult = async () => {
  await resultFormRef.value.validate()
  submitting.value = true
  try {
    await saveDefenseResult(resultDialog.form)
    ElMessage.success('保存成功')
    resultDialog.visible = false
  } finally {
    submitting.value = false
  }
}

const openBatchDialog = () => {
  batchDialog.form = { batchId: batchId.value || null, type: 'final_defense', name: '', startTime: '', endTime: '', locationTemplate: '' }
  batchDialog.visible = true
}

onMounted(async () => {
  await loadBatchOptions()
  await fetchBatches()
})
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
.item-actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
</style>
