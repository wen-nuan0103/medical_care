<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiGet, apiPut } from '@/api/client'
import type { DoctorSummary, PageResult } from '@/types/api'

const props = withDefaults(defineProps<{ admin?: boolean }>(), {
  admin: false,
})

const router = useRouter()
const keyword = ref('')
const department = ref('')
const serviceStatus = ref<string>('')
const loading = ref(false)
const doctors = ref<DoctorSummary[]>([])
const total = ref(0)

const DEPARTMENTS = [
  '内科', '外科', '儿科', '妇产科', '眼科', '耳鼻喉科',
  '骨科', '神经科', '心内科', '呼吸内科', '内分泌科',
  '消化内科', '肿瘤科', '皮肤科', '精神科', '中医科',
]

async function loadDoctors() {
  loading.value = true
  try {
    const page = await apiGet<PageResult<DoctorSummary>>('/api/doctors', {
      current: 1,
      pageSize: 30,
      keyword: keyword.value,
      department: department.value,
      serviceStatus: serviceStatus.value !== '' ? serviceStatus.value : undefined,
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

function goDetail(id: number) {
  const base = props.admin ? '/admin/doctors' : '/patient/doctors'
  router.push(`${base}/${id}`)
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

    <div class="toolbar doctors-toolbar">
      <input v-model="keyword" placeholder="搜索医生、医院、擅长方向" @keyup.enter="loadDoctors" />
      <select v-model="department" @change="loadDoctors">
        <option value="">全部科室</option>
        <option v-for="dept in DEPARTMENTS" :key="dept" :value="dept">{{ dept }}</option>
      </select>
      <select v-model="serviceStatus" @change="loadDoctors">
        <option value="">全部状态</option>
        <option value="1">服务中</option>
        <option value="0">停诊</option>
      </select>
      <button class="primary-button" type="button" @click="loadDoctors">查询</button>
    </div>

    <div v-if="loading" class="empty-text">加载中...</div>

    <div v-else-if="doctors.length === 0" class="empty-text">暂无医生数据</div>

    <div v-else class="doctor-grid">
      <article
        v-for="doctor in doctors"
        :key="doctor.id"
        class="data-card doctor-list-card"
        @click="goDetail(doctor.id)"
      >
        <div class="doc-card-avatar">{{ doctor.realName.slice(-1) }}</div>
        <div class="doc-card-body">
          <div class="card-topline">
            <strong>{{ doctor.realName }}</strong>
            <span :class="['status-pill', doctor.serviceStatus === 1 ? 'success' : 'muted']">
              {{ doctor.serviceStatus === 1 ? '服务中' : '停诊' }}
            </span>
          </div>
          <p>{{ doctor.title }} · {{ doctor.department }}</p>
          <p>{{ doctor.hospital }}</p>
          <p class="muted-text specialty-text">{{ doctor.specialty }}</p>
          <div class="card-footer">
            <span>评分 {{ doctor.score }}</span>
            <span>接诊 {{ doctor.consultationCount }}</span>
          </div>
          <div v-if="admin" class="admin-actions" @click.stop>
            <button
              class="ghost-button small-btn"
              type="button"
              @click="toggleStatus(doctor)"
            >
              {{ doctor.serviceStatus === 1 ? '停用服务' : '启用服务' }}
            </button>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.doctors-toolbar {
  grid-template-columns: minmax(200px, 1fr) 160px 140px auto;
}

.doctor-list-card {
  display: flex;
  gap: 16px;
  cursor: pointer;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.doctor-list-card:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 14px rgba(15, 118, 110, 0.1);
}

.doc-card-avatar {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  font-size: 18px;
  font-weight: 800;
  display: grid;
  place-items: center;
  margin-top: 2px;
}

.doc-card-body {
  flex: 1;
  min-width: 0;
}

.doc-card-body p {
  margin: 5px 0 0;
  font-size: 13px;
}

.specialty-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-actions {
  margin-top: 10px;
}

.small-btn {
  font-size: 13px;
  min-height: 30px;
  padding: 4px 12px;
}
</style>
