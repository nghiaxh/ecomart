export const SESSION_KEY = 'ecomart_session'
export const TOKEN_KEY = 'ecomart_token'
export const UNAUTHORIZED_EVENT = 'ecomart:unauthorized'

function storageOf(persistent: boolean) {
  return persistent ? localStorage : sessionStorage
}

export function isPersistentSession(): boolean {
  return localStorage.getItem(SESSION_KEY) !== null
}

export function loadSessionRaw(): string | null {
  return localStorage.getItem(SESSION_KEY) ?? sessionStorage.getItem(SESSION_KEY)
}

export function loadToken(): string | null {
  return localStorage.getItem(TOKEN_KEY) ?? sessionStorage.getItem(TOKEN_KEY)
}

export function saveSession(raw: string, token: string, persistent: boolean): void {
  const primary = storageOf(persistent)
  const secondary = storageOf(!persistent)
  primary.setItem(SESSION_KEY, raw)
  primary.setItem(TOKEN_KEY, token)
  secondary.removeItem(SESSION_KEY)
  secondary.removeItem(TOKEN_KEY)
}

export function clearSession(): void {
  localStorage.removeItem(SESSION_KEY)
  localStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(SESSION_KEY)
  sessionStorage.removeItem(TOKEN_KEY)
}

export function emitUnauthorized(): void {
  document.dispatchEvent(new Event(UNAUTHORIZED_EVENT))
}
