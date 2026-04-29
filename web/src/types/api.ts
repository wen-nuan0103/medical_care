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

// ─── Phase 2 Types ──────────────────────────────────────

export interface CardPlan {
  id: number
  doctorId: number
  cardType: string
  planName: string
  price: number
  validDays: number
  consultationTimes: number
  totalMinutes: number
  singleMinutes: number
  giftLimitMinutes: number
  description: string | null
  status: number
}

export interface PrivateDoctorCard {
  id: number
  doctorId: number
  doctorName: string
  doctorTitle: string
  doctorAvatar: string | null
  cardType: string
  planName: string
  startTime: string
  expireTime: string
  totalTimes: number
  remainingTimes: number
  totalMinutes: number
  remainingMinutes: number
  giftedMinutes: number
  status: string
}

export interface ConsultationSession {
  id: number
  sessionNo: string
  patientId: number
  patientName: string
  patientAvatar: string | null
  doctorId: number
  doctorName: string
  doctorAvatar: string | null
  chiefComplaint: string | null
  diseaseDesc: string | null
  status: string
  startTime: string | null
  endTime: string | null
  allowedMinutes: number
  usedMinutes: number
  summary: string | null
  createTime: string
}

export interface ChatMessage {
  id: number
  sessionId: number
  senderId: number
  senderName: string
  senderAvatar: string | null
  receiverId: number
  messageType: string
  content: string
  attachmentId: number | null
  attachmentUrl: string | null
  readStatus: number
  sendTime: string
}
