<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiGet } from '@/api/client'
import { getDoctorCardPlans, purchaseCard, getMyCards } from '@/api/consultation'
import type { DoctorDetail, CardPlan, PrivateDoctorCard } from '@/types/api'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const doctor = ref<DoctorDetail | null>(null)
const loading = ref(true)
const error = ref('')
const plans = ref<CardPlan[]>([])
const myCards = ref<PrivateDoctorCard[]>([])
const purchasing = ref(false)

const isPatient = computed(() => auth.user?.roles.includes('PATIENT'))
const activeCard = computed(() => myCards.value.find(c => c.doctorId === Number(route.params.id) && c.status === 'ACTIVE'))

onMounted(async () => {
  try {
    const doctorId = Number(route.params.id)
    doctor.value = await apiGet<DoctorDetail>(`/api/doctors/${doctorId}`)
    plans.value = (await getDoctorCardPlans(doctorId)).filter(p => p.status === 1)
    if (isPatient.value) {
      try { myCards.value = await getMyCards() } catch { /* ok */ }
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
})

async function handlePurchase(planId: number) {
  purchasing.value = true
  try {
    await purchaseCard(Number(route.params.id), planId)
    myCards.value = await getMyCards()
  } catch (e) {
    alert(e instanceof Error ? e.message : '购买失败')
  } finally {
    purchasing.value = false
  }
}

function goConsult() {
  router.push({ name: 'patient-new-consultation', query: { doctorId: route.params.id as string } })
}

function cardTypeLabel(t: string) {
  const m: Record<string, string> = { ONCE: '单次', MONTH: '月卡', QUARTER: '季卡', HALF_YEAR: '半年卡', YEAR: '年卡' }
  return m[t] ?? t
}

function auditLabel(status: string) {
  const map: Record<string, string> = { APPROVED: '已认证', PENDING: '审核中', REJECTED: '未通过' }
  return map[status] ?? status
}
function auditClass(status: string) {
  return status === 'APPROVED' ? 'success' : status === 'REJECTED' ? 'muted' : 'warning'
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>医生详情</h1>
        <p>查看医生档案、专长和接诊状态</p>
      </div>
      <button class="ghost-button" type="button" @click="router.back()">← 返回</button>
    </div>

    <div v-if="loading" class="empty-text">加载中...</div>
    <div v-else-if="error" class="error-text">{{ error }}</div>

    <template v-else-if="doctor">
      <!-- 基本信息卡 -->
      <div class="detail-hero">
        <div class="avatar-circle">{{ doctor.realName.slice(-1) }}</div>
        <div class="hero-info">
          <h2>{{ doctor.realName }}</h2>
          <p class="hero-sub">{{ doctor.title }} · {{ doctor.department }} · {{ doctor.hospital }}</p>
          <div class="hero-tags">
            <span :class="['status-pill', doctor.serviceStatus === 1 ? 'success' : 'muted']">
              {{ doctor.serviceStatus === 1 ? '服务中' : '停诊' }}
            </span>
            <span :class="['status-pill', auditClass(doctor.auditStatus)]">
              {{ auditLabel(doctor.auditStatus) }}
            </span>
          </div>
        </div>
        <div class="hero-stats">
          <div class="stat-item">
            <strong>{{ doctor.score ?? '-' }}</strong>
            <span>综合评分</span>
          </div>
          <div class="stat-item">
            <strong>{{ doctor.consultationCount }}</strong>
            <span>累计接诊</span>
          </div>
        </div>
      </div>

      <!-- 详细信息 -->
      <div class="detail-grid">
        <div class="detail-section">
          <h3>擅长方向</h3>
          <p>{{ doctor.specialty || '暂无信息' }}</p>
        </div>
        <div class="detail-section">
          <h3>医生简介</h3>
          <p>{{ doctor.introduction || '暂无简介' }}</p>
        </div>
        <div class="detail-section">
          <h3>联系方式</h3>
          <p>{{ doctor.phone || '暂无电话' }}</p>
        </div>
        <div class="detail-section" v-if="doctor.licenseNo">
          <h3>执业证号</h3>
          <p>{{ doctor.licenseNo }}</p>
        </div>
      </div>

      <!-- 私人医生服务 -->
      <div v-if="isPatient" class="action-panel">
        <div v-if="activeCard">
          <strong>已持有服务卡：{{ activeCard.planName }}</strong>
          <p>剩余 {{ activeCard.remainingTimes }} 次问诊 / {{ activeCard.remainingMinutes }} 分钟</p>
        </div>
        <div v-else>
          <strong>预约问诊</strong>
          <p>购买私人医生服务卡，获得一对一在线问诊服务</p>
        </div>
        <button v-if="activeCard" class="primary-button" type="button" @click="goConsult">发起问诊</button>
      </div>

      <!-- 服务卡套餐列表 -->
      <div v-if="isPatient && plans.length > 0 && !activeCard" class="plans-section">
        <h3>可选服务卡套餐</h3>
        <div class="plans-row">
          <div v-for="plan in plans" :key="plan.id" class="plan-card-mini">
            <span class="pcm-type">{{ cardTypeLabel(plan.cardType) }}</span>
            <strong>{{ plan.planName }}</strong>
            <div class="pcm-price">¥{{ plan.price.toFixed(2) }}</div>
            <ul>
              <li>{{ plan.validDays }}天 · {{ plan.consultationTimes }}次 · {{ plan.totalMinutes }}分钟</li>
            </ul>
            <button class="primary-button" type="button" :disabled="purchasing" @click="handlePurchase(plan.id)">
              {{ purchasing ? '购买中...' : '立即购买' }}
            </button>
          </div>
        </div>
      </div>
    </template>
  </section>
</template>

<style scoped>
.detail-hero {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 28px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--panel);
  flex-wrap: wrap;
}

.avatar-circle {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  font-size: 32px;
  font-weight: 800;
  display: grid;
  place-items: center;
}

.hero-info {
  flex: 1;
  min-width: 200px;
}

.hero-info h2 {
  margin: 0 0 4px;
  font-size: 24px;
  font-weight: 800;
}

.hero-sub {
  margin: 0 0 10px;
  color: var(--muted);
}

.hero-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.hero-stats {
  display: flex;
  gap: 28px;
  margin-left: auto;
}

.stat-item {
  text-align: center;
}

.stat-item strong {
  display: block;
  font-size: 28px;
  font-weight: 800;
  color: var(--primary);
}

.stat-item span {
  color: var(--muted);
  font-size: 13px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 14px;
}

.detail-section {
  padding: 20px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--panel);
}

.detail-section h3 {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 700;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.detail-section p {
  margin: 0;
  line-height: 1.6;
}

.action-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 24px;
  border: 1px dashed var(--line);
  border-radius: 12px;
  background: var(--panel-soft);
  flex-wrap: wrap;
}

.action-panel strong {
  display: block;
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 4px;
}

.action-panel p {
  margin: 0;
  color: var(--muted);
}

.plans-section {
  display: grid;
  gap: 12px;
}
.plans-section h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}
.plans-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}
.plan-card-mini {
  display: grid;
  gap: 8px;
  padding: 18px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: var(--panel);
}
.pcm-type {
  font-size: 11px;
  color: var(--muted);
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 0.06em;
}
.pcm-price {
  font-size: 22px;
  font-weight: 800;
  color: var(--primary);
}
.plan-card-mini ul {
  margin: 0;
  padding-left: 16px;
  color: var(--muted);
  font-size: 13px;
}
</style>
