<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { createHealthTrack, getPatientFollowUps, getPatientHealthTracks } from '@/api/care'
import type { FollowUpPlan, HealthTrackRecord } from '@/types/api'

const records = ref<HealthTrackRecord[]>([])
const followUps = ref<FollowUpPlan[]>([])
const loading = ref(false)
const saving = ref(false)
const form = ref({
  recordDate: new Date().toISOString().slice(0, 10),
  symptom: '',
  temperature: null as number | null,
  systolicPressure: null as number | null,
  diastolicPressure: null as number | null,
  heartRate: null as number | null,
  bloodGlucose: null as number | null,
  medicationFeedback: '',
})

onMounted(load)

async function load() {
  loading.value = true
  try {
    records.value = await getPatientHealthTracks()
    followUps.value = await getPatientFollowUps()
  } catch {
    records.value = []
    followUps.value = []
  } finally {
    loading.value = false
  }
}

async function submit() {
  const hasSymptom = !!form.value.symptom.trim()
  const hasVitals = form.value.temperature !== null
    || form.value.systolicPressure !== null
    || form.value.diastolicPressure !== null
    || form.value.heartRate !== null
    || form.value.bloodGlucose !== null
  const hasFeedback = !!form.value.medicationFeedback.trim()

  if (!hasSymptom && !hasVitals && !hasFeedback) {
    alert('请至少填写症状描述、一项体征指标或用药反馈。')
    return
  }
  saving.value = true
  try {
    await createHealthTrack({
      recordDate: form.value.recordDate,
      symptom: form.value.symptom,
      temperature: cleanNumber(form.value.temperature),
      systolicPressure: cleanNumber(form.value.systolicPressure),
      diastolicPressure: cleanNumber(form.value.diastolicPressure),
      heartRate: cleanNumber(form.value.heartRate),
      bloodGlucose: cleanNumber(form.value.bloodGlucose),
      medicationFeedback: form.value.medicationFeedback,
    })
    form.value.symptom = ''
    form.value.temperature = null
    form.value.systolicPressure = null
    form.value.diastolicPressure = null
    form.value.heartRate = null
    form.value.bloodGlucose = null
    form.value.medicationFeedback = ''
    await load()
  } catch (error) {
    alert(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

function cleanNumber(value: unknown) {
  if (value === '' || value === null || value === undefined) return null
  return Number(value)
}

function statusLabel(value: string) {
  const map: Record<string, string> = { DONE: '已完成', PENDING: '待随访', CANCELED: '已取消' }
  return map[value] ?? value
}

function statusClass(value: string) {
  if (value === 'DONE') return 'success'
  if (value === 'PENDING') return 'warning'
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
        <h1>健康跟踪</h1>
        <p>记录症状、体征指标、用药反馈和随访计划。</p>
      </div>
      <button class="primary-button" type="button" @click="load">刷新</button>
    </div>

    <div class="track-layout">
      <div class="work-panel form-panel">
        <h2>新建记录</h2>
        <label>
          记录日期
          <input v-model="form.recordDate" type="date" />
        </label>
        <label>
          症状描述
          <input v-model="form.symptom" placeholder="咳嗽、发热、头晕..." />
        </label>
        <div class="form-grid">
          <label>
            体温（℃）
            <input v-model.number="form.temperature" type="number" min="30" max="45" step="0.1" />
          </label>
          <label>
            收缩压
            <input v-model.number="form.systolicPressure" type="number" min="50" max="260" />
          </label>
          <label>
            舒张压
            <input v-model.number="form.diastolicPressure" type="number" min="30" max="180" />
          </label>
          <label>
            心率
            <input v-model.number="form.heartRate" type="number" min="30" max="220" />
          </label>
          <label>
            血糖
            <input v-model.number="form.bloodGlucose" type="number" min="1" max="40" step="0.01" />
          </label>
        </div>
        <label>
          用药反馈
          <input v-model="form.medicationFeedback" placeholder="副作用或改善情况" />
        </label>
        <button class="primary-button" type="button" :disabled="saving" @click="submit">保存记录</button>
      </div>

      <div class="work-panel">
        <h2>最近记录</h2>
        <div v-if="loading" class="empty-text">加载中...</div>
        <div v-else-if="records.length === 0" class="empty-text">暂无健康记录。</div>
        <div v-else class="record-list">
          <article v-for="item in records" :key="item.id" class="record-card">
            <div class="card-topline">
              <strong>{{ item.recordDate }}</strong>
              <span :class="['status-pill', item.abnormalFlag ? 'warning' : 'success']">
                {{ item.abnormalFlag ? '需关注' : '正常' }}
              </span>
            </div>
            <p>{{ item.symptom || '无症状描述' }}</p>
            <small>
              体温 {{ item.temperature ?? '-' }} · 血压 {{ item.systolicPressure ?? '-' }}/{{ item.diastolicPressure ?? '-' }}
              · 心率 {{ item.heartRate ?? '-' }} · 血糖 {{ item.bloodGlucose ?? '-' }}
            </small>
            <p class="ai-text">{{ item.aiAnalysisText || '暂无AI分析' }}</p>
          </article>
        </div>
      </div>

      <div class="work-panel">
        <h2>随访计划</h2>
        <div v-if="followUps.length === 0" class="empty-text">暂无随访计划。</div>
        <div v-else class="record-list">
          <article v-for="item in followUps" :key="item.id" class="record-card">
            <div class="card-topline">
              <strong>{{ formatTime(item.planTime) }}</strong>
              <span :class="['status-pill', statusClass(item.status)]">{{ statusLabel(item.status) }}</span>
            </div>
            <p>{{ item.content }}</p>
            <small>{{ item.doctorName || '医生' }}</small>
          </article>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.track-layout {
  display: grid;
  grid-template-columns: minmax(280px, 420px) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.form-panel {
  display: grid;
  gap: 12px;
}

.record-list {
  display: grid;
  gap: 12px;
  max-height: 500px;
  overflow-y: auto;
  padding-right: 8px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.record-card {
  display: grid;
  gap: 8px;
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--panel-soft);
}

.record-card p {
  margin: 0;
}

.record-card small,
.ai-text {
  color: var(--muted);
}

@media (max-width: 960px) {
  .track-layout,
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
