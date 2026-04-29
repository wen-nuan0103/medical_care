<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  auditPrescription,
  getPendingAuditPrescriptions,
  getPrescriptionAuditCheck,
} from '@/api/prescription'
import type { Prescription, PrescriptionAuditCheck } from '@/types/api'

const loading = ref(false)
const prescriptions = ref<Prescription[]>([])
const selected = ref<Prescription | null>(null)
const check = ref<PrescriptionAuditCheck | null>(null)
const auditResult = ref('APPROVED')
const advice = ref('')
const submitting = ref(false)

onMounted(loadPending)

async function loadPending() {
  loading.value = true
  try {
    prescriptions.value = await getPendingAuditPrescriptions()
    if (selected.value && !prescriptions.value.some((item) => item.id === selected.value?.id)) {
      selected.value = null
      check.value = null
    }
  } finally {
    loading.value = false
  }
}

async function selectPrescription(prescription: Prescription) {
  selected.value = prescription
  check.value = await getPrescriptionAuditCheck(prescription.id)
  auditResult.value = check.value.riskLevel === 'FORBIDDEN' ? 'REJECTED' : 'APPROVED'
  advice.value = check.value.summary
}

async function submitAudit() {
  if (!selected.value) return
  submitting.value = true
  try {
    await auditPrescription(selected.value.id, auditResult.value, advice.value)
    selected.value = null
    check.value = null
    advice.value = ''
    await loadPending()
  } catch (error) {
    alert(error instanceof Error ? error.message : '审核失败')
  } finally {
    submitting.value = false
  }
}

function statusLabel(value: string) {
  const map: Record<string, string> = {
    PENDING_AUDIT: '待审核',
    APPROVED: '审核通过',
    REJECTED: '已驳回',
    NEED_MODIFY: '需修改',
  }
  return map[value] ?? value
}

function riskClass(value: string | null | undefined) {
  if (value === 'FORBIDDEN' || value === 'HIGH') return 'danger'
  if (value === 'MEDIUM' || value === 'LOW') return 'warning'
  return 'success'
}

function formatDate(value: string | null) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-'
}
</script>

<template>
  <section class="page audit-page">
    <div class="page-header">
      <div>
        <h1>处方审核</h1>
        <p>复核库存、药物相互作用、剂量和用药建议，最终审核结果由药剂师提交。</p>
      </div>
      <button class="primary-button" type="button" @click="loadPending">刷新</button>
    </div>

    <div class="audit-layout">
      <div class="audit-list">
        <div v-if="loading" class="empty-text">加载中...</div>
        <div v-else-if="prescriptions.length === 0" class="empty-text">暂无待审核处方</div>
        <template v-else>
          <button
            v-for="prescription in prescriptions"
            :key="prescription.id"
            class="audit-card"
            type="button"
            @click="selectPrescription(prescription)"
          >
            <span class="status-pill warning">{{ statusLabel(prescription.status) }}</span>
            <strong>{{ prescription.diagnosis }}</strong>
            <small>{{ prescription.prescriptionNo }}</small>
            <span>{{ prescription.patientName }} / {{ prescription.doctorName }}</span>
            <span>提交：{{ formatDate(prescription.submitTime) }}</span>
          </button>
        </template>
      </div>

      <div class="audit-detail">
        <div v-if="!selected" class="empty-text">选择一张待审处方开始审核</div>
        <template v-else>
          <div class="detail-head">
            <div>
              <h2>{{ selected.diagnosis }}</h2>
              <p>{{ selected.prescriptionNo }}</p>
            </div>
            <span :class="['status-pill', riskClass(check?.riskLevel)]">
              综合风险 {{ check?.riskLevel || '-' }}
            </span>
          </div>

          <div class="meta-grid">
            <span>患者：{{ selected.patientName || '-' }}</span>
            <span>医生：{{ selected.doctorName || '-' }}</span>
            <span>药品：{{ selected.items.length }} 种</span>
            <span>金额：¥{{ selected.totalAmount }}</span>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>药品</th>
                  <th>数量</th>
                  <th>剂量/频次</th>
                  <th>疗程</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in selected.items" :key="item.id">
                  <td>
                    <strong>{{ item.drugName }}</strong>
                    <small>{{ item.specification }}</small>
                  </td>
                  <td>{{ item.quantity }}</td>
                  <td>{{ item.dosage }} · {{ item.frequency }} · {{ item.usageMethod }}</td>
                  <td>{{ item.durationDays }} 天</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="check" class="check-grid">
            <div class="check-panel">
              <strong>库存检查</strong>
              <p>{{ check.stockResult }}</p>
            </div>
            <div class="check-panel">
              <strong>相互作用检查</strong>
              <p>{{ check.interactionResult }}</p>
            </div>
            <div class="check-panel">
              <strong>剂量复核提示</strong>
              <p>{{ check.dosageResult }}</p>
            </div>
            <div class="check-panel">
              <strong>审核摘要</strong>
              <p>{{ check.summary }}</p>
            </div>
          </div>

          <div class="audit-form">
            <label>
              审核结果
              <select v-model="auditResult">
                <option value="APPROVED">通过</option>
                <option value="REJECTED">驳回</option>
                <option value="NEED_MODIFY">要求医生修改</option>
              </select>
            </label>
            <label>
              药师意见
              <textarea v-model="advice"></textarea>
            </label>
            <div class="form-actions">
              <button class="primary-button" type="button" :disabled="submitting" @click="submitAudit">
                提交审核
              </button>
            </div>
          </div>
        </template>
      </div>
    </div>
  </section>
</template>

<style scoped>
.audit-layout {
  display: grid;
  grid-template-columns: minmax(260px, 360px) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.audit-list {
  display: grid;
  gap: 10px;
}

.audit-card {
  display: grid;
  gap: 7px;
  width: 100%;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 8px;
  text-align: left;
  color: var(--text);
  background: var(--panel);
}

.audit-card:hover {
  border-color: var(--primary);
}

.audit-card small,
.detail-head p,
.meta-grid {
  color: var(--muted);
}

.audit-detail {
  min-height: 480px;
  padding: 20px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--panel);
}

.detail-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.detail-head h2,
.detail-head p {
  margin: 0;
}

.meta-grid,
.check-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 10px;
  margin: 16px 0;
}

.check-panel {
  display: grid;
  gap: 8px;
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--panel-soft);
}

.check-panel p {
  margin: 0;
  line-height: 1.6;
}

.audit-form {
  display: grid;
  gap: 14px;
  margin-top: 16px;
}

textarea {
  min-height: 96px;
  resize: vertical;
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  font: inherit;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.status-pill.danger {
  color: var(--danger);
  background: #fef2f2;
}

@media (max-width: 920px) {
  .audit-layout {
    grid-template-columns: 1fr;
  }
}
</style>
