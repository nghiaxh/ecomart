import { loadToken } from '@/utils/session-storage'
import type { ChatMessage } from '@/types'

const apiBase = import.meta.env.VITE_API_BASE || ''

export interface ChatHandlers {
  onText: (chunk: string) => void
  onSource: (source: string) => void
  onDone: () => void
}

const handleEvent = (block: string, handlers: ChatHandlers) => {
  const payload = block
    .split('\n')
    .filter((line) => line.startsWith('data:'))
    .map((line) => line.slice(5).trimStart())
    .join('\n')
  if (payload === '[DONE]') {
    handlers.onDone()
    return
  }
  try {
    const parsed = JSON.parse(payload) as Record<string, string>
    if (typeof parsed.text === 'string') handlers.onText(parsed.text)
    else if (typeof parsed.source === 'string') handlers.onSource(parsed.source)
  } catch {
    // keepalive or blank frames: ignore
  }
}

const processEvents = (buffer: string, handlers: ChatHandlers): string => {
  let index: number
  while ((index = buffer.indexOf('\n\n')) !== -1) {
    const block = buffer.slice(0, index)
    buffer = buffer.slice(index + 2)
    if (block) handleEvent(block, handlers)
  }
  return buffer
}

export const useChat = () => {
  const send = async (messages: ChatMessage[], handlers: ChatHandlers, signal?: AbortSignal) => {
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    const token = loadToken()
    if (token) headers.Authorization = `Bearer ${token}`

    const response = await fetch(`${apiBase}/api/chat`, {
      method: 'POST',
      headers,
      body: JSON.stringify({ messages }),
      signal
    })
    if (!response.ok) throw new Error(`Lỗi trợ lý (HTTP ${response.status})`)
    if (!response.body) throw new Error('Trình duyệt không hỗ trợ trả lời trực tiếp')

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      buffer = processEvents(buffer, handlers)
    }
    if (buffer.trim()) {
      buffer = processEvents(buffer, handlers)
      if (buffer.trim()) handleEvent(buffer, handlers)
    }
  }

  return { send }
}