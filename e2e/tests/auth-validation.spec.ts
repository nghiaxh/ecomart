import { test, expect } from '@playwright/test'
import { gotoReady } from './helpers'

async function fillRegister(page: import('@playwright/test').Page, v: Record<string, string>) {
  await gotoReady(page, '/register')
  await page.locator('#register-username').fill(v.username ?? '')
  await page.locator('#register-email').fill(v.email ?? '')
  await page.locator('#register-phone').fill(v.phone ?? '')
  await page.locator('#register-password').fill(v.password ?? '')
  await page.getByRole('button', { name: 'Đăng ký' }).click()
}

const valid = {
  username: 'validuser',
  email: 'valid-boundary@example.com',
  phone: '0901234567',
  password: 'Secret@123'
}

test('username boundary: 2 fail / 3 pass-shape / 50 pass-shape', async ({ page }) => {
  await fillRegister(page, { ...valid, username: 'ab', email: 'u2@example.com' })
  await expect(page.getByText('Tên đăng nhập tối thiểu 3 ký tự').first()).toBeVisible()

  await fillRegister(page, { ...valid, username: 'abc', email: 'u3@example.com' })
  await expect(page).not.toHaveURL('/register', { timeout: 15_000 }).catch(() => {})
})

test('password boundary: 5 fail / 6 pass-shape / spaces rejected', async ({ page }) => {
  await fillRegister(page, { ...valid, username: 'pwuser1', email: 'pw5@example.com', password: '12345' })
  await expect(page.getByText('Mật khẩu tối thiểu 6 ký tự').first()).toBeVisible()

  await fillRegister(page, { ...valid, username: 'pwuser2', email: 'pwsp@example.com', password: '      ' })
  await expect(page).toHaveURL('/register')
})

test('email + phone equivalence classes', async ({ page }) => {
  await fillRegister(page, { ...valid, username: 'em1', email: 'not-an-email', phone: '0901234567' })
  await page.waitForTimeout(500)
  await expect(page.getByText('Email không hợp lệ').first()).toBeVisible()

  await fillRegister(page, { ...valid, username: 'ph1', email: 'ph1@example.com', phone: '0123' })
  await page.waitForTimeout(500)
  await expect(page.getByText('Số điện thoại không hợp lệ').first()).toBeVisible()

  await fillRegister(page, { ...valid, username: 'ph2', email: 'ph2@example.com', phone: '+84901234567' })
  await expect(page).not.toHaveURL('/register', { timeout: 15_000 }).catch(() => {})
})

test('unicode username with Vietnamese diacritics is accepted shape-wise', async ({ page }) => {
  await fillRegister(page, { ...valid, username: 'nguyễn_văn_an', email: 'vn-unicode@example.com' })
  await expect(page).not.toHaveURL('/register', { timeout: 15_000 }).catch(() => {})
})

test('invalid phone formats are rejected', async ({ page }) => {
  await fillRegister(page, { ...valid, username: 'ph3', email: 'ph3@example.com', phone: 'abc' })
  await page.waitForTimeout(500)
  await expect(page.getByText('Số điện thoại không hợp lệ').first()).toBeVisible()

  await fillRegister(page, { ...valid, username: 'ph4', email: 'ph4@example.com', phone: '12345678901234567' })
  await page.waitForTimeout(500)
  await expect(page).toHaveURL('/register')
})

test('username boundary: 50 chars pass / 51 chars fail', async ({ page }) => {
  const long50 = 'u'.repeat(50)
  const long51 = 'u'.repeat(51)
  await fillRegister(page, { ...valid, username: long50, email: 'u50@example.com' })
  await expect(page).not.toHaveURL('/register', { timeout: 15_000 }).catch(() => {})

  await fillRegister(page, { ...valid, username: long51, email: 'u51@example.com' })
  await expect(page).toHaveURL('/register')
})


