<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getMyCardPlans, saveCardPlan, updateCardPlan } from '@/api/consultation'
import type { CardPlan } from '@/types/api'

const plans = ref<CardPlan[]>([])
const loading = ref(true)
const showForm = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const error = ref('')

const form = ref({
  cardType: 'MONTH',
  planName: '',
  price: 0,
  validDays: 30,
  consultationTimes: 4,
  totalMinutes: 120,
  singleMinutes: 30,
  giftLimitMinutes: 30,
  description: '',
})

const cardTypes = [
  { value: 'ONCE', label: '单次' },
  { value: 'MONTH', label: '月卡' },
  { value: 'QUARTER', label: '季卡' },
  { value: 'HALF_YEAR', label: '半年卡' },
  { value: 'YEAR', label: '年卡' },
]

onMounted(async () => {
  await reload()
})

async function reload() {
  loading.value = true
  try {
    plans.value = await getMyCardPlans()
  } catch { /* empty */ } finally {
    loading.value = false
  }
}

function openAdd() {
  editingId.value = null
  form.value = { cardType: 'MONTH', planName: '', price: 0, validDays: 30, consultationTimes: 4, totalMinutes: 120, singleMinutes: 30, giftLimitMinutes: 30, description: '' }
  showForm.value = true
}

function openEdit(plan: CardPlan) {
  editingId.value = plan.id
  form.value = {
    cardType: plan.cardType,
    planName: plan.planName,
    price: plan.price,
    validDays: plan.validDays,
    consultationTimes: plan.consultationTimes,
    totalMinutes: plan.totalMinutes,
    singleMinutes: plan.singleMinutes,
    giftLimitMinutes: plan.giftLimitMinutes,
    description: plan.description || '',
  }
  showForm.value = true
}

async function handleSave() {
  if (saving.value) return
  saving.value = true
  error.value = ''
  try {
    if (editingId.value) {
      await updateCardPlan(editingId.value, form.value)
    } else {
      await saveCardPlan(form.value)
    }
    showForm.value = false
    await reload()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  } finally {
    saving.value = false
  }
}

function cardTypeLabel(t: string) {
  return cardTypes.find(c => c.value === t)?.label ?? t
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>服务卡配置</h1>
        <p>管理您的私人医生服务卡套餐</p>
      </div>
      <button class="primary-button" type="button" @click="openAdd">+ 新增套餐</button>
    </div>

    <div v-if="loading" class="empty-text">加载中...</div>
    <div v-else-if="plans.length === 0" class="empty-text">暂无套餐，点击上方按钮新增</div>

    <div class="plans-grid" v-else>
      <div v-for="plan in plans" :key="plan.id" class="plan-tile">
        <div class="pt-header">
          <span class="pt-type">{{ cardTypeLabel(plan.cardType) }}</span>
          <span :class="['status-pill', plan.status === 1 ? 'success' : 'muted']">{{ plan.status === 1 ? '上架' : '下架' }}</span>
        </div>
        <strong class="pt-name">{{ plan.planName }}</strong>
        <div class="pt-price">¥{{ plan.price.toFixed(2) }}</div>
        <ul class="pt-features">
          <li>有效期 {{ plan.validDays }} 天</li>
          <li>{{ plan.consultationTimes }} 次问诊</li>
          <li>总时长 {{ plan.totalMinutes }} 分钟</li>
          <li>单次上限 {{ plan.singleMinutes }} 分钟</li>
        </ul>
        <button class="ghost-button" type="button" @click="openEdit(plan)">编辑</button>
      </div>
    </div>

    <!-- 编辑/新增弹窗 -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-panel">
        <h3>{{ editingId ? '编辑套餐' : '新增套餐' }}</h3>
        <form class="plan-form" @submit.prevent="handleSave">
          <label>
            套餐名称
            <input v-model="form.planName" required />
          </label>
          <label>
            卡型
            <select v-model="form.cardType">
              <option v-for="ct in cardTypes" :key="ct.value" :value="ct.value">{{ ct.label }}</option>
            </select>
          </label>
          <div class="form-row">
            <label>
              价格（元）
              <input v-model.number="form.price" type="number" step="0.01" min="0" required />
            </label>
            <label>
              有效天数
              <input v-model.number="form.validDays" type="number" min="1" required />
            </label>
          </div>
          <div class="form-row">
            <label>
              问诊次数
              <input v-model.number="form.consultationTimes" type="number" min="1" required />
            </label>
            <label>
              总时长（分钟）
              <input v-model.number="form.totalMinutes" type="number" min="1" required />
            </label>
          </div>
          <div class="form-row">
            <label>
              单次上限（分钟）
              <input v-model.number="form.singleMinutes" type="number" min="1" required />
            </label>
            <label>
              可赠送上限（分钟）
              <input v-model.number="form.giftLimitMinutes" type="number" min="0" />
            </label>
          </div>
          <label>
            服务说明
            <textarea v-model="form.description" rows="2"></textarea>
          </label>
          <p v-if="error" class="error-text">{{ error }}</p>
          <div class="modal-footer">
            <button class="ghost-button" type="button" @click="showForm = false">取消</button>
            <button class="primary-button" type="submit" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<style scoped>
.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}
.plan-tile {
  display: grid;
  gap: 10px;
  padding: 22px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--panel);
}
.pt-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pt-type {
  font-size: 12px;
  color: var(--muted);
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 0.06em;
}
.pt-name {
  font-size: 17px;
}
.pt-price {
  font-size: 26px;
  font-weight: 800;
  color: var(--primary);
}
.pt-features {
  margin: 0;
  padding-left: 18px;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.8;
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
  max-height: 85vh;
  overflow-y: auto;
}
.modal-panel h3 {
  margin: 0;
}
.plan-form {
  display: grid;
  gap: 14px;
}
.plan-form textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  font: inherit;
  resize: vertical;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
