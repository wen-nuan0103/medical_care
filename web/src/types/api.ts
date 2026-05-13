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

export interface PrescriptionItem {
  id: number
  prescriptionId: number
  drugId: number
  drugName: string
  specification: string | null
  quantity: number
  unitPrice: number
  dosage: string
  frequency: string
  durationDays: number
  usageMethod: string
  medicationTime: string | null
  remark: string | null
  currentStock: number | null
  warningThreshold: number | null
  prescriptionRequired: number | null
  insuranceCovered: number | null
  stockStatus: string | null
}

export interface PrescriptionAudit {
  id: number
  prescriptionId: number
  pharmacistId: number
  pharmacistName: string | null
  auditResult: string
  riskLevel: string | null
  interactionResult: string | null
  stockResult: string | null
  dosageResult: string | null
  advice: string | null
  auditTime: string
}

export interface Prescription {
  id: number
  prescriptionNo: string
  sessionId: number
  patientId: number
  patientName: string | null
  doctorId: number
  doctorName: string | null
  diagnosis: string
  status: string
  validUntil: string | null
  doctorNote: string | null
  patientInstruction: string | null
  submitTime: string | null
  approveTime: string | null
  expireTime: string | null
  createTime: string
  totalAmount: number
  items: PrescriptionItem[]
  latestAudit: PrescriptionAudit | null
}

export interface PrescriptionItemPayload {
  drugId: number
  quantity: number
  dosage: string
  frequency: string
  durationDays: number
  usageMethod: string
  medicationTime?: string
  remark?: string
}

export interface PrescriptionSavePayload {
  sessionId: number
  diagnosis: string
  doctorNote?: string
  patientInstruction?: string
  submit?: boolean
  items: PrescriptionItemPayload[]
}

export interface PrescriptionStockWarning {
  drugId: number
  drugName: string
  requestedQuantity: number
  currentStock: number
  warningThreshold: number
  stockEnough: boolean
  warningMessage: string
}

export interface DrugInteractionRisk {
  ruleId: number
  drugAId: number
  drugAName: string
  drugBId: number
  drugBName: string
  riskLevel: string
  description: string
  suggestion: string | null
}

export interface PrescriptionAuditCheck {
  prescriptionId: number
  riskLevel: string
  stockResult: string
  interactionResult: string
  dosageResult: string
  summary: string
  stockWarnings: PrescriptionStockWarning[]
  interactionRisks: DrugInteractionRisk[]
}

export interface InsuranceCard {
  id: number
  patientId: number
  cardNo: string
  holderName: string
  holderIdCard: string | null
  balance: number
  reimbursementRate: number
  status: string
  bindTime: string
}

export interface InsuranceCardBindPayload {
  cardNo: string
  holderName: string
  holderIdCard?: string
  balance: number
  reimbursementRate?: number
}

export interface MedicineOrderItem {
  id: number
  orderId: number
  drugId: number
  drugName: string
  specification: string | null
  quantity: number
  unitPrice: number
  amount: number
  insuranceCovered: number
  insuranceAmount: number
}

export interface InsurancePaymentRecord {
  id: number
  paymentNo: string
  orderId: number
  patientId: number
  insuranceCardId: number
  beforeBalance: number
  paidAmount: number
  afterBalance: number
  status: string
  failReason: string | null
  payTime: string
}

export interface MedicineOrder {
  id: number
  orderNo: string
  patientId: number
  prescriptionId: number
  prescriptionNo: string | null
  diagnosis: string | null
  insuranceCardId: number
  insuranceCardNo: string | null
  totalAmount: number
  insuranceAmount: number
  selfAmount: number
  status: string
  payTime: string | null
  cancelTime: string | null
  createTime: string
  items: MedicineOrderItem[]
  paymentRecord: InsurancePaymentRecord | null
}

export interface MedicationReminder {
  id: number
  planId: number
  patientId: number
  drugId: number
  drugName: string | null
  dosage: string | null
  usageMethod: string | null
  aiReminderText: string | null
  remindTime: string
  status: string
  confirmTime: string | null
  snoozeTime: string | null
  feedback: string | null
  createTime: string
}

export interface MedicationPlan {
  id: number
  patientId: number
  prescriptionId: number
  prescriptionItemId: number
  drugId: number
  drugName: string
  startDate: string
  endDate: string
  timesPerDay: number
  reminderTimes: string
  dosage: string | null
  usageMethod: string | null
  aiReminderText: string | null
  status: string
  createTime: string
  reminders: MedicationReminder[]
}

export interface HealthTrackRecord {
  id: number
  patientId: number
  patientName: string | null
  doctorId: number | null
  doctorName: string | null
  recordDate: string
  symptom: string | null
  temperature: number | null
  systolicPressure: number | null
  diastolicPressure: number | null
  heartRate: number | null
  bloodGlucose: number | null
  medicationFeedback: string | null
  abnormalFlag: number
  aiAnalysisId: number | null
  aiAnalysisText: string | null
  createTime: string
  remark: string | null
}

export interface HealthTrackPayload {
  doctorId?: number
  recordDate?: string
  symptom?: string
  temperature?: number | null
  systolicPressure?: number | null
  diastolicPressure?: number | null
  heartRate?: number | null
  bloodGlucose?: number | null
  medicationFeedback?: string
  remark?: string
}

export interface PatientCare {
  patientId: number
  userId: number
  patientName: string | null
  phone: string | null
  latestSymptom: string | null
  latestRecordDate: string | null
  abnormalRecordCount: number
  activeMedicationPlanCount: number
  pendingFollowUpCount: number
}

export interface FollowUpPlan {
  id: number
  patientId: number
  patientName: string | null
  doctorId: number
  doctorName: string | null
  sessionId: number | null
  planTime: string
  content: string
  status: string
  finishTime: string | null
  createTime: string
  remark: string | null
}

export interface FollowUpPayload {
  patientId: number
  sessionId?: number | null
  planTime: string
  content: string
  remark?: string
}

export interface SystemNotification {
  id: number
  receiverId: number
  senderId: number | null
  title: string
  content: string
  notificationType: string
  businessId: number | null
  readStatus: number
  readTime: string | null
  createTime: string
}
