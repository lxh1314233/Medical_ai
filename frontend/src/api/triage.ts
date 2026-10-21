import { post } from '@/utils/request'
import type { TriageReq, TriageVO } from '@/types'

export function triage(data: TriageReq): Promise<TriageVO> {
  return post<TriageVO>('/triage', data)
}
