import type { AuthResponse, UserRole } from '~/types'

interface Session {
  token: string
  refreshToken: string
  id: number
  username: string
  email: string
  avatarUrl?: string
  role: UserRole
}

export const useAuth = () => {
  const session = useState<Session | null>('ecomart_session', () => null)
  const { request } = useApi()

  const token = computed(() => session.value?.token || null)
  const isLoggedIn = computed(() => !!session.value)
  const isAdmin = computed(() => session.value?.role === 'ADMIN')

  const SESSION_KEY = 'ecomart_session'
  const TOKEN_KEY = 'ecomart_token'

  const clearStorages = () => {
    localStorage.removeItem(SESSION_KEY)
    localStorage.removeItem(TOKEN_KEY)
    sessionStorage.removeItem(SESSION_KEY)
    sessionStorage.removeItem(TOKEN_KEY)
  }

  const isRemembered = () => {
    if (!import.meta.client) return false
    return localStorage.getItem(SESSION_KEY) !== null
  }

  const persist = (value: Session | null, remember?: boolean) => {
    if (!import.meta.client) return
    if (!value) {
      clearStorages()
      return
    }
    const usePersistent = remember ?? isRemembered()
    const primary = usePersistent ? localStorage : sessionStorage
    const secondary = usePersistent ? sessionStorage : localStorage
    primary.setItem(SESSION_KEY, JSON.stringify(value))
    primary.setItem(TOKEN_KEY, value.token)
    secondary.removeItem(SESSION_KEY)
    secondary.removeItem(TOKEN_KEY)
  }

  const restore = () => {
    if (!import.meta.client) return
    const raw = localStorage.getItem(SESSION_KEY) ?? sessionStorage.getItem(SESSION_KEY)
    if (raw) {
      try {
        session.value = JSON.parse(raw)
        persist(session.value)
      } catch {
        session.value = null
        clearStorages()
      }
    } else if (session.value) {
      persist(session.value)
    }
  }

  const setSession = (data: AuthResponse, opts?: { remember?: boolean }) => {
    session.value = {
      token: data.token,
      refreshToken: data.refreshToken,
      id: data.id,
      username: data.username,
      email: data.email,
      avatarUrl: data.avatarUrl,
      role: data.role
    }
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
      try {
        Promise.resolve(request('/api/auth/logout', {
          method: 'POST',
          body: { refreshToken }
        })).catch(() => {})
      } catch {
        // Bỏ qua lỗi revoke phía server khi đăng xuất
      }
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
