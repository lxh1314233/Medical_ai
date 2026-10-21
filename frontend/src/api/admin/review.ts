import { get, put } from '@/utils/request'
import type { AuditLogVO, FeedbackReviewVO, PageVO } from '@/types'

export function listFeedbackReviews(params: {
  page?: number
  size?: number
  type?: string
  status?: string
}): Promise<PageVO<FeedbackReviewVO>> {
  return get<PageVO<FeedbackReviewVO>>('/admin/feedbacks', { params })
}

export function processFeedback(id: number): Promise<void> {
  return put<void>(`/admin/feedbacks/${id}/process`)
}

export function listAuditLogs(params: { page?: number; size?: number }): Promise<PageVO<AuditLogVO>> {
  return get<PageVO<AuditLogVO>>('/admin/audit-logs', { params })
}
