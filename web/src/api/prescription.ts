import { apiGet, apiPost, apiPut } from './client'
import type {
  Prescription,
  PrescriptionAuditCheck,
  PrescriptionSavePayload,
} from '@/types/api'

export const getPatientPrescriptions = (status?: string) =>
  apiGet<Prescription[]>('/api/patient/prescriptions', { status })

export const getDoctorPrescriptions = (status?: string) =>
  apiGet<Prescription[]>('/api/doctor/prescriptions', { status })

export const getPrescriptionDetail = (id: number) =>
  apiGet<Prescription>(`/api/prescriptions/${id}`)

export const createPrescription = (payload: PrescriptionSavePayload) =>
  apiPost<Prescription>('/api/doctor/prescriptions', payload)

export const updatePrescription = (id: number, payload: PrescriptionSavePayload) =>
  apiPut<Prescription>(`/api/doctor/prescriptions/${id}`, payload)

export const submitPrescription = (id: number) =>
  apiPost<Prescription>(`/api/doctor/prescriptions/${id}/submit`)

export const getPendingAuditPrescriptions = () =>
  apiGet<Prescription[]>('/api/pharmacist/audits/pending')

export const getAuditPrescriptionDetail = (id: number) =>
  apiGet<Prescription>(`/api/pharmacist/audits/${id}`)

export const getPrescriptionAuditCheck = (id: number) =>
  apiGet<PrescriptionAuditCheck>(`/api/pharmacist/audits/${id}/check`)

export const auditPrescription = (id: number, auditResult: string, advice?: string, dosageResult?: string) =>
  apiPost<Prescription>(`/api/pharmacist/audits/${id}`, { auditResult, advice, dosageResult })
