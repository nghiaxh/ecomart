import { describe, it, expect, vi, beforeEach } from 'vitest'

const { requestMock, routerPushMock } = vi.hoisted(() => ({
  requestMock: vi.fn(),
  routerPushMock: vi.fn()
}))

vi.mock('@/composables/useApi', () => ({
  useApi: () => ({ request: requestMock })
}))

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>()
  return {
    ...(actual as Record<string, unknown>),
    useRouter: () => ({ push: routerPushMock })
  }
})

import { useAuth } from './useAuth'
import { router } from '@/router'

describe('useAuth', () => {
  beforeEach(() => {
    requestMock.mockReset()
    requestMock.mockResolvedValue({})
    routerPushMock.mockReset()
    localStorage.clear()
    sessionStorage.clear()
    useAuth().session.value = null
  })

  it('starts logged out', () => {
    const { isLoggedIn, isAdmin } = useAuth()
    expect(isLoggedIn.value).toBe(false)
    expect(isAdmin.value).toBe(false)
  })

  it('login persists token to sessionStorage by default', async () => {
    requestMock.mockResolvedValue({ token: 'tok', refreshToken: 'ref', expiresIn: 3600, id: 1, username: 'minh', email: 'a@b.c', numberPhone: '0901234567', role: 'CUSTOMER' })
    const { login } = useAuth()
    await login('minh', 'secret')
    expect(sessionStorage.getItem('ecomart_token')).toBe('tok')
    expect(sessionStorage.getItem('ecomart_session')).toContain('minh')
    expect(localStorage.getItem('ecomart_token')).toBeNull()
  })

  it('login with remember persists to localStorage', async () => {
    requestMock.mockResolvedValue({ token: 'tok', refreshToken: 'ref', expiresIn: 3600, id: 1, username: 'minh', email: 'a@b.c', numberPhone: '0901234567', role: 'CUSTOMER' })
    const { login } = useAuth()
    await login('minh', 'secret', { remember: true })
    expect(localStorage.getItem('ecomart_token')).toBe('tok')
    expect(localStorage.getItem('ecomart_session')).toContain('minh')
    expect(sessionStorage.getItem('ecomart_token')).toBeNull()
  })

  it('isLoggedIn reflects a restored session from sessionStorage', () => {
    sessionStorage.setItem('ecomart_session', JSON.stringify({ token: 'tok', id: 1, username: 'minh', email: 'a@b.c', role: 'ADMIN' }))
    const { isLoggedIn, isAdmin, restore } = useAuth()
    restore()
    expect(isLoggedIn.value).toBe(true)
    expect(isAdmin.value).toBe(true)
  })

  it('restore keeps backward compatibility with localStorage', () => {
    localStorage.setItem('ecomart_session', JSON.stringify({ token: 'tok', id: 1, username: 'minh', email: 'a@b.c', role: 'ADMIN' }))
    const { isLoggedIn, isAdmin, restore } = useAuth()
    restore()
    expect(isLoggedIn.value).toBe(true)
    expect(isAdmin.value).toBe(true)
  })

  it('logout clears both storages and navigates home', async () => {
    const pushSpy = vi.spyOn(router, 'push').mockImplementation(() => Promise.resolve())
    requestMock.mockResolvedValue({ token: 'tok', refreshToken: 'ref', expiresIn: 3600, id: 1, username: 'minh', email: 'a@b.c', numberPhone: '0901234567', role: 'CUSTOMER' })
    const { login, logout, isLoggedIn } = useAuth()
    await login('minh', 'secret', { remember: true })
    logout()
    expect(isLoggedIn.value).toBe(false)
    expect(localStorage.getItem('ecomart_token')).toBeNull()
    expect(sessionStorage.getItem('ecomart_token')).toBeNull()
    expect(pushSpy).toHaveBeenCalledWith('/')
  })

  it('login calls request and stores in sessionStorage by default', async () => {
    requestMock.mockResolvedValue({ token: 'tok', refreshToken: 'ref', expiresIn: 3600, id: 2, username: 'lan', email: 'x@y.z', numberPhone: '0901234567', role: 'CUSTOMER' })
    const { login } = useAuth()
    await login('lan', 'secret')
    expect(requestMock).toHaveBeenCalledWith('/api/auth/login', { method: 'POST', body: { identifier: 'lan', password: 'secret' } })
    expect(sessionStorage.getItem('ecomart_token')).toBe('tok')
    expect(localStorage.getItem('ecomart_token')).toBeNull()
  })

  it('login with remember stores in localStorage', async () => {
    requestMock.mockResolvedValue({ token: 'tok', refreshToken: 'ref', expiresIn: 3600, id: 2, username: 'lan', email: 'x@y.z', numberPhone: '0901234567', role: 'CUSTOMER' })
    const { login } = useAuth()
    await login('lan', 'secret', { remember: true })
    expect(localStorage.getItem('ecomart_token')).toBe('tok')
    expect(sessionStorage.getItem('ecomart_token')).toBeNull()
  })

  it('updateSession merges partial fields in the same storage', async () => {
    requestMock.mockResolvedValue({ token: 'tok', refreshToken: 'ref', expiresIn: 3600, id: 1, username: 'minh', email: 'a@b.c', numberPhone: '0901234567', role: 'CUSTOMER' })
    const { login, updateSession } = useAuth()
    await login('minh', 'secret')
    updateSession({ username: 'minh98' })
    expect(JSON.parse(sessionStorage.getItem('ecomart_session')!).username).toBe('minh98')
  })
})
