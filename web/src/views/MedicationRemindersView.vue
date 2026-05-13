<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  getMedicationPlans,
  getMedicationReminders,
  markReminderMissed,
  markReminderTaken,
  snoozeReminder,
} from '@/api/care'
import type { MedicationPlan, MedicationReminder } from '@/types/api'

const status = ref('')
const plans = ref<MedicationPlan[]>([])
const reminders = ref<MedicationReminder[]>([])
const loading = ref(false)
const busyId = ref<number | null>(null)

const waitingCount = computed(() => reminders.value.filter((item) => item.status === 'WAITING').length)

onMounted(load)

async function load() {
  loading.value = true
  try {
    plans.value = await getMedicationPlans('ACTIVE')
    reminders.value = await getMedicationReminders(status.value ? { status: status.value } : undefined)
  } catch {
    plans.value = []
    reminders.value = []
  } finally {
    loading.value = false
  }
}

async function act(id: number, action: 'taken' | 'missed' | 'snooze') {
  busyId.value = id
  try {
    if (action === 'taken') await markReminderTaken(id)
    if (action === 'missed') await markReminderMissed(id)
    if (action === 'snooze') await snoozeReminder(id, 15)
    await load()
  } catch (error) {
    alert(error instanceof Error ? error.message : '操作失败')
  } finally {
    busyId.value = null
  }
}

function statusLabel(value: string) {
  const map: Record<string, string> = {
    WAITING: '待服药',
    TAKEN: '已服药',
    MISSED: '已错过',
    SNOOZED: '已推迟',
  }
  return map[value] ?? value
}

function statusClass(value: string) {
  if (value === 'TAKEN') return 'success'
  if (value === 'WAITING' || value === 'SNOOZED') return 'warning'
  return 'muted'
}

function formatTime(value: string | null) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-'
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>用药提醒</h1>
        <p>跟踪医保购药后生成的用药计划和服药提醒。</p>
      </div>
      <button class="primary-button" type="button" @click="load">刷新</button>
    </div>

    <div class="metric-grid">
      <article class="metric-card">
        <span>用药计划</span>
        <strong>{{ plans.length }}</strong>
        <small class="muted-text">由已支付处方自动生成</small>
      </article>
      <article class="metric-card">
        <span>待确认提醒</span>
        <strong>{{ waitingCount }}</strong>
        <small class="muted-text">需要患者确认服药</small>
      </article>
    </div>

    <div class="toolbar reminder-toolbar">
      <select v-model="status" @change="load">
        <option value="">全部提醒</option>
        <option value="WAITING">待服药</option>
        <option value="TAKEN">已服药</option>
        <option value="MISSED">已错过</option>
        <option value="SNOOZED">已推迟</option>
      </select>
    </div>

    <div class="care-layout">
      <div class="work-panel">
        <h2>用药计划</h2>
        <div v-if="loading" class="empty-text">加载中...</div>
        <div v-else-if="plans.length === 0" class="empty-text">暂无用药计划。</div>
        <div v-else class="plan-list">
          <article v-for="plan in plans" :key="plan.id" class="mini-card">
            <div class="card-topline">
              <strong>{{ plan.drugName }}</strong>
              <span class="status-pill success">{{ plan.status === 'ACTIVE' ? '进行中' : plan.status }}</span>
            </div>
            <p>{{ plan.dosage || '-' }} / {{ plan.usageMethod || '-' }}</p>
            <small>{{ plan.startDate }} 至 {{ plan.endDate }} · {{ plan.reminderTimes }}</small>
            <p class="ai-text">{{ plan.aiReminderText || '暂无AI提醒文案' }}</p>
          </article>
        </div>
      </div>

      <div class="work-panel">
        <h2>提醒记录</h2>
        <div v-if="loading" class="empty-text">加载中...</div>
        <div v-else-if="reminders.length === 0" class="empty-text">暂无提醒记录。</div>
        <div v-else class="reminder-list">
          <article v-for="item in reminders" :key="item.id" class="reminder-card">
            <div>
              <span :class="['status-pill', statusClass(item.status)]">{{ statusLabel(item.status) }}</span>
              <h3>{{ item.drugName || '药品' }}</h3>
              <p>{{ formatTime(item.remindTime) }} · {{ item.dosage || '-' }}</p>
              <small>{{ item.aiReminderText || '暂无提醒说明' }}</small>
            </div>
            <div v-if="item.status === 'WAITING' || item.status === 'SNOOZED'" class="row-actions">
              <button class="ghost-button" type="button" :disabled="busyId === item.id" @click="act(item.id, 'snooze')">
                稍后提醒
              </button>
              <button class="ghost-button" type="button" :disabled="busyId === item.id" @click="act(item.id, 'missed')">
                已错过
              </button>
              <button class="primary-button" type="button" :disabled="busyId === item.id" @click="act(item.id, 'taken')">
                已服药
              </button>
            </div>
          </article>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.reminder-toolbar {
  grid-template-columns: 200px;
}

.care-layout {
  display: grid;
  grid-template-columns: minmax(260px, 420px) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.plan-list,
.reminder-list {
  display: grid;
  gap: 12px;
  max-height: 500px;
  overflow-y: auto;
  padding-right: 8px;
}

.mini-card,
.reminder-card {
  display: grid;
  gap: 8px;
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--panel-soft);
}

.mini-card p,
.reminder-card h3,
.reminder-card p {
  margin: 0;
}

.mini-card small,
.reminder-card small,
.ai-text {
  color: var(--muted);
}

.reminder-card {
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
}

.row-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

@media (max-width: 920px) {
  .care-layout,
  .reminder-card {
    grid-template-columns: 1fr;
  }
}
</style>
