<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  getDoctorPrescriptions,
  getPatientPrescriptions,
  getPrescriptionDetail,
  submitPrescription,
} from '@/api/prescription'
import type { Prescription } from '@/types/api'

const props = defineProps<{ mode: 'patient' | 'doctor' }>()

const router = useRouter()
const status = ref('')
const loading = ref(false)
const records = ref<Prescription[]>([])
const current = ref<Prescription | null>(null)
const isDoctor = computed(() => props.mode === 'doctor')

onMounted(load)

async function load() {
  loading.value = true
  try {
    records.value = isDoctor.value
      ? await getDoctorPrescriptions(status.value || undefined)
      : await getPatientPrescriptions(status.value || undefined)
  } finally {
    loading.value = false
  }
}

async function pick(item: Prescription) {
  current.value = await getPrescriptionDetail(item.id)
}

async function submit(item: Prescription) {
  if (!confirm('Submit this prescription for pharmacist audit?')) return
  current.value = await submitPrescription(item.id)
  await load()
}

function statusLabel(value: string) {
  const map: Record<string, string> = {
    DRAFT: 'Draft',
    PENDING_AUDIT: 'Pending audit',
    APPROVED: 'Approved',
    REJECTED: 'Rejected',
    NEED_MODIFY: 'Need modify',
    PAID: 'Paid',
    EXPIRED: 'Expired',
  }
  return map[value] ?? value
}

function statusClass(value: string | null) {
  if (value === 'APPROVED') return 'success'
  if (value === 'PENDING_AUDIT' || value === 'NEED_MODIFY') return 'warning'
  if (value === 'REJECTED' || value === 'EXPIRED') return 'danger'
  return 'muted'
}

function stockLabel(value: string | null) {
  if (value === 'INSUFFICIENT') return 'Insufficient'
  if (value === 'LOW') return 'Low'
  return 'Enough'
}

function stockClass(value: string | null) {
  if (value === 'INSUFFICIENT') return 'danger'
  if (value === 'LOW') return 'warning'
  return 'success'
}

function formatTime(value: string | null) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-'
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>{{ isDoctor ? 'Doctor Prescriptions' : 'My Prescriptions' }}</h1>
        <p>{{ isDoctor ? 'Create and submit prescriptions for audit.' : 'View prescription audit status.' }}</p>
      </div>
      <button v-if="isDoctor" class="primary-button" type="button" @click="router.push('/doctor/sessions')">
        New from session
      </button>
    </div>

    <div class="toolbar rx-toolbar">
      <select v-model="status" @change="load">
        <option value="">All status</option>
        <option value="DRAFT">Draft</option>
        <option value="PENDING_AUDIT">Pending audit</option>
        <option value="APPROVED">Approved</option>
        <option value="REJECTED">Rejected</option>
        <option value="NEED_MODIFY">Need modify</option>
      </select>
      <button class="primary-button" type="button" @click="load">Refresh</button>
    </div>

    <div class="rx-layout">
      <div class="rx-list">
        <div v-if="loading" class="empty-text">Loading...</div>
        <div v-else-if="records.length === 0" class="empty-text">No prescriptions yet.</div>
        <template v-else>
          <button
            v-for="item in records"
            :key="item.id"
            class="rx-card"
            type="button"
            @click="pick(item)"
          >
            <span :class="['status-pill', statusClass(item.status)]">{{ statusLabel(item.status) }}</span>
            <strong>{{ item.diagnosis }}</strong>
            <small>{{ item.prescriptionNo }}</small>
            <span>{{ isDoctor ? item.patientName : item.doctorName }}</span>
            <span>{{ item.items.length }} medicines / ¥{{ item.totalAmount }}</span>
            <span>{{ formatTime(item.createTime) }}</span>
          </button>
        </template>
      </div>

      <div class="rx-detail">
        <div v-if="!current" class="empty-text">Select a prescription to view details.</div>
        <template v-else>
          <div class="detail-head">
            <div>
              <h2>{{ current.diagnosis }}</h2>
              <p>{{ current.prescriptionNo }}</p>
            </div>
            <span :class="['status-pill', statusClass(current.status)]">{{ statusLabel(current.status) }}</span>
          </div>

          <div class="detail-grid">
            <span>Patient: {{ current.patientName || '-' }}</span>
            <span>Doctor: {{ current.doctorName || '-' }}</span>
            <span>Submitted: {{ formatTime(current.submitTime) }}</span>
            <span>Valid until: {{ formatTime(current.validUntil) }}</span>
          </div>

          <div class="note">
            <strong>Doctor note</strong>
            <p>{{ current.doctorNote || 'None' }}</p>
          </div>
          <div class="note">
            <strong>Patient instruction</strong>
            <p>{{ current.patientInstruction || 'None' }}</p>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Medicine</th>
                  <th>Qty</th>
                  <th>Usage</th>
                  <th>Course</th>
                  <th>Stock</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="drug in current.items" :key="drug.id">
                  <td>
                    <strong>{{ drug.drugName }}</strong>
                    <small>{{ drug.specification }}</small>
                  </td>
                  <td>{{ drug.quantity }}</td>
                  <td>{{ drug.dosage }} / {{ drug.frequency }} / {{ drug.usageMethod }}</td>
                  <td>{{ drug.durationDays }} days</td>
                  <td>
                    <span :class="['status-pill', stockClass(drug.stockStatus)]">
                      {{ stockLabel(drug.stockStatus) }}
                    </span>
                    <small>Current {{ drug.currentStock ?? '-' }}</small>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="current.latestAudit" class="note audit-note">
            <strong>Latest audit</strong>
            <p>{{ current.latestAudit.advice || 'No advice.' }}</p>
            <small>{{ current.latestAudit.stockResult }}</small>
            <small>{{ current.latestAudit.interactionResult }}</small>
          </div>

          <div class="detail-actions">
            <button
              v-if="isDoctor && ['DRAFT', 'REJECTED', 'NEED_MODIFY'].includes(current.status)"
              class="primary-button"
              type="button"
              @click="submit(current)"
            >
              Submit audit
            </button>
          </div>
        </template>
      </div>
    </div>
  </section>
</template>

<style scoped>
.rx-toolbar {
  grid-template-columns: 180px auto;
  justify-content: start;
}

.rx-layout {
  display: grid;
  grid-template-columns: minmax(260px, 360px) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.rx-list {
  display: grid;
  gap: 10px;
}

.rx-card,
.rx-detail {
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--panel);
}

.rx-card {
  display: grid;
  gap: 7px;
  width: 100%;
  padding: 16px;
  color: var(--text);
  text-align: left;
}

.rx-card:hover {
  border-color: var(--primary);
}

.rx-card small,
.detail-head p,
.detail-grid,
.audit-note small {
  color: var(--muted);
}

.rx-detail {
  min-height: 420px;
  padding: 20px;
}

.detail-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.detail-head h2,
.detail-head p,
.note p {
  margin: 0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 8px;
  margin: 16px 0;
}

.note {
  display: grid;
  gap: 6px;
  margin: 14px 0;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.status-pill.danger {
  color: var(--danger);
  background: #fef2f2;
}

@media (max-width: 920px) {
  .rx-layout {
    grid-template-columns: 1fr;
  }
}
</style>
