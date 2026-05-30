<template>
  <div class="workspace-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">审核日志</h1>
        <p class="page-subtitle">流程审核、课题审核等关键审计记录</p>
      </div>
    </div>

    <el-card class="work-card">
      <el-form inline class="filter-form">
        <el-form-item label="流程ID">
          <el-input v-model="query.processInstanceId" clearable placeholder="流程实例ID" style="width:160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="list" stripe>
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column prop="auditorId" label="审核人" width="100" />
        <el-table-column prop="targetType" label="对象" width="140" />
        <el-table-column prop="processInstanceId" label="流程ID" width="100" />
        <el-table-column label="动作" width="100">
          <template #default="{ row }">
            <el-tag :type="row.action === 'approve' ? 'success' : 'danger'">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="意见" min-width="240" show-overflow-tooltip />
      </el-table>
      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="fetchList"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getAuditLogPage } from '@/api/log'

const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, processInstanceId: '' })

const fetchList = async () => {
  const params = { ...query }
  if (!params.processInstanceId) delete params.processInstanceId
  const res = await getAuditLogPage(params)
  list.value = res.data?.records || []
  total.value = res.data?.total || 0
}

onMounted(fetchList)
</script>

<style scoped>
.filter-form { margin-bottom: 8px; }
</style>
