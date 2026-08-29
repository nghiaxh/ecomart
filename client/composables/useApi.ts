import type { FetchOptions } from 'ofetch'

export const useApi = () => {
  const config = useRuntimeConfig()

  const getToken = () => {
    if (import.meta.client) {
      return localStorage.getItem('ecomart_token')
    }
    return null
  }

  const request = <T>(url: string, opts: FetchOptions = {}) => {
    const headers: Record<string, string> = {
      ...(opts.headers as Record<string, string> || {})
    }
    const token = getToken()
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }
    return $fetch<T>(`${config.public.apiBase}${url}`, {
      ...opts,
      headers
    })
  }

  return { request, getToken, apiBase: config.public.apiBase }
}
