<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { apiGet } from '@/api/client'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{
  role: 'PATIENT' | 'DOCTOR' | 'PHARMACIST' | 'ADMIN'
}>()

const authStore = useAuthStore()
const overview = ref<Record<string, number>>({})

const copy = {
  PATIENT: {
    title: '患者工作台',
    subtitle: '查看私人医生、药品商城和处方购药入口。',
    cards: ['已购买医生服务', '待处理处方', '今日用药提醒'],
  },
  DOCTOR: {
    title: '医生工作台',
    subtitle: '处理在线问诊、患者资料和处方草稿。',
    cards: ['今日待接诊', '我的患者', '待确认 AI 摘要'],
  },
  PHARMACIST: {
    title: '药剂师工作台',
    subtitle: '审核处方、维护药品库和库存预警。',
    cards: ['待审核处方', '库存预警', '药物相互作用规则'],
  },
  ADMIN: {
    title: '管理看板',
    subtitle: '查看第一阶段基础数据和账号运行状态。',
    cards: ['用户', '医生', '药剂师', '药品'],
  },
}

onMounted(async () => {
  await authStore.hydrate()
  if (props.role === 'ADMIN') {
    overview.value = await apiGet<Record<string, number>>('/api/admin/dashboard/phase-one')
  }
})
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>{{ copy[role].title }}</h1>
        <p>{{ copy[role].subtitle }}</p>
      </div>
    </div>

    <div class="metric-grid">
      <article v-for="item in copy[role].cards" :key="item" class="metric-card">
        <span>{{ item }}</span>
        <strong v-if="role === 'ADMIN'">{{ overview[item === '用户' ? 'users' : item === '医生' ? 'doctors' : item === '药剂师' ? 'pharmacists' : 'drugs'] ?? '-' }}</strong>
        <strong v-else>待接入</strong>
      </article>
    </div>

    <section class="work-panel">
      <h2>第一阶段已接入能力</h2>
      <div class="timeline">
        <span>登录认证</span>
        <span>角色菜单</span>
        <span>医生资料</span>
        <span>药品库</span>
        <span>管理接口</span>
      </div>
    </section>
  </section>
</template>

