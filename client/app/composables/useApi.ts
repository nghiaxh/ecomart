import type { FetchOptions } from 'ofetch'

export const useApi = () => {
  const config = useRuntimeConfig()
  const router = useRouter()

  const getToken = () => {
    if (import.meta.client) {
      return localStorage.getItem('ecomart_token')
    }
    return null
  }

  const clearSession = () => {
    if (!import.meta.client) return
    localStorage.removeItem('ecomart_session')
    localStorage.removeItem('ecomart_token')
  }

  const request = async <T>(url: string, opts: FetchOptions = {}) => {
    const headers: Record<string, string> = {
      ...(opts.headers as Record<string, string> || {})
    }
    const token = getToken()
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }
    try {
      return await $fetch<T>(`${config.public.apiBase}${url}`, {
        ...opts,
        method: opts.method as 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE',
        headers
      })
    } catch (error: any) {
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
