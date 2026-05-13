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
import PrescriptionListView from '@/views/PrescriptionListView.vue'
import PrescriptionEditorView from '@/views/PrescriptionEditorView.vue'
import PharmacistAuditView from '@/views/PharmacistAuditView.vue'
import InsuranceCardsView from '@/views/InsuranceCardsView.vue'
import PrescriptionPurchaseView from '@/views/PrescriptionPurchaseView.vue'
import MedicineOrdersView from '@/views/MedicineOrdersView.vue'
import MedicationRemindersView from '@/views/MedicationRemindersView.vue'
import HealthTracksView from '@/views/HealthTracksView.vue'
import DoctorPatientsView from '@/views/DoctorPatientsView.vue'
import NotificationsView from '@/views/NotificationsView.vue'

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
      component: PrescriptionListView,
      props: { mode: 'patient' },
    },
    {
      path: '/patient/prescriptions/:id/purchase',
      name: 'patient-prescription-purchase',
      component: PrescriptionPurchaseView,
    },
    {
      path: '/patient/orders',
      name: 'patient-orders',
      component: MedicineOrdersView,
    },
    {
      path: '/patient/medication-reminders',
      name: 'patient-medication-reminders',
      component: MedicationRemindersView,
    },
    {
      path: '/patient/health-tracks',
      name: 'patient-health-tracks',
      component: HealthTracksView,
    },
    {
      path: '/patient/insurance-card',
      name: 'patient-insurance-card',
      component: InsuranceCardsView,
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
      component: DoctorPatientsView,
    },
    {
      path: '/doctor/card-plans',
      name: 'doctor-card-plans',
      component: DoctorCardPlansView,
    },
    {
      path: '/doctor/prescriptions',
      name: 'doctor-prescriptions',
      component: PrescriptionListView,
      props: { mode: 'doctor' },
    },
    {
      path: '/doctor/prescriptions/new/:sessionId',
      name: 'doctor-prescription-new',
      component: PrescriptionEditorView,
    },

    {
      path: '/chat/:sessionId',
      name: 'chat',
      component: ChatView,
    },
    {
      path: '/notifications',
      name: 'notifications',
      component: NotificationsView,
    },

    {
      path: '/pharmacist/workbench',
      name: 'pharmacist-workbench',
      component: DashboardView,
      props: { role: 'PHARMACIST' },
    },
    {
      path: '/pharmacist/audits',
      name: 'pharmacist-audits',
      component: PharmacistAuditView,
    },
    {
      path: '/pharmacist/drugs',
      name: 'pharmacist-drugs',
      component: DrugsView,
      props: { manage: true },
    },

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
