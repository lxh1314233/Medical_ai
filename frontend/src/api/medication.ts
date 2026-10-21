import { get, post } from '@/utils/request'
import type { DrugInfoVO, InteractionReq, InteractionResultVO } from '@/types'

export function getDrugInfo(name: string): Promise<DrugInfoVO> {
  return get<DrugInfoVO>(`/medications/${encodeURIComponent(name)}`)
}

export function checkInteractions(data: InteractionReq): Promise<InteractionResultVO> {
  return post<InteractionResultVO>('/medications/interactions', data)
}
