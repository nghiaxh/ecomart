import { describe, it, expect, beforeEach } from 'vitest'
import {
  SESSION_KEY,
  TOKEN_KEY,
  clearSession,
  isPersistentSession,
  loadSessionRaw,
  loadToken,
  saveSession
} from './session-storage'

describe('session-storage', () => {
  beforeEach(() => {
    localStorage.clear()
    sessionStorage.clear()
  })

  it('saves to sessionStorage by default', () => {
    saveSession('{"username":"minh"}', 'tok', false)
    expect(sessionStorage.getItem(SESSION_KEY)).toBe('{"username":"minh"}')
    expect(sessionStorage.getItem(TOKEN_KEY)).toBe('tok')
    expect(localStorage.getItem(SESSION_KEY)).toBeNull()
  })

  it('saves to localStorage when persistent and clears the other storage', () => {
    sessionStorage.setItem(SESSION_KEY, 'old')
    saveSession('{"username":"lan"}', 'tok2', true)
    expect(localStorage.getItem(SESSION_KEY)).toBe('{"username":"lan"}')
    expect(localStorage.getItem(TOKEN_KEY)).toBe('tok2')
    expect(sessionStorage.getItem(SESSION_KEY)).toBeNull()
  })

  it('loads token preferring localStorage', () => {
    sessionStorage.setItem(TOKEN_KEY, 'sess-tok')
    expect(loadToken()).toBe('sess-tok')
    localStorage.setItem(TOKEN_KEY, 'local-tok')
    expect(loadToken()).toBe('local-tok')
  })

  it('loads raw session from either storage', () => {
    expect(loadSessionRaw()).toBeNull()
    sessionStorage.setItem(SESSION_KEY, 'sess')
    expect(loadSessionRaw()).toBe('sess')
  })

  it('detects persistent sessions', () => {
    expect(isPersistentSession()).toBe(false)
    localStorage.setItem(SESSION_KEY, 'x')
    expect(isPersistentSession()).toBe(true)
  })

  it('clears both storages', () => {
    localStorage.setItem(SESSION_KEY, 'a')
    sessionStorage.setItem(TOKEN_KEY, 'b')
    clearSession()
    expect(localStorage.getItem(SESSION_KEY)).toBeNull()
    expect(sessionStorage.getItem(TOKEN_KEY)).toBeNull()
  })
})
