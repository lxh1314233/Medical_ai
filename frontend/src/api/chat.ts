import { del, get, put } from '@/utils/request'
import type { MessageVO, RenameSessionReq, SessionVO } from '@/types'

export function listSessions(): Promise<SessionVO[]> {
  return get<SessionVO[]>('/chat/sessions')
}

export function listMessages(sessionId: number): Promise<MessageVO[]> {
  return get<MessageVO[]>(`/chat/sessions/${sessionId}/messages`)
}

export function renameSession(sessionId: number, data: RenameSessionReq): Promise<void> {
  return put<void>(`/chat/sessions/${sessionId}`, data)
}

export function deleteSession(sessionId: number): Promise<void> {
  return del<void>(`/chat/sessions/${sessionId}`)
}
