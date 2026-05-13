<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  createFollowUp,
  getDoctorFollowUps,
  getDoctorPatientTracks,
  getDoctorPatients,
  updateFollowUpStatus,
} from '@/api/care'
import type { FollowUpPlan, HealthTrackRecord, PatientCare } from '@/types/api'

const patients = ref<PatientCare[]>([])
const selected = ref<PatientCare | null>(null)
const tracks = ref<HealthTrackRecord[]>([])
const followUps = ref<FollowUpPlan[]>([])
const loading = ref(false)
const saving = ref(false)
const form = ref({
  planTime: '',
  content: '',
})

const selectedName = computed(() => selected.value?.patientName || '患者')

onMounted(loadPatients)

async function loadPatients() {
  loading.value = true
  try {
    patients.value = await getDoctorPatients()
    const firstPatient = patients.value[0]
    if (!selected.value && firstPatient) {
      await pick(firstPatient)
    }
  } catch {
    patients.value = []
  } finally {
    loading.value = false
  }
}

async function pick(patient: PatientCare) {
  selected.value = patient
  try {
    tracks.value = await getDoctorPatientTracks(patient.patientId)
    followUps.value = await getDoctorFollowUps({ patientId: patient.patientId })
  } catch {
    tracks.value = []
    followUps.value = []
  }
}

async function submitFollowUp() {
  if (!selected.value || !form.value.planTime || !form.value.content) {
    alert('请选择患者并填写随访时间和内容。')
    return
  }
  saving.value = true
  try {
    await createFollowUp({
      patientId: selected.value.patientId,
      planTime: form.value.planTime,
      content: form.value.content,
    })
    form.value.content = ''
    form.value.planTime = ''
    await pick(selected.value)
    await loadPatients()
  } catch (error) {
    alert(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function finishFollowUp(item: FollowUpPlan) {
  await updateFollowUpStatus(item.id, 'DONE')
  if (selected.value) await pick(selected.value)
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
        <h1>我的患者</h1>
        <p>查看患者健康跟踪记录，管理随访计划。</p>
      </div>
      <button class="primary-button" type="button" @click="loadPatients">刷新</button>
    </div>

    <div class="patient-layout">
      <div class="patient-list">
        <div v-if="loading" class="empty-text">加载中...</div>
        <div v-else-if="patients.length === 0" class="empty-text">暂无关联患者。</div>
        <template v-else>
          <button
            v-for="patient in patients"
            :key="patient.patientId"
            class="patient-card"
            :class="{ active: selected?.patientId === patient.patientId }"
            type="button"
            @click="pick(patient)"
          >
            <strong>{{ patient.patientName || '患者' }}</strong>
            <small>{{ patient.phone || '-' }}</small>
            <span>用药计划：{{ patient.activeMedicationPlanCount }}</span>
            <span>待随访：{{ patient.pendingFollowUpCount }}</span>
            <span :class="['status-pill', patient.abnormalRecordCount ? 'warning' : 'success']">
              {{ patient.abnormalRecordCount }} 条异常记录
            </span>
          </button>
        </template>
      </div>

      <div class="detail-grid">
        <div class="work-panel">
          <h2>{{ selectedName }} 的健康记录</h2>
          <div v-if="tracks.length === 0" class="empty-text">暂无健康记录。</div>
          <div v-else class="record-list">
            <article v-for="item in tracks" :key="item.id" class="record-card">
              <div class="card-topline">
                <strong>{{ item.recordDate }}</strong>
                <span :class="['status-pill', item.abnormalFlag ? 'warning' : 'success']">
                  {{ item.abnormalFlag ? '需关注' : '正常' }}
                </span>
              </div>
              <p>{{ item.symptom || '无症状描述' }}</p>
              <small>
                血压 {{ item.systolicPressure ?? '-' }}/{{ item.diastolicPressure ?? '-' }}
                · 体温 {{ item.temperature ?? '-' }} · 心率 {{ item.heartRate ?? '-' }}
              </small>
              <p class="ai-text">{{ item.aiAnalysisText || '暂无AI分析' }}</p>
            </article>
          </div>
        </div>

        <div class="work-panel">
          <h2>创建随访</h2>
          <div class="follow-form">
            <label>
              随访时间
              <input v-model="form.planTime" type="datetime-local" />
            </label>
            <label>
              随访内容
              <input v-model="form.content" placeholder="随访目标和建议" />
            </label>
            <button class="primary-button" type="button" :disabled="saving || !selected" @click="submitFollowUp">
              创建
            </button>
          </div>

          <h2>随访计划</h2>
          <div v-if="followUps.length === 0" class="empty-text">暂无随访计划。</div>
          <div v-else class="record-list">
            <article v-for="item in followUps" :key="item.id" class="record-card">
              <div class="card-topline">
                <strong>{{ formatTime(item.planTime) }}</strong>
                <span :class="['status-pill', statusClass(item.status)]">{{ statusLabel(item.status) }}</span>
              </div>
              <p>{{ item.content }}</p>
              <button
                v-if="item.status === 'PENDING'"
                class="ghost-button small-action"
                type="button"
                @click="finishFollowUp(item)"
              >
                标记完成
              </button>
            </article>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.patient-layout {
  display: grid;
  grid-template-columns: minmax(260px, 340px) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.patient-list,
.detail-grid,
.follow-form {
  display: grid;
  gap: 12px;
}

.patient-list,
.record-list {
  max-height: 600px;
  overflow-y: auto;
  padding-right: 8px;
}

.record-list {
  display: grid;
  gap: 12px;
}

.detail-grid {
  grid-template-columns: minmax(0, 1fr) minmax(300px, 420px);
  align-items: start;
}

.patient-card,
.record-card {
  display: grid;
  gap: 8px;
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--panel);
  color: var(--text);
  text-align: left;
}

.patient-card.active,
.patient-card:hover {
  border-color: var(--primary);
}

.patient-card small,
.record-card small,
.ai-text {
  color: var(--muted);
}

.record-card p {
  margin: 0;
}

.small-action {
  width: fit-content;
}

@media (max-width: 1100px) {
  .patient-layout,
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
