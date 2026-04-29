import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/LoginView.vue'
import DashboardView from '@/views/DashboardView.vue'
import DoctorsView from '@/views/DoctorsView.vue'
import DrugsView from '@/views/DrugsView.vue'
import UsersView from '@/views/UsersView.vue'

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
    {
      path: '/patient/home',
      component: DashboardView,
      props: { role: 'PATIENT' },
    },
    {
      path: '/patient/doctors',
      component: DoctorsView,
    },
    {
      path: '/patient/drugs',
      component: DrugsView,
      props: { manage: false },
    },
    {
      path: '/patient/prescriptions',
      component: DashboardView,
      props: { role: 'PATIENT' },
    },
    {
      path: '/doctor/workbench',
      component: DashboardView,
      props: { role: 'DOCTOR' },
    },
    {
      path: '/doctor/sessions',
      component: DashboardView,
      props: { role: 'DOCTOR' },
    },
    {
      path: '/doctor/patients',
      component: DashboardView,
      props: { role: 'DOCTOR' },
    },
    {
      path: '/pharmacist/workbench',
      component: DashboardView,
      props: { role: 'PHARMACIST' },
    },
    {
      path: '/pharmacist/audits',
      component: DashboardView,
      props: { role: 'PHARMACIST' },
    },
    {
      path: '/pharmacist/drugs',
      component: DrugsView,
      props: { manage: true },
    },
    {
      path: '/admin/dashboard',
      component: DashboardView,
      props: { role: 'ADMIN' },
    },
    {
      path: '/admin/users',
      component: UsersView,
    },
    {
      path: '/admin/doctors',
      component: DoctorsView,
      props: { admin: true },
    },
    {
      path: '/admin/drugs',
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
