import { describe, it, expect, vi, beforeEach } from 'vitest'

const {
  instanceRequestMock,
  refreshMock,
  clearSessionMock,
  emitUnauthorizedMock,
  loadTokenMock
} = vi.hoisted(() => ({
  instanceRequestMock: vi.fn(),
  refreshMock: vi.fn(),
  clearSessionMock: vi.fn(),
  emitUnauthorizedMock: vi.fn(),
  loadTokenMock: vi.fn()
}))

const requestInterceptor: { fn?: (config: any) => any } = vi.hoisted(() => ({}))

vi.mock('axios', () => ({
  default: {
    create: () => ({
      request: instanceRequestMock,
      interceptors: {
        request: { use: (fn: (config: any) => any) => { requestInterceptor.fn = fn } }
      }
    }),
    isAxiosError: (error: unknown) => Boolean((error as { isAxiosError?: boolean })?.isAxiosError)
  }
}))

vi.mock('@/utils/session-storage', () => ({
  clearSession: clearSessionMock,
  emitUnauthorized: emitUnauthorizedMock,
  loadToken: loadTokenMock
}))

vi.mock('@/composables/useAuth', () => ({
  useAuth: () => ({ refresh: refreshMock })
}))

import { useApi } from './useApi'

const err401 = { isAxiosError: true, response: { status: 401, data: { message: 'Unauthorized' } } }
const err500 = { isAxiosError: true, response: { status: 500, data: { message: 'boom' } } }

describe('useApi', () => {
  beforeEach(() => {
    instanceRequestMock.mockReset()
    refreshMock.mockReset()
    clearSessionMock.mockReset()
    emitUnauthorizedMock.mockReset()
    loadTokenMock.mockReset()
  })

  it('attaches the Bearer token through the request interceptor', () => {
    loadTokenMock.mockReturnValue('tok123')
    const config = requestInterceptor.fn!({ headers: {} })
    expect(config.headers.Authorization).toBe('Bearer tok123')
  })

  it('leaves the config untouched when no token is stored', () => {
    loadTokenMock.mockReturnValue(null)
    const config = requestInterceptor.fn!({ headers: { Accept: 'application/json' } })
    expect(config.headers.Authorization).toBeUndefined()
    expect(config.headers.Accept).toBe('application/json')
  })

  it('returns the response body on success', async () => {
    instanceRequestMock.mockResolvedValue({ data: { fine: true } })
    const { request } = useApi()
    await expect(request('/api/products?page=0')).resolves.toEqual({ fine: true })
  })

  it('does not refresh on 401 for /api/auth/** paths but clears the session', async () => {
    instanceRequestMock.mockRejectedValue(err401)
    const { request } = useApi()
    await expect(request('/api/auth/login', { method: 'POST', body: {} })).rejects.toBeTruthy()
    expect(refreshMock).not.toHaveBeenCalled()
    expect(clearSessionMock).toHaveBeenCalledTimes(1)
    expect(emitUnauthorizedMock).toHaveBeenCalledTimes(1)
  })

  it('refreshes once and retries after a 401 outside auth', async () => {
    instanceRequestMock
      .mockRejectedValueOnce(err401)
      .mockResolvedValueOnce({ data: { ok: true } })
    refreshMock.mockResolvedValue({ token: 'new-token' })
    const { request } = useApi()
    await expect(request('/api/cart')).resolves.toEqual({ ok: true })
    expect(refreshMock).toHaveBeenCalledTimes(1)
    expect(instanceRequestMock).toHaveBeenCalledTimes(2)
    expect(clearSessionMock).not.toHaveBeenCalled()
  })

  it('clears the session when the retry also returns 401', async () => {
    instanceRequestMock
      .mockRejectedValueOnce(err401)
      .mockRejectedValueOnce(err401)
    refreshMock.mockResolvedValue({ token: 'new-token' })
    const { request } = useApi()
    await expect(request('/api/cart')).rejects.toBeTruthy()
    expect(refreshMock).toHaveBeenCalledTimes(1)
    expect(clearSessionMock).toHaveBeenCalledTimes(1)
    expect(emitUnauthorizedMock).toHaveBeenCalledTimes(1)
  })

  it('clears the session when refresh fails', async () => {
    instanceRequestMock.mockRejectedValueOnce(err401)
    refreshMock.mockRejectedValue(new Error('refresh boom'))
    const { request } = useApi()
    await expect(request('/api/cart')).rejects.toBeTruthy()
    expect(clearSessionMock).toHaveBeenCalledTimes(1)
    expect(emitUnauthorizedMock).toHaveBeenCalledTimes(1)
  })

  it('single-flights concurrent refresh calls', async () => {
    let resolveRefresh: (value: unknown) => void = () => {}
    const pending = new Promise((resolve) => { resolveRefresh = resolve })
    refreshMock.mockReturnValue(pending)
    instanceRequestMock
      .mockRejectedValueOnce(err401)
      .mockRejectedValueOnce(err401)
      .mockResolvedValueOnce({ data: 'one' })
      .mockResolvedValueOnce({ data: 'two' })

    const { request } = useApi()
    const p1 = request('/api/cart')
    const p2 = request('/api/orders/mine')
    resolveRefresh({ token: 'rotated' })
    await expect(p1).resolves.toBe('one')
    await expect(p2).resolves.toBe('two')
    expect(refreshMock).toHaveBeenCalledTimes(1)
  })

  it('passes through non-401 errors without clearing the session', async () => {
    instanceRequestMock.mockRejectedValue(err500)
    const { request } = useApi()
    await expect(request('/api/cart')).rejects.toBeTruthy()
    expect(refreshMock).not.toHaveBeenCalled()
    expect(clearSessionMock).not.toHaveBeenCalled()
  })
})