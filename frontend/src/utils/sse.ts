import { fetchEventSource } from '@microsoft/fetch-event-source'
import { API_BASE_URL } from './env'
import type { RefVO, SendMessageReq, SseEvent } from '@/types'

export interface StreamCallbacks {
  onDelta: (content: string) => void
  onDone: (e: { sessionId: number; messageId: number; refs: RefVO[] }) => void
  onError: (message: string) => void
}

export interface StreamHandle {
  close: () => void
}

/**
 * 以 POST + SSE 流式发送聊天消息（后端 /api/chat/messages 返回 text/event-stream，
 * data-only 事件，payload 为 JSON，type ∈ delta / done / error）。
 * EventSource 仅支持 GET，故用 fetch-event-source 自定义 POST 请求体与鉴权头。
 */
export function streamChat(token: string, body: SendMessageReq, cb: StreamCallbacks): StreamHandle {
  const ctrl = new AbortController()

  const promise = fetchEventSource(`${API_BASE_URL}/chat/messages`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(body),
    signal: ctrl.signal,
    openWhenHidden: true,
    onmessage(msg) {
      try {
        const evt: SseEvent = JSON.parse(msg.data)
        if (evt.type === 'delta') cb.onDelta(evt.content)
        else if (evt.type === 'done') cb.onDone({ sessionId: evt.sessionId, messageId: evt.messageId, refs: evt.refs })
        else if (evt.type === 'error') cb.onError(evt.message)
      } catch {
        /* 忽略无法解析的数据行 */
      }
    },
    onerror(err) {
      // 网络错误 / 非 200（401/403 等）走这里，不自动重连
      const msg = err instanceof Error ? err.message : '连接中断'
      cb.onError(msg || '连接中断')
      throw err
    },
  })

  // 吞掉 onerror 里 throw 抛出的 rejection，避免未处理
  promise.catch(() => {})

  return {
    close: () => ctrl.abort(),
  }
}
