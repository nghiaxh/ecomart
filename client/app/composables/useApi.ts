import type { FetchOptions } from 'ofetch'

const AUTH_URL_PREFIX = '/api/auth'

let refreshInflight: Promise<unknown> | null = null

export const useApi = () => {
  const config = useRuntimeConfig()
  const router = useRouter()

  const getToken = () => {
    if (import.meta.client) {
      return localStorage.getItem('ecomart_token') ?? sessionStorage.getItem('ecomart_token')
    }
    return null
  }

  const clearSession = () => {
    if (!import.meta.client) return
    localStorage.removeItem('ecomart_session')
    localStorage.removeItem('ecomart_token')
    sessionStorage.removeItem('ecomart_session')
    sessionStorage.removeItem('ecomart_token')
  }

  const tryRefreshOnce = async () => {
    try {
      if (!refreshInflight) {
        refreshInflight = useAuth().refresh().finally(() => {
          refreshInflight = null
        })
      }
      await refreshInflight
      return true
    } catch {
      return false
    }
  }

  const rawRequest = async <T>(url: string, opts: FetchOptions = {}) => {
    const headers: Record<string, string> = {
      ...(opts.headers as Record<string, string> || {})
    }
    const token = getToken()
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }
    return await $fetch<T>(`${config.public.apiBase}${url}`, {
      ...opts,
      method: opts.method as 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE',
      headers
    })
  }

  const request = async <T>(url: string, opts: FetchOptions = {}) => {
    try {
      return await rawRequest<T>(url, opts)
    } catch (error: any) {
      const status = error?.response?.status
      if (import.meta.client && status === 401 && !url.startsWith(AUTH_URL_PREFIX)) {
        const refreshed = await tryRefreshOnce()
        if (refreshed) {
          try {
            return await rawRequest<T>(url, opts)
          } catch (retryError: any) {
            if (retryError?.response?.status !== 401) {
              throw retryError
            }
            error = retryError
          }
        }
      }
      if (import.meta.client && error?.response?.status === 401) {
        clearSession()
        document.dispatchEvent(new Event('ecomart:unauthorized'))
        const noAuthPage = ['/login', '/register'].includes(router.currentRoute.value.path)
        if (!noAuthPage) {
          navigateTo('/login')
        }
      }
      throw error
    }
  }

  return { request, getToken, apiBase: config.public.apiBase }
}
