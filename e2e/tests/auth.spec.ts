import { test, expect } from '@playwright/test'

const identifier = process.env.E2E_CUSTOMER_IDENTIFIER || 'customer'
const password = process.env.E2E_CUSTOMER_PASSWORD || 'customer123'

test('customer can log in', async ({ page }) => {
  await page.goto('/login')
  await page.locator('#login-identifier').fill(identifier)
  await page.locator('#login-password').fill(password)
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page).toHaveURL('/')
})

test('invalid credentials show an error toast', async ({ page }) => {
  await page.goto('/login')
  await page.locator('#login-identifier').fill(identifier)
  await page.locator('#login-password').fill('wrong-password')
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page.getByText('Email hoặc mật khẩu không đúng', { exact: true })).toBeVisible()
})