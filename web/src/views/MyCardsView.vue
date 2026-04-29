<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMyCards } from '@/api/consultation'
import type { PrivateDoctorCard } from '@/types/api'

const router = useRouter()
const cards = ref<PrivateDoctorCard[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    cards.value = await getMyCards()
  } catch { /* empty */ } finally {
    loading.value = false
  }
})

function statusLabel(s: string) {
  const m: Record<string, string> = { ACTIVE: '生效中', EXPIRED: '已过期', USED_UP: '已用完', REFUNDED: '已退款' }
  return m[s] ?? s
}
function statusClass(s: string) {
  return s === 'ACTIVE' ? 'success' : 'muted'
}
function formatDate(s: string | null) {
  return s ? s.replace('T', ' ').slice(0, 16) : '-'
}
function startConsultation(card: PrivateDoctorCard) {
  router.push({ name: 'patient-new-consultation', query: { doctorId: card.doctorId } })
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>我的私人医生</h1>
        <p>查看已购买的服务卡和剩余权益</p>
      </div>
    </div>

    <div v-if="loading" class="empty-text">加载中...</div>
    <div v-else-if="cards.length === 0" class="empty-text">暂无服务卡，快去医生详情页购买吧~</div>

    <div class="card-list" v-else>
      <div v-for="card in cards" :key="card.id" class="service-card">
        <div class="sc-header">
          <div class="sc-avatar">{{ (card.doctorName || '医').slice(-1) }}</div>
          <div class="sc-title">
            <strong>{{ card.doctorName }}</strong>
            <small>{{ card.doctorTitle }} · {{ card.planName }}</small>
          </div>
          <span :class="['status-pill', statusClass(card.status)]">{{ statusLabel(card.status) }}</span>
        </div>

        <div class="sc-progress-grid">
          <div class="sc-progress-item">
            <span>剩余次数</span>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: card.totalTimes ? (card.remainingTimes / card.totalTimes * 100) + '%' : '0%' }"></div>
            </div>
            <strong>{{ card.remainingTimes }} / {{ card.totalTimes }}</strong>
          </div>
          <div class="sc-progress-item">
            <span>剩余时长</span>
            <div class="progress-bar">
              <div class="progress-fill accent" :style="{ width: card.totalMinutes ? (card.remainingMinutes / card.totalMinutes * 100) + '%' : '0%' }"></div>
            </div>
            <strong>{{ card.remainingMinutes }} / {{ card.totalMinutes }} 分钟</strong>
          </div>
        </div>

        <div class="sc-footer">
          <span class="muted-text">{{ formatDate(card.startTime) }} 至 {{ formatDate(card.expireTime) }}</span>
          <button
            class="primary-button"
            type="button"
            :disabled="card.status !== 'ACTIVE'"
            @click="startConsultation(card)"
          >发起问诊</button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.card-list {
  display: grid;
  gap: 16px;
}
.service-card {
  display: grid;
  gap: 18px;
  padding: 24px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--panel);
  transition: box-shadow 0.2s;
}
.service-card:hover {
  box-shadow: 0 4px 24px rgba(15, 118, 110, 0.08);
}
.sc-header {
  display: flex;
  align-items: center;
  gap: 14px;
}
.sc-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  font-size: 20px;
  font-weight: 800;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}
.sc-title {
  flex: 1;
}
.sc-title strong {
  display: block;
  font-size: 16px;
  font-weight: 700;
}
.sc-title small {
  color: var(--muted);
}
.sc-progress-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.sc-progress-item {
  display: grid;
  gap: 6px;
}
.sc-progress-item span {
  color: var(--muted);
  font-size: 13px;
}
.sc-progress-item strong {
  font-size: 13px;
}
.progress-bar {
  height: 8px;
  border-radius: 4px;
  background: #e2e8f0;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  border-radius: 4px;
  background: var(--primary);
  transition: width 0.3s ease;
}
.progress-fill.accent {
  background: var(--accent);
}
.sc-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
</style>
