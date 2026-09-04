import { test, expect } from '@playwright/test'
import { loginAsCustomer } from './helpers'

const adminIdentifier = process.env.E2E_ADMIN_IDENTIFIER || 'admin'
const adminPassword = process.env.E2E_ADMIN_PASSWORD || 'admin123'

test('admin cannot log in with a normal customer middleware', async ({ page, browser }) => {
  const ctx = await browser.newContext()
  const adminPage = await ctx.newPage()
  await adminPage.goto('/login')
  await adminPage.locator('#login-identifier').fill(adminIdentifier)
  await adminPage.locator('#login-password').fill(adminPassword)
  await adminPage.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(adminPage).toHaveURL('/admin')
  await ctx.close()
})

test('customer hitting admin route is redirected away', async ({ page }) => {
  await loginAsCustomer(page)
  await page.goto('/admin/products')
  await expect(page).not.toHaveURL(/\/admin\/products/)
})