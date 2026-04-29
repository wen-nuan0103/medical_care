<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiGet } from '@/api/client'
import { getConsultationDetail } from '@/api/consultation'
import { createPrescription } from '@/api/prescription'
import type { ConsultationSession, DrugSummary, PageResult, PrescriptionItemPayload } from '@/types/api'

interface ItemForm {
  drugId: number | ''
  quantity: number
  dosage: string
  frequency: string
  durationDays: number
  usageMethod: string
  medicationTime: string
  remark: string
}

const route = useRoute()
const router = useRouter()
const rawSessionId = route.params.sessionId
const sessionId = Number(Array.isArray(rawSessionId) ? rawSessionId[0] : rawSessionId)

const loading = ref(true)
const saving = ref(false)
const session = ref<ConsultationSession | null>(null)
const drugs = ref<DrugSummary[]>([])
const diagnosis = ref('')
const doctorNote = ref('')
const patientInstruction = ref('')
const items = ref<ItemForm[]>([newItem()])

const totalAmount = computed(() => items.value.reduce((sum, item) => {
  const drug = findDrug(item.drugId)
  return sum + (drug ? Number(drug.price) * item.quantity : 0)
}, 0))

onMounted(async () => {
  try {
    const [sessionData, drugPage] = await Promise.all([
      getConsultationDetail(sessionId),
      apiGet<PageResult<DrugSummary>>('/api/drugs', { current: 1, pageSize: 100, status: 1 }),
    ])
    session.value = sessionData
    drugs.value = drugPage.records
  } finally {
    loading.value = false
  }
})

function newItem(): ItemForm {
  return {
    drugId: '',
    quantity: 1,
    dosage: '',
    frequency: '',
    durationDays: 3,
    usageMethod: '口服',
    medicationTime: '',
    remark: '',
  }
}

function addItem() {
  items.value.push(newItem())
}

function removeItem(index: number) {
  if (items.value.length === 1) return
  items.value.splice(index, 1)
}

function findDrug(drugId: number | '') {
  if (drugId === '') return undefined
  return drugs.value.find((drug) => drug.id === Number(drugId))
}

function stockLabel(item: ItemForm) {
  const drug = findDrug(item.drugId)
  if (!drug) return '请选择药品'
  if (drug.stockQuantity < item.quantity) return `库存不足：${drug.stockQuantity}`
  if (drug.stockQuantity <= drug.warningThreshold) return `库存预警：${drug.stockQuantity}`
  return `库存充足：${drug.stockQuantity}`
}

function stockClass(item: ItemForm) {
  const drug = findDrug(item.drugId)
  if (!drug) return 'muted'
  if (drug.stockQuantity < item.quantity) return 'danger'
  if (drug.stockQuantity <= drug.warningThreshold) return 'warning'
  return 'success'
}

async function save(submit: boolean) {
  if (!diagnosis.value.trim()) {
    alert('请填写诊断')
    return
  }
  const payloadItems = items.value.map((item) => {
    if (item.drugId === '') {
      throw new Error('请选择药品')
    }
    return {
      drugId: Number(item.drugId),
      quantity: item.quantity,
      dosage: item.dosage,
      frequency: item.frequency,
      durationDays: item.durationDays,
      usageMethod: item.usageMethod,
      medicationTime: item.medicationTime,
      remark: item.remark,
    } satisfies PrescriptionItemPayload
  })
  if (payloadItems.some((item) => !item.dosage || !item.frequency || !item.usageMethod)) {
    alert('请补全药品剂量、频次和用法')
    return
  }

  saving.value = true
  try {
    await createPrescription({
      sessionId,
      diagnosis: diagnosis.value.trim(),
      doctorNote: doctorNote.value,
      patientInstruction: patientInstruction.value,
      submit,
      items: payloadItems,
    })
    router.push('/doctor/prescriptions')
  } catch (error) {
    alert(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>开具电子处方</h1>
        <p>处方提交后进入药剂师审核，审核通过后患者才能进入购药流程。</p>
      </div>
      <button class="ghost-button" type="button" @click="router.back()">返回</button>
    </div>

    <div v-if="loading" class="empty-text">加载中...</div>
    <template v-else>
      <div v-if="session" class="data-card session-summary">
        <strong>{{ session.patientName }} / {{ session.doctorName }}</strong>
        <span>问诊编号：{{ session.sessionNo }}</span>
        <span>主诉：{{ session.chiefComplaint || '暂无' }}</span>
      </div>

      <div class="editor-grid">
        <div class="work-panel form-panel">
          <label>
            诊断
            <input v-model="diagnosis" placeholder="如：急性上呼吸道感染" />
          </label>

          <label>
            医生备注
            <textarea v-model="doctorNote" placeholder="给药师查看的补充说明"></textarea>
          </label>

          <label>
            患者用药说明
            <textarea v-model="patientInstruction" placeholder="面向患者的用药注意事项"></textarea>
          </label>
        </div>

        <div class="work-panel amount-panel">
          <span>药品合计</span>
          <strong>¥{{ totalAmount.toFixed(2) }}</strong>
          <small>开方阶段只做库存提示，不扣减库存。</small>
        </div>
      </div>

      <div class="work-panel">
        <div class="section-head">
          <h2>处方药品</h2>
          <button class="ghost-button" type="button" @click="addItem">添加药品</button>
        </div>

        <div class="item-list">
          <div v-for="(item, index) in items" :key="index" class="item-row">
            <label>
              药品
              <select v-model="item.drugId">
                <option value="">请选择药品</option>
                <option v-for="drug in drugs" :key="drug.id" :value="drug.id">
                  {{ drug.drugName }} / {{ drug.specification }} / ¥{{ drug.price }}
                </option>
              </select>
            </label>
            <label>
              数量
              <input v-model.number="item.quantity" min="1" type="number" />
            </label>
            <label>
              单次剂量
              <input v-model="item.dosage" placeholder="如：1粒" />
            </label>
            <label>
              频次
              <input v-model="item.frequency" placeholder="如：每日3次" />
            </label>
            <label>
              疗程
              <input v-model.number="item.durationDays" min="1" type="number" />
            </label>
            <label>
              用法
              <input v-model="item.usageMethod" placeholder="如：口服" />
            </label>
            <label>
              用药时间
              <input v-model="item.medicationTime" placeholder="如：饭后" />
            </label>
            <label>
              备注
              <input v-model="item.remark" placeholder="可选" />
            </label>
            <div class="stock-line">
              <span :class="['status-pill', stockClass(item)]">{{ stockLabel(item) }}</span>
              <button class="ghost-button" type="button" @click="removeItem(index)">删除</button>
            </div>
          </div>
        </div>

        <div class="form-actions">
          <button class="ghost-button" type="button" :disabled="saving" @click="save(false)">保存草稿</button>
          <button class="primary-button" type="button" :disabled="saving" @click="save(true)">保存并提交审核</button>
        </div>
      </div>
    </template>
  </section>
</template>

<style scoped>
textarea {
  min-height: 96px;
  resize: vertical;
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  font: inherit;
}

.session-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  align-items: center;
}

.session-summary span {
  color: var(--muted);
}

.editor-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  gap: 16px;
  align-items: start;
}

.form-panel {
  display: grid;
  gap: 16px;
}

.amount-panel {
  display: grid;
  gap: 8px;
}

.amount-panel strong {
  font-size: 28px;
}

.section-head,
.form-actions,
.stock-line {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.section-head h2 {
  margin: 0;
}

.item-list {
  display: grid;
  gap: 14px;
}

.item-row {
  display: grid;
  grid-template-columns: minmax(220px, 1.5fr) 90px repeat(3, minmax(120px, 1fr));
  gap: 12px;
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--panel-soft);
}

.stock-line {
  grid-column: 1 / -1;
}

.status-pill.danger {
  color: var(--danger);
  background: #fef2f2;
}

.form-actions {
  justify-content: flex-end;
  margin-top: 18px;
}

@media (max-width: 980px) {
  .editor-grid,
  .item-row {
    grid-template-columns: 1fr;
  }
}
</style>
