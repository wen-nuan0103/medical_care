<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getInsuranceCards } from '@/api/insurance'
import { createMedicineOrder } from '@/api/medicineOrder'
import { getPrescriptionDetail } from '@/api/prescription'
import type { InsuranceCard, Prescription } from '@/types/api'

const route = useRoute()
const router = useRouter()
const prescriptionId = Number(route.params.id)
const loading = ref(true)
const creating = ref(false)
const prescription = ref<Prescription | null>(null)
const cards = ref<InsuranceCard[]>([])
const selectedCardId = ref<number | ''>('')

const activeCards = computed(() => cards.value.filter((card) => card.status === 'ACTIVE'))

onMounted(async () => {
  try {
    const [rx, cardList] = await Promise.all([
      getPrescriptionDetail(prescriptionId),
      getInsuranceCards(),
    ])
    prescription.value = rx
    cards.value = cardList
    selectedCardId.value = activeCards.value[0]?.id ?? ''
  } finally {
    loading.value = false
  }
})

async function createOrder() {
  if (!selectedCardId.value) {
    alert('请选择一张有效的医保卡。')
    return
  }
  creating.value = true
  try {
    await createMedicineOrder(prescriptionId, Number(selectedCardId.value))
    router.push('/patient/orders')
  } catch (error) {
    alert(error instanceof Error ? error.message : '创建订单失败')
  } finally {
    creating.value = false
  }
}

function formatTime(value: string | null) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-'
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>处方购药</h1>
        <p>使用已审核通过的处方创建购药订单。</p>
      </div>
      <button class="ghost-button" type="button" @click="router.back()">← 返回</button>
    </div>

    <div v-if="loading" class="empty-text">加载中...</div>
    <template v-else-if="prescription">
      <div class="purchase-grid">
        <div class="work-panel">
          <h2>{{ prescription.diagnosis }}</h2>
          <p class="muted-text">{{ prescription.prescriptionNo }}</p>
          <div class="info-grid">
            <span>医生：{{ prescription.doctorName || '-' }}</span>
            <span>有效期至：{{ formatTime(prescription.validUntil) }}</span>
            <span>总金额：¥{{ prescription.totalAmount }}</span>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>药品</th>
                  <th>数量</th>
                  <th>用法</th>
                  <th>库存</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in prescription.items" :key="item.id">
                  <td>
                    <strong>{{ item.drugName }}</strong>
                    <small>{{ item.specification }}</small>
                  </td>
                  <td>{{ item.quantity }}</td>
                  <td>{{ item.dosage }} / {{ item.frequency }}</td>
                  <td>{{ item.currentStock ?? '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="work-panel card-picker">
          <h2>选择医保卡</h2>
          <div v-if="activeCards.length === 0" class="empty-text">
            暂无有效医保卡。
          </div>
          <label v-else>
            选择卡片
            <select v-model="selectedCardId">
              <option v-for="card in activeCards" :key="card.id" :value="card.id">
                {{ card.cardNo }} / 余额 ¥{{ card.balance }}
              </option>
            </select>
          </label>
          <button class="primary-button" type="button" :disabled="creating || !selectedCardId" @click="createOrder">
            创建订单
          </button>
          <button class="ghost-button" type="button" @click="router.push('/patient/insurance-card')">
            管理医保卡
          </button>
        </div>
      </div>
    </template>
  </section>
</template>

<style scoped>
.purchase-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
  align-items: start;
}

.work-panel h2 {
  margin: 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 8px;
  margin: 16px 0;
  color: var(--muted);
}

.card-picker {
  display: grid;
  gap: 14px;
}

@media (max-width: 900px) {
  .purchase-grid {
    grid-template-columns: 1fr;
  }
}
</style>
