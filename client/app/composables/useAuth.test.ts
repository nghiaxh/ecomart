import { describe, it, expect, vi, beforeEach } from 'vitest'

const { requestMock, navigateToMock } = vi.hoisted(() => ({
  requestMock: vi.fn(),
  navigateToMock: vi.fn()
}))

vi.mock('~/composables/useApi', () => ({
  useApi: () => ({ request: requestMock })
}))

vi.mock('#app/composables/router', async (importOriginal) => {
  const mod = await importOriginal<Record<string, unknown>>()
  return {
    ...mod,
    navigateTo: navigateToMock
  }
})

import { useAuth } from './useAuth'

describe('useAuth', () => {
  beforeEach(() => {
    requestMock.mockReset()
    navigateToMock.mockReset()
    localStorage.clear()
    sessionStorage.clear()
    useAuth().session.value = null
    if (import.meta.client) {
      document.getElementById('__nuxt')?.remove()
    }
  })

  it('starts logged out', () => {
    const { isLoggedIn, isAdmin } = useAuth()
    expect(isLoggedIn.value).toBe(false)
    expect(isAdmin.value).toBe(false)
  })

  it('setSession persists token to sessionStorage by default', () => {
    const { setSession } = useAuth()
    setSession({ token: 'tok', refreshToken: 'ref', expiresIn: 3600, id: 1, username: 'minh', email: 'a@b.c', numberPhone: '0901234567', role: 'CUSTOMER' })
    expect(sessionStorage.getItem('ecomart_token')).toBe('tok')
    expect(sessionStorage.getItem('ecomart_session')).toContain('minh')
    expect(localStorage.getItem('ecomart_token')).toBeNull()
  })

  it('setSession with remember persists to localStorage', () => {
    const { setSession } = useAuth()
    setSession({ token: 'tok', refreshToken: 'ref', expiresIn: 3600, id: 1, username: 'minh', email: 'a@b.c', numberPhone: '0901234567', role: 'CUSTOMER' }, { remember: true })
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

  it('logout clears both storages and navigates home', () => {
    const { setSession, logout, isLoggedIn } = useAuth()
    setSession({ token: 'tok', refreshToken: 'ref', expiresIn: 3600, id: 1, username: 'minh', email: 'a@b.c', numberPhone: '0901234567', role: 'CUSTOMER' }, { remember: true })
    logout()
    expect(isLoggedIn.value).toBe(false)
    expect(localStorage.getItem('ecomart_token')).toBeNull()
    expect(sessionStorage.getItem('ecomart_token')).toBeNull()
    expect(navigateToMock).toHaveBeenCalledWith('/')
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

  it('updateSession merges partial fields in the same storage', () => {
    const { setSession, updateSession } = useAuth()
    setSession({ token: 'tok', refreshToken: 'ref', expiresIn: 3600, id: 1, username: 'minh', email: 'a@b.c', numberPhone: '0901234567', role: 'CUSTOMER' })
    updateSession({ username: 'minh98' })
    expect(JSON.parse(sessionStorage.getItem('ecomart_session')!).username).toBe('minh98')
  })
})
