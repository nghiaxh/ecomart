import { test, expect } from '@playwright/test'

export async function loginAsCustomer(page, identifier = process.env.E2E_CUSTOMER_IDENTIFIER || 'customer', password = process.env.E2E_CUSTOMER_PASSWORD || 'customer123') {
  await page.goto('/login')
  await page.locator('#login-identifier').fill(identifier)
  await page.locator('#login-password').fill(password)
  await page.getByRole('button', { name: 'Đăng nhập' }).click()
  await expect(page).toHaveURL('/')
}