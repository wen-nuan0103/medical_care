export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  pageSize: number
}

export interface DoctorSummary {
  id: number
  userId: number
  realName: string
  phone: string
  avatarUrl: string | null
  hospital: string
  department: string
  title: string
  specialty: string
  consultationCount: number
  score: number
  serviceStatus: number
  auditStatus: string
  createTime: string
}

export interface DoctorDetail extends DoctorSummary {
  licenseNo: string | null
  introduction: string | null
}

export interface DrugSummary {
  id: number
  drugCode: string
  drugName: string
  genericName: string
  categoryName: string | null
  specification: string
  dosageForm: string | null
  manufacturer: string | null
  price: number
  stockQuantity: number
  warningThreshold: number
  prescriptionRequired: number
  insuranceCovered: number
  status: number
}

export interface UserSummary {
  id: number
  username: string
  realName: string
  phone: string
  email: string
  status: number
  roles: string[]
  createTime: string
}

