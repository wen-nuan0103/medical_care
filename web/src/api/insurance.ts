import { apiGet, apiPost } from './client'
import type { InsuranceCard, InsuranceCardBindPayload } from '@/types/api'

export const getInsuranceCards = () =>
  apiGet<InsuranceCard[]>('/api/patient/insurance-cards')

export const bindInsuranceCard = (payload: InsuranceCardBindPayload) =>
  apiPost<InsuranceCard>('/api/patient/insurance-cards', payload)
