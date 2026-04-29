<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { apiGet, apiPut } from '@/api/client'
import type { DrugSummary, PageResult } from '@/types/api'

const props = withDefaults(defineProps<{ manage?: boolean }>(), {
  manage: false,
})

const keyword = ref('')
const loading = ref(false)
const drugs = ref<DrugSummary[]>([])
const total = ref(0)

async function loadDrugs() {
  loading.value = true
  try {
    const page = await apiGet<PageResult<DrugSummary>>('/api/drugs', {
      current: 1,
      pageSize: 30,
      keyword: keyword.value,
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
    reason: '第一阶段页面快速补库存',
  })
  await loadDrugs()
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

    <div class="toolbar">
      <input v-model="keyword" placeholder="搜索药品名称、通用名、编码" @keyup.enter="loadDrugs" />
      <button class="primary-button" type="button" @click="loadDrugs">查询</button>
    </div>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>药品</th>
            <th>规格</th>
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
            <td>{{ drug.specification }}</td>
            <td>¥{{ drug.price }}</td>
            <td>
              <span :class="['status-pill', drug.stockQuantity <= drug.warningThreshold ? 'warning' : 'success']">
                {{ drug.stockQuantity }}
              </span>
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

