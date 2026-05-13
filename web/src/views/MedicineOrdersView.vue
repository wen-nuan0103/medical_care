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
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

async function pick(order: MedicineOrder) {
  try {
    current.value = await getMedicineOrderDetail(order.id)
  } catch (error) {
    alert(error instanceof Error ? error.message : '加载订单详情失败')
  }
}

async function pay(order: MedicineOrder) {
  if (!confirm('确认使用医保卡支付该订单？')) return
  busy.value = true
  try {
    current.value = await payMedicineOrder(order.id, order.insuranceCardId)
    await loadOrders()
  } catch (error) {
    alert(error instanceof Error ? error.message : '支付失败')
  } finally {
    busy.value = false
  }
}

async function cancel(order: MedicineOrder) {
  if (!confirm('确认取消该未支付订单？')) return
  busy.value = true
  try {
    current.value = await cancelMedicineOrder(order.id)
    await loadOrders()
  } catch (error) {
    alert(error instanceof Error ? error.message : '取消失败')
  } finally {
    busy.value = false
  }
}

function statusLabel(value: string) {
  const map: Record<string, string> = {
    UNPAID: '待支付',
    PAID: '已支付',
    CANCELED: '已取消',
    COMPLETED: '已完成',
  }
  return map[value] ?? value
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
        <h1>购药订单</h1>
        <p>查看购药订单并使用模拟医保卡支付。</p>
      </div>
      <button class="primary-button" type="button" @click="loadOrders">刷新</button>
    </div>

    <div class="toolbar order-toolbar">
      <select v-model="status" @change="loadOrders">
        <option value="">所有状态</option>
        <option value="UNPAID">待支付</option>
        <option value="PAID">已支付</option>
        <option value="CANCELED">已取消</option>
      </select>
    </div>

    <div class="order-layout">
      <div class="order-list">
        <div v-if="loading" class="empty-text">加载中...</div>
        <div v-else-if="orders.length === 0" class="empty-text">暂无购药订单。</div>
        <template v-else>
          <button v-for="order in orders" :key="order.id" class="order-card" type="button" @click="pick(order)">
            <span :class="['status-pill', statusClass(order.status)]">{{ statusLabel(order.status) }}</span>
            <strong>{{ order.diagnosis || '处方购药订单' }}</strong>
            <small>{{ order.orderNo }}</small>
            <span>总额 ¥{{ order.totalAmount }} / 医保 ¥{{ order.insuranceAmount }}</span>
            <span>{{ formatTime(order.createTime) }}</span>
          </button>
        </template>
      </div>

      <div class="order-detail">
        <div v-if="!current" class="empty-text">选择订单查看详情。</div>
        <template v-else>
          <div class="detail-head">
            <div>
              <h2>{{ current.diagnosis || '购药订单' }}</h2>
              <p>{{ current.orderNo }}</p>
            </div>
            <span :class="['status-pill', statusClass(current.status)]">{{ statusLabel(current.status) }}</span>
          </div>

          <div class="amount-grid">
            <span>总金额 <strong>¥{{ current.totalAmount }}</strong></span>
            <span>医保支付 <strong>¥{{ current.insuranceAmount }}</strong></span>
            <span>自费金额 <strong>¥{{ current.selfAmount }}</strong></span>
            <span>医保卡 <strong>{{ current.insuranceCardNo || '-' }}</strong></span>
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>药品</th>
                  <th>数量</th>
                  <th>金额</th>
                  <th>医保</th>
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
                  <td>{{ item.insuranceCovered ? `¥${item.insuranceAmount}` : '自费' }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-if="current.paymentRecord" class="payment-box">
            <strong>支付记录</strong>
            <p>{{ current.paymentRecord.paymentNo }}</p>
            <small>
              实付 ¥{{ current.paymentRecord.paidAmount }}，
              余额 {{ current.paymentRecord.beforeBalance }} → {{ current.paymentRecord.afterBalance }}
            </small>
          </div>

          <div v-if="current.status === 'UNPAID'" class="detail-actions">
            <button class="ghost-button" type="button" :disabled="busy" @click="cancel(current)">取消订单</button>
            <button class="primary-button" type="button" :disabled="busy" @click="pay(current)">医保支付</button>
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
