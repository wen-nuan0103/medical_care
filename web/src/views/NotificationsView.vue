<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getNotifications, markNotificationRead } from '@/api/care'
import type { SystemNotification } from '@/types/api'

const readStatus = ref<number | ''>('')
const records = ref<SystemNotification[]>([])
const loading = ref(false)

onMounted(load)

async function load() {
  loading.value = true
  try {
    records.value = await getNotifications(readStatus.value === '' ? undefined : Number(readStatus.value))
  } catch {
    records.value = []
  } finally {
    loading.value = false
  }
}

async function read(item: SystemNotification) {
  await markNotificationRead(item.id)
  await load()
}

function formatTime(value: string | null) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-'
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>系统通知</h1>
        <p>用药提醒、随访通知和审核相关的系统消息。</p>
      </div>
      <button class="primary-button" type="button" @click="load">刷新</button>
    </div>

    <div class="toolbar notification-toolbar">
      <select v-model="readStatus" @change="load">
        <option value="">全部</option>
        <option :value="0">未读</option>
        <option :value="1">已读</option>
      </select>
    </div>

    <div class="work-panel">
      <div v-if="loading" class="empty-text">加载中...</div>
      <div v-else-if="records.length === 0" class="empty-text">暂无通知。</div>
      <div v-else class="notification-list">
        <article v-for="item in records" :key="item.id" class="notification-card">
          <div>
            <span :class="['status-pill', item.readStatus ? 'muted' : 'warning']">
              {{ item.readStatus ? '已读' : '未读' }}
            </span>
            <h2>{{ item.title }}</h2>
            <p>{{ item.content }}</p>
            <small>{{ item.notificationType }} · {{ formatTime(item.createTime) }}</small>
          </div>
          <button v-if="!item.readStatus" class="ghost-button" type="button" @click="read(item)">标记已读</button>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.notification-toolbar {
  grid-template-columns: 180px;
}

.notification-list {
  display: grid;
  gap: 12px;
}

.notification-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--panel-soft);
}

.notification-card h2,
.notification-card p {
  margin: 6px 0 0;
}

.notification-card h2 {
  font-size: 16px;
}

.notification-card small {
  color: var(--muted);
}

@media (max-width: 760px) {
  .notification-card {
    grid-template-columns: 1fr;
  }
}
</style>
