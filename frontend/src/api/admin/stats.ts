import { get } from '@/utils/request'
import type { StatsOverviewVO, TopQuestionVO } from '@/types'

export function getStatsOverview(): Promise<StatsOverviewVO> {
  return get<StatsOverviewVO>('/admin/stats/overview')
}

export function getTopQuestions(limit = 10): Promise<TopQuestionVO[]> {
  return get<TopQuestionVO[]>('/admin/stats/top-questions', { params: { limit } })
}
