import { test, expect } from '@playwright/test'
import { makeUniqueUser, gotoReady, API_BASE } from './helpers'

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

test('register shows validation toast for short username', async ({ page }) => {
  await gotoReady(page, '/register')
  await page.locator('#register-username').fill('ab')
  await page.locator('#register-email').fill('not-an-email')
  await page.locator('#register-phone').fill('123')
  await page.locator('#register-password').fill('123')
  await page.getByRole('button', { name: 'Đăng ký' }).click()
  await page.waitForTimeout(500)
  await expect(page.getByText('Tên đăng nhập tối thiểu 3 ký tự').first()).toBeVisible()
  await expect(page).toHaveURL('/register')
})

test('register with duplicate email shows failure toast', async ({ page, request }) => {
  const user = makeUniqueUser('dup')
  const res = await request.post(
    `${API_BASE}/api/auth/register`,
    { data: user }
  )
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
  const res = await request.post(
    `${API_BASE}/api/auth/register`,
    { data: user }
  )
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

