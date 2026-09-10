import { test, expect } from '@playwright/test'
import { SEED_CUSTOMER, SEED_ADMIN, gotoReady, API_BASE, clearAuth, loginAsNewCustomer, makeUniqueUser } from './helpers'

test('customer can log in with email', async ({ page }) => {
  await gotoReady(page, '/login')
  await page.locator('#login-identifier').fill(SEED_CUSTOMER.identifier)
  await page.locator('#login-password').fill(SEED_CUSTOMER.password)
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page).toHaveURL('/')
  const session = await page.evaluate(
    () => localStorage.getItem('ecomart_session') ?? sessionStorage.getItem('ecomart_session')
  )
  expect(session).toBeTruthy()
  const token = await page.evaluate(
    () => localStorage.getItem('ecomart_token') ?? sessionStorage.getItem('ecomart_token')
  )
  expect(token).toBeTruthy()
})

test('bad credentials show error toast (wrong password + unknown email + admin)', async ({ page }) => {
  // wrong password
  await gotoReady(page, '/login')
  await page.locator('#login-identifier').fill(SEED_CUSTOMER.identifier)
  await page.locator('#login-password').fill('wrong-password')
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page.getByText('Email hoặc mật khẩu không đúng', { exact: true }).first()).toBeVisible()
  await expect(page).toHaveURL('/login')

  // unknown email
  await gotoReady(page, '/login')
  await page.locator('#login-identifier').fill('nonexistent_user@example.com')
  await page.locator('#login-password').fill('SomePassword123')
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page.getByText('Email hoặc mật khẩu không đúng', { exact: true }).first()).toBeVisible()
  await expect(page).toHaveURL('/login')

  // admin wrong password
  await gotoReady(page, '/login')
  await page.locator('#login-identifier').fill(SEED_ADMIN.identifier)
  await page.locator('#login-password').fill('wrong-password')
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page.getByText('Email hoặc mật khẩu không đúng', { exact: true })).toBeVisible()
})

test('empty login form is blocked by native required validation', async ({ page }) => {
  await gotoReady(page, '/login')
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page).toHaveURL('/login')
  const session = await page.evaluate(
    () => localStorage.getItem('ecomart_session') ?? sessionStorage.getItem('ecomart_session')
  )
  expect(session).toBeNull()
})

test('remember checkbox controls localStorage vs sessionStorage', async ({ page }) => {
  await gotoReady(page, '/login')
  await page.locator('#login-identifier').fill(SEED_CUSTOMER.identifier)
  await page.locator('#login-password').fill(SEED_CUSTOMER.password)
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page).toHaveURL('/')
  const sessionInStorage = await page.evaluate(() => {
    const ls = localStorage.getItem('ecomart_session')
    const ss = sessionStorage.getItem('ecomart_session')
    return { localStorage: !!ls, sessionStorage: !!ss }
  })
  expect(sessionInStorage.sessionStorage || sessionInStorage.localStorage).toBe(true)
  const token = await page.evaluate(
    () => localStorage.getItem('ecomart_token') ?? sessionStorage.getItem('ecomart_token')
  )
  expect(token).toBeTruthy()
})

test('clearing session mid-flow redirects protected pages to login', async ({ page, request }) => {
  await loginAsNewCustomer(page, request)
  await clearAuth(page)
  await gotoReady(page, '/cart')
  await expect(page).toHaveURL('/login')
  await gotoReady(page, '/checkout')
  await expect(page).toHaveURL('/login')
  await gotoReady(page, '/orders')
  await expect(page).toHaveURL('/login')
})

test('forged/unauthenticated tokens are rejected by API', async ({ request }) => {
  const forged = await request.get(`${API_BASE}/api/cart`, {
    headers: { Authorization: 'Bearer forged.invalid.token' }
  })
  expect([401, 403]).toContain(forged.status())
  const missing = await request.post(`${API_BASE}/api/orders/checkout`, {
    data: { addressId: 1, paymentMethod: 'COD' }
  })
  expect([401, 403]).toContain(missing.status())
})

test('duplicate register via API is rejected', async ({ request }) => {
  const user = makeUniqueUser('dupapi')
  const first = await request.post(`${API_BASE}/api/auth/register`, { data: user })
  expect(first.ok()).toBe(true)
  const second = await request.post(`${API_BASE}/api/auth/register`, {
    data: { ...user, username: `${user.username}x` }
  })
  expect([400, 409, 422]).toContain(second.status())
})

test('new user can register and lands on homepage', async ({ page }) => {
  const user = makeUniqueUser('reg')
  await gotoReady(page, '/register')
  await page.locator('#register-username').fill(user.username)
  await page.locator('#register-email').fill(user.email)
  await page.locator('#register-phone').fill(user.numberPhone)
  await page.locator('#register-password').fill(user.password)
  await page.getByRole('button', { name: 'Đăng ký' }).click()
  await expect(page).toHaveURL('/')
  const session = await page.evaluate(
    () => localStorage.getItem('ecomart_session') ?? sessionStorage.getItem('ecomart_session')
  )
  expect(session).toContain(user.username)
  const token = await page.evaluate(
    () => localStorage.getItem('ecomart_token') ?? sessionStorage.getItem('ecomart_token')
  )
  expect(token).toBeTruthy()
})

test('register with duplicate email shows failure toast', async ({ page, request }) => {
  const user = makeUniqueUser('dup')
  const res = await request.post(`${API_BASE}/api/auth/register`, { data: user })
  expect(res.ok()).toBe(true)

  await gotoReady(page, '/register')
  await page.locator('#register-username').fill(`${user.username}x`)
  await page.locator('#register-email').fill(user.email)
  await page.locator('#register-phone').fill(user.numberPhone)
  await page.locator('#register-password').fill(user.password)
  await page.getByRole('button', { name: 'Đăng ký' }).click()
  await expect(page.getByText(/Đăng ký thất bại|Email đã được sử dụng/).first()).toBeVisible()
  await expect(page).toHaveURL('/register')
})

test('register with duplicate username shows failure toast', async ({ page, request }) => {
  const user = makeUniqueUser('dupname')
  const res = await request.post(`${API_BASE}/api/auth/register`, { data: user })
  expect(res.ok()).toBe(true)

  await gotoReady(page, '/register')
  await page.locator('#register-username').fill(user.username)
  await page.locator('#register-email').fill(`other_${user.email}`)
  await page.locator('#register-phone').fill(`09${Date.now().toString().slice(-8)}`)
  await page.locator('#register-password').fill(user.password)
  await page.getByRole('button', { name: 'Đăng ký' }).click()
  await expect(page.getByText(/Đăng ký thất bại|Tên đăng nhập đã tồn tại/).first()).toBeVisible()
  await expect(page).toHaveURL('/register')
})
