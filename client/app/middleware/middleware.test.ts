import { describe, it, expect, vi, beforeEach } from 'vitest'

const { navigateToMock, authMock } = vi.hoisted(() => ({
  navigateToMock: vi.fn(),
  authMock: { isLoggedIn: { value: false }, isAdmin: { value: false } }
}))

vi.mock('~/composables/useAuth', () => ({
  useAuth: () => authMock
}))

vi.mock('#app/composables/router', async (importOriginal) => {
  const mod = await importOriginal<Record<string, unknown>>()
  return {
    ...mod,
    navigateTo: navigateToMock
  }
})

import customerMiddleware from './customer'
import adminMiddleware from './admin'
import authMiddleware from './auth'

describe('middleware', () => {
  const mockRoute = { path: '/' } as any

  beforeEach(() => {
    navigateToMock.mockReset()
    authMock.isLoggedIn.value = false
    authMock.isAdmin.value = false
  })

  it('auth redirects to login when logged out', () => {
    authMiddleware(mockRoute, mockRoute)
    expect(navigateToMock).toHaveBeenCalledWith('/login')
  })

  it('auth passes when logged in', () => {
    navigateToMock.mockReset()
    authMock.isLoggedIn.value = true
    expect(authMiddleware(mockRoute, mockRoute)).toBeUndefined()
    expect(navigateToMock).not.toHaveBeenCalled()
  })

  it('customer redirects to login when logged out', () => {
    customerMiddleware(mockRoute, mockRoute)
    expect(navigateToMock).toHaveBeenCalledWith('/login')
  })

  it('customer redirects admin to /admin', () => {
    navigateToMock.mockReset()
    authMock.isLoggedIn.value = true
    authMock.isAdmin.value = true
    customerMiddleware(mockRoute, mockRoute)
    expect(navigateToMock).toHaveBeenCalledWith('/admin')
  })

  it('admin redirects non-admin to /', () => {
    navigateToMock.mockReset()
    authMock.isLoggedIn.value = true
adminMiddleware(mockRoute, mockRoute)
    expect(navigateToMock).toHaveBeenCalledWith('/')
  })

  it('admin passes for admin user', () => {
    navigateToMock.mockReset()
    authMock.isLoggedIn.value = true
    authMock.isAdmin.value = true
    expect(adminMiddleware(mockRoute, mockRoute)).toBeUndefined()
    expect(navigateToMock).not.toHaveBeenCalled()
  })
})