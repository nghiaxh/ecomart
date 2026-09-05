import type { AuthResponse } from '~/types'
import {
  clearSession,
  isPersistentSession,
  loadSessionRaw,
  saveSession
} from '~/utils/session-storage'

export type Session = Pick<AuthResponse, 'token' | 'refreshToken' | 'id' | 'username' | 'email' | 'avatarUrl' | 'role'>

function isValidSessionShape(value: unknown): value is Session {
  if (typeof value !== 'object' || value === null) return false
  const v = value as Record<string, unknown>
  return typeof v.token === 'string' && typeof v.username === 'string'
    && typeof v.email === 'string' && typeof v.role === 'string'
}

export const useAuth = () => {
  const session = useState<Session | null>('ecomart_session', () => null)
  const { request } = useApi()

  const token = computed(() => session.value?.token || null)
  const isLoggedIn = computed(() => !!session.value)
  const isAdmin = computed(() => session.value?.role === 'ADMIN')

  const isRemembered = () => isPersistentSession()

  const persist = (value: Session | null, remember?: boolean) => {
    if (!import.meta.client) return
    if (!value) {
      clearSession()
      return
    }
    const usePersistent = remember ?? isRemembered()
    saveSession(JSON.stringify(value), value.token, usePersistent)
  }

  const restore = () => {
    if (!import.meta.client) return
    const raw = loadSessionRaw()
    if (raw) {
      try {
        const parsed: unknown = JSON.parse(raw)
        if (!isValidSessionShape(parsed)) throw new Error('invalid session')
        session.value = parsed
        persist(session.value)
      } catch {
        session.value = null
        clearSession()
      }
    } else if (session.value) {
      persist(session.value)
    }
  }

  const setSession = (data: AuthResponse, opts?: { remember?: boolean }) => {
    const { token, refreshToken, id, username, email, avatarUrl, role } = data
    session.value = { token, refreshToken, id, username, email, avatarUrl, role }
    persist(session.value, opts?.remember ?? false)
  }

  const login = async (identifier: string, password: string, opts?: { remember?: boolean }) => {
    const data = await request<AuthResponse>('/api/auth/login', {
      method: 'POST',
      body: { identifier, password }
    })
    setSession(data, opts)
    return data
  }

  const register = async (payload: { username: string; email: string; numberPhone: string; password: string }, opts?: { remember?: boolean }) => {
    const data = await request<AuthResponse>('/api/auth/register', {
      method: 'POST',
      body: payload
    })
    setSession(data, opts)
    return data
  }

  const refresh = async () => {
    if (!session.value) {
      restore()
    }
    const refreshToken = session.value?.refreshToken
    if (!refreshToken) {
      throw new Error('Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại')
    }
    const data = await request<AuthResponse>('/api/auth/refresh', {
      method: 'POST',
      body: { refreshToken }
    })
    setSession(data)
    return data
  }

  const logout = () => {
    const refreshToken = session.value?.refreshToken
    session.value = null
    persist(null)
    if (refreshToken) {
      request('/api/auth/logout', {
        method: 'POST',
        body: { refreshToken }
      }).catch(() => {})
    }
    navigateTo('/')
  }

  const forceLogout = () => {
    session.value = null
    persist(null)
  }

  const updateSession = (partial: Partial<Session>) => {
    if (session.value) {
      session.value = { ...session.value, ...partial }
      persist(session.value)
    }
  }

  return {
    session,
    token,
    isLoggedIn,
    isAdmin,
    isRemembered,
    restore,
    setSession,
    login,
    register,
    refresh,
    logout,
    forceLogout,
    updateSession
  }
}
