<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiGet } from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import type { DoctorSummary, PageResult } from '@/types/api'

const router = useRouter()
const authStore = useAuthStore()
const featuredDoctors = ref<DoctorSummary[]>([])
const loading = ref(true)

interface QuickEntry {
  label: string
  desc: string
  path: string
  emoji: string
  soon?: boolean
}

onMounted(async () => {
  await authStore.hydrate()
  try {
    const page = await apiGet<PageResult<DoctorSummary>>('/api/doctors', {
      current: 1,
      pageSize: 6,
      serviceStatus: 1,
    })
    featuredDoctors.value = page.records
  } finally {
    loading.value = false
  }
})

const quickEntries: QuickEntry[] = [
  { label: '找医生', desc: '浏览全部医生，按科室和专长筛选', path: '/patient/doctors', emoji: '🩺' },
  { label: '我的医生', desc: '查看已购买的私人医生服务卡', path: '/patient/my-cards', emoji: '💳' },
  { label: '问诊记录', desc: '查看问诊会话和聊天历史', path: '/patient/sessions', emoji: '💬' },
  { label: '药品商城', desc: '查看药品、价格和医保属性', path: '/patient/drugs', emoji: '💊' },
  { label: '我的处方', desc: '查看处方记录和审核状态', path: '/patient/prescriptions', emoji: '📋' },
]
</script>

<template>
  <section class="page">
    <!-- 欢迎栏 -->
    <div class="welcome-banner">
      <div>
        <h1>你好，{{ authStore.user?.realName ?? '患者' }} 👋</h1>
        <p>欢迎使用悦医健康平台，您可以在这里预约医生、查看药品和管理处方。</p>
      </div>
    </div>

    <!-- 快速入口 -->
    <div>
      <h2 class="section-title">快速入口</h2>
      <div class="entry-grid">
        <button
          v-for="entry in quickEntries"
          :key="entry.path"
          class="entry-card"
          :class="{ disabled: entry.soon }"
          type="button"
          @click="!entry.soon && router.push(entry.path)"
        >
          <span class="entry-emoji">{{ entry.emoji }}</span>
          <strong>{{ entry.label }}</strong>
          <p>{{ entry.desc }}</p>
          <span v-if="entry.soon" class="soon-badge">即将开放</span>
        </button>
      </div>
    </div>

    <!-- 推荐医生 -->
    <div>
      <div class="section-header">
        <h2 class="section-title">推荐医生</h2>
        <button class="ghost-button" type="button" @click="router.push('/patient/doctors')">查看全部 →</button>
      </div>
      <div v-if="loading" class="empty-text">加载中...</div>
      <div v-else-if="featuredDoctors.length === 0" class="empty-text">暂无在线医生</div>
      <div v-else class="featured-grid">
        <article
          v-for="doctor in featuredDoctors"
          :key="doctor.id"
          class="doctor-card"
          @click="router.push(`/patient/doctors/${doctor.id}`)"
        >
          <div class="doc-avatar">{{ doctor.realName.slice(-1) }}</div>
          <div class="doc-info">
            <div class="doc-topline">
              <strong>{{ doctor.realName }}</strong>
              <span class="status-pill success">服务中</span>
            </div>
            <p>{{ doctor.title }} · {{ doctor.department }}</p>
            <p class="muted-text">{{ doctor.hospital }}</p>
            <p class="muted-text specialty-clip">{{ doctor.specialty }}</p>
          </div>
          <div class="doc-stats">
            <div>
              <strong>{{ doctor.score }}</strong>
              <small>评分</small>
            </div>
            <div>
              <strong>{{ doctor.consultationCount }}</strong>
              <small>接诊</small>
            </div>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.welcome-banner {
  padding: 28px 32px;
  border-radius: 12px;
  background: linear-gradient(135deg, #0f766e 0%, #0e7490 100%);
  color: #fff;
}

.welcome-banner h1 {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 800;
}

.welcome-banner p {
  margin: 0;
  opacity: 0.88;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.entry-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 14px;
  margin-top: 12px;
}

.entry-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding: 22px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--panel);
  text-align: left;
  transition: box-shadow 0.18s, transform 0.18s;
}

.entry-card:not(.disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(15, 118, 110, 0.12);
  border-color: var(--primary);
}

.entry-card.disabled {
  opacity: 0.62;
  cursor: default;
}

.entry-emoji {
  font-size: 28px;
}

.entry-card strong {
  font-size: 16px;
  font-weight: 800;
}

.entry-card p {
  margin: 0;
  font-size: 13px;
  color: var(--muted);
}

.soon-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  background: #fff7ed;
  color: var(--warning);
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 14px;
}

.doctor-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--panel);
  cursor: pointer;
  transition: box-shadow 0.18s, border-color 0.18s;
}

.doctor-card:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 16px rgba(15, 118, 110, 0.1);
}

.doc-avatar {
  flex-shrink: 0;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  font-size: 20px;
  font-weight: 800;
  display: grid;
  place-items: center;
}

.doc-info {
  flex: 1;
  min-width: 0;
}

.doc-topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
}

.doc-topline strong {
  font-weight: 800;
}

.doc-info p {
  margin: 4px 0 0;
  font-size: 13px;
}

.specialty-clip {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doc-stats {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-shrink: 0;
  padding-left: 14px;
  border-left: 1px solid var(--line);
}

.doc-stats div {
  text-align: center;
}

.doc-stats strong {
  display: block;
  font-size: 18px;
  font-weight: 800;
  color: var(--primary);
}

.doc-stats small {
  font-size: 11px;
  color: var(--muted);
}
</style>
