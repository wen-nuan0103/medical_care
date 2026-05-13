<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { bindInsuranceCard, getInsuranceCards } from '@/api/insurance'
import type { InsuranceCard } from '@/types/api'

const cards = ref<InsuranceCard[]>([])
const loading = ref(false)
const saving = ref(false)
const form = ref({
  cardNo: '',
  holderName: '',
  holderIdCard: '',
  balance: 500,
  reimbursementRate: 0.7,
})

onMounted(loadCards)

async function loadCards() {
  loading.value = true
  try {
    cards.value = await getInsuranceCards()
  } catch {
    cards.value = []
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!form.value.cardNo || !form.value.holderName) {
    alert('请输入卡号和持卡人姓名。')
    return
  }
  saving.value = true
  try {
    await bindInsuranceCard({ ...form.value })
    form.value.cardNo = ''
    form.value.holderName = ''
    form.value.holderIdCard = ''
    form.value.balance = 500
    form.value.reimbursementRate = 0.7
    await loadCards()
  } catch (error) {
    alert(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

function statusLabel(status: string) {
  const m: Record<string, string> = { ACTIVE: '正常', FROZEN: '已冻结', CANCELED: '已注销' }
  return m[status] ?? status
}

function statusClass(status: string) {
  if (status === 'ACTIVE') return 'success'
  if (status === 'FROZEN') return 'warning'
  return 'danger'
}

function formatTime(value: string | null) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-'
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>医保卡</h1>
        <p>绑定模拟医保卡，用于在线购药结算。</p>
      </div>
      <button class="primary-button" type="button" @click="loadCards">刷新</button>
    </div>

    <div class="card-layout">
      <div class="work-panel form-panel">
        <h2>绑定医保卡</h2>
        <label>
          卡号
          <input v-model="form.cardNo" placeholder="MC-202604-0001" />
        </label>
        <label>
          持卡人姓名
          <input v-model="form.holderName" placeholder="请输入姓名" />
        </label>
        <label>
          身份证号
          <input v-model="form.holderIdCard" placeholder="选填" />
        </label>
        <label>
          余额
          <input v-model.number="form.balance" type="number" min="0" step="0.01" />
        </label>
        <label>
          报销比例
          <input v-model.number="form.reimbursementRate" type="number" min="0" max="1" step="0.01" />
        </label>
        <button class="primary-button" type="button" :disabled="saving" @click="submit">绑定</button>
      </div>

      <div class="card-list">
        <div v-if="loading" class="empty-text">加载中...</div>
        <div v-else-if="cards.length === 0" class="empty-text">暂无医保卡记录。</div>
        <template v-else>
          <article v-for="card in cards" :key="card.id" class="data-card insurance-card">
            <div class="card-topline">
              <strong>{{ card.cardNo }}</strong>
              <span :class="['status-pill', statusClass(card.status)]">{{ statusLabel(card.status) }}</span>
            </div>
            <p>{{ card.holderName }}</p>
            <div class="card-metrics">
              <span>余额 <strong>¥{{ card.balance }}</strong></span>
              <span>报销比例 <strong>{{ Math.round(card.reimbursementRate * 100) }}%</strong></span>
              <span>绑定时间 <strong>{{ formatTime(card.bindTime) }}</strong></span>
            </div>
          </article>
        </template>
      </div>
    </div>
  </section>
</template>

<style scoped>
.card-layout {
  display: grid;
  grid-template-columns: minmax(260px, 360px) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.form-panel {
  display: grid;
  gap: 14px;
}

.card-list {
  display: grid;
  gap: 12px;
}

.insurance-card {
  display: grid;
  gap: 10px;
}

.card-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
  color: var(--muted);
}

.card-metrics strong {
  display: block;
  color: var(--text);
}

.status-pill.danger {
  color: var(--danger);
  background: #fef2f2;
}

@media (max-width: 900px) {
  .card-layout {
    grid-template-columns: 1fr;
  }
}
</style>
