<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getConsultations } from '@/api/consultation'
import type { ConsultationSession } from '@/types/api'

const router = useRouter()
const sessions = ref<ConsultationSession[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    sessions.value = await getConsultations()
  } catch { /* empty */ } finally {
    loading.value = false
  }
})

function statusLabel(s: string) {
  const m: Record<string, string> = { WAITING: '等待接诊', IN_PROGRESS: '问诊中', ENDED: '已结束', CANCELED: '已取消' }
  return m[s] ?? s
}
function statusClass(s: string) {
  if (s === 'IN_PROGRESS') return 'success'
  if (s === 'WAITING') return 'warning'
  return 'muted'
}
function formatDate(s: string | null) {
  return s ? s.replace('T', ' ').slice(0, 16) : '-'
}
function enterChat(session: ConsultationSession) {
  router.push({ name: 'chat', params: { sessionId: session.id } })
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>问诊列表</h1>
        <p>查看所有问诊会话</p>
      </div>
    </div>

    <div v-if="loading" class="empty-text">加载中...</div>
    <div v-else-if="sessions.length === 0" class="empty-text">暂无问诊记录</div>

    <div class="session-list" v-else>
      <div v-for="s in sessions" :key="s.id" class="session-card" @click="enterChat(s)">
        <div class="sess-top">
          <div class="sess-peer">
            <div class="sc-avatar">{{ (s.doctorName || s.patientName || '问').slice(-1) }}</div>
            <div>
              <strong>{{ s.doctorName || '医生' }} / {{ s.patientName || '患者' }}</strong>
              <small>{{ s.sessionNo }}</small>
            </div>
          </div>
          <span :class="['status-pill', statusClass(s.status)]">{{ statusLabel(s.status) }}</span>
        </div>

        <p v-if="s.chiefComplaint" class="sess-complaint">主诉：{{ s.chiefComplaint }}</p>

        <div class="sess-meta">
          <span>时长 {{ s.usedMinutes }}/{{ s.allowedMinutes }} 分钟</span>
          <span>{{ formatDate(s.createTime) }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.session-list {
  display: grid;
  gap: 12px;
}
.session-card {
  display: grid;
  gap: 12px;
  padding: 20px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--panel);
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s;
}
.session-card:hover {
  border-color: var(--primary);
  box-shadow: 0 2px 16px rgba(15, 118, 110, 0.1);
}
.sess-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.sess-peer {
  display: flex;
  align-items: center;
  gap: 12px;
}
.sess-peer strong {
  display: block;
  font-weight: 700;
}
.sess-peer small {
  color: var(--muted);
  font-size: 12px;
}
.sc-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  font-weight: 800;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}
.sess-complaint {
  margin: 0;
  color: var(--muted);
  font-size: 14px;
}
.sess-meta {
  display: flex;
  justify-content: space-between;
  color: var(--muted);
  font-size: 13px;
}
</style>
