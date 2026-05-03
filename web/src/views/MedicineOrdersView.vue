<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { cancelMedicineOrder, getMedicineOrderDetail, getMedicineOrders, payMedicineOrder } from '@/api/medicineOrder'
import type { MedicineOrder } from '@/types/api'

const status = ref('')
const loading = ref(false)
const orders = ref<MedicineOrder[]>([])
const current = ref<MedicineOrder | null>(null)
const busy = ref(false)

onMounted(loadOrders)

async function loadOrders() {
  loading.value = true
  try {
    orders.value = await getMedicineOrders(status.value || undefined)
  } finally {
    loading.value = false
  }
}

async function pick(order: MedicineOrder) {
  current.value = await getMedicineOrderDetail(order.id)
}

async function pay(order: MedicineOrder) {
  if (!confirm('Pay this order with the selected insurance card?')) return
  busy.value = true
  try {
    current.value = await payMedicineOrder(order.id, order.insuranceCardId)
    await loadOrders()
  } catch (error) {
    alert(error instanceof Error ? error.message : 'Payment failed')
  } finally {
    busy.value = false
  }
}

async function cancel(order: MedicineOrder) {
  if (!confirm('Cancel this unpaid order?')) return
  busy.value = true
  try {
    current.value = await cancelMedicineOrder(order.id)
    await loadOrders()
  } catch (error) {
    alert(error instanceof Error ? error.message : 'Cancel failed')
  } finally {
    busy.value = false
  }
}

function statusClass(value: string) {
  if (value === 'PAID' || value === 'COMPLETED') return 'success'
  if (value === 'UNPAID') return 'warning'
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
        <h1>Medicine Orders</h1>
        <p>View medicine orders and pay with a mock insurance card.</p>
      </div>
      <button class="primary-button" type="button" @click="loadOrders">Refresh</button>
    </div>

    <div class="toolbar order-toolbar">
      <select v-model="status" @change="loadOrders">
        <option value="">All status</option>
        <option value="UNPAID">Unpaid</option>
        <option value="PAID">Paid</option>
        <option value="CANCELED">Canceled</option>
      </select>
    </div>

    <div class="order-layout">
      <div class="order-list">
        <div v-if="loading" class="empty-text">Loading...</div>
        <div v-else-if="orders.length === 0" class="empty-text">No medicine orders.</div>
        <template v-else>
          <button v-for="order in orders" :key="order.id" class="order-card" type="button" @click="pick(order)">
            <span :class="['status-pill', statusClass(order.status)]">{{ order.status }}</span>
            <strong>{{ order.diagnosis || 'Prescription order' }}</strong>
            <small>{{ order.orderNo }}</small>
            <span>Total ¥{{ order.totalAmount }} / Insurance ¥{{ order.insuranceAmount }}</span>
            <span>{{ formatTime(order.createTime) }}</span>
          </button>
        </template>
      </div>

      <div class="order-detail">
        <div v-if="!current" class="empty-text">Select an order to view details.</div>
        <template v-else>
          <div class="detail-head">
            <div>
              <h2>{{ current.diagnosis || 'Medicine order' }}</h2>
              <p>{{ current.orderNo }}</p>
            </div>
            <span :class="['status-pill', statusClass(current.status)]">{{ current.status }}</span>
          </div>

          <div class="amount-grid">
            <span>Total <strong>¥{{ current.totalAmount }}</strong></span>
            <span>Insurance <strong>¥{{ current.insuranceAmount }}</strong></span>
            <span>Self amount <strong>¥{{ current.selfAmount }}</strong></span>
            <span>Card <strong>{{ current.insuranceCardNo || '-' }}</strong></span>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Medicine</th>
                  <th>Qty</th>
                  <th>Amount</th>
                  <th>Insurance</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in current.items" :key="item.id">
                  <td>
                    <strong>{{ item.drugName }}</strong>
                    <small>{{ item.specification }}</small>
                  </td>
                  <td>{{ item.quantity }}</td>
                  <td>¥{{ item.amount }}</td>
                  <td>{{ item.insuranceCovered ? `¥${item.insuranceAmount}` : 'Self-pay' }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="current.paymentRecord" class="payment-box">
            <strong>Payment</strong>
            <p>{{ current.paymentRecord.paymentNo }}</p>
            <small>
              Paid ¥{{ current.paymentRecord.paidAmount }},
              balance {{ current.paymentRecord.beforeBalance }} -> {{ current.paymentRecord.afterBalance }}
            </small>
          </div>

          <div v-if="current.status === 'UNPAID'" class="detail-actions">
            <button class="ghost-button" type="button" :disabled="busy" @click="cancel(current)">Cancel</button>
            <button class="primary-button" type="button" :disabled="busy" @click="pay(current)">Pay insurance</button>
          </div>
        </template>
      </div>
    </div>
  </section>
</template>

<style scoped>
.order-toolbar {
  grid-template-columns: 180px;
}

.order-layout {
  display: grid;
  grid-template-columns: minmax(260px, 360px) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.order-list {
  display: grid;
  gap: 10px;
}

.order-card,
.order-detail {
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--panel);
}

.order-card {
  display: grid;
  gap: 7px;
  width: 100%;
  padding: 16px;
  color: var(--text);
  text-align: left;
}

.order-card:hover {
  border-color: var(--primary);
}

.order-detail {
  min-height: 420px;
  padding: 20px;
}

.detail-head,
.detail-actions {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.detail-head h2,
.detail-head p,
.payment-box p {
  margin: 0;
}

.detail-head p,
.order-card small,
.payment-box small {
  color: var(--muted);
}

.amount-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
  margin: 16px 0;
  color: var(--muted);
}

.amount-grid strong {
  display: block;
  color: var(--text);
}

.payment-box {
  display: grid;
  gap: 6px;
  margin-top: 14px;
}

.detail-actions {
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 900px) {
  .order-layout {
    grid-template-columns: 1fr;
  }
}
</style>
