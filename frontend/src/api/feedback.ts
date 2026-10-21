import { get, post } from '@/utils/request'
import type { FeedbackReq, FeedbackVO } from '@/types'

export function submitFeedback(data: FeedbackReq): Promise<FeedbackVO> {
  return post<FeedbackVO>('/feedbacks', data)
}

export function listMyFeedback(): Promise<FeedbackVO[]> {
  return get<FeedbackVO[]>('/feedbacks')
}
