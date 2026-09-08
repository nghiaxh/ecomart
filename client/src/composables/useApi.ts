import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { clearSession, emitUnauthorized, loadToken } from '@/utils/session-storage'
import { useAuth } from '@/composables/useAuth'

const AUTH_URL_PREFIX = '/api/auth'

const apiBase = import.meta.env.VITE_API_BASE || ''

let refreshInflight: Promise<unknown> | null = null

export type RequestOptions = {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  headers?: Record<string, string>
  params?: Record<string, string>
}

type RawError = {
  response?: { status?: number; data?: { message?: string } }
  data?: { message?: string }
}

const http: AxiosInstance = axios.create({
  baseURL: apiBase
})

http.interceptors.request.use((config) => {
  const token = loadToken()
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function toRawOptions(opts: RequestOptions = {}): AxiosRequestConfig {
  const { method, body, headers, params } = opts
  return {
    method: (method || 'GET') as AxiosRequestConfig['method'],
    data: body,
    headers,
    params
  }
}

function normalizeError(error: unknown): RawError {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status
    const message = (error.response?.data as { message?: string } | undefined)?.message
    return { response: { status, data: { message } }, ...error }
  }
  return error as RawError
}

export const useApi = () => {
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

  const rawRequest = async <T>(url: string, opts: RequestOptions = {}): Promise<T> => {
    const { data } = await http.request<T>({ ...toRawOptions(opts), url })
    return data
  }

  const request = async <T>(url: string, opts: RequestOptions = {}): Promise<T> => {
    try {
      return await rawRequest<T>(url, opts)
    } catch (error: any) {
      const status = error?.response?.status
      if (status === 401 && !url.startsWith(AUTH_URL_PREFIX)) {
        const refreshed = await tryRefreshOnce()
        if (refreshed) {
          try {
            return await rawRequest<T>(url, opts)
          } catch (retryError: any) {
            if (retryError?.response?.status !== 401) {
              throw normalizeError(retryError)
            }
            error = retryError
          }
        }
      }
      if (error?.response?.status === 401) {
        clearSession()
        emitUnauthorized()
      }
      throw normalizeError(error)
    }
  }

  return { request }
}
