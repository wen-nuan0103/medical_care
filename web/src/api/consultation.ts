import { apiGet, apiPost, apiPut } from './client'
import type { CardPlan, PrivateDoctorCard, ConsultationSession, ChatMessage } from '@/types/api'

// ─── 服务卡 ─────────────────────────────────────────────

export const getDoctorCardPlans = (doctorId: number) =>
  apiGet<CardPlan[]>(`/api/doctors/${doctorId}/card-plans`)

export const purchaseCard = (doctorId: number, planId: number) =>
  apiPost<unknown>('/api/patient/private-cards/purchase', { doctorId, planId })

export const getMyCards = () =>
  apiGet<PrivateDoctorCard[]>('/api/patient/private-cards')

// ─── 医生端服务卡配置 ────────────────────────────────────

export const getMyCardPlans = () =>
  apiGet<CardPlan[]>('/api/doctor/card-plans')

export const saveCardPlan = (plan: Partial<CardPlan>) =>
  apiPost<boolean>('/api/doctor/card-plans', plan)

export const updateCardPlan = (id: number, plan: Partial<CardPlan>) =>
  apiPut<boolean>(`/api/doctor/card-plans/${id}`, plan)

// ─── 问诊会话 ───────────────────────────────────────────

export const createConsultation = (doctorId: number, chiefComplaint?: string, diseaseDesc?: string) =>
  apiPost<ConsultationSession>('/api/consultations', { doctorId, chiefComplaint, diseaseDesc })

export const getConsultations = () =>
  apiGet<ConsultationSession[]>('/api/consultations')

export const getConsultationDetail = (id: number) =>
  apiGet<ConsultationSession>(`/api/consultations/${id}`)

export const endConsultation = (id: number) =>
  apiPut<boolean>(`/api/consultations/${id}/end`)

export const giftTime = (id: number, minutes: number, reason: string) =>
  apiPost<boolean>(`/api/consultations/${id}/gift-time`, { minutes, reason })

// ─── 聊天消息 ───────────────────────────────────────────

export const getHistoryMessages = (sessionId: number) =>
  apiGet<ChatMessage[]>(`/api/consultations/${sessionId}/messages`)
