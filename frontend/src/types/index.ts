// ============================================================================
// 后端 DTO 的 TypeScript 类型定义（对照后端勘察结果逐字段手写）
// 约定：LocalDateTime → string（ISO-8601）、Long → number、条件字段标可选。
// ============================================================================

// ---------------------------------------------------------------------------
// 通用
// ---------------------------------------------------------------------------

/** 统一响应包裹 */
export interface Result<T> {
  code: number
  message: string
  data: T
}

/** 管理端分页返回（records/total/page/size，page 从 1 开始） */
export interface PageVO<T> {
  records: T[]
  total: number
  page: number
  size: number
}

// ---------------------------------------------------------------------------
// 角色 / 枚举
// ---------------------------------------------------------------------------

export type Role = 'USER' | 'SUPER_ADMIN' | 'OPERATOR' | 'AUDITOR'
export type AdminRole = 'SUPER_ADMIN' | 'OPERATOR' | 'AUDITOR'
export type SmsScene = 'REGISTER' | 'LOGIN' | 'RESET_PASSWORD'
export type LoginType = 'PASSWORD' | 'SMS'
export type FeedbackType = 'HELPFUL' | 'NOT_HELPFUL' | 'CORRECTION'

// ---------------------------------------------------------------------------
// Auth（/api/auth）
// ---------------------------------------------------------------------------

export interface SendCodeReq {
  phone: string
  scene: SmsScene
}

export interface RegisterReq {
  phone: string
  code: string
  password: string
  nickname?: string
}

export interface LoginReq {
  phone: string
  type: LoginType
  password?: string
  code?: string
}

export interface RefreshReq {
  refreshToken: string
}

export interface ResetPasswordReq {
  phone: string
  code: string
  newPassword: string
}

export interface TokenResp {
  accessToken: string
  refreshToken: string
  expiresIn: number
  userId: number
  phone: string
}

// ---------------------------------------------------------------------------
// Profile（/api/profile，字段均选填）
// ---------------------------------------------------------------------------

export interface Profile {
  gender?: string
  birthDate?: string
  height?: string
  weight?: string
  allergyHistory?: string
  pastHistory?: string
  medicationHistory?: string
  familyHistory?: string
}

export type ProfileReq = Profile
export type ProfileVO = Profile

// ---------------------------------------------------------------------------
// Chat（/api/chat）
// ---------------------------------------------------------------------------

export interface SendMessageReq {
  sessionId?: number
  message: string
}

export interface RefVO {
  title: string
  source: string
}

export interface SessionVO {
  id: number
  title: string
  createTime: string
  updateTime: string
}

export interface MessageVO {
  id: number
  role: 'user' | 'assistant'
  content: string
  refs: RefVO[]
  createTime: string
}

export interface RenameSessionReq {
  title: string
}

/** SSE 事件（POST /api/chat/messages 流式，data-only，按 type 区分） */
export interface DeltaEvent {
  type: 'delta'
  content: string
}

export interface DoneEvent {
  type: 'done'
  sessionId: number
  messageId: number
  refs: RefVO[]
}

export interface ErrorEvent {
  type: 'error'
  message: string
}

export type SseEvent = DeltaEvent | DoneEvent | ErrorEvent

// ---------------------------------------------------------------------------
// Triage（/api/triage）
// ---------------------------------------------------------------------------

export interface TriageReq {
  chiefComplaint: string
  duration?: string
  severity?: string
  accompanyingSymptoms?: string
  pastHistory?: string
}

export interface TriageVO {
  redFlag: boolean
  redFlagMessage?: string
  departments: string[]
  urgencyLevel: string
  reasons: string[]
  observationNotes: string
  disclaimer: string
}

// ---------------------------------------------------------------------------
// Medication（/api/medications）
// ---------------------------------------------------------------------------

export interface DrugInfoVO {
  found: boolean
  message: string
  genericName: string
  brandNames: string
  indications: string
  dosage: string
  contraindications: string
  adverseReactions: string
  storage: string
  specialPopulation: string
  warnings: string[]
  disclaimer: string
}

export interface InteractionReq {
  drugs: string[]
}

export interface InteractionVO {
  drugA: string
  drugB: string
  riskLevel: string
  description: string
}

export interface InteractionResultVO {
  interactions: InteractionVO[]
  note: string
}

// ---------------------------------------------------------------------------
// Report（/api/reports）
// ---------------------------------------------------------------------------

export interface IndicatorVO {
  item: string
  value: string
  unit: string
  refRange: string
  level: string
  meaning: string
  reason?: string
}

export interface ReportDetailVO {
  id: number
  status: 'PENDING' | 'READY' | 'FAILED'
  criticalFlag: boolean
  summary?: string
  disclaimer: string
  indicators?: IndicatorVO[]
  message?: string
  createTime: string
}

// ---------------------------------------------------------------------------
// Feedback（/api/feedbacks）
// ---------------------------------------------------------------------------

export interface FeedbackReq {
  type: FeedbackType
  messageId?: number
  content?: string
}

export interface FeedbackVO {
  id: number
  messageId: number
  type: FeedbackType
  content: string
  status: 'PENDING' | 'PROCESSED'
  createTime: string
}

// ---------------------------------------------------------------------------
// Knowledge（/api/knowledge，用户个人知识库）
// ---------------------------------------------------------------------------

export interface DocumentVO {
  id: number
  fileName: string
  fileType: string
  status: 'PROCESSING' | 'READY' | 'FAILED'
  chunkCount: number
  errorMsg?: string
  createTime: string
}

// ---------------------------------------------------------------------------
// Admin（/api/admin）
// ---------------------------------------------------------------------------

export interface AdminLoginReq {
  username: string
  password: string
}

export interface AdminLoginResp {
  accessToken: string
  expiresIn: number
  username: string
  role: AdminRole
}

export interface KnowledgeReq {
  title: string
  content: string
  source?: string
  category?: string
}

export interface KnowledgeVO {
  id: number
  title: string
  content: string
  source: string
  category: string
  status: string
  version: number
  createBy: number
  createTime: string
  updateTime: string
}

export interface FeedbackReviewVO {
  id: number
  userId: number
  messageId: number
  type: FeedbackType
  content: string
  status: 'PENDING' | 'PROCESSED'
  createTime: string
}

export interface AuditLogVO {
  id: number
  operatorId: number
  targetType: string
  targetId: number
  action: string
  detail: string
  createTime: string
}

export interface StatsOverviewVO {
  userCount: number
  chatQuestionCount: number
  triageCount: number
  reportCount: number
  feedbackCount: number
  pendingCorrectionCount: number
  helpfulCount: number
  evaluationCount: number
  satisfactionRate: number | null
}

export interface TopQuestionVO {
  content: string
  count: number
}

export interface UserVO {
  id: number
  phone: string
  nickname: string
  status: number
  registerType: string
  createTime: string
}

export interface UserStatusReq {
  status: number
}

export interface AdminCreateReq {
  username: string
  password: string
  role: AdminRole
}

export interface AdminVO {
  id: number
  username: string
  role: AdminRole
  status: number
  createTime: string
}
