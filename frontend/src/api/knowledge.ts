import { del, get, post } from '@/utils/request'
import type { DocumentVO } from '@/types'

export function uploadDocument(file: File): Promise<number> {
  const form = new FormData()
  form.append('file', file)
  return post<number>('/knowledge/upload', form)
}

export function listDocuments(): Promise<DocumentVO[]> {
  return get<DocumentVO[]>('/knowledge/documents')
}

export function deleteDocument(id: number): Promise<void> {
  return del<void>(`/knowledge/documents/${id}`)
}
