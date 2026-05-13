import { apiGet, apiPost, apiPut } from './client'
import type {
  FollowUpPayload,
  FollowUpPlan,
  HealthTrackPayload,
  HealthTrackRecord,
  MedicationPlan,
  MedicationReminder,
  PatientCare,
  SystemNotification,
} from '@/types/api'

export const getMedicationPlans = (status?: string) =>
  apiGet<MedicationPlan[]>('/api/patient/medication-plans', { status })

export const getMedicationReminders = (query?: { status?: string; from?: string; to?: string }) =>
  apiGet<MedicationReminder[]>('/api/patient/medication-reminders', query)

export const markReminderTaken = (id: number, feedback?: string) =>
  apiPost<MedicationReminder>(`/api/patient/medication-reminders/${id}/taken`, { feedback })

export const markReminderMissed = (id: number, feedback?: string) =>
  apiPost<MedicationReminder>(`/api/patient/medication-reminders/${id}/missed`, { feedback })

export const snoozeReminder = (id: number, snoozeMinutes = 15, feedback?: string) =>
  apiPost<MedicationReminder>(`/api/patient/medication-reminders/${id}/snooze`, {
    snoozeMinutes,
    feedback,
  })

export const getPatientHealthTracks = () =>
  apiGet<HealthTrackRecord[]>('/api/patient/health-tracks')

export const createHealthTrack = (payload: HealthTrackPayload) =>
  apiPost<HealthTrackRecord>('/api/patient/health-tracks', payload)

export const getDoctorPatients = () =>
  apiGet<PatientCare[]>('/api/doctor/patients')

export const getDoctorPatientTracks = (patientId: number) =>
  apiGet<HealthTrackRecord[]>(`/api/doctor/patients/${patientId}/health-tracks`)

export const getPatientFollowUps = (status?: string) =>
  apiGet<FollowUpPlan[]>('/api/patient/follow-ups', { status })

export const getDoctorFollowUps = (query?: { patientId?: number; status?: string }) =>
  apiGet<FollowUpPlan[]>('/api/doctor/follow-ups', query)

export const createFollowUp = (payload: FollowUpPayload) =>
  apiPost<FollowUpPlan>('/api/doctor/follow-ups', payload)

export const updateFollowUpStatus = (id: number, status: string, remark?: string) =>
  apiPut<FollowUpPlan>(`/api/doctor/follow-ups/${id}/status`, { status, remark })

export const getNotifications = (readStatus?: number) =>
  apiGet<SystemNotification[]>('/api/notifications', { readStatus })

export const getUnreadNotificationCount = () =>
  apiGet<number>('/api/notifications/unread-count')

export const markNotificationRead = (id: number) =>
  apiPost<SystemNotification>(`/api/notifications/${id}/read`)
