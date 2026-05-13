<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiGet } from '@/api/client'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{
  role: 'PATIENT' | 'DOCTOR' | 'PHARMACIST' | 'ADMIN'
}>()

const router = useRouter()
const authStore = useAuthStore()
const overview = ref<Record<string, number>>({})

type RoleKey = 'PATIENT' | 'DOCTOR' | 'PHARMACIST' | 'ADMIN'

const roleConfig: Record<RoleKey, { title: string; subtitle: string; entries: { label: string; path: string; desc: string; soon?: boolean }[] }> = {
  PATIENT: {
    title: '患者工作台',
    subtitle: '管理私人医生、查看药品商城和处方记录。',
    entries: [
      { label: '找医生', path: '/patient/doctors', desc: '浏览全部医生，按科室和专长筛选' },
      { label: '药品商城', path: '/patient/drugs', desc: '查看药品、价格和医保属性' },
      { label: '我的处方', path: '/patient/prescriptions', desc: '查看处方记录和审核状态' },
      { label: '医保卡', path: '/patient/insurance-card', desc: '绑定模拟医保卡并查看余额' },
      { label: '购药订单', path: '/patient/orders', desc: '查看医保购药订单和支付记录' },
      { label: '用药提醒', path: '/patient/medication-reminders', desc: '查看用药计划并确认提醒' },
      { label: '健康跟踪', path: '/patient/health-tracks', desc: '记录症状、指标和用药反馈' },
    ],
  },
  DOCTOR: {
    title: '医生工作台',
    subtitle: '处理在线问诊、患者资料和处方草稿。',
    entries: [
      { label: '问诊列表', path: '/doctor/sessions', desc: '查看当前和历史问诊会话' },
      { label: '我的患者', path: '/doctor/patients', desc: '管理患者资料和随访记录' },
    ],
  },
  PHARMACIST: {
    title: '药剂师工作台',
    subtitle: '审核处方、维护药品库和关注库存预警。',
    entries: [
      { label: '处方审核', path: '/pharmacist/audits', desc: '处理待审核处方' },
      { label: '药品管理', path: '/pharmacist/drugs', desc: '维护药品信息和库存' },
    ],
  },
  ADMIN: {
    title: '管理看板',
    subtitle: '查看平台数据总览，管理用户、医生和药品基础信息。',
    entries: [
      { label: '用户管理', path: '/admin/users', desc: '查看和管理全部账号' },
      { label: '医生管理', path: '/admin/doctors', desc: '管理医生档案和接诊状态' },
      { label: '药品管理', path: '/admin/drugs', desc: '维护药品信息、库存和定价' },
    ],
  },
}

const config = computed(() => roleConfig[props.role])

const overviewKeys = [
  { key: 'users', label: '注册用户', unit: '个' },
  { key: 'doctors', label: '医生档案', unit: '位' },
  { key: 'pharmacists', label: '药剂师', unit: '位' },
  { key: 'drugs', label: '药品总数', unit: '种' },
]

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
        <h1>{{ config.title }}</h1>
        <p>{{ config.subtitle }}</p>
      </div>
      <span v-if="authStore.user" class="user-chip">{{ authStore.user.realName }}</span>
    </div>

    <!-- 管理员：数据指标 -->
    <template v-if="role === 'ADMIN'">
      <div class="metric-grid">
        <article v-for="item in overviewKeys" :key="item.key" class="metric-card">
          <span>{{ item.label }}</span>
          <strong>{{ overview[item.key] ?? '-' }}</strong>
          <small class="muted-text">{{ item.unit }}</small>
        </article>
      </div>
    </template>

    <!-- 其他角色：阶段提示 -->
    <template v-else>
      <div class="phase-hint">
        <span class="phase-badge">第五阶段</span>
        <p>当前已接入问诊、处方、医保购药、用药提醒、健康跟踪和随访模块。</p>
      </div>
    </template>

    <!-- 快捷入口 -->
    <div>
      <h2 class="section-label">快捷入口</h2>
      <div class="entry-row">
        <button
          v-for="entry in config.entries"
          :key="entry.path"
          class="entry-btn"
          :class="{ 'entry-soon': entry.soon }"
          type="button"
          @click="!entry.soon && router.push(entry.path)"
        >
          <strong>{{ entry.label }}</strong>
          <small>{{ entry.desc }}</small>
          <span v-if="entry.soon" class="soon-tag">即将开放</span>
        </button>
      </div>
    </div>

    <!-- 管理员：第一阶段已接入能力 -->
    <div v-if="role === 'ADMIN'" class="work-panel">
      <h2>第一阶段已接入能力</h2>
      <div class="timeline">
        <span>登录认证</span>
        <span>角色菜单</span>
        <span>医生资料</span>
        <span>药品库</span>
        <span>管理接口</span>
        <span>分页查询</span>
        <span>状态管理</span>
      </div>
    </div>
  </section>
</template>

<style scoped>
.user-chip {
  padding: 6px 14px;
  border-radius: 999px;
  background: var(--panel);
  border: 1px solid var(--line);
  font-weight: 700;
  font-size: 14px;
}

.section-label {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 700;
  color: var(--muted);
}

.phase-hint {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--panel-soft);
  color: var(--muted);
}

.phase-badge {
  flex-shrink: 0;
  padding: 4px 10px;
  border-radius: 999px;
  background: #e0f2fe;
  color: #0369a1;
  font-size: 12px;
  font-weight: 700;
}

.phase-hint p {
  margin: 0;
  font-size: 14px;
}

.entry-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.entry-btn {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 5px;
  padding: 18px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: var(--panel);
  text-align: left;
  transition: border-color 0.15s, box-shadow 0.15s;
  position: relative;
}

.entry-btn:not(.entry-soon):hover {
  border-color: var(--primary);
  box-shadow: 0 4px 12px rgba(15, 118, 110, 0.1);
}

.entry-btn.entry-soon {
  opacity: 0.6;
  cursor: default;
}

.entry-btn strong {
  font-size: 15px;
  font-weight: 800;
}

.entry-btn small {
  font-size: 12px;
  color: var(--muted);
}

.soon-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 999px;
  background: #fff7ed;
  color: var(--warning);
}
</style>
