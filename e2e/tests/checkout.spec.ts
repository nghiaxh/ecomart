import { test, expect } from '@playwright/test'
import { loginAsCustomer } from './helpers'

test('customer reaches the checkout page from the cart', async ({ page }) => {
  await loginAsCustomer(page)
  await page.goto('/products')
  await page.locator('a[href^="/products/"]').first().click()
  await page.getByRole('button', { name: 'Thêm vào giỏ' }).click()
  await page.getByRole('link', { name: 'Giỏ hàng' }).click()
  await page.getByRole('link', { name: 'Tiến hành thanh toán' }).click()
  await expect(page.getByRole('heading', { name: 'Thanh toán', exact: true })).toBeVisible()
})