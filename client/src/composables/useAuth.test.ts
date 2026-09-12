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
    const { isLoggedIn, isAdmin, isStaff, isStaffOrAdmin } = useAuth()
    expect(isLoggedIn.value).toBe(false)
    expect(isAdmin.value).toBe(false)
    expect(isStaff.value).toBe(false)
    expect(isStaffOrAdmin.value).toBe(false)
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
    const { isLoggedIn, isAdmin, isStaff, isStaffOrAdmin, restore } = useAuth()
    restore()
    expect(isLoggedIn.value).toBe(true)
    expect(isAdmin.value).toBe(true)
    expect(isStaff.value).toBe(false)
    expect(isStaffOrAdmin.value).toBe(true)
  })

  it('isStaff flags a staff session', () => {
    sessionStorage.setItem('ecomart_session', JSON.stringify({ token: 'tok', id: 1, username: 'nhanvien', email: 'nv@a.bc', role: 'STAFF' }))
    const { isAdmin, isStaff, isStaffOrAdmin, restore } = useAuth()
    restore()
    expect(isAdmin.value).toBe(false)
    expect(isStaff.value).toBe(true)
    expect(isStaffOrAdmin.value).toBe(true)
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

  it('register posts payload and persists the session', async () => {
    requestMock.mockResolvedValue({ token: 'tok', refreshToken: 'ref', expiresIn: 3600, id: 1, username: 'minh', email: 'a@b.c', numberPhone: '0901234567', role: 'CUSTOMER' })
    const { register } = useAuth()
    await register({ username: 'minh', email: 'a@b.c', numberPhone: '0901234567', password: 'secret' })
    expect(requestMock).toHaveBeenCalledWith('/api/auth/register', {
      method: 'POST',
      body: { username: 'minh', email: 'a@b.c', numberPhone: '0901234567', password: 'secret' }
    })
    expect(sessionStorage.getItem('ecomart_token')).toBe('tok')
    expect(sessionStorage.getItem('ecomart_session')).toContain('minh')
  })

  it('refresh rotates the refresh token and persists the new pair', async () => {
    requestMock.mockResolvedValueOnce({ token: 'tok', refreshToken: 'ref', expiresIn: 3600, id: 1, username: 'minh', email: 'a@b.c', numberPhone: '0901234567', role: 'CUSTOMER' })
    const { login, refresh } = useAuth()
    await login('minh', 'secret')
    requestMock.mockResolvedValueOnce({ token: 'tok2', refreshToken: 'ref2', expiresIn: 3600, id: 1, username: 'minh', email: 'a@b.c', numberPhone: '0901234567', role: 'CUSTOMER' })
    await refresh()
    expect(requestMock).toHaveBeenCalledWith('/api/auth/refresh', {
      method: 'POST',
      body: { refreshToken: 'ref' }
    })
    expect(sessionStorage.getItem('ecomart_token')).toBe('tok2')
  })

  it('refresh throws when there is no stored refresh token', async () => {
    const { refresh } = useAuth()
    await expect(refresh()).rejects.toThrow('Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại')
  })

  it('forceLogout clears session and storage without navigating', async () => {
    const pushSpy = vi.spyOn(router, 'push')
    requestMock.mockResolvedValue({ token: 'tok', refreshToken: 'ref', expiresIn: 3600, id: 1, username: 'minh', email: 'a@b.c', numberPhone: '0901234567', role: 'CUSTOMER' })
    const { login, forceLogout, isLoggedIn } = useAuth()
    await login('minh', 'secret', { remember: true })
    forceLogout()
    expect(isLoggedIn.value).toBe(false)
    expect(localStorage.getItem('ecomart_token')).toBeNull()
    expect(localStorage.getItem('ecomart_session')).toBeNull()
    expect(pushSpy).not.toHaveBeenCalled()
  })

  it('restore with a corrupted session clears the storage', () => {
    sessionStorage.setItem('ecomart_session', JSON.stringify({ foo: 'bar', token: 'x' }))
    const { restore, isLoggedIn } = useAuth()
    restore()
    expect(isLoggedIn.value).toBe(false)
    expect(sessionStorage.getItem('ecomart_session')).toBeNull()
    expect(sessionStorage.getItem('ecomart_token')).toBeNull()
  })
})
