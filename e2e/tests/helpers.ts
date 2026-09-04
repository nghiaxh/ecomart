import { expect, type APIRequestContext, type Page } from '@playwright/test'

export const API_BASE = process.env.E2E_API_BASE || 'http://localhost:8080'

export const SEED_CUSTOMER = {
  identifier: process.env.E2E_CUSTOMER_IDENTIFIER || 'customer@ecomart.vn',
  password: process.env.E2E_CUSTOMER_PASSWORD || 'Customer@123'
}

export const SEED_ADMIN = {
  identifier: process.env.E2E_ADMIN_IDENTIFIER || 'admin@ecomart.vn',
  password: process.env.E2E_ADMIN_PASSWORD || 'Admin@123'
}

/**
 * Navigate and wait for the Nuxt client to hydrate. Clicking form buttons
 * before hydration triggers a native submit (page reloads, no API call),
 * so every spec must use this instead of raw `page.goto()`.
 */
export async function gotoReady(page: Page, url: string): Promise<void> {
  await page.goto(url)
  await page.waitForLoadState('networkidle', { timeout: 30_000 }).catch(() => {})
  // Wait for Nuxt hydration - the app root should have content
  await page.waitForSelector('#app', { state: 'attached', timeout: 10_000 }).catch(() => {})
}

export interface TestUser {
  username: string
  email: string
  numberPhone: string
  password: string
}

export interface AuthPayload {
  token: string
  refreshToken: string
  id: number
  username: string
  email: string
  avatarUrl?: string | null
  role: 'ADMIN' | 'CUSTOMER'
}

export function makeUniqueUser(prefix = 'e2e'): TestUser {
  const rand = `${Date.now()}${Math.floor(Math.random() * 1e6)}`.slice(-10)
  return {
    username: `${prefix}_${rand}`,
    email: `${prefix}_${rand}@example.com`,
    numberPhone: `09${rand.slice(0, 8)}`,
    password: 'Test@1234'
  }
}

export async function registerViaAPI(request: APIRequestContext, user: TestUser): Promise<AuthPayload> {
  const res = await request.post(`${API_BASE}/api/auth/register`, { data: user })
  expect(res.ok(), `register failed: ${res.status()} ${await res.text()}`).toBe(true)
  return (await res.json()) as AuthPayload
}

export async function loginViaAPI(
  request: APIRequestContext,
  identifier: string,
  password: string
): Promise<AuthPayload> {
  const res = await request.post(`${API_BASE}/api/auth/login`, {
    data: { identifier, password }
  })
  expect(res.ok(), `login failed: ${res.status()} ${await res.text()}`).toBe(true)
  return (await res.json()) as AuthPayload
}

/** Inject JWT session into the page by setting localStorage and reloading. */
export async function setAuthStorage(page: Page, auth: AuthPayload): Promise<void> {
  const session = {
    token: auth.token,
    refreshToken: auth.refreshToken,
    id: auth.id,
    username: auth.username,
    email: auth.email,
    avatarUrl: auth.avatarUrl ?? null,
    role: auth.role
  }
  // Navigate to base URL first so page.evaluate can access localStorage
  await page.goto('/')
  await page.waitForLoadState('domcontentloaded', { timeout: 15_000 }).catch(() => {})
  await page.evaluate(
    ({ session, token }: { session: unknown; token: string }) => {
      localStorage.setItem('ecomart_session', JSON.stringify(session))
      localStorage.setItem('ecomart_token', token)
    },
    { session, token: auth.token }
  )
  await page.reload()
  await page.waitForLoadState('networkidle', { timeout: 30_000 }).catch(() => {})
}

/**
 * Register a brand-new isolated customer via API and land on `/` authenticated.
 * Preferred for write-flows (cart, checkout, orders, reviews, chat, account).
 */
export async function loginAsNewCustomer(
  page: Page,
  request: APIRequestContext,
  user: TestUser = makeUniqueUser()
): Promise<TestUser> {
  const auth = await registerViaAPI(request, user)
  expect(auth.role).toBe('CUSTOMER')
  await setAuthStorage(page, auth)
  await expect(page).toHaveURL('/')
  return user
}

/** Legacy helper: log in with the seeded customer through the real login form. */
export async function loginAsCustomer(
  page: Page,
  identifier = SEED_CUSTOMER.identifier,
  password = SEED_CUSTOMER.password
): Promise<void> {
  await gotoReady(page, '/login')
  await page.locator('#login-identifier').fill(identifier)
  await page.locator('#login-password').fill(password)
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page).toHaveURL('/')
}

/** Log in with the seeded admin through the real login form. */
export async function loginAsAdmin(
  page: Page,
  identifier = SEED_ADMIN.identifier,
  password = SEED_ADMIN.password
): Promise<void> {
  await gotoReady(page, '/login')
  await page.locator('#login-identifier').fill(identifier)
  await page.locator('#login-password').fill(password)
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page).toHaveURL('/admin')
}

export async function clearAuth(page: Page): Promise<void> {
  await page.evaluate(() => {
    localStorage.removeItem('ecomart_session')
    localStorage.removeItem('ecomart_token')
    sessionStorage.removeItem('ecomart_session')
    sessionStorage.removeItem('ecomart_token')
  })
}

export async function authHeaders(request: APIRequestContext, identifier: string, password: string) {
  const auth = await loginViaAPI(request, identifier, password)
  return {
    auth,
    headers: { Authorization: `Bearer ${auth.token}` }
  }
}

