import { apiGet, apiPost } from './client'
import type { MedicineOrder, Prescription } from '@/types/api'

export const getPurchasablePrescriptions = () =>
  apiGet<Prescription[]>('/api/patient/prescriptions/purchasable')

export const createMedicineOrder = (prescriptionId: number, insuranceCardId: number) =>
  apiPost<MedicineOrder>('/api/patient/medicine-orders/from-prescription', {
    prescriptionId,
    insuranceCardId,
  })

export const getMedicineOrders = (status?: string) =>
  apiGet<MedicineOrder[]>('/api/patient/medicine-orders', { status })

export const getMedicineOrderDetail = (id: number) =>
  apiGet<MedicineOrder>(`/api/patient/medicine-orders/${id}`)

export const payMedicineOrder = (id: number, insuranceCardId: number) =>
  apiPost<MedicineOrder>(`/api/patient/medicine-orders/${id}/pay-insurance`, { insuranceCardId })

export const cancelMedicineOrder = (id: number) =>
  apiPost<MedicineOrder>(`/api/patient/medicine-orders/${id}/cancel`)
