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
  } catch {
    records.value = []
  } finally {
    loading.value = false
  }
}

async function pick(item: Prescription) {
  try {
    current.value = await getPrescriptionDetail(item.id)
  } catch (error) {
    alert(error instanceof Error ? error.message : '加载处方详情失败')
  }
}

async function submit(item: Prescription) {
  if (!confirm('是否提交此处方供药剂师审核？')) return
  try {
    current.value = await submitPrescription(item.id)
    await load()
  } catch (error) {
    alert(error instanceof Error ? error.message : '提交失败')
  }
}

function statusLabel(value: string) {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    PENDING_AUDIT: '待审核',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    NEED_MODIFY: '需修改',
    PAID: '已支付',
    EXPIRED: '已过期',
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
  if (value === 'INSUFFICIENT') return '不足'
  if (value === 'LOW') return '偏低'
  return '充足'
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
        <h1>{{ isDoctor ? '医生处方' : '我的处方' }}</h1>
        <p>{{ isDoctor ? '创建并提交处方进行审核。' : '查看处方审核状态。' }}</p>
      </div>
      <button v-if="isDoctor" class="primary-button" type="button" @click="router.push('/doctor/sessions')">
        从会话创建
      </button>
    </div>

    <div class="toolbar rx-toolbar">
      <select v-model="status" @change="load">
        <option value="">所有状态</option>
        <option value="DRAFT">草稿</option>
        <option value="PENDING_AUDIT">待审核</option>
        <option value="APPROVED">已通过</option>
        <option value="REJECTED">已驳回</option>
        <option value="NEED_MODIFY">需修改</option>
      </select>
      <button class="primary-button" type="button" @click="load">刷新</button>
    </div>

    <div class="rx-layout">
      <div class="rx-list">
        <div v-if="loading" class="empty-text">加载中...</div>
        <div v-else-if="records.length === 0" class="empty-text">暂无处方。</div>
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
            <span>{{ item.items.length }} 种药品 / ¥{{ item.totalAmount }}</span>
            <span>{{ formatTime(item.createTime) }}</span>
          </button>
        </template>
      </div>

      <div class="rx-detail">
        <div v-if="!current" class="empty-text">选择处方以查看详情。</div>
        <template v-else>
          <div class="detail-head">
            <div>
              <h2>{{ current.diagnosis }}</h2>
              <p>{{ current.prescriptionNo }}</p>
            </div>
            <span :class="['status-pill', statusClass(current.status)]">{{ statusLabel(current.status) }}</span>
          </div>

          <div class="detail-grid">
            <span>患者: {{ current.patientName || '-' }}</span>
            <span>医生: {{ current.doctorName || '-' }}</span>
            <span>提交时间: {{ formatTime(current.submitTime) }}</span>
            <span>有效期至: {{ formatTime(current.validUntil) }}</span>
          </div>

          <div class="note">
            <strong>医生附言</strong>
            <p>{{ current.doctorNote || '无' }}</p>
          </div>
          <div class="note">
            <strong>用药说明</strong>
            <p>{{ current.patientInstruction || '无' }}</p>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>药品</th>
                  <th>数量</th>
                  <th>用法</th>
                  <th>疗程</th>
                  <th>库存</th>
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
                  <td>{{ drug.durationDays }} 天</td>
                  <td>
                    <span :class="['status-pill', stockClass(drug.stockStatus)]">
                      {{ stockLabel(drug.stockStatus) }}
                    </span>
                    <small>当前 {{ drug.currentStock ?? '-' }}</small>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="current.latestAudit" class="note audit-note">
            <strong>最新审核记录</strong>
            <p>{{ current.latestAudit.advice || '暂无建议。' }}</p>
            <small>{{ current.latestAudit.stockResult }}</small>
            <small>{{ current.latestAudit.interactionResult }}</small>
          </div>

          <div class="detail-actions">
            <button
              v-if="!isDoctor && current.status === 'APPROVED'"
              class="primary-button"
              type="button"
              @click="router.push(`/patient/prescriptions/${current.id}/purchase`)"
            >
              购买药品
            </button>
            <button
              v-if="isDoctor && ['DRAFT', 'REJECTED', 'NEED_MODIFY'].includes(current.status)"
              class="primary-button"
              type="button"
              @click="submit(current)"
            >
              提交审核
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
