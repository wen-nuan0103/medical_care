<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createConsultation, getDoctorCardPlans, purchaseCard, getMyCards } from '@/api/consultation'
import type { CardPlan, PrivateDoctorCard } from '@/types/api'

const route = useRoute()
const router = useRouter()

const doctorId = Number(route.query.doctorId)
const plans = ref<CardPlan[]>([])
const myCards = ref<PrivateDoctorCard[]>([])
const activeCard = ref<PrivateDoctorCard | null>(null)
const chiefComplaint = ref('')
const diseaseDesc = ref('')
const loading = ref(true)
const submitting = ref(false)
const error = ref('')

// 购买相关
const showPurchase = ref(false)
const purchasing = ref(false)

onMounted(async () => {
  try {
    const [cardsRes, plansRes] = await Promise.all([
      getMyCards(),
      getDoctorCardPlans(doctorId),
    ])
    myCards.value = cardsRes
    plans.value = plansRes.filter(p => p.status === 1)
    activeCard.value = cardsRes.find(c => c.doctorId === doctorId && c.status === 'ACTIVE') ?? null
  } catch { /* empty */ } finally {
    loading.value = false
  }
})

async function handleSubmit() {
  if (submitting.value) return
  submitting.value = true
  error.value = ''
  try {
    const session = await createConsultation(doctorId, chiefComplaint.value, diseaseDesc.value)
    router.push({ name: 'chat', params: { sessionId: session.id } })
  } catch (e) {
    error.value = e instanceof Error ? e.message : '发起失败'
  } finally {
    submitting.value = false
  }
}

async function handlePurchase(planId: number) {
  purchasing.value = true
  try {
    await purchaseCard(doctorId, planId)
    // 刷新卡片列表
    myCards.value = await getMyCards()
    activeCard.value = myCards.value.find(c => c.doctorId === doctorId && c.status === 'ACTIVE') ?? null
    showPurchase.value = false
  } catch (e) {
    error.value = e instanceof Error ? e.message : '购买失败'
  } finally {
    purchasing.value = false
  }
}

function cardTypeLabel(t: string) {
  const m: Record<string, string> = { ONCE: '单次', MONTH: '月卡', QUARTER: '季卡', HALF_YEAR: '半年卡', YEAR: '年卡' }
  return m[t] ?? t
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>发起问诊</h1>
        <p>请填写主诉和病情描述</p>
      </div>
      <button class="ghost-button" type="button" @click="router.back()">← 返回</button>
    </div>

    <div v-if="loading" class="empty-text">加载中...</div>

    <template v-else>
      <!-- 无有效卡提示 -->
      <div v-if="!activeCard" class="no-card-panel">
        <div>
          <strong>您尚未购买该医生的服务卡</strong>
          <p>请先购买一张私人医生服务卡才能发起问诊</p>
        </div>
        <button class="primary-button" type="button" @click="showPurchase = true">购买服务卡</button>
      </div>

      <!-- 有卡，显示表单 -->
      <template v-else>
        <div class="active-card-tip">
          <span class="status-pill success">已持有服务卡</span>
          <span>{{ activeCard.planName }} · 剩余 {{ activeCard.remainingTimes }} 次 / {{ activeCard.remainingMinutes }} 分钟</span>
        </div>

        <form class="consult-form" @submit.prevent="handleSubmit">
          <label>
            主诉 *
            <input v-model="chiefComplaint" placeholder="简要描述您的主要症状" required />
          </label>
          <label>
            病情描述
            <textarea v-model="diseaseDesc" rows="4" placeholder="详细描述您的病情、持续时间、已服用药物等"></textarea>
          </label>
          <p v-if="error" class="error-text">{{ error }}</p>
          <button class="primary-button" type="submit" :disabled="submitting">
            {{ submitting ? '提交中...' : '发起问诊' }}
          </button>
        </form>
      </template>

      <!-- 购买服务卡弹窗 -->
      <div v-if="showPurchase" class="modal-overlay" @click.self="showPurchase = false">
        <div class="modal-panel">
          <h3>选择服务卡套餐</h3>
          <div v-if="plans.length === 0" class="empty-text">该医生暂未配置服务卡</div>
          <div v-for="plan in plans" :key="plan.id" class="plan-card">
            <div class="plan-info">
              <strong>{{ plan.planName }}</strong>
              <small>{{ cardTypeLabel(plan.cardType) }} · {{ plan.validDays }}天 · {{ plan.consultationTimes }}次 · {{ plan.totalMinutes }}分钟</small>
              <p v-if="plan.description" class="muted-text">{{ plan.description }}</p>
            </div>
            <div class="plan-action">
              <span class="plan-price">¥{{ plan.price.toFixed(2) }}</span>
              <button class="primary-button" type="button" :disabled="purchasing" @click="handlePurchase(plan.id)">购买</button>
            </div>
          </div>
          <p v-if="error" class="error-text">{{ error }}</p>
          <div class="modal-footer">
            <button class="ghost-button" type="button" @click="showPurchase = false">关闭</button>
          </div>
        </div>
      </div>
    </template>
  </section>
</template>

<style scoped>
.no-card-panel {
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
.no-card-panel strong {
  font-size: 16px;
}
.no-card-panel p {
  margin: 4px 0 0;
  color: var(--muted);
}
.active-card-tip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  border: 1px solid #c6e9d6;
  border-radius: 8px;
  background: #e7f5ee;
  font-size: 14px;
}
.consult-form {
  display: grid;
  gap: 16px;
  max-width: 640px;
}
.consult-form textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  font: inherit;
  resize: vertical;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: grid;
  place-items: center;
  z-index: 100;
}
.modal-panel {
  width: min(520px, 90vw);
  padding: 28px;
  border-radius: 12px;
  background: var(--panel);
  display: grid;
  gap: 16px;
  max-height: 80vh;
  overflow-y: auto;
}
.modal-panel h3 {
  margin: 0;
  font-size: 18px;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.plan-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--panel-soft);
}
.plan-info strong {
  display: block;
  font-size: 15px;
}
.plan-info small {
  color: var(--muted);
}
.plan-info p {
  margin: 4px 0 0;
  font-size: 13px;
}
.plan-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.plan-price {
  font-size: 20px;
  font-weight: 800;
  color: var(--primary);
}
</style>
