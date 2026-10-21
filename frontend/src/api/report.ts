import { del, get, post } from '@/utils/request'
import type { ReportDetailVO } from '@/types'

export function uploadReport(file: File): Promise<number> {
  const form = new FormData()
  form.append('file', file)
  return post<number>('/reports', form)
}

export function getReport(id: number): Promise<ReportDetailVO> {
  return get<ReportDetailVO>(`/reports/${id}`)
}

export function deleteReport(id: number): Promise<void> {
  return del<void>(`/reports/${id}`)
}
