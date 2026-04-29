<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { apiGet, apiPut } from '@/api/client'
import type { DoctorSummary, PageResult } from '@/types/api'

const props = withDefaults(defineProps<{ admin?: boolean }>(), {
  admin: false,
})

const keyword = ref('')
const department = ref('')
const loading = ref(false)
const doctors = ref<DoctorSummary[]>([])
const total = ref(0)

async function loadDoctors() {
  loading.value = true
  try {
    const page = await apiGet<PageResult<DoctorSummary>>('/api/doctors', {
      current: 1,
      pageSize: 20,
      keyword: keyword.value,
      department: department.value,
    })
    doctors.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

async function toggleStatus(doctor: DoctorSummary) {
  await apiPut<boolean>(`/api/admin/doctors/${doctor.id}/status`, {
    status: doctor.serviceStatus === 1 ? 0 : 1,
  })
  await loadDoctors()
}

onMounted(loadDoctors)
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>{{ admin ? '医生管理' : '找医生' }}</h1>
        <p>按科室、姓名、医院或擅长方向筛选医生。</p>
      </div>
      <strong>{{ total }} 位医生</strong>
    </div>

    <div class="toolbar">
      <input v-model="keyword" placeholder="搜索医生、医院、擅长方向" @keyup.enter="loadDoctors" />
      <select v-model="department" @change="loadDoctors">
        <option value="">全部科室</option>
        <option value="呼吸内科">呼吸内科</option>
        <option value="心内科">心内科</option>
        <option value="内分泌科">内分泌科</option>
      </select>
      <button class="primary-button" type="button" @click="loadDoctors">查询</button>
    </div>

    <div class="doctor-grid">
      <article v-for="doctor in doctors" :key="doctor.id" class="data-card">
        <div class="card-topline">
          <strong>{{ doctor.realName }}</strong>
          <span :class="['status-pill', doctor.serviceStatus === 1 ? 'success' : 'muted']">
            {{ doctor.serviceStatus === 1 ? '服务中' : '停诊' }}
          </span>
        </div>
        <p>{{ doctor.title }} · {{ doctor.department }}</p>
        <p>{{ doctor.hospital }}</p>
        <p class="muted-text">{{ doctor.specialty }}</p>
        <div class="card-footer">
          <span>评分 {{ doctor.score }}</span>
          <span>接诊 {{ doctor.consultationCount }}</span>
        </div>
        <button v-if="admin" class="ghost-button" type="button" @click="toggleStatus(doctor)">
          {{ doctor.serviceStatus === 1 ? '停用服务' : '启用服务' }}
        </button>
      </article>
    </div>

    <p v-if="!loading && doctors.length === 0" class="empty-text">暂无医生数据</p>
  </section>
</template>

