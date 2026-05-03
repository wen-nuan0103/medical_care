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
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!form.value.cardNo || !form.value.holderName) {
    alert('Please enter card number and holder name.')
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
    alert(error instanceof Error ? error.message : 'Save failed')
  } finally {
    saving.value = false
  }
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
        <h1>Insurance Cards</h1>
        <p>Bind a mock insurance card and use it for online medicine orders.</p>
      </div>
      <button class="primary-button" type="button" @click="loadCards">Refresh</button>
    </div>

    <div class="card-layout">
      <div class="work-panel form-panel">
        <h2>Bind card</h2>
        <label>
          Card number
          <input v-model="form.cardNo" placeholder="MC-202604-0001" />
        </label>
        <label>
          Holder name
          <input v-model="form.holderName" placeholder="Patient name" />
        </label>
        <label>
          ID card
          <input v-model="form.holderIdCard" placeholder="Optional" />
        </label>
        <label>
          Balance
          <input v-model.number="form.balance" type="number" min="0" step="0.01" />
        </label>
        <label>
          Reimbursement rate
          <input v-model.number="form.reimbursementRate" type="number" min="0" max="1" step="0.01" />
        </label>
        <button class="primary-button" type="button" :disabled="saving" @click="submit">Bind</button>
      </div>

      <div class="card-list">
        <div v-if="loading" class="empty-text">Loading...</div>
        <div v-else-if="cards.length === 0" class="empty-text">No insurance cards yet.</div>
        <template v-else>
          <article v-for="card in cards" :key="card.id" class="data-card insurance-card">
            <div class="card-topline">
              <strong>{{ card.cardNo }}</strong>
              <span :class="['status-pill', statusClass(card.status)]">{{ card.status }}</span>
            </div>
            <p>{{ card.holderName }}</p>
            <div class="card-metrics">
              <span>Balance <strong>¥{{ card.balance }}</strong></span>
              <span>Rate <strong>{{ Math.round(card.reimbursementRate * 100) }}%</strong></span>
              <span>Bind time <strong>{{ formatTime(card.bindTime) }}</strong></span>
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
