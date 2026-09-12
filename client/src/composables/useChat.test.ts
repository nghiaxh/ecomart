import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

const loadTokenMock = vi.hoisted(() => vi.fn())

vi.mock('@/utils/session-storage', () => ({ loadToken: loadTokenMock }))

import { useChat } from './useChat'

const sseResponse = (chunks: string[], status = 200) => {
  const body = new ReadableStream<Uint8Array>({
    start(controller) {
      for (const chunk of chunks) controller.enqueue(new TextEncoder().encode(chunk))
      controller.close()
    }
  })
  return new Response(body, { status })
}

describe('useChat', () => {
  beforeEach(() => {
    loadTokenMock.mockReset()
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('assembles text chunks, forwards the source and signals done', async () => {
    const fetchMock = vi.fn().mockResolvedValue(sseResponse([
      'data:{"text":"Một"}\n\ndata: {"text":" hai"}\n\n',
      'data:{"source":"bm25"}\n\ndata:[DONE]\n\n'
    ]))
    vi.stubGlobal('fetch', fetchMock)

    const text: string[] = []
    const sources: string[] = []
    let done = false
    const { send } = useChat()
    await send([{ role: 'user', content: 'khi nao nhan hang?' }], {
      onText: (chunk) => text.push(chunk),
      onSource: (source) => sources.push(source),
      onDone: () => { done = true }
    })

    expect(text.join('')).toBe('Một hai')
    expect(sources).toEqual(['bm25'])
    expect(done).toBe(true)
  })

  it('sends the message history as JSON', async () => {
    const fetchMock = vi.fn().mockResolvedValue(sseResponse(['data:[DONE]\n\n']))
    vi.stubGlobal('fetch', fetchMock)

    const { send } = useChat()
    await send([{ role: 'user', content: 'xin chao' }], handlers())

    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/chat')
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body)).toEqual({
      messages: [{ role: 'user', content: 'xin chao' }]
    })
  })

  it('attaches the Bearer token when present', async () => {
    loadTokenMock.mockReturnValue('tok123')
    const fetchMock = vi.fn().mockResolvedValue(sseResponse(['data:[DONE]\n\n']))
    vi.stubGlobal('fetch', fetchMock)

    const { send } = useChat()
    await send([{ role: 'user', content: 'hi' }], handlers())

    expect(fetchMock.mock.calls[0][1].headers.Authorization).toBe('Bearer tok123')
  })

  it('omits the Authorization header when no token is stored', async () => {
    loadTokenMock.mockReturnValue(null)
    const fetchMock = vi.fn().mockResolvedValue(sseResponse(['data:[DONE]\n\n']))
    vi.stubGlobal('fetch', fetchMock)

    const { send } = useChat()
    await send([{ role: 'user', content: 'hi' }], handlers())

    expect(fetchMock.mock.calls[0][1].headers.Authorization).toBeUndefined()
  })

  it('throws when the server responds with an error status', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response('', { status: 500 }))
    vi.stubGlobal('fetch', fetchMock)

    const { send } = useChat()
    await expect(send([{ role: 'user', content: 'hi' }], handlers())).rejects.toThrow(/500/)
  })

  const handlers = () => ({
    onText: vi.fn(),
    onSource: vi.fn(),
    onDone: vi.fn()
  })
})