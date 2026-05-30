<template>
  <div class="workspace-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">通知中心</h1>
        <p class="page-subtitle">系统消息、审核提醒和公共公告</p>
      </div>
      <el-tag class="unread-filter" type="warning" effect="plain" @click="toggleUnreadOnly">
        未读 {{ unread }}
      </el-tag>
    </div>

    <el-card class="work-card">
      <el-table :data="filteredList" stripe class="notification-table" @row-click="openNotification">
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="isRead(row) ? 'info' : 'danger'" size="small">{{ isRead(row) ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ typeText(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="primary" link @click.stop="openNotification(row)">查看</el-button>
            <el-button v-if="!isRead(row)" type="primary" link @click.stop="markRead(row)">标记已读</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="fetchList"
      />
    </el-card>

    <el-dialog v-model="detailVisible" :title="currentNotification?.title || '通知详情'" width="560px">
      <div v-if="currentNotification" class="notification-detail">
        <div class="detail-meta">
          <el-tag :type="isRead(currentNotification) ? 'info' : 'danger'" size="small">
            {{ isRead(currentNotification) ? '已读' : '未读' }}
          </el-tag>
          <span>{{ typeText(currentNotification.type) }}</span>
          <span>{{ currentNotification.createdAt }}</span>
        </div>
        <div class="detail-content">{{ notificationContent(currentNotification) }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getNotificationPage, getUnreadCount, markNotificationRead } from '@/api/notification'

const list = ref([])
const total = ref(0)
const unread = ref(0)
const detailVisible = ref(false)
const currentNotification = ref(null)
const unreadOnly = ref(false)
const query = reactive({ current: 1, size: 10 })

const isRead = (row) => row.isRead === 1
const typeText = (type) => ({ announcement: '公告', system: '系统', deadline: '截止提醒', audit: '审核' }[type] || type || '消息')
const notificationContent = (row) => row?.content || row?.title || '暂无消息内容'
const filteredList = computed(() => unreadOnly.value ? list.value.filter(row => !isRead(row)) : list.value)

const fetchUnread = async () => {
  const res = await getUnreadCount()
  unread.value = res.data || 0
}

const fetchList = async () => {
  const res = await getNotificationPage(query)
  list.value = res.data?.records || []
  total.value = res.data?.total || 0
  await fetchUnread()
}

const markRead = async (row) => {
  await markNotificationRead(row.id)
  await fetchList()
}

const toggleUnreadOnly = () => {
  unreadOnly.value = !unreadOnly.value
}

const openNotification = async (row) => {
  currentNotification.value = row
  detailVisible.value = true
  if (!isRead(row)) {
    await markNotificationRead(row.id)
    row.isRead = 1
    currentNotification.value = { ...row, isRead: 1 }
    await fetchUnread()
  }
}

onMounted(fetchList)
</script>

<style scoped>
.notification-table :deep(.el-table__row) {
  cursor: pointer;
}

.unread-filter {
  cursor: pointer;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--gp-text-muted);
  font-size: 13px;
}

.detail-content {
  margin-top: 16px;
  color: var(--gp-text);
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
