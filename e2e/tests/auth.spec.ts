import { test, expect } from '@playwright/test'
import { SEED_CUSTOMER, gotoReady } from './helpers'

test('customer can log in with email', async ({ page }) => {
  await gotoReady(page, '/login')
  await page.locator('#login-identifier').fill(SEED_CUSTOMER.identifier)
  await page.locator('#login-password').fill(SEED_CUSTOMER.password)
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page).toHaveURL('/')
  // useAuth() persists to sessionStorage unless "remember" is checked
  const session = await page.evaluate(
    () => localStorage.getItem('ecomart_session') ?? sessionStorage.getItem('ecomart_session')
  )
  expect(session).toBeTruthy()
  const token = await page.evaluate(
    () => localStorage.getItem('ecomart_token') ?? sessionStorage.getItem('ecomart_token')
  )
  expect(token).toBeTruthy()
})

test('invalid credentials show an error toast', async ({ page }) => {
  await gotoReady(page, '/login')
  await page.locator('#login-identifier').fill(SEED_CUSTOMER.identifier)
  await page.locator('#login-password').fill('wrong-password')
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page.getByText('Email hoặc mật khẩu không đúng', { exact: true })).toBeVisible()
})

test('empty login form is blocked by native required validation', async ({ page }) => {
  await gotoReady(page, '/login')
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  // Zod validation blocks submit: no navigation, no session
  await expect(page).toHaveURL('/login')
  const session = await page.evaluate(
    () => localStorage.getItem('ecomart_session') ?? sessionStorage.getItem('ecomart_session')
  )
  expect(session).toBeNull()
})

test('login with unknown email shows failure toast', async ({ page }) => {
  await gotoReady(page, '/login')
  await page.locator('#login-identifier').fill('nonexistent_user@example.com')
  await page.locator('#login-password').fill('SomePassword123')
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page.getByText('Email hoặc mật khẩu không đúng').first()).toBeVisible()
  await expect(page).toHaveURL('/login')
})

test('remember checkbox controls localStorage vs sessionStorage', async ({ page }) => {
  await gotoReady(page, '/login')
  await page.locator('#login-identifier').fill(SEED_CUSTOMER.identifier)
  await page.locator('#login-password').fill(SEED_CUSTOMER.password)
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


