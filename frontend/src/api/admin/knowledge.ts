import { del, get, post, put } from '@/utils/request'
import type { KnowledgeReq, KnowledgeVO, PageVO } from '@/types'

export interface KnowledgeQuery {
  page?: number
  size?: number
  status?: string
  category?: string
  keyword?: string
}

export function listKnowledge(params: KnowledgeQuery): Promise<PageVO<KnowledgeVO>> {
  return get<PageVO<KnowledgeVO>>('/admin/knowledge', { params })
}

export function getKnowledge(id: number): Promise<KnowledgeVO> {
  return get<KnowledgeVO>(`/admin/knowledge/${id}`)
}

export function createKnowledge(data: KnowledgeReq): Promise<KnowledgeVO> {
  return post<KnowledgeVO>('/admin/knowledge', data)
}

export function updateKnowledge(id: number, data: KnowledgeReq): Promise<KnowledgeVO> {
  return put<KnowledgeVO>(`/admin/knowledge/${id}`, data)
}

export function publishKnowledge(id: number): Promise<KnowledgeVO> {
  return put<KnowledgeVO>(`/admin/knowledge/${id}/publish`)
}

export function offlineKnowledge(id: number): Promise<KnowledgeVO> {
  return put<KnowledgeVO>(`/admin/knowledge/${id}/offline`)
}

export function deleteKnowledge(id: number): Promise<void> {
  return del<void>(`/admin/knowledge/${id}`)
}
