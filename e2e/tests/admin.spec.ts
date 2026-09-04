import { test, expect } from '@playwright/test'
import { loginAsAdmin, loginAsCustomer, SEED_ADMIN, gotoReady } from './helpers'

test('admin logs in and lands on dashboard', async ({ page }) => {
  await loginAsAdmin(page)
  await expect(page.getByText('Quản lý đơn hàng').first()).toBeVisible()
})

test('admin login with wrong password shows error', async ({ page }) => {
  await gotoReady(page, '/login')
  await page.locator('#login-identifier').fill(SEED_ADMIN.identifier)
  await page.locator('#login-password').fill('wrong-password')
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page.getByText('Email hoặc mật khẩu không đúng', { exact: true })).toBeVisible()
})

test('customer hitting admin route is redirected away', async ({ page }) => {
  await loginAsCustomer(page)
  await gotoReady(page, '/admin/products')
  await expect(page).not.toHaveURL(/\/admin\/products/)
})

test('guest hitting admin route is sent to login', async ({ page }) => {
  await gotoReady(page, '/admin')
  await expect(page).toHaveURL('/login')
})


