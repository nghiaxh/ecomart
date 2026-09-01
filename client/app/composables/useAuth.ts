import type { AuthResponse, UserRole } from '~/types'

interface Session {
  token: string
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

  const persist = (value: Session | null) => {
    if (!import.meta.client) return
    if (value) {
      localStorage.setItem('ecomart_session', JSON.stringify(value))
      localStorage.setItem('ecomart_token', value.token)
    } else {
      localStorage.removeItem('ecomart_session')
      localStorage.removeItem('ecomart_token')
    }
  }

  const restore = () => {
    if (!import.meta.client) return
    const raw = localStorage.getItem('ecomart_session')
    if (raw) {
      try {
        session.value = JSON.parse(raw)
        persist(session.value)
      } catch {
        persist(null)
      }
    } else if (session.value) {
      persist(session.value)
    }
  }

  const setSession = (data: AuthResponse) => {
    session.value = {
      token: data.token,
      id: data.id,
      username: data.username,
      email: data.email,
      avatarUrl: data.avatarUrl,
      role: data.role
    }
    persist(session.value)
  }

  const login = async (email: string, password: string) => {
    const data = await request<AuthResponse>('/api/auth/login', {
      method: 'POST',
      body: { email, password }
    })
    setSession(data)
    return data
  }

  const register = async (payload: { username: string; email: string; numberPhone: string; password: string }) => {
    const data = await request<AuthResponse>('/api/auth/register', {
      method: 'POST',
      body: payload
    })
    setSession(data)
    return data
  }

  const logout = () => {
    session.value = null
    persist(null)
    navigateTo('/')
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
    restore,
    setSession,
    login,
    register,
    logout,
    updateSession
  }
}
