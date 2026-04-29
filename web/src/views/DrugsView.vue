<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { apiGet, apiPut } from '@/api/client'
import type { DrugSummary, PageResult } from '@/types/api'

const props = withDefaults(defineProps<{ manage?: boolean }>(), {
  manage: false,
})

const keyword = ref('')
const prescriptionRequired = ref<string>('')
const insuranceCovered = ref<string>('')
const loading = ref(false)
const drugs = ref<DrugSummary[]>([])
const total = ref(0)

async function loadDrugs() {
  loading.value = true
  try {
    const page = await apiGet<PageResult<DrugSummary>>('/api/drugs', {
      current: 1,
      pageSize: 50,
      keyword: keyword.value,
      prescriptionRequired: prescriptionRequired.value !== '' ? prescriptionRequired.value : undefined,
      insuranceCovered: insuranceCovered.value !== '' ? insuranceCovered.value : undefined,
    })
    drugs.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

async function addStock(drug: DrugSummary) {
  await apiPut(`/api/admin/drugs/${drug.id}/stock`, {
    stockQuantity: drug.stockQuantity + 10,
    reason: '管理页面手动补库存',
  })
  await loadDrugs()
}

function stockClass(drug: DrugSummary) {
  if (drug.stockQuantity === 0) return 'danger'
  if (drug.stockQuantity <= drug.warningThreshold) return 'warning'
  return 'success'
}

onMounted(loadDrugs)
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>{{ manage ? '药品管理' : '药品商城' }}</h1>
        <p>查看药品、处方药标识、医保属性和库存状态。</p>
      </div>
      <strong>{{ total }} 个药品</strong>
    </div>

    <div class="toolbar drug-toolbar">
      <input v-model="keyword" placeholder="搜索药品名称、通用名、编码" @keyup.enter="loadDrugs" />
      <select v-model="prescriptionRequired" @change="loadDrugs">
        <option value="">全部类型</option>
        <option value="1">处方药</option>
        <option value="0">非处方药</option>
      </select>
      <select v-model="insuranceCovered" @change="loadDrugs">
        <option value="">全部报销</option>
        <option value="1">医保</option>
        <option value="0">自费</option>
      </select>
      <button class="primary-button" type="button" @click="loadDrugs">查询</button>
    </div>

    <div v-if="loading" class="empty-text">加载中...</div>
    <div v-else-if="drugs.length === 0" class="empty-text">暂无药品数据</div>

    <div v-else class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>药品</th>
            <th>规格 / 剂型</th>
            <th>分类</th>
            <th>价格</th>
            <th>库存</th>
            <th>属性</th>
            <th v-if="manage">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="drug in drugs" :key="drug.id">
            <td>
              <strong>{{ drug.drugName }}</strong>
              <small>{{ drug.genericName }} · {{ drug.drugCode }}</small>
            </td>
            <td>
              {{ drug.specification }}
              <small v-if="drug.dosageForm">{{ drug.dosageForm }}</small>
            </td>
            <td>{{ drug.categoryName || '—' }}</td>
            <td>¥{{ drug.price }}</td>
            <td>
              <span :class="['status-pill', stockClass(drug)]">
                {{ drug.stockQuantity === 0 ? '缺货' : drug.stockQuantity }}
              </span>
              <small v-if="drug.stockQuantity > 0 && drug.stockQuantity <= drug.warningThreshold" class="warn-tip">预警</small>
            </td>
            <td>
              <span class="tag">{{ drug.prescriptionRequired ? '处方药' : '非处方' }}</span>
              <span class="tag">{{ drug.insuranceCovered ? '医保' : '自费' }}</span>
            </td>
            <td v-if="manage">
              <button class="ghost-button" type="button" @click="addStock(drug)">+10 库存</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.drug-toolbar {
  grid-template-columns: minmax(200px, 1fr) 140px 140px auto;
}

.status-pill.danger {
  color: var(--danger);
  background: #fef2f2;
}

.warn-tip {
  display: block;
  font-size: 11px;
  color: var(--warning);
  margin-top: 2px;
}
</style>
