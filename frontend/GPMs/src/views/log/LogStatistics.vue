<template>
  <div class="workspace-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">日志统计</h1>
        <p class="page-subtitle">操作频次、活跃用户、业务模块和近期日志</p>
      </div>
      <el-button type="primary" @click="fetchData">刷新</el-button>
    </div>

    <div class="metric-grid">
      <div class="metric-card" v-for="item in cards" :key="item.label">
        <div class="metric-card__value">{{ item.value }}</div>
        <div class="metric-card__label">{{ item.label }}</div>
      </div>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="12">
        <el-card header="近 7 天操作趋势" class="work-card">
          <div class="trend-bars">
            <div v-for="item in dailyTrend" :key="item.date" class="trend-item">
              <div class="trend-count">{{ item.count }}</div>
              <div class="trend-bar" :style="{ height: trendHeight(item.count) }"></div>
              <div class="trend-label">{{ shortDate(item.date) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="操作类型排行" class="work-card">
          <div class="rank-list">
            <div v-for="item in actionStats" :key="item.name" class="rank-item">
              <span>{{ item.name || '未知操作' }}</span>
              <el-progress :percentage="rankPercent(item.count, maxActionCount)" :format="() => String(item.count)" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="8">
        <el-card header="业务模块排行" class="work-card">
          <div class="target-list">
            <div v-for="item in targetStats" :key="item.name" class="target-item">
              <span>{{ item.name || '未分类' }}</span>
              <strong>{{ item.count }}</strong>
            </div>
            <el-empty v-if="!targetStats.length" description="暂无数据" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card header="操作日志明细" class="work-card">
          <el-form inline class="filter-form">
            <el-form-item label="用户ID">
              <el-input v-model="query.userId" clearable placeholder="用户ID" style="width:140px" />
            </el-form-item>
            <el-form-item label="操作">
              <el-input v-model="query.action" clearable placeholder="操作类型" style="width:160px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchLogs">查询</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="logs" stripe size="small">
            <el-table-column prop="createdAt" label="时间" width="170" />
            <el-table-column prop="userId" label="用户" width="80" />
            <el-table-column prop="action" label="操作" width="110" />
            <el-table-column prop="targetType" label="对象" width="110" />
            <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
          </el-table>
          <el-pagination
            v-model:current-page="query.current"
            v-model:page-size="query.size"
            layout="total, prev, pager, next"
            :total="total"
            @current-change="fetchLogs"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getLogPage, getLogStatistics } from '@/api/log'

const stats = ref({})
const logs = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, userId: '', action: '' })

const cards = computed(() => [
  { label: '日志总量', value: stats.value.totalLogs || 0 },
  { label: '今日操作', value: stats.value.todayLogs || 0 },
  { label: '活跃用户', value: stats.value.activeUsers || 0 },
  { label: '今日活跃', value: stats.value.todayActiveUsers || 0 }
])

const actionStats = computed(() => stats.value.actionStats || [])
const targetStats = computed(() => stats.value.targetStats || [])
const dailyTrend = computed(() => stats.value.dailyTrend || [])
const maxActionCount = computed(() => Math.max(1, ...actionStats.value.map(i => i.count || 0)))
const maxTrendCount = computed(() => Math.max(1, ...dailyTrend.value.map(i => i.count || 0)))

const rankPercent = (count, max) => Math.round(((count || 0) / max) * 100)
const trendHeight = (count) => Math.max(8, ((count || 0) / maxTrendCount.value) * 130) + 'px'
const shortDate = (date) => String(date || '').slice(5)

const fetchStats = async () => {
  const res = await getLogStatistics()
  stats.value = res.data || {}
}

const fetchLogs = async () => {
  const params = { ...query }
  if (!params.userId) delete params.userId
  if (!params.action) delete params.action
  const res = await getLogPage(params)
  logs.value = res.data?.records || []
  total.value = res.data?.total || 0
}

const fetchData = async () => {
  await Promise.all([fetchStats(), fetchLogs()])
}

onMounted(fetchData)
</script>

<style scoped>
.stats-row { margin-bottom: 16px; }
.trend-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 190px;
  padding-top: 12px;
}
.trend-item { width: 48px; text-align: center; }
.trend-count { color: var(--gp-text-secondary); font-size: 13px; }
.trend-bar {
  width: 26px;
  margin: 6px auto;
  background: var(--gp-primary);
  border-radius: 5px 5px 0 0;
}
.trend-label { color: var(--gp-text-muted); font-size: 12px; }
.rank-item { margin-bottom: 14px; }
.rank-item span {
  display: block;
  margin-bottom: 6px;
  color: var(--gp-text-secondary);
  font-size: 13px;
}
.target-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--gp-border);
}
.target-item span { color: var(--gp-text-secondary); }
.target-item strong { color: var(--gp-primary); font-size: 18px; }
.filter-form { margin-bottom: 8px; }
</style>
