import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/LoginView.vue'
import PatientHomeView from '@/views/PatientHomeView.vue'
import DashboardView from '@/views/DashboardView.vue'
import DoctorsView from '@/views/DoctorsView.vue'
import DoctorDetailView from '@/views/DoctorDetailView.vue'
import DrugsView from '@/views/DrugsView.vue'
import UsersView from '@/views/UsersView.vue'
import MyCardsView from '@/views/MyCardsView.vue'
import SessionListView from '@/views/SessionListView.vue'
import ChatView from '@/views/ChatView.vue'
import NewConsultationView from '@/views/NewConsultationView.vue'
import DoctorCardPlansView from '@/views/DoctorCardPlansView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true },
    },

    // ─── 患者端 ─────────────────────────────────────────
    {
      path: '/patient/home',
      name: 'patient-home',
      component: PatientHomeView,
    },
    {
      path: '/patient/doctors',
      name: 'patient-doctors',
      component: DoctorsView,
    },
    {
      path: '/patient/doctors/:id',
      name: 'patient-doctor-detail',
      component: DoctorDetailView,
    },
    {
      path: '/patient/drugs',
      name: 'patient-drugs',
      component: DrugsView,
      props: { manage: false },
    },
    {
      path: '/patient/prescriptions',
      name: 'patient-prescriptions',
      component: DashboardView,
      props: { role: 'PATIENT' },
    },
    {
      path: '/patient/my-cards',
      name: 'patient-my-cards',
      component: MyCardsView,
    },
    {
      path: '/patient/sessions',
      name: 'patient-sessions',
      component: SessionListView,
    },
    {
      path: '/patient/new-consultation',
      name: 'patient-new-consultation',
      component: NewConsultationView,
    },

    // ─── 医生端 ─────────────────────────────────────────
    {
      path: '/doctor/workbench',
      name: 'doctor-workbench',
      component: DashboardView,
      props: { role: 'DOCTOR' },
    },
    {
      path: '/doctor/sessions',
      name: 'doctor-sessions',
      component: SessionListView,
    },
    {
      path: '/doctor/patients',
      name: 'doctor-patients',
      component: DashboardView,
      props: { role: 'DOCTOR' },
    },
    {
      path: '/doctor/card-plans',
      name: 'doctor-card-plans',
      component: DoctorCardPlansView,
    },

    // ─── 聊天页面（共用） ──────────────────────────────
    {
      path: '/chat/:sessionId',
      name: 'chat',
      component: ChatView,
    },

    // ─── 药剂师端 ────────────────────────────────────────
    {
      path: '/pharmacist/workbench',
      name: 'pharmacist-workbench',
      component: DashboardView,
      props: { role: 'PHARMACIST' },
    },
    {
      path: '/pharmacist/audits',
      name: 'pharmacist-audits',
      component: DashboardView,
      props: { role: 'PHARMACIST' },
    },
    {
      path: '/pharmacist/drugs',
      name: 'pharmacist-drugs',
      component: DrugsView,
      props: { manage: true },
    },

    // ─── 管理后台 ────────────────────────────────────────
    {
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: DashboardView,
      props: { role: 'ADMIN' },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: UsersView,
    },
    {
      path: '/admin/doctors',
      name: 'admin-doctors',
      component: DoctorsView,
      props: { admin: true },
    },
    {
      path: '/admin/doctors/:id',
      name: 'admin-doctor-detail',
      component: DoctorDetailView,
    },
    {
      path: '/admin/drugs',
      name: 'admin-drugs',
      component: DrugsView,
      props: { manage: true },
    },
  ],
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  if (authStore.token && !authStore.user) {
    await authStore.hydrate()
  }
  if (!to.meta.public && !authStore.token) {
    return '/login'
  }
  if (to.path === '/login' && authStore.token) {
    return authStore.homePath
  }
})

export default router
